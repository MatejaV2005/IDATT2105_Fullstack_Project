<script setup lang="ts">
import { reactive } from 'vue'

import PhotoUploadControl from '@/components/PhotoUploadControl.vue'
import PrimaryActionButton from '@/components/PrimaryActionButton.vue'
import SectionHeading from '@/components/SectionHeading.vue'
import { humidityMetrics, temperatureMetrics, timedLogs } from '@/data/mockHaccp'
import type { UploadedPhoto } from '@/types/uploads'

const loggingState = reactive({
  temperatureMetrics: temperatureMetrics.map((metric) => ({ ...metric, photos: [] as UploadedPhoto[] })),
  timedLogs: timedLogs.map((item) => ({ ...item, photos: [] as UploadedPhoto[] })),
  humidityMetrics: humidityMetrics.map((metric) => ({ ...metric, photos: [] as UploadedPhoto[] })),
})
</script>

<template>
  <section class="page-layout page-layout--logging">
    <div class="section-stack">
      <SectionHeading title="Temperatur" icon="temperature" />

      <div class="metric-grid metric-grid--single">
        <article
          v-for="metric in loggingState.temperatureMetrics"
          :key="metric.id"
          class="metric-card"
        >
          <div class="metric-card__header">
            <p class="eyebrow">{{ metric.label }}</p>
            <PhotoUploadControl v-model="metric.photos" :label="`Legg ved bilde for ${metric.label}`" />
          </div>

          <label class="field-shell">
            <input v-model="metric.value" class="field-shell__input" inputmode="decimal" />
            <span class="field-shell__suffix">{{ metric.unit }}</span>
          </label>
        </article>
      </div>
    </div>

    <div class="section-stack">
      <SectionHeading title="Tid" icon="clock" />

      <article class="surface-card">
        <div v-for="item in loggingState.timedLogs" :key="item.id" class="timed-task">
          <div class="timed-task__copy">
            <h3 class="timed-task__title">{{ item.title }}</h3>
            <p class="timed-task__meta">{{ item.description }}</p>
          </div>

          <div class="timed-task__controls">
            <label class="field-shell timed-task__field">
              <input v-model="item.durationMinutes" class="field-shell__input" inputmode="numeric" />
              <span class="field-shell__suffix timed-task__suffix">
                min
              </span>
            </label>

            <PhotoUploadControl v-model="item.photos" :label="`Legg ved bilde for ${item.title}`" />
          </div>
        </div>
      </article>
    </div>

    <div class="section-stack">
      <SectionHeading title="Luftfuktighet" icon="humidity" />

      <div class="metric-grid">
        <article
          v-for="metric in loggingState.humidityMetrics"
          :key="metric.id"
          class="metric-card"
        >
          <div class="metric-card__header">
            <p class="eyebrow">{{ metric.label }}</p>
            <PhotoUploadControl v-model="metric.photos" :label="`Legg ved bilde for ${metric.label}`" />
          </div>

          <label class="field-shell">
            <input v-model="metric.value" class="field-shell__input" inputmode="numeric" />
            <span class="field-shell__suffix">{{ metric.unit }}</span>
          </label>
        </article>
      </div>
    </div>

    <PrimaryActionButton label="Send inn logg" />
  </section>
</template>
