<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

const props = withDefaults(
  defineProps<{
    variant?: 'desktop' | 'mobile'
  }>(),
  {
    variant: 'desktop',
  },
)

const route = useRoute()
const activeView = computed(() => (route.path.startsWith('/mobile') ? 'mobile' : 'desktop'))

const items = [
  {
    key: 'desktop',
    label: 'Desktop',
    to: '/desktop/users/me',
  },
  {
    key: 'mobile',
    label: 'Mobile',
    to: '/mobile/rutiner',
  },
]
</script>

<template>
  <nav
    class="view-mode-switcher"
    :class="`view-mode-switcher--${variant}`"
    aria-label="Bytt mellom desktop og mobile visning"
  >
    <RouterLink
      v-for="item in items"
      :key="item.key"
      :to="item.to"
      class="view-mode-switcher__item transition"
      :class="{ 'view-mode-switcher__item--active': activeView === item.key }"
    >
      {{ item.label }}
    </RouterLink>
  </nav>
</template>

<style scoped>
.view-mode-switcher {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  border: 1px solid var(--blue-navy-20);
  background-color: var(--white-greek);
  max-width: 100%;
}

.view-mode-switcher--desktop {
  padding: 0.2rem;
  border-radius: 999px;
}

.view-mode-switcher--mobile {
  width: fit-content;
  padding: 0.2rem;
  border-radius: 999px;
}

.view-mode-switcher__item {
  flex: 1 1 auto;
  text-decoration: none;
  color: var(--blue-navy-80);
  font-weight: 600;
  text-align: center;
  white-space: nowrap;
}

.view-mode-switcher__item--active {
  color: var(--white-greek);
  background-color: var(--blue-navy);
}

.view-mode-switcher--desktop .view-mode-switcher__item {
  min-width: 4.75rem;
  padding: 0.35rem 0.75rem;
  border-radius: 999px;
  font-size: 0.82rem;
}

.view-mode-switcher--mobile .view-mode-switcher__item {
  min-width: 4.25rem;
  padding: 0.35rem 0.65rem;
  border-radius: 999px;
  font-size: 0.8rem;
}
</style>
