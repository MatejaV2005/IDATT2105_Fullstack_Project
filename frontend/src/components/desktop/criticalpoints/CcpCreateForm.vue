<script setup lang="ts">
import CorrectiveMeasures from '@/components/desktop/criticalpoints/CorrectiveMeasures.vue'
import AddUsers from '@/components/desktop/shared/AddUsers.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import type { NewCriticalControlPoint } from '@/interfaces/api-interfaces'
import type { BasicUserWithAccessLevel } from '@/interfaces/util-interfaces'
import { Save, X } from '@lucide/vue'
import { computed, ref } from 'vue'

const props = defineProps<{
  isCreatingCcp: boolean
  createError: boolean
}>()

const emit = defineEmits<{
  create: [payload: NewCriticalControlPoint]
  cancel: []
}>()

const name = ref('')
const how = ref('')
const equipment = ref('')
const instructionsAndCalibration = ref('')
const immediateCorrectiveAction = ref('')
const criticalMin = ref(0)
const criticalMax = ref(0)
const unit = ref('C')
const monitoredDescription = ref('')
const correctiveMeasures = ref<NewCriticalControlPoint['ccpCorrectiveMeasure']>([])
const verifiers = ref<BasicUserWithAccessLevel[]>([])
const deviationRecievers = ref<BasicUserWithAccessLevel[]>([])
const performers = ref<BasicUserWithAccessLevel[]>([])
const deputy = ref<BasicUserWithAccessLevel[]>([])

const canSaveNewCcp = computed(() => {
  return (
    name.value.trim().length > 0 &&
    how.value.trim().length > 0 &&
    equipment.value.trim().length > 0 &&
    instructionsAndCalibration.value.trim().length > 0 &&
    immediateCorrectiveAction.value.trim().length > 0 &&
    unit.value.trim().length > 0 &&
    monitoredDescription.value.trim().length > 0 &&
    verifiers.value.length > 0 &&
    deviationRecievers.value.length > 0 &&
    performers.value.length > 0 &&
    deputy.value.length > 0 &&
    Number.isFinite(criticalMin.value) &&
    Number.isFinite(criticalMax.value) &&
    correctiveMeasures.value.length > 0 &&
    criticalMin.value <= criticalMax.value
  )
})

function setCorrectiveMeasures(measures: NewCriticalControlPoint['ccpCorrectiveMeasure']) {
  correctiveMeasures.value = measures
}

function setVerifiers(users: BasicUserWithAccessLevel[]) {
  verifiers.value = users
}

function setDeviationRecievers(users: BasicUserWithAccessLevel[]) {
  deviationRecievers.value = users
}

function setPerformers(users: BasicUserWithAccessLevel[]) {
  performers.value = users
}

function setDeputy(users: BasicUserWithAccessLevel[]) {
  deputy.value = users
}

function createCcp() {
  if (props.isCreatingCcp || !canSaveNewCcp.value) {
    return
  }

  emit('create', {
    name: name.value.trim(),
    how: how.value.trim(),
    equipment: equipment.value.trim(),
    instructionsAndCalibration: instructionsAndCalibration.value.trim(),
    immediateCorrectiveAction: immediateCorrectiveAction.value.trim(),
    criticalMin: criticalMin.value,
    criticalMax: criticalMax.value,
    unit: unit.value.trim(),
    monitoredDescription: monitoredDescription.value.trim(),
    verifiers: verifiers.value.map((user) => user.id),
    deviationRecievers: deviationRecievers.value.map((user) => user.id),
    performers: performers.value.map((user) => user.id),
    deputy: deputy.value.map((user) => user.id),
    ccpCorrectiveMeasure: correctiveMeasures.value,
  })
}

function cancel() {
  if (props.isCreatingCcp) {
    return
  }

  emit('cancel')
}
</script>

