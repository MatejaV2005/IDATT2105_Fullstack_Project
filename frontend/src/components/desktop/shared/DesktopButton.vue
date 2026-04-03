<script setup lang="ts">
import type { Component } from 'vue'

defineProps<{
  isLoading?: boolean
  loadingText?: string
  content: string
  icon: Component
  onClick?: () => any
  buttonColor?: 'navy' | 'cherry' | 'blue-decor'
}>()
</script>

<template>
  <button
    @click="onClick"
    :class="'transition navy-button navy-button--' + (buttonColor || 'navy')"
    v-bind="$attrs"
  >
    <component class="icon" v-if="!isLoading" :is="icon" />
    <span v-else class="loading-spinner"></span>
    <span class="no-margin">
      {{ isLoading ? loadingText || 'Loading...' : content }}
    </span>
  </button>
</template>
<style scoped>
.icon {
  width: 1rem;
  height: 1rem;
}
.navy-button {
  border-radius: 0.5rem;
  color: var(--white-greek);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  padding-top: 0.25rem;
  padding-bottom: 0.25rem;
  padding-left: 1rem;
  padding-right: 1rem;
  stroke: var(--white-greek);
  /* font-size: x-large; */
}

.navy-button--navy {
  background-color: var(--blue-navy);
  border: 1px solid var(--blue-navy);
}
.navy-button--navy:hover,
.navy-button--navy:focus {
  background-color: var(--blue-navy-20);
  color: var(--blue-navy);
  svg {
    stroke: var(--blue-navy);
  }
}
.navy-button--navy:hover .loading-spinner,
.navy-button--navy:focus .loading-spinner {
  border-color: var(--blue-navy);
  border-bottom-color: transparent;
}

.navy-button--cherry {
  background-color: var(--red-cherry);
  border: 1px solid var(--red-cherry);
}
.navy-button--cherry:hover,
.navy-button--cherry:focus {
  background-color: var(--red-cherry-20);
  color: var(--red-cherry);
  svg {
    stroke: var(--red-cherry);
  }
}
.navy-button--cherry:hover .loading-spinner,
.navy-button--cherry:focus .loading-spinner {
  border-color: var(--red-cherry);
  border-bottom-color: transparent;
}

.navy-button--blue-decor {
  background-color: var(--blue-decor);
  border: 1px solid var(--blue-decor);
}
.navy-button--blue-decor:hover,
.navy-button--blue-decor:focus {
  background-color: var(--blue-decor-20);
  color: var(--blue-decor);
  svg {
    stroke: var(--blue-decor);
  }
}
.navy-button--blue-decor:hover .loading-spinner,
.navy-button--blue-decor:focus .loading-spinner {
  border-color: var(--blue-decor);
  border-bottom-color: transparent;
}

.loading-spinner {
  width: 1rem;
  height: 1rem;
  border: 2px solid var(--white-greek);
  border-bottom-color: transparent;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
