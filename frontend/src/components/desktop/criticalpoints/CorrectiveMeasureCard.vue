<script setup lang="ts">
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import { Edit2, Save, Trash2, X } from '@lucide/vue'
import { computed, ref } from 'vue'

const props = defineProps<{
  id: number
  productCategoryId: number
  productCategoryName: string
  measureDescription: string
  disabled?: boolean
}>()

const emit = defineEmits<{
  update: [payload: { id: number; measureDescription: string }]
  delete: [payload: { id: number }]
}>()

const isEditing = ref(false)
const draftDescription = ref('')

const canSave = computed(() => draftDescription.value.trim().length > 0)

function startEdit() {
  if (props.disabled) {
    return
  }

  draftDescription.value = props.measureDescription
  isEditing.value = true
}

function cancelEdit() {
  isEditing.value = false
  draftDescription.value = ''
}

function saveEdit() {
  if (!canSave.value) {
    return
  }

  emit('update', {
    id: props.id,
    measureDescription: draftDescription.value.trim(),
  })
  isEditing.value = false
}

function deleteMeasure() {
  if (props.disabled) {
    return
  }

  emit('delete', { id: props.id })
}
</script>

<template>
  <div class="measure-card">
    <div class="measure-header">
      <span class="navy-subtitle">{{ productCategoryName }} (#{{ productCategoryId }})</span>
      <div class="measure-actions">
        <DesktopButton
          v-if="!isEditing"
          :icon="Edit2"
          content="Rediger"
          :on-click="startEdit"
          :disabled="disabled"
        />
        <template v-else>
          <DesktopButton
            :icon="Save"
            content="Lagre"
            :on-click="saveEdit"
            :disabled="!canSave"
          />
          <DesktopButton
            :icon="Trash2"
            content="Slett"
            button-color="cherry"
            :on-click="deleteMeasure"
          />
          <DesktopButton
            :icon="X"
            content="Avbryt"
            :on-click="cancelEdit"
            button-color="boring-ghost"
          />
        </template>
      </div>
    </div>

    <p
      v-if="!isEditing"
      class="measure-description"
    >
      {{ measureDescription }}
    </p>
    <textarea
      v-else
      v-model="draftDescription"
      class="simple-text-input"
      rows="3"
      placeholder="Beskriv korrigerende tiltak"
    />
  </div>
</template>

<style scoped>
.measure-card {
  padding: 1rem;
  border-radius: 0.5rem;
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.measure-header {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  align-items: center;
}

.measure-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.measure-description {
  margin: 0;
}

textarea.simple-text-input {
  width: 100%;
  resize: vertical;
  box-sizing: border-box;
  text-indent: 0;
  padding: 0.5rem;
}

@media (max-width: 768px) {
  .measure-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .measure-actions {
    width: 100%;
    flex-direction: column;
  }
}
</style>
