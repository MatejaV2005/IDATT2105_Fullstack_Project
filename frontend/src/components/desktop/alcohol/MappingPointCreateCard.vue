<script setup lang="ts">
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import { Save, X } from '@lucide/vue'
import { ref } from 'vue'

const props = defineProps<{
  isCreating: boolean
  createError: boolean
}>()

const emit = defineEmits<{
  create: [
    payload: {
      title: string
      law: string
      dots: number
      challenges: string
      measures: string
      responsibleText: string
    },
  ]
  cancel: []
}>()

const title = ref('')
const law = ref('AL § x-y')
const dots = ref(1)
const challenges = ref('')
const measures = ref('')
const responsibleText = ref('')

function createPoint() {
  if (
    props.isCreating ||
    title.value.trim().length === 0 ||
    law.value.trim().length === 0 ||
    dots.value < 0 ||
    challenges.value.trim().length === 0 ||
    measures.value.trim().length === 0 ||
    responsibleText.value.trim().length === 0
  ) {
    return
  }

  emit('create', {
    title: title.value,
    law: law.value,
    dots: dots.value,
    challenges: challenges.value,
    measures: measures.value,
    responsibleText: responsibleText.value,
  })
}

function cancelCreate() {
  if (props.isCreating) {
    return
  }

  title.value = ''
  law.value = 'AL § 1-5'
  dots.value = 0
  challenges.value = ''
  measures.value = ''
  responsibleText.value = ''
  emit('cancel')
}
</script>

<template>
  <div class="mapping-point">
    <div class="point-header">
      <div class="edit-meta-fields">
        <label class="meta-field">
          <span class="navy-subtitle">Tittel</span>
          <input
            v-model="title"
            class="simple-text-input"
            :disabled="isCreating"
            placeholder="F.eks. Salg av alkohol til mindrearige"
            type="text"
          />
        </label>
        <label class="meta-field">
          <span class="navy-subtitle">Lov</span>
          <input
            v-model="law"
            class="simple-text-input law-input"
            :disabled="isCreating"
            placeholder="F.eks. AL § 1-5"
            type="text"
          />
        </label>
        <label class="meta-field">
          <span class="navy-subtitle">Prikker</span>
          <input
            v-model.number="dots"
            class="simple-text-input dots-input"
            :disabled="isCreating"
            min="0"
            step="1"
            placeholder="0"
            type="number"
          />
        </label>
      </div>
      <div class="edit-actions">
        <DesktopButton :icon="Save" content="Lagre" :on-click="createPoint" />
        <DesktopButton :icon="X" content="Avbryt" button-color="cherry" :on-click="cancelCreate" />
      </div>
    </div>

    <div>
      <h3 class="navy-subtitle no-margin">Utfordringer</h3>
      <textarea
        v-model="challenges"
        class="simple-text-input"
        :disabled="isCreating"
        rows="6"
        placeholder="Beskriv utfordringene knyttet til dette punktet"
      />
    </div>

    <div>
      <h3 class="navy-subtitle no-margin">Tiltak / Rutiner</h3>
      <textarea
        v-model="measures"
        class="simple-text-input"
        :disabled="isCreating"
        rows="7"
        placeholder="Beskriv tiltak og rutiner som skal folges"
      />
    </div>

    <div>
      <h3 class="navy-subtitle no-margin">Ansvarlige</h3>
      <textarea
        v-model="responsibleText"
        class="simple-text-input"
        :disabled="isCreating"
        rows="3"
        placeholder="Beskriv hvem som er ansvarlige"
      />
      <Loading v-if="isCreating" />
      <p v-if="createError" class="error-message">Klarte ikke å opprette punkt.</p>
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

  .edit-meta-fields {
    width: 100%;
  }
}
</style>
