<script setup lang="ts">
import Badge from '@/components/desktop/shared/Badge.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import type { MappingPoint } from '@/interfaces/api-interfaces'
import { CircleAlert, Edit2, Save, Trash2, X } from '@lucide/vue'
import { ref, watch } from 'vue'

const props = defineProps<{
  point: MappingPoint
  isSaving: boolean
  isDeleting: boolean
  saveError: boolean
  deleteError: boolean
}>()

const emit = defineEmits<{
  save: [payload: MappingPoint]
  deletePoint: [payload: MappingPoint]
}>()

const isEditing = ref(false)
const awaitingSaveResult = ref(false)
const editTitle = ref('')
const editLaw = ref('')
const editDots = ref(0)
const editChallenges = ref('')
const editMeasures = ref('')
const editResponsibleText = ref('')

function setFormFromPoint() {
  editTitle.value = props.point.title
  editLaw.value = props.point.law
  editDots.value = props.point.dots
  editChallenges.value = props.point.challenges
  editMeasures.value = props.point.measures
  editResponsibleText.value = props.point.responsibleText
}

function startEditing() {
  if (props.isSaving || props.isDeleting) {
    return
  }

  setFormFromPoint()
  isEditing.value = true
}

function cancelEditing() {
  if (props.isSaving || props.isDeleting) {
    return
  }

  isEditing.value = false
}

function savePoint() {
  if (
    props.isSaving ||
    props.isDeleting ||
    editTitle.value.trim().length === 0 ||
    editLaw.value.trim().length === 0 ||
    editDots.value < 0 ||
    editChallenges.value.trim().length === 0 ||
    editMeasures.value.trim().length === 0 ||
    editResponsibleText.value.trim().length === 0
  ) {
    return
  }

  awaitingSaveResult.value = true

  emit('save', {
    ...props.point,
    title: editTitle.value,
    law: editLaw.value,
    dots: editDots.value,
    challenges: editChallenges.value,
    measures: editMeasures.value,
    responsibleText: editResponsibleText.value,
  })
}

watch(
  () => props.isSaving,
  (next, prev) => {
    if (prev && !next && awaitingSaveResult.value) {
      if (!props.saveError) {
        isEditing.value = false
      }
      awaitingSaveResult.value = false
    }
  },
)

function deletePoint() {
  if (props.isSaving || props.isDeleting) {
    return
  }

  emit('deletePoint', props.point)
}
</script>

<template>
  <div class="mapping-point">
    <div class="point-header">
      <div>
        <template v-if="!isEditing">
          <Badge badge-color="navy">
            {{ point.law }}
          </Badge>
          <Badge badge-color="cherry" :icon="CircleAlert"> {{ point.dots }} Prikker </Badge>
          <h2 class="no-margin">
            {{ point.title }}
          </h2>
        </template>
        <div v-else class="edit-meta-fields">
          <label class="meta-field">
            <span class="navy-subtitle">Tittel</span>
            <input
              v-model="editTitle"
              class="simple-text-input"
              :disabled="isSaving || isDeleting"
              type="text"
            />
          </label>
          <label class="meta-field">
            <span class="navy-subtitle">Lov</span>
            <input
              v-model="editLaw"
              class="simple-text-input law-input"
              :disabled="isSaving || isDeleting"
              type="text"
            />
          </label>
          <label class="meta-field">
            <span class="navy-subtitle">Prikker</span>
            <input
              v-model.number="editDots"
              class="simple-text-input dots-input"
              :disabled="isSaving || isDeleting"
              min="0"
              step="1"
              type="number"
            />
          </label>
        </div>
      </div>
      <DesktopButton v-if="!isEditing" content="Rediger" :icon="Edit2" :on-click="startEditing" />
      <div v-else class="edit-actions">
        <DesktopButton :icon="Save" content="Lagre" :on-click="savePoint" />
        <DesktopButton :icon="X" content="Avbryt" button-color="cherry" :on-click="cancelEditing" />
        <DesktopButton
          :icon="Trash2"
          content="Slett"
          button-color="cherry"
          :on-click="deletePoint"
        />
      </div>
    </div>

    <div>
      <h3 class="navy-subtitle no-margin">Utfordringer</h3>
      <textarea
        v-if="isEditing"
        v-model="editChallenges"
        class="simple-text-input"
        :disabled="isSaving || isDeleting"
        rows="6"
      />
      <span v-else>{{ point.challenges }}</span>
    </div>

    <div>
      <h3 class="navy-subtitle no-margin">Tiltak / Rutiner</h3>
      <textarea
        v-if="isEditing"
        v-model="editMeasures"
        class="simple-text-input"
        :disabled="isSaving || isDeleting"
        rows="7"
      />
      <span v-else>{{ point.measures }}</span>
    </div>

    <div>
      <h3 class="navy-subtitle no-margin">Ansvarlige</h3>
      <textarea
        v-if="isEditing"
        v-model="editResponsibleText"
        class="simple-text-input"
        :disabled="isSaving || isDeleting"
        rows="3"
      />
      <span v-else>{{ point.responsibleText }}</span>
      <Loading v-if="isEditing && (isSaving || isDeleting)" />
      <p v-if="isEditing && saveError" class="error-message">Klarte ikke å oppdatere tiltak.</p>
      <p v-if="isEditing && deleteError" class="error-message">Klarte ikke å slette punkt.</p>
    </div>
  </div>
</template>

<style scoped>
.mapping-point {
  background-color: var(--white-greek);
  border-radius: 1rem;
  border: 1px solid var(--blue-navy-40);
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  overflow-wrap: anywhere;
}

.point-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--blue-navy-40);
}

.point-header > :first-child {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  min-width: 0;
}

.edit-actions {
  display: flex;
  gap: 0.5rem;
}

.edit-meta-fields {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.meta-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  min-width: 10rem;
}

.law-input {
  max-width: 10rem;
}

.dots-input {
  max-width: 8rem;
}

textarea.simple-text-input {
  width: 100%;
  max-width: 100%;
  resize: vertical;
  box-sizing: border-box;
}

.error-message {
  color: #b42318;
  margin: 0;
}

@media (max-width: 768px) {
  .mapping-point {
    padding: 0.75rem;
  }

  .point-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .point-header > :first-child {
    width: 100%;
  }
}
</style>