<template>
  <div class="create-ccp">
    <div class="ccp-header">
      <h2 class="no-margin">
        Nytt kritisk kontrollpunkt
      </h2>
      <div class="create-actions">
        <DesktopButton
          :icon="Save"
          :is-loading="isCreatingCcp"
          loading-text="Lagrer"
          content="Lagre"
          :on-click="createCcp"
          :disabled="!canSaveNewCcp || isCreatingCcp"
        />
        <DesktopButton
          :icon="X"
          content="Avbryt"
          button-color="cherry"
          :on-click="cancel"
          :disabled="isCreatingCcp"
        />
      </div>
    </div>

    <div class="ccp-grid">
      <label class="form-field">
        <span class="navy-subtitle">Navn</span>
        <input
          v-model="name"
          class="simple-text-input"
          type="text"
          :disabled="isCreatingCcp"
          placeholder="F.eks. kjøledisk fisk"
        >
      </label>
      <label class="form-field">
        <span class="navy-subtitle">Enhet</span>
        <input
          v-model="unit"
          class="simple-text-input"
          type="text"
          :disabled="isCreatingCcp"
          placeholder="F.eks. C"
        >
      </label>
      <label class="form-field">
        <span class="navy-subtitle">Kritisk min</span>
        <input
          v-model.number="criticalMin"
          class="simple-text-input"
          type="number"
          :disabled="isCreatingCcp"
          placeholder="F.eks. 0"
        >
      </label>
      <label class="form-field">
        <span class="navy-subtitle">Kritisk max</span>
        <input
          v-model.number="criticalMax"
          class="simple-text-input"
          type="number"
          :disabled="isCreatingCcp"
          placeholder="F.eks. 4"
        >
      </label>
    </div>

    <label class="form-field">
      <span class="navy-subtitle">Hvordan overvåkes det</span>
      <textarea
        v-model="how"
        class="simple-text-input"
        rows="3"
        :disabled="isCreatingCcp"
        placeholder="Beskriv hvordan dette punktet overvåkes i praksis"
      />
    </label>

    <label class="form-field">
      <span class="navy-subtitle">Utstyr</span>
      <textarea
        v-model="equipment"
        class="simple-text-input"
        rows="3"
        :disabled="isCreatingCcp"
        placeholder="F.eks. kalibrert termometer og loggskjema"
      />
    </label>

    <label class="form-field">
      <span class="navy-subtitle">Instruks og kalibrering</span>
      <textarea
        v-model="instructionsAndCalibration"
        class="simple-text-input"
        rows="3"
        :disabled="isCreatingCcp"
        placeholder="Beskriv instruks for bruk og kalibrering"
      />
    </label>

    <label class="form-field">
      <span class="navy-subtitle">Umiddelbart avvikstiltak</span>
      <textarea
        v-model="immediateCorrectiveAction"
        class="simple-text-input"
        rows="3"
        :disabled="isCreatingCcp"
        placeholder="Hva skal gjøres med en gang ved avvik"
      />
    </label>

    <label class="form-field">
      <span class="navy-subtitle">Hva overvåkes</span>
      <textarea
        v-model="monitoredDescription"
        class="simple-text-input"
        rows="2"
        :disabled="isCreatingCcp"
        placeholder="F.eks. kjølerom for fisk, kjøtt og meieri"
      />
    </label>

    <div class="people-grid">
      <label class="form-field">
        <span class="navy-subtitle">Godkjennere</span>
        <AddUsers :set-users="setVerifiers" />
      </label>
      <label class="form-field">
        <span class="navy-subtitle">Avviksmottakere</span>
        <AddUsers :set-users="setDeviationRecievers" />
      </label>
      <label class="form-field">
        <span class="navy-subtitle">Utførere</span>
        <AddUsers :set-users="setPerformers" />
      </label>
      <label class="form-field">
        <span class="navy-subtitle">Vikarleder</span>
        <AddUsers :set-users="setDeputy" />
      </label>
    </div>

    <CorrectiveMeasures
      :measures="correctiveMeasures"
      :set-measures="setCorrectiveMeasures"
      :disabled="isCreatingCcp"
    />

    <p
      v-if="criticalMin > criticalMax"
      class="error-message"
    >
      Kritisk min må være mindre enn eller lik kritisk max.
    </p>
    <p
      v-if="createError"
      class="error-message"
    >
      Klarte ikke å opprette kritisk kontrollpunkt.
    </p>
  </div>
</template>

<style scoped>
.create-ccp {
  border-radius: 1rem;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
}

.ccp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.create-actions {
  display: flex;
  gap: 0.5rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.ccp-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.people-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-field textarea.simple-text-input {
  width: 100%;
  resize: vertical;
  min-height: 3rem;
  padding: 0.5rem;
  box-sizing: border-box;
  text-indent: 0;
}

.error-message {
  color: #b42318;
  margin: 0;
}

@media (max-width: 1200px) {
  .ccp-grid,
  .people-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .ccp-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .create-actions {
    width: 100%;
    flex-direction: column;
  }
}
</style>
