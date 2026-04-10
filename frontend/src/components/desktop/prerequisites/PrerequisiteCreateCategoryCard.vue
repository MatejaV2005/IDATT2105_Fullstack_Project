<script setup lang="ts">
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import { Save, X } from '@lucide/vue'

defineProps<{
  newCategoryName: string
  isLoading: boolean
  hasError: boolean
  setNewCategoryName: (value: string) => void
  onCreate: () => void
  onCancel: () => void
}>()
</script>

<template>
  <div class="create-category-card">
    <h2 class="no-margin">Ny kategori</h2>
    <label class="create-category-field">
      <span class="navy-subtitle">Kategorinavn</span>
      <input
        :value="newCategoryName"
        class="simple-text-input category-input"
        :disabled="isLoading"
        type="text"
        placeholder="F.eks. Renhold og hygiene"
        @input="setNewCategoryName(($event.target as HTMLInputElement).value)"
      />
    </label>
    <div class="edit-actions">
      <DesktopButton
        :icon="Save"
        content="Opprett"
        :on-click="onCreate"
        :disabled="newCategoryName.trim().length === 0"
        :is-loading="isLoading"
        loading-text="Oppretter"
      />
      <DesktopButton
        :icon="X"
        content="Avbryt"
        button-color="cherry"
        :on-click="onCancel"
        :disabled="isLoading"
      />
    </div>
    <p v-if="hasError" class="error-message no-margin">Klarte ikke å opprette kategori.</p>
  </div>
</template>

<style scoped>
.create-category-card {
  border-radius: 0.75rem;
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.create-category-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.category-input {
  width: 100%;
  max-width: 28rem;
  min-height: 2rem;
}

.edit-actions {
  display: flex;
  gap: 0.5rem;
}

.error-message {
  color: #b42318;
}
</style>
