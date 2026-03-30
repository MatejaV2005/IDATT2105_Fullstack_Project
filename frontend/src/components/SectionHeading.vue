<script setup lang="ts">
import AppIcon from './AppIcon.vue'

defineProps<{
  title: string
  icon: 'temperature' | 'clock' | 'humidity' | 'checklist' | 'alert' | 'spark'
  subtitle?: string
}>()
</script>

<template>
  <div class="section-heading">
    <div class="section-heading__main">
      <div class="section-heading__icon">
        <AppIcon :name="icon" />
      </div>
      <div class="section-heading__content">
        <h2 class="section-heading__title">{{ title }}</h2>
        <p v-if="subtitle" class="section-heading__subtitle">{{ subtitle }}</p>
      </div>
    </div>
    <div v-if="$slots.actions" class="section-heading__actions">
      <slot name="actions" />
    </div>
  </div>
</template>
