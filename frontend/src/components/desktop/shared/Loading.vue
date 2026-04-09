<script setup lang="ts">
import { LoaderCircle } from '@lucide/vue'
</script>

<template>
  <div class="center-content loading-container">
    <LoaderCircle
      :size="32"
      class="loading-spinner"
    />
    Laster
  </div>
</template>
<style scoped>
.loading-spinner {
  animation: spin 0.7s linear infinite;
}
.loading-container {
  gap: 0.5rem;
  font-size: large;
  color: var(--blue-navy);
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
