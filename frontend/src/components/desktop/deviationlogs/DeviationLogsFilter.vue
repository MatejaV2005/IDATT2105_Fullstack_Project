<script setup lang="ts">
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import { Check, Eye, X } from '@lucide/vue'
import type { DeviationReviewStatus } from './types'

defineProps<{
  reviewFilter: DeviationReviewStatus | 'ALL'
}>()

const emit = defineEmits<{
  setFilter: [filter: DeviationReviewStatus | 'ALL']
}>()
</script>

<template>
  <div class="filter-row">
    <DesktopButton
      content="Apne"
      :icon="Check"
      :on-click="() => emit('setFilter', 'OPEN')"
      :disabled="reviewFilter === 'OPEN'"
      button-color="navy"
    />
    <DesktopButton
      content="Lukkede"
      :icon="X"
      :on-click="() => emit('setFilter', 'CLOSED')"
      :disabled="reviewFilter === 'CLOSED'"
      button-color="navy"
    />
    <DesktopButton
      content="Alle"
      :icon="Eye"
      :on-click="() => emit('setFilter', 'ALL')"
      :disabled="reviewFilter === 'ALL'"
      button-color="navy"
    />
  </div>
</template>

<style scoped>
.filter-row {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}
</style>
