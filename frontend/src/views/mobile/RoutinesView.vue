<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

import AppIcon from '@/components/AppIcon.vue'
import PrimaryActionButton from '@/components/PrimaryActionButton.vue'
import SectionHeading from '@/components/SectionHeading.vue'
import { useOrgSession } from '@/composables/useOrgSession'
import { routineItems } from '@/data/mockHaccp'
import type { AssignedRoutine } from '@/types/routines'
import { clearAuthToken, createAuthHeaders, getAuthToken } from '@/utils/auth'

interface RoutineCard {
  key: string
  routineId: number | null
  title: string
  categoryName: string | null
  description: string
  immediateCorrectiveAction: string
  dueAtLabel: string
  completedAtLabel: string
  lastCompletedAtLabel: string
  completedForCurrentInterval: boolean
  selected: boolean
  isAssignedRoutine: boolean
}

const router = useRouter()
const { claims } = useOrgSession()
let activeFetchId = 0

const DEFAULT_CORRECTIVE_ACTION =
  'Kontakt ansvarlig leder og noter hvorfor oppgaven ikke kunne fullføres.'

const routines = ref<RoutineCard[]>([])
const loading = ref(true)
const errorMessage = ref<string | null>(null)
const infoMessage = ref<string | null>(null)
const submitting = ref(false)
const openInfoId = ref<string | null>(null)
const hasAuthenticatedSession = ref(Boolean(getAuthToken()))
const showingAssignedRoutines = ref(false)

const routineCountLabel = computed(() => {
  if (loading.value) {
    return 'Laster rutiner'
  }

  return showingAssignedRoutines.value
    ? `${routines.value.length} tildelte rutiner`
    : `${routines.value.length} eksempelrutiner`
})

const selectedRoutineCount = computed(() =>
  routines.value.filter((routine) => routine.isAssignedRoutine && routine.selected && !routine.completedForCurrentInterval)
    .length,
)

const primaryActionLabel = computed(() => {
  if (submitting.value) {
    return 'Sender inn...'
  }

  if (!hasAuthenticatedSession.value) {
    return 'Logg inn for å sende inn'
  }

  if (!showingAssignedRoutines.value) {
    return 'Last inn tildelte rutiner'
  }

  if (selectedRoutineCount.value === 0) {
    return 'Velg rutiner for innsending'
  }

  return `Send inn ${selectedRoutineCount.value} rutiner`
})

const primaryActionDisabled = computed(() => {
  if (submitting.value) {
    return true
  }

  return showingAssignedRoutines.value && selectedRoutineCount.value === 0
})

const helperText = computed(() => {
  if (!hasAuthenticatedSession.value) {
    return 'Logg inn for å laste inn tildelte rutiner fra databasen og sende dem inn.'
  }

  if (!showingAssignedRoutines.value) {
    return 'Eksempelrutiner vises foreløpig. Prøv å laste inn tildelte rutiner på nytt.'
  }

  return 'Kryss av rutinene du har utført, og send dem inn samlet når du er klar.'
})

