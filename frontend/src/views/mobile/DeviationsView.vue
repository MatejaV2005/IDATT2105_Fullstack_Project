<script setup lang="ts">
import { computed, reactive, ref } from 'vue'

import AppIcon from '@/components/AppIcon.vue'
import PhotoUploadControl from '@/components/PhotoUploadControl.vue'
import PrimaryActionButton from '@/components/PrimaryActionButton.vue'
import SectionHeading from '@/components/SectionHeading.vue'
import { deviationOptions } from '@/data/mockHaccp'
import type { UploadedPhoto } from '@/types/uploads'

const now = new Date()
const pad = (value: number) => String(value).padStart(2, '0')

const formState = reactive({
  title: '',
  routineArea: deviationOptions.routineAreas[1],
  severity: deviationOptions.severityLevels[1],
  relatedToFood: false,
  relatedToAlcohol: false,
  happenedDate: `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())}`,
  happenedTime: `${pad(now.getHours())}:${pad(now.getMinutes())}`,
  description: '',
  immediateAction: '',
})

const showInfo = ref(false)
const uploadedPhotos = ref<UploadedPhoto[]>([])
const formattedDateTime = computed(() => `${formState.happenedDate} kl. ${formState.happenedTime}`)
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
        Bruk skjemaet når en kontroll ikke er utført som planlagt eller en måling havner utenfor ønsket verdi.
      </p>
      <p class="helper-text">
        Valgt tidspunkt: {{ formattedDateTime }}
      </p>
    </article>

    <form
      class="form-grid"
      @submit.prevent
    >
      <article class="form-card form-card--full">
        <label
          class="form-label"
          for="deviationTitle"
        >Tittel</label>
        <input
          id="deviationTitle"
          v-model="formState.title"
          class="field-shell__input"
          type="text"
          maxlength="120"
          placeholder="For høy temperatur i kjøleskap"
        >
      </article>

      <article class="form-card">
        <label
          class="form-label"
          for="routineArea"
        >Rutineområde</label>
        <div class="field-shell">
          <select
            id="routineArea"
            v-model="formState.routineArea"
            class="field-shell__select"
          >
            <option
              v-for="area in deviationOptions.routineAreas"
              :key="area"
              :value="area"
            >
              {{ area }}
            </option>
          </select>
          <span class="field-shell__caret">
            <AppIcon name="chevron" />
          </span>
        </div>
      </article>

      <article class="form-card">
        <label
          class="form-label"
          for="severity"
        >Alvorlighetsgrad</label>
        <div class="field-shell">
          <select
            id="severity"
            v-model="formState.severity"
            class="field-shell__select"
          >
            <option
              v-for="level in deviationOptions.severityLevels"
              :key="level"
              :value="level"
            >
              {{ level }}
            </option>
          </select>
          <span class="field-shell__caret">
            <AppIcon name="chevron" />
          </span>
        </div>
      </article>

      <article class="form-card">
        <label
          class="form-label"
          for="happenedDate"
        >Dato oppdaget</label>
        <input
          id="happenedDate"
          v-model="formState.happenedDate"
          class="field-shell__input field-shell__input--date"
          type="date"
        >
      </article>

      <article class="form-card">
        <label
          class="form-label"
          for="happenedTime"
        >Tid oppdaget</label>
        <input
          id="happenedTime"
          v-model="formState.happenedTime"
          class="field-shell__input field-shell__input--time"
          type="time"
          step="60"
        >
      </article>
      
      <article class="form-card">
        <p class="form-label">
          Relatert til
        </p>
        <div class="option-checks">
          <label class="option-check">
            <input
              v-model="formState.relatedToFood"
              type="checkbox"
            >
            <span
              class="option-check__box"
              aria-hidden="true"
            />
            <span class="option-check__label">Mat</span>
          </label>

          <label class="option-check">
            <input
              v-model="formState.relatedToAlcohol"
              type="checkbox"
            >
            <span
              class="option-check__box"
              aria-hidden="true"
            />
            <span class="option-check__label">Alkohol</span>
          </label>
        </div>
      </article>

      <article class="form-card">
        <label class="form-label">Bildevedlegg</label>
        <PhotoUploadControl
          v-model="uploadedPhotos"
          label="Legg til dokumentasjon"
          description="Velg flere bilder fra album eller ta nye bilder med kamera."
          :helper-text="`Eksempelbilder: ${deviationOptions.samplePhotos.join(', ')}`"
          variant="panel"
          multiple
          :max-files="8"
        />
      </article>

      <article class="form-card form-card--full">
        <label
          class="form-label"
          for="description"
        >* Beskrivelse av avvik</label>
        <textarea
          id="description"
          v-model="formState.description"
          class="field-shell__textarea"
          placeholder="Temperaturen i Kjøleskap 1 ble målt til 8 °C under ettermiddagskontrollen."
        />
      </article>

      <article class="form-card form-card--full">
        <label
          class="form-label"
          for="immediateAction"
        >* Umiddelbar handling utført</label>
        <textarea
          id="immediateAction"
          v-model="formState.immediateAction"
          class="field-shell__textarea"
          placeholder="Flyttet varer til reservekjøler og startet ekstra temperaturmåling hvert 15. minutt."
        />
      </article>

      <div class="form-card--full">
        <PrimaryActionButton
          label="Send inn avvik"
          type="submit"
        />
        <p class="caption-note">
          Alle felt med * må fylles ut før innsending.
        </p>
      </div>
    </form>
  </section>
</template>
