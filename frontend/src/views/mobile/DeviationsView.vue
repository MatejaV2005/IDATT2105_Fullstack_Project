<script setup lang="ts">
import { reactive, ref } from 'vue'

import AppIcon from '@/components/AppIcon.vue'
import PrimaryActionButton from '@/components/PrimaryActionButton.vue'
import SectionHeading from '@/components/SectionHeading.vue'
import { deviationCategories, type DeviationCategory } from '@/data/deviations'

const formState = reactive({
  organizationId: 1,
  ccpRecordId: null as number | null,
  category: 'OTHER' as DeviationCategory,
  whatWentWrong: '',
  immediateActionTaken: '',
  potentialCause: '',
  potentialPreventativeMeasure: '',
})

const showInfo = ref(false)
const isSubmitting = ref(false)
const submitError = ref<string | null>(null)
const submitSuccess = ref(false)

async function submitDeviation() {
  isSubmitting.value = true
  submitError.value = null
  
  try {
    const response = await fetch('/api/deviations', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer <JWT_TOKEN>' //TODO
      },
      body: JSON.stringify({
        organizationId: formState.organizationId,
        ccpRecordId: formState.ccpRecordId,
        category: formState.category,
        whatWentWrong: formState.whatWentWrong,
        immediateActionTaken: formState.immediateActionTaken,
        potentialCause: formState.potentialCause,
        potentialPreventativeMeasure: formState.potentialPreventativeMeasure,
      }),
    })
    
    if (!response.ok) {
      throw new Error('Failed to submit deviation')
    }
    
    submitSuccess.value = true
  } catch (err) {
    submitError.value = 'Kunne ikke sende avvik. Prøv igjen.'
    console.error(err)
  } finally {
    isSubmitting.value = false
  }
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

    <div v-if="submitSuccess" class="success-message">
      <AppIcon name="upload" />
      <p>Avviket er sendt inn!</p>
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
        <PrimaryActionButton
          label="Send inn avvik"
          type="submit"
          :loading="isSubmitting"
        />
        <p v-if="submitError" class="error-text">
          {{ submitError }}
        </p>
        <p class="caption-note">
          Alle felt med * må fylles ut før innsending.
        </p>
      </div>
    </form>
  </section>
</template>