function toggleInfo(routineId: string) {
  openInfoId.value = openInfoId.value === routineId ? null : routineId
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

function createMockRoutines(): RoutineCard[] {
  return routineItems.map((routine) => ({
    key: `mock-${routine.id}`,
    routineId: null,
    title: routine.title,
    categoryName: routine.area,
    description: routine.description,
    immediateCorrectiveAction: routine.immediateCorrectiveAction ?? DEFAULT_CORRECTIVE_ACTION,
    dueAtLabel: routine.dueAt,
    completedAtLabel: routine.checked ? routine.lastCompletedAt : 'Ikke utført',
    lastCompletedAtLabel: routine.lastCompletedAt,
    completedForCurrentInterval: routine.checked,
    selected: false,
    isAssignedRoutine: false,
  }))
}

function mapAssignedRoutine(routine: AssignedRoutine): RoutineCard {
  return {
    key: `assigned-${routine.routineId}`,
    routineId: routine.routineId,
    title: routine.title,
    categoryName: routine.categoryName,
    description: routine.description,
    immediateCorrectiveAction: routine.immediateCorrectiveAction,
    dueAtLabel: formatDateTime(routine.dueAt),
    completedAtLabel: routine.completedAt ? formatDateTime(routine.completedAt) : 'Ikke utført',
    lastCompletedAtLabel: formatDateTime(routine.lastCompletedAt),
    completedForCurrentInterval: routine.completedForCurrentInterval,
    selected: false,
    isAssignedRoutine: true,
  }
}

function showMockRoutines(message: string) {
  routines.value = createMockRoutines()
  showingAssignedRoutines.value = false
  infoMessage.value = message
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

async function fetchRoutines() {
  const fetchId = ++activeFetchId
  hasAuthenticatedSession.value = Boolean(getAuthToken())

  if (!hasAuthenticatedSession.value) {
    if (fetchId === activeFetchId) {
      showMockRoutines('Viser eksempelrutiner. Logg inn for å se og sende inn tildelte rutiner.')
      loading.value = false
    }
    return
  }

  loading.value = true
  errorMessage.value = null
  infoMessage.value = null

  try {
    const response = await fetch('/api/me/routines', {
      credentials: 'same-origin',
      headers: createAuthHeaders(),
    })

    if (response.status === 401 || response.status === 403) {
      clearAuthToken()
      hasAuthenticatedSession.value = false
      if (fetchId === activeFetchId) {
        showMockRoutines('Innloggingen er utløpt. Logg inn igjen for å hente tildelte rutiner.')
      }
      return
    }

    if (!response.ok) {
      throw new Error(await readErrorMessage(response, 'Kunne ikke hente tildelte rutiner.'))
    }

    if (fetchId !== activeFetchId) {
      return
    }

    routines.value = ((await response.json()) as AssignedRoutine[]).map(mapAssignedRoutine)
    showingAssignedRoutines.value = true
    openInfoId.value = null
    infoMessage.value =
      routines.value.length === 0 ? 'Du har ingen rutiner tildelt akkurat nå.' : null
  } catch (error) {
    if (fetchId === activeFetchId) {
      showMockRoutines('Kunne ikke laste tildelte rutiner nå. Viser eksempelrutiner foreløpig.')
      errorMessage.value =
        error instanceof Error ? error.message : 'Kunne ikke laste tildelte rutiner.'
    }
  } finally {
    if (fetchId === activeFetchId) {
      loading.value = false
    }
  }
}

function handleRoutineCheck(routine: RoutineCard, event: Event) {
  const input = event.target as HTMLInputElement

  if (!routine.isAssignedRoutine || routine.completedForCurrentInterval || submitting.value) {
    input.checked = routine.completedForCurrentInterval || routine.selected
    return
  }

  routines.value = routines.value.map((item) =>
    item.key === routine.key
      ? {
          ...item,
          selected: input.checked,
        }
      : item,
  )
}

async function handlePrimaryAction() {
  if (!hasAuthenticatedSession.value) {
    await router.push('/auth')
    return
  }

  if (!showingAssignedRoutines.value) {
    await fetchRoutines()
    return
  }

  const selectedRoutines = routines.value.filter(
    (routine) => routine.isAssignedRoutine && routine.selected && !routine.completedForCurrentInterval && routine.routineId,
  )

  if (selectedRoutines.length === 0) {
    return
  }

  submitting.value = true
  errorMessage.value = null

  try {
    const updatedRoutines = new Map<number, RoutineCard>()

    for (const routine of selectedRoutines) {
      const response = await fetch(`/api/me/routines/${routine.routineId}/records`, {
        method: 'POST',
        credentials: 'same-origin',
        headers: createAuthHeaders(),
      })

      if (response.status === 401 || response.status === 403) {
        clearAuthToken()
        hasAuthenticatedSession.value = false
        showMockRoutines('Innloggingen er utløpt. Logg inn igjen for å sende inn rutiner.')
        return
      }

      if (!response.ok) {
        throw new Error(await readErrorMessage(response, 'Kunne ikke registrere rutinen som utført.'))
      }

      const updatedRoutine = (await response.json()) as AssignedRoutine
      updatedRoutines.set(updatedRoutine.routineId, mapAssignedRoutine(updatedRoutine))
    }

    routines.value = routines.value.map((item) =>
      item.routineId !== null && updatedRoutines.has(item.routineId) ? updatedRoutines.get(item.routineId)! : item,
    )
    infoMessage.value = `${updatedRoutines.size} rutiner ble registrert som utført.`
  } catch (error) {
    errorMessage.value =
      error instanceof Error ? error.message : 'Kunne ikke registrere rutinen som utført.'
  } finally {
    submitting.value = false
  }
}

watch(() => claims.value?.orgId ?? null, () => {
  void fetchRoutines()
}, { immediate: true })
</script>

<template>
  <section class="page-layout">
    <SectionHeading
      title="Rutiner"
      :subtitle="routineCountLabel"
      icon="checklist"
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
        Laster rutiner...
      </p>
    </article>

    <article
      v-else-if="routines.length === 0"
      class="surface-card"
    >
      <p class="card-copy">
        {{ showingAssignedRoutines ? 'Du har ingen rutiner tildelt akkurat nå.' : 'Ingen eksempelrutiner tilgjengelig.' }}
      </p>
    </article>

    <div
      v-else
      class="routine-grid"
    >
      <article
        v-for="routine in routines"
        :key="routine.key"
        class="routine-card routine-card--checkable"
      >
        <div class="routine-card__header">
          <div>
            <p class="eyebrow">
              {{ routine.categoryName ?? 'Rutine' }}
            </p>
            <div class="routine-card__title-row">
              <h3 class="card-title">
                {{ routine.title }}
              </h3>
              <button
                class="info-button info-button--routine"
                type="button"
                :aria-label="`Vis beskrivelse for ${routine.title}`"
                @click="toggleInfo(routine.key)"
              >
                <AppIcon name="info" />
              </button>
            </div>
          </div>
          <label
            class="routine-checkbox"
            :aria-label="`Marker ${routine.title} for innsending`"
          >
            <input
              :checked="routine.completedForCurrentInterval || routine.selected"
              :disabled="!routine.isAssignedRoutine || routine.completedForCurrentInterval || submitting"
              type="checkbox"
              @change="handleRoutineCheck(routine, $event)"
            >
            <span
              class="routine-checkbox__box"
              aria-hidden="true"
            />
          </label>
        </div>

        <article
          v-if="openInfoId === routine.key"
          class="info-popover routine-card__info"
        >
          <p class="info-popover__title">
            Beskrivelse
          </p>
          <p class="card-copy">
            {{ routine.description }}
          </p>

          <p class="info-popover__title routine-card__subheading">
            Hvis oppgaven ikke kan fullføres
          </p>
          <p class="card-copy">
            {{ routine.immediateCorrectiveAction }}
          </p>
        </article>

        <p
          class="status-pill"
          :class="routine.completedForCurrentInterval ? 'status-pill--success' : 'status-pill--warning'"
        >
          {{ routine.completedForCurrentInterval ? 'Utført i denne perioden' : 'Venter på utførelse' }}
        </p>

        <dl class="detail-list">
          <div class="detail-list__item">
            <dt>Frist</dt>
            <dd>{{ routine.dueAtLabel }}</dd>
          </div>
          <div class="detail-list__item">
            <dt>Sist utført</dt>
            <dd>{{ routine.lastCompletedAtLabel }}</dd>
          </div>
          <div class="detail-list__item">
            <dt>Denne perioden</dt>
            <dd>{{ routine.completedAtLabel }}</dd>
          </div>
        </dl>
      </article>
    </div>

    <div v-if="!loading">
      <p class="caption-note caption-note--before-action">
        {{ helperText }}
      </p>
      <PrimaryActionButton
        :disabled="primaryActionDisabled"
        :label="primaryActionLabel"
        type="button"
        @click="handlePrimaryAction"
      />
    </div>
  </section>
</template>
