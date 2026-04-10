<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import PrimaryActionButton from '@/components/PrimaryActionButton.vue'
import SectionHeading from '@/components/SectionHeading.vue'
import { useOrgSession } from '@/composables/useOrgSession'
import type { AssignedCcp, SubmittedCcpRecordResponse } from '@/types/ccps'
import { clearAuthToken, createAuthHeaders, getAuthToken } from '@/utils/auth'

interface CcpCard extends AssignedCcp {
  key: string
  value: string
  comment: string
  noteExpanded: boolean
  recordId: number | null
  outsideCriticalRange: boolean
}

const router = useRouter()
const { claims } = useOrgSession()
let activeFetchId = 0

const ccps = ref<CcpCard[]>([])
const loading = ref(true)
const submitting = ref(false)
const errorMessage = ref<string | null>(null)
const infoMessage = ref<string | null>(null)
const hasAuthenticatedSession = ref(Boolean(getAuthToken()))

const ccpCountLabel = computed(() => {
  if (loading.value) {
    return 'Laster logging'
  }

  return `${ccps.value.length} tildelte CCP-er`
})

const filledCcpCount = computed(() =>
  ccps.value.filter((ccp) => !ccp.completedForCurrentInterval && ccp.value.trim()).length,
)

const primaryActionLabel = computed(() => {
  if (submitting.value) {
    return 'Sender inn...'
  }

  if (!hasAuthenticatedSession.value) {
    return 'Logg inn for å sende inn'
  }

  if (filledCcpCount.value === 0) {
    return 'Fyll inn minst en måling'
  }

  return `Send inn ${filledCcpCount.value} målinger`
})

const helperText = computed(() => {
  if (!hasAuthenticatedSession.value) {
    return 'Logg inn for å hente tildelte CCP-kontroller og sende inn målinger.'
  }

  return 'Fyll inn målingene du har utført i denne perioden. Målinger utenfor kritisk grense blir lagret og kan følges opp som avvik.'
})

function mapAssignedCcp(ccp: AssignedCcp, overrides: Partial<CcpCard> = {}): CcpCard {
  return {
    ...ccp,
    key: `ccp-${ccp.ccpId}`,
    value: '',
    comment: '',
    noteExpanded: false,
    recordId: null,
    outsideCriticalRange: false,
    ...overrides,
  }
}

