<script setup lang="ts">
import type { Component } from 'vue'
defineProps<{
  name: string
  isSelected: boolean
  to: string
  icon: Component
}>()
</script>

<template>
  <RouterLink
    :to="String(to)"
    :class="'transition sidebar-option ' + (isSelected ? 'is-selected' : '')"
  >
    <component
      :is="icon"
      class="icon"
    />
    <span>
      {{ name }}
    </span>
  </RouterLink>
</template>

<style scoped>
.sidebar-option {
  display: flex;
  justify-content: start;
  align-items: center;
  color: var(--blue-navy);
  border-radius: 1rem;
  font-size: large;
  gap: 1rem;
  svg {
    stroke: var(--blue-navy);
  }
  > span {
    min-width: 0;
  }
}
.sidebar-option:not(.is-selected):hover {
  background-color: var(--blue-navy-10);
}
.is-selected {
  background-color: var(--blue-navy-10);
}
.is-selected:hover {
  background-color: var(--blue-navy-20);
}
@media (min-width: 1200px) {
  /* Styles for devices larger than 1200px */
  .sidebar-option {
    padding: 1rem;
  }
}
@media (min-width: 768px) and (max-width: 1200px) {
  /* Styles for devices between 768px and 1200px */
  .sidebar-option {
    padding: 0.5rem;
  }
  .sidebar-option > span {
    display: block;
    width: 100%;
    min-width: 0;
    overflow-wrap: break-word;
  }
}
@media (min-width: 576px) and (max-width: 768px) {
  /* Styles for devices between 576px and 768px */
  .sidebar-option {
    padding: 1rem 0.25rem;
    flex-direction: column;
    > span {
      font-size: small;
    }
  }
  .sidebar-option > span {
    font-size: small;
    display: block;
    width: 100%;
    min-width: 0;
    overflow-wrap: break-word;
    text-align: center;
  }
}
@media (max-width: 576px) {
  .sidebar-option {
    padding: 1rem 0.25rem;
    flex-direction: column;
  }

  .sidebar-option > span {
    font-size: small;
    display: block;
    width: 100%;
    min-width: 0;
    overflow-wrap: break-word;
    text-align: center;
  }
}
</style>
