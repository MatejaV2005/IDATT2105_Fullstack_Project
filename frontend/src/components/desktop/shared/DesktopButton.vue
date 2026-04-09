<script setup lang="ts">
import type { Component } from 'vue'

const props = defineProps<{
  isLoading?: boolean
  loadingText?: string
  content: string
  icon: Component
  onClick?: () => any
  buttonColor?: 'navy' | 'cherry' | 'blue-decor' | 'boring-ghost'
  disabled?: boolean
}>()

function click() {
  if (!props.disabled && props.onClick) {
    props.onClick()
  }
}
</script>

<template>
  <button
    :class="
      'transition desktop-button desktop-button--' +
        (buttonColor || 'navy') +
        (disabled ? ' disabled-classy' : '')
    "
    v-bind="$attrs"
    :disabled="Boolean(disabled)"
    @click="click"
  >
    <component
      :is="icon"
      v-if="!isLoading"
      class="icon"
    />
    <span
      v-else
      class="loading-spinner"
    />
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
.desktop-button {
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
.desktop-button:disabled {
  opacity: 40%;
  cursor: auto !important;
}

.desktop-button--navy {
  background-color: var(--blue-navy);
  border: 1px solid var(--blue-navy);
}
.desktop-button--navy:not(.disabled-classy):hover,
.desktop-button--navy:not(.disabled-classy):focus {
  background-color: var(--blue-navy-20);
  color: var(--blue-navy);
  svg {
    stroke: var(--blue-navy);
  }
}
.desktop-button--navy:not(.disabled-classy):hover .loading-spinner,
.desktop-button--navy:not(.disabled-classy):focus .loading-spinner {
  border-color: var(--blue-navy);
  border-bottom-color: transparent;
}

.desktop-button--cherry {
  background-color: var(--red-cherry);
  border: 1px solid var(--red-cherry);
}
.desktop-button--cherry:not(.disabled-classy):hover,
.desktop-button--cherry:not(.disabled-classy):focus {
  background-color: var(--red-cherry-20);
  color: var(--red-cherry);
  svg {
    stroke: var(--red-cherry);
  }
}
.desktop-button--cherry:not(.disabled-classy):hover .loading-spinner,
.desktop-button--cherry:not(.disabled-classy):focus .loading-spinner {
  border-color: var(--red-cherry);
  border-bottom-color: transparent;
}

.desktop-button--boring-ghost {
  background-color: transparent;
  color: var(--black-no-face);
}
.desktop-button--boring-ghost:not(.disabled-classy):hover,
.desktop-button--boring-ghost:not(.disabled-classy):focus {
  background-color: var(--black-no-face-20);
  color: var(--black-no-face);
  svg {
    stroke: var(--black-no-face);
  }
}
.desktop-button--boring-ghost:not(.disabled-classy):hover .loading-spinner,
.desktop-button--boring-ghost:not(.disabled-classy):focus .loading-spinner {
  border-color: var(--black-no-face);
  border-bottom-color: transparent;
}

.desktop-button--blue-decor {
  background-color: var(--blue-decor);
  border: 1px solid var(--blue-decor);
}
.desktop-button--blue-decor:not(.disabled-classy):hover,
.desktop-button--blue-decor:not(.disabled-classy):focus {
  background-color: var(--blue-decor-20);
  color: var(--blue-decor);
  svg {
    stroke: var(--blue-decor);
  }
}
.desktop-button--blue-decor:not(.disabled-classy):hover .loading-spinner,
.desktop-button--blue-decor:not(.disabled-classy):focus .loading-spinner {
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
