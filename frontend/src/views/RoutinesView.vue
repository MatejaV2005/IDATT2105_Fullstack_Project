<script setup lang="ts">
import { reactive } from 'vue'

import PrimaryActionButton from '@/components/PrimaryActionButton.vue'
import SectionHeading from '@/components/SectionHeading.vue'
import { routineItems } from '@/data/mockHaccp'

const routines = reactive(routineItems.map((routine) => ({ ...routine })))
</script>

<template>
  <section class="page-layout">
    <SectionHeading title="Rutiner" icon="checklist" />

    <div class="routine-grid">
      <article v-for="routine in routines" :key="routine.id" class="routine-card routine-card--checkable">
        <div class="routine-card__header">
          <div>
            <p class="eyebrow">{{ routine.area }}</p>
            <h3 class="card-title">{{ routine.title }}</h3>
          </div>
          <label class="routine-checkbox" :aria-label="`Marker ${routine.title} som utført`">
            <input v-model="routine.checked" type="checkbox" />
            <span class="routine-checkbox__box" aria-hidden="true"></span>
          </label>
        </div>

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
