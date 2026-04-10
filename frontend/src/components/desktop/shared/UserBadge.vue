<script setup lang="ts">
import { User } from '@lucide/vue'
import Badge from './Badge.vue'
defineProps<{
  name: string
  userId?: number
}>()
</script>
<template>
  <RouterLink
    :to="userId ? '/desktop/users/' + userId : '#'"
    class="user-badge-link"
  >
    <Badge
      badge-color="navy"
      :icon="User"
      class="user-badge"
    >
      {{ name }}
    </Badge>
  </RouterLink>
</template>
<style scoped>
.user-badge-link {
}
.user-badge {
}
</style>
