<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'

import AppIcon from '@/components/AppIcon.vue'
import PrimaryActionButton from '@/components/PrimaryActionButton.vue'
import SectionHeading from '@/components/SectionHeading.vue'
import { deviationCategories, type DeviationCategory } from '@/data/deviations'
import api from '@/api/api'

const route = useRoute()

const prefilledRecordId = Number.parseInt(String(route.query.ccpRecordId ?? ''), 10)
const prefilledCategory = String(route.query.category ?? 'OTHER') as DeviationCategory
const prefilledMeasuredValue = String(route.query.measuredValue ?? '').trim()
const prefilledUnit = String(route.query.unit ?? '').trim()
const prefilledCcpName = String(route.query.ccpName ?? '').trim()

const formState = reactive({
  ccpRecordId: Number.isFinite(prefilledRecordId) ? prefilledRecordId : null as number | null,
  category: deviationCategories.some((item) => item.value === prefilledCategory)
    ? prefilledCategory
    : 'OTHER' as DeviationCategory,
  whatWentWrong: '',
  immediateActionTaken: '',
  potentialCause: '',
  potentialPreventativeMeasure: '',
})

const showInfo = ref(false)
const isSubmitting = ref(false)
const submitError = ref<string | null>(null)
const submitSuccess = ref(false)

const deviationContext = computed(() => {
  if (!formState.ccpRecordId) {
    return null
  }

  if (prefilledCcpName && prefilledMeasuredValue) {
    return `${prefilledCcpName}: ${prefilledMeasuredValue}${prefilledUnit ? ` ${prefilledUnit}` : ''}`
  }

  if (prefilledCcpName) {
    return prefilledCcpName
  }

  return `CCP-logg #${formState.ccpRecordId}`
})

async function submitDeviation() {
  isSubmitting.value = true
  submitError.value = null

  try {
    await api.post('/me/deviations', {
      ccpRecordId: formState.ccpRecordId,
      category: formState.category,
      whatWentWrong: formState.whatWentWrong,
      immediateActionTaken: formState.immediateActionTaken,
      potentialCause: formState.potentialCause,
      potentialPreventativeMeasure: formState.potentialPreventativeMeasure,
    })

    submitSuccess.value = true
  } catch (err) {
    submitError.value = 'Kunne ikke sende avvik. Prøv igjen.'
    console.error(err)
  } finally {
    isSubmitting.value = false
  }
}

function resetDeviationForm() {
  formState.category = deviationCategories.some((item) => item.value === prefilledCategory)
    ? prefilledCategory
    : 'OTHER'
  formState.whatWentWrong = ''
  formState.immediateActionTaken = ''
  formState.potentialCause = ''
  formState.potentialPreventativeMeasure = ''
  submitError.value = null
  submitSuccess.value = false
}
</script>

<template>
  <section class="page-layout">
    <SectionHeading
      title="Avvik"
      icon="warning"
    >
      <template #actions>
        <button
          class="info-button"
          type="button"
          aria-label="Vis informasjon om avviksskjema"
          @click="showInfo = !showInfo"
        >
          <AppIcon name="info" />
        </button>
      </template>
    </SectionHeading>

    <article
      v-if="showInfo"
      class="info-popover"
    >
      <p class="info-popover__title">
        Om avvik
      </p>
      <p class="card-copy">
        Fyll inn dette skjemaet ved et avvik, for eksempel om en måling havner utenfor ønsket verdi.
      </p>
    </article>

    <article
      v-if="deviationContext"
      class="info-banner info-banner--danger"
    >
      <p class="info-popover__title">
        Koblet til måling
      </p>
      <p class="card-copy">
        {{ deviationContext }}
      </p>
    </article>

    <div
      v-if="submitSuccess"
      class="success-message surface-card"
    >
      <AppIcon name="upload" />
      <p>Avviket er sendt inn!</p>
      <p class="card-copy">
        Du kan registrere et nytt avvik dersom det er flere hendelser som skal dokumenteres.
      </p>
      <div class="success-message__actions">
        <PrimaryActionButton
          label="Registrer nytt avvik"
          type="button"
          @click="resetDeviationForm"
        />
      </div>
    </div>

    <form
      v-else
      class="form-grid"
      @submit.prevent="submitDeviation"
    >
      <article class="form-card form-card--full">
        <label
          class="form-label"
          for="category"
        >Kategori</label>
        <div class="field-shell">
          <select
            id="category"
            v-model="formState.category"
            class="field-shell__select"
          >
            <option
              v-for="cat in deviationCategories"
              :key="cat.value"
              :value="cat.value"
            >
              {{ cat.label }}
            </option>
          </select>
          <span class="field-shell__caret">
            <AppIcon name="chevron" />
          </span>
        </div>
      </article>

      <article class="form-card form-card--full">
        <label
          class="form-label"
          for="whatWentWrong"
        >* Hva gikk galt</label>
        <textarea
          id="whatWentWrong"
          v-model="formState.whatWentWrong"
          class="field-shell__textarea"
          placeholder="Beskriv hva som skjedde..."
        />
      </article>

      <article class="form-card form-card--full">
        <label
          class="form-label"
          for="immediateActionTaken"
        >* Umiddelbar handling utført</label>
        <textarea
          id="immediateActionTaken"
          v-model="formState.immediateActionTaken"
          class="field-shell__textarea"
          placeholder="Hva ble gjort umiddelbart?"
        />
      </article>

      <article class="form-card form-card--full">
        <label
          class="form-label"
          for="potentialCause"
        >* Potensiell årsak</label>
        <textarea
          id="potentialCause"
          v-model="formState.potentialCause"
          class="field-shell__textarea"
          placeholder="Hva kan være årsaken til avviket?"
        />
      </article>

      <article class="form-card form-card--full">
        <label
          class="form-label"
          for="potentialPreventativeMeasure"
        >* Potensielt tiltak</label>
        <textarea
          id="potentialPreventativeMeasure"
          v-model="formState.potentialPreventativeMeasure"
          class="field-shell__textarea"
          placeholder="Hva kan gjøres for å forebygge?"
        />
      </article>

      <div class="form-card--full">
        <p class="caption-note caption-note--before-action">
          Alle felt med * må fylles ut før innsending.
        </p>
        <PrimaryActionButton
          label="Send inn avvik"
          type="submit"
          :loading="isSubmitting"
        />
        <p
          v-if="submitError"
          class="error-text"
        >
          {{ submitError }}
        </p>
      </div>
    </form>
  </section>
</template>

<style scoped>
.success-message {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  text-align: center;
}

.success-message__actions {
  width: 100%;
}
</style>
