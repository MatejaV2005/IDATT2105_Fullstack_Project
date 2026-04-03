<script setup lang="ts">
import { reactive, ref } from 'vue'

import AppIcon from '@/components/AppIcon.vue'
import PrimaryActionButton from '@/components/PrimaryActionButton.vue'
import SectionHeading from '@/components/SectionHeading.vue'
import { routineItems } from '@/data/mockHaccp'

const routines = reactive(routineItems.map((routine) => ({ ...routine })))
const openInfoId = ref<string | null>(null)

function toggleInfo(routineId: string) {
  openInfoId.value = openInfoId.value === routineId ? null : routineId
}
</script>

<template>
  <section class="page-layout">
    <SectionHeading
      title="Rutiner"
      icon="checklist"
    />

    <div class="routine-grid">
      <article
        v-for="routine in routines"
        :key="routine.id"
        class="routine-card routine-card--checkable"
      >
        <div class="routine-card__header">
          <div>
            <p class="eyebrow">
              {{ routine.area }}
            </p>
            <div class="routine-card__title-row">
              <h3 class="card-title">
                {{ routine.title }}
              </h3>
              <button
                class="info-button info-button--routine"
                type="button"
                :aria-label="`Vis beskrivelse for ${routine.title}`"
                @click="toggleInfo(routine.id)"
              >
                <AppIcon name="info" />
              </button>
            </div>
          </div>
          <label
            class="routine-checkbox"
            :aria-label="`Marker ${routine.title} som utført`"
          >
            <input
              v-model="routine.checked"
              type="checkbox"
            >
            <span
              class="routine-checkbox__box"
              aria-hidden="true"
            />
          </label>
        </div>

        <article
          v-if="openInfoId === routine.id"
          class="info-popover routine-card__info"
        >
          <p class="info-popover__title">
            Beskrivelse
          </p>
          <p class="card-copy">
            {{ routine.description }}
          </p>
        </article>

        <dl class="detail-list">
          <div class="detail-list__item">
            <dt>Skal gjøres</dt>
            <dd>{{ routine.dueAt }}</dd>
          </div>
          <div class="detail-list__item">
            <dt>Sist utført</dt>
            <dd>{{ routine.lastCompletedAt }}</dd>
          </div>
        </dl>
      </article>
    </div>

    <PrimaryActionButton label="Send inn" />
  </section>
</template>