function formatDateTime(value: string | null): string {
  if (!value) {
    return 'Ikke registrert'
  }

  return new Date(value).toLocaleString('nb-NO', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function formatMeasurement(value: number | null, unit: string | null): string {
  if (value === null || Number.isNaN(value)) {
    return 'Ikke registrert'
  }

  return `${value}${unit ? ` ${unit}` : ''}`
}

async function readErrorMessage(response: Response, fallback: string) {
  const bodyText = (await response.text()).trim()

  if (!bodyText) {
    return fallback
  }

  try {
    const parsed = JSON.parse(bodyText) as { error?: string }
    return typeof parsed.error === 'string' ? parsed.error : bodyText
  } catch {
    return bodyText
  }
}

async function fetchCcps() {
  const fetchId = ++activeFetchId
  hasAuthenticatedSession.value = Boolean(getAuthToken())
  if (fetchId === activeFetchId) {
    errorMessage.value = null
    infoMessage.value = null
  }

  if (!hasAuthenticatedSession.value) {
    if (fetchId === activeFetchId) {
      ccps.value = []
      loading.value = false
      infoMessage.value = 'Logg inn for å se tildelte CCP-målinger.'
    }
    return
  }

  loading.value = true

  try {
    const response = await fetch('/api/me/ccps', {
      credentials: 'same-origin',
      headers: createAuthHeaders(),
    })

    if (response.status === 401 || response.status === 403) {
      clearAuthToken()
      hasAuthenticatedSession.value = false
      if (fetchId === activeFetchId) {
        ccps.value = []
        infoMessage.value = 'Innloggingen er utløpt. Logg inn igjen for å hente tildelte CCP-er.'
      }
      return
    }

    if (!response.ok) {
      throw new Error(await readErrorMessage(response, 'Kunne ikke hente tildelte CCP-er.'))
    }

    if (fetchId !== activeFetchId) {
      return
    }

    ccps.value = ((await response.json()) as AssignedCcp[]).map((ccp) => mapAssignedCcp(ccp))
    infoMessage.value = ccps.value.length === 0 ? 'Du har ingen CCP-kontroller tildelt akkurat nå.' : null
  } catch (error) {
    if (fetchId === activeFetchId) {
      errorMessage.value = error instanceof Error ? error.message : 'Kunne ikke hente tildelte CCP-er.'
      ccps.value = []
    }
  } finally {
    if (fetchId === activeFetchId) {
      loading.value = false
    }
  }
}

function updateCard(cardId: number, updates: Partial<CcpCard>) {
  ccps.value = ccps.value.map((ccp) =>
    ccp.ccpId === cardId
      ? {
          ...ccp,
          ...updates,
        }
      : ccp,
  )
}

function toggleComment(card: CcpCard) {
  updateCard(card.ccpId, { noteExpanded: !card.noteExpanded })
}

function openDeviation(card: CcpCard) {
  if (!card.recordId) {
    return
  }

  void router.push({
    name: 'avvik',
    query: {
      ccpRecordId: String(card.recordId),
      category: 'IK_MAT',
      ccpName: card.name,
      measuredValue: card.lastMeasuredValue !== null ? String(card.lastMeasuredValue) : '',
      unit: card.unit ?? '',
    },
  })
}

async function handlePrimaryAction() {
  if (!hasAuthenticatedSession.value) {
    await router.push('/mobile/login')
    return
  }

  const pendingEntries = ccps.value.filter((ccp) => !ccp.completedForCurrentInterval && ccp.value.trim())
  if (pendingEntries.length === 0) {
    return
  }

  submitting.value = true
  errorMessage.value = null
  infoMessage.value = null

  try {
    const updatedCards = new Map<number, CcpCard>()
    let outsideRangeCount = 0

    for (const ccp of pendingEntries) {
      const response = await fetch(`/api/me/ccps/${ccp.ccpId}/records`, {
        method: 'POST',
        credentials: 'same-origin',
        headers: createAuthHeaders({
          'Content-Type': 'application/json',
        }),
        body: JSON.stringify({
          measuredValue: ccp.value.trim(),
          comment: ccp.comment.trim() || null,
        }),
      })

      if (response.status === 401 || response.status === 403) {
        clearAuthToken()
        hasAuthenticatedSession.value = false
        throw new Error('Innloggingen er utløpt. Logg inn igjen for å sende inn målinger.')
      }

      if (!response.ok) {
        throw new Error(await readErrorMessage(response, 'Kunne ikke registrere målingen.'))
      }

      const updated = (await response.json()) as SubmittedCcpRecordResponse
      if (updated.outsideCriticalRange) {
        outsideRangeCount += 1
      }

      updatedCards.set(
        updated.ccp.ccpId,
        mapAssignedCcp(updated.ccp, {
          recordId: updated.recordId,
          outsideCriticalRange: updated.outsideCriticalRange,
        }),
      )
    }

    ccps.value = ccps.value.map((ccp) => updatedCards.get(ccp.ccpId) ?? ccp)

    infoMessage.value =
      outsideRangeCount > 0
        ? `${updatedCards.size} målinger ble lagret. ${outsideRangeCount} målinger trenger avviksvurdering.`
        : `${updatedCards.size} målinger ble lagret.`
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Kunne ikke registrere målingene.'
  } finally {
    submitting.value = false
  }
}

watch(() => claims.value?.orgId ?? null, () => {
  void fetchCcps()
}, { immediate: true })
</script>

<template>
  <section class="page-layout page-layout--logging">
    <SectionHeading
      title="Logging"
      :subtitle="ccpCountLabel"
      icon="temperature"
    />

    <article
      v-if="errorMessage"
      class="info-banner info-banner--danger"
    >
      <p class="card-copy">
        {{ errorMessage }}
      </p>
    </article>

    <article
      v-if="!loading && infoMessage"
      class="info-banner"
    >
      <p class="card-copy">
        {{ infoMessage }}
      </p>
    </article>

    <article
      v-if="loading"
      class="surface-card"
    >
      <p class="card-copy">
        Laster CCP-logging...
      </p>
    </article>

    <article
      v-else-if="ccps.length === 0"
      class="surface-card"
    >
      <p class="card-copy">
        {{ hasAuthenticatedSession ? 'Ingen tildelte CCP-kontroller akkurat nå.' : 'Du må logge inn for å se CCP-logging.' }}
      </p>
    </article>

    <div
      v-else
      class="metric-grid metric-grid--single"
    >
      <article
        v-for="ccp in ccps"
        :key="ccp.key"
        class="metric-card logging-card"
      >
        <div class="metric-card__header">
          <div>
            <p class="eyebrow">
              {{ ccp.monitoredDescription || 'Kritisk kontrollpunkt' }}
            </p>
            <h3 class="card-title">
              {{ ccp.name }}
            </h3>
          </div>

          <p
            class="status-pill"
            :class="
              ccp.outsideCriticalRange
                ? 'status-pill--danger'
                : ccp.completedForCurrentInterval
                  ? 'status-pill--success'
                  : 'status-pill--warning'
            "
          >
            {{
              ccp.outsideCriticalRange
                ? 'Trenger avvik'
                : ccp.completedForCurrentInterval
                  ? 'Utført i denne perioden'
                  : 'Venter på måling'
            }}
          </p>
        </div>

        <dl class="detail-list">
          <div class="detail-list__item">
            <dt>Kritisk grense</dt>
            <dd>{{ ccp.criticalMin }} - {{ ccp.criticalMax }} {{ ccp.unit }}</dd>
          </div>
          <div class="detail-list__item">
            <dt>Frist</dt>
            <dd>{{ formatDateTime(ccp.dueAt) }}</dd>
          </div>
          <div class="detail-list__item">
            <dt>Sist logget</dt>
            <dd>{{ formatMeasurement(ccp.lastMeasuredValue, ccp.unit) }}</dd>
          </div>
          <div class="detail-list__item">
            <dt>Sist tidspunkt</dt>
            <dd>{{ formatDateTime(ccp.lastCompletedAt) }}</dd>
          </div>
          <div
            v-if="ccp.repeatText"
            class="detail-list__item detail-list__item--stacked"
          >
            <dt>Intervall</dt>
            <dd>{{ ccp.repeatText }}</dd>
          </div>
        </dl>

        <div
          v-if="!ccp.completedForCurrentInterval"
          class="logging-card__inputs"
        >
          <label class="field-shell">
            <input
              :value="ccp.value"
              :placeholder="ccp.unit === 'C' ? '0.0' : '0'"
              class="field-shell__input field-shell__input--with-suffix"
              inputmode="decimal"
              @input="updateCard(ccp.ccpId, { value: ($event.target as HTMLInputElement).value })"
            >
            <span class="field-shell__suffix">{{ ccp.unit }}</span>
          </label>

          <button
            class="inline-text-button"
            type="button"
            @click="toggleComment(ccp)"
          >
            {{ ccp.noteExpanded ? 'Skjul kommentar' : 'Legg til kommentar' }}
          </button>

          <label
            v-if="ccp.noteExpanded"
            class="field-shell"
          >
            <textarea
              :value="ccp.comment"
              class="field-shell__textarea logging-card__textarea"
              placeholder="Noter observasjon eller forklaring"
              @input="updateCard(ccp.ccpId, { comment: ($event.target as HTMLTextAreaElement).value })"
            />
          </label>
        </div>

        <article class="info-popover logging-card__action">
          <p class="info-popover__title">
            Hvis målingen er utenfor grensen
          </p>
          <p class="card-copy">
            {{ ccp.immediateCorrectiveAction }}
          </p>
        </article>

        <article
          v-if="ccp.outsideCriticalRange"
          class="info-banner info-banner--danger logging-card__warning"
        >
          <div class="logging-card__warning-copy">
            <p class="info-popover__title">
              Målingen er lagret utenfor kritisk grense
            </p>
            <p class="card-copy">
              Registrer et avvik for å dokumentere hva som skjedde og hvilke tiltak som ble gjort.
            </p>
          </div>

          <button
            class="inline-action-button"
            type="button"
            @click="openDeviation(ccp)"
          >
            Registrer avvik
          </button>
        </article>
      </article>
    </div>

    <div v-if="!loading">
      <p class="caption-note caption-note--before-action">
        {{ helperText }}
      </p>
      <PrimaryActionButton
        :disabled="submitting || (hasAuthenticatedSession && filledCcpCount === 0)"
        :label="primaryActionLabel"
        type="button"
        @click="handlePrimaryAction"
      />
    </div>
  </section>
</template>
