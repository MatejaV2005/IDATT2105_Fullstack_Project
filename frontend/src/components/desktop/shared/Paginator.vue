<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ChevronRight } from '@lucide/vue'

const route = useRoute()

function formatSegment(segment: string) {
  return decodeURIComponent(segment)
    .replace(/[-_]/g, ' ')
    .replace(/\b\w/g, (char) => char.toUpperCase())
}

const path = computed(() => {
  const normalizedPath = route.path.replace(/\/+/g, '/').replace(/\/$/, '')

  return normalizedPath || '/'
})

const rootItem = computed(() => {
  const segments = path.value.split('/').filter(Boolean)
  const rootSegment = segments[0] === 'mobile' ? 'mobile' : 'desktop'

  return {
    label: rootSegment === 'desktop' ? 'Home' : formatSegment(rootSegment),
    to: `/${rootSegment}`,
    isCurrent: segments.length <= 1,
  }
})

const pathItems = computed(() => {
  const segments = path.value.split('/').filter(Boolean)
  const restSegments = segments.slice(1)

  return restSegments.map((segment, index) => ({
    label: formatSegment(segment),
    to: '/' + segments.slice(0, index + 2).join('/'),
  }))
})
</script>

<template>
  <nav class="paginator" aria-label="Breadcrumb">
    <RouterLink v-if="!rootItem.isCurrent" class="pagination-item" :to="rootItem.to">
      {{ rootItem.label }}
    </RouterLink>
    <span v-else class="pagination-item is-current">{{ rootItem.label }}</span>
    <template v-for="(item, index) in pathItems" :key="item.to">
      <ChevronRight class="chevron" />
      <RouterLink v-if="index !== pathItems.length - 1" class="pagination-item" :to="item.to">
        {{ item.label }}
      </RouterLink>
      <span v-else class="pagination-item is-current">{{ item.label }}</span>
    </template>
  </nav>
</template>

<style scoped>
.paginator {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.25rem;
}

.pagination-item {
  color: var(--blue-navy);
  font-size: 0.95rem;
  text-decoration: none;
}

.pagination-item:hover {
  text-decoration: underline;
}

.is-current {
  color: var(--blue-navy-60);
  text-decoration: none;
}

.chevron {
  width: 0.95rem;
  height: 0.95rem;
  color: var(--blue-navy-60);
}
</style>
