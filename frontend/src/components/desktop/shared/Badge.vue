<script setup lang="ts">
import type { Component } from 'vue'

defineProps<{
  badgeColor: 'navy' | 'grey' | 'cherry'
  icon?: Component
}>()
</script>

<template>
  <div
    :class="'transition badge badge-' + badgeColor"
    v-bind="$attrs"
  >
    <component
      :is="icon"
      :v-if="!!icon"
      class="icon"
    />
    <slot />
  </div>
</template>
<style scoped>
.badge {
  border-radius: 0.5rem;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  padding-top: 0.25rem;
  padding-bottom: 0.25rem;
  padding-left: 1rem;
  padding-right: 1rem;
  width: max-content;
  /* font-size: x-large; */
}
.badge.badge-navy {
  background-color: var(--blue-navy-20);
  border: 1px solid var(--blue-navy-40);
  color: var(--blue-navy);
  stroke: var(--blue-navy);
}
.badge.badge-cherry {
  background-color: var(--red-cherry-20);
  border: 1px solid var(--red-cherry-40);
  color: var(--red-cherry);
  stroke: var(--red-cherry);
}
.badge.badge-grey {
  background-color: var(--blue-navy-20);
  border: 1px solid var(--blue-navy-40);
  color: var(--blue-navy);
  stroke: var(--blue-navy);
}
.icon {
  width: 1rem;
  height: 1rem;
}
</style>
