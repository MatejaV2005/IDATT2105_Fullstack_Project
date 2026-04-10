<script setup lang="ts">
import CorrectiveMeasures from '@/components/desktop/criticalpoints/CorrectiveMeasures.vue'
import AddUsers from '@/components/desktop/shared/AddUsers.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import type { DesktopProductCategory, NewCriticalControlPoint } from '@/interfaces/api-interfaces'
import type { BasicUserWithAccessLevel } from '@/interfaces/util-interfaces'
import { Save, X } from '@lucide/vue'
import { computed, ref, watch } from 'vue'

type FormMode = 'create' | 'edit'

type InitialRoleUsers = {
  verifiers: BasicUserWithAccessLevel[]
  deviationRecievers: BasicUserWithAccessLevel[]
  performers: BasicUserWithAccessLevel[]
  deputy: BasicUserWithAccessLevel[]
}

const props = withDefaults(
  defineProps<{
    mode?: FormMode
    isSubmitting: boolean
    submitError: boolean
    submitErrorText?: string
    initialPayload?: NewCriticalControlPoint | null
    initialRoleUsers?: InitialRoleUsers | null
    availableUsers?: BasicUserWithAccessLevel[]
    productCategories?: DesktopProductCategory[]
  }>(),
  {
    mode: 'create',
    initialPayload: null,
    initialRoleUsers: null,
    submitErrorText: '',
    availableUsers: () => [],
    productCategories: () => [],
  },
)

const emit = defineEmits<{
  submit: [payload: NewCriticalControlPoint]
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
const intervalStart = ref<number | null>(null)
const intervalRepeatTime = ref<number | null>(null)
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

const isEditMode = computed(() => props.mode === 'edit')
const headerTitle = computed(() =>
  isEditMode.value ? 'Rediger kritisk kontrollpunkt' : 'Nytt kritisk kontrollpunkt',
)
const submitButtonText = computed(() => (isEditMode.value ? 'Oppdater' : 'Lagre'))
const submitLoadingText = computed(() => (isEditMode.value ? 'Oppdaterer' : 'Lagrer'))
const submitErrorText = computed(() => {
  if (props.submitErrorText.trim().length > 0) {
    return props.submitErrorText
  }

  return isEditMode.value
    ? 'Klarte ikke å oppdatere kritisk kontrollpunkt.'
    : 'Klarte ikke å opprette kritisk kontrollpunkt.'
})

function applyInitialPayload(payload: NewCriticalControlPoint | null) {
  if (!payload) {
    name.value = ''
    how.value = ''
    equipment.value = ''
    instructionsAndCalibration.value = ''
    immediateCorrectiveAction.value = ''
    criticalMin.value = 0
    criticalMax.value = 0
    unit.value = 'C'
    monitoredDescription.value = ''
    intervalStart.value = null
    intervalRepeatTime.value = null
    correctiveMeasures.value = []
    verifiers.value = []
    deviationRecievers.value = []
    performers.value = []
    deputy.value = []
    return
  }

  name.value = payload.name
  how.value = payload.how
  equipment.value = payload.equipment
  instructionsAndCalibration.value = payload.instructionsAndCalibration
  immediateCorrectiveAction.value = payload.immediateCorrectiveAction
  criticalMin.value = payload.criticalMin
  criticalMax.value = payload.criticalMax
  unit.value = payload.unit
  monitoredDescription.value = payload.monitoredDescription
  intervalStart.value = payload.intervalStart ?? null
  intervalRepeatTime.value = payload.intervalRepeatTime ?? null
  correctiveMeasures.value = payload.ccpCorrectiveMeasure.map((measure) => ({ ...measure }))

  if (props.initialRoleUsers) {
    verifiers.value = props.initialRoleUsers.verifiers.map((user) => ({ ...user }))
    deviationRecievers.value = props.initialRoleUsers.deviationRecievers.map((user) => ({
      ...user,
    }))
    performers.value = props.initialRoleUsers.performers.map((user) => ({ ...user }))
    deputy.value = props.initialRoleUsers.deputy.map((user) => ({ ...user }))
    return
  }

  verifiers.value = payload.verifiers.map((id) => ({
    id,
    email: '',
    legalName: '',
    accessLevel: 'WORKER',
  }))
  deviationRecievers.value = payload.deviationRecievers.map((id) => ({
    id,
    email: '',
    legalName: '',
    accessLevel: 'WORKER',
  }))
  performers.value = payload.performers.map((id) => ({
    id,
    email: '',
    legalName: '',
    accessLevel: 'WORKER',
  }))
  deputy.value = payload.deputy.map((id) => ({
    id,
    email: '',
    legalName: '',
    accessLevel: 'WORKER',
  }))
}

watch(
  () => [props.initialPayload, props.initialRoleUsers, props.mode],
  () => {
    applyInitialPayload(props.mode === 'edit' ? props.initialPayload : null)
  },
  { immediate: true, deep: true },
)

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

function submitCcp() {
  if (props.isSubmitting || !canSaveNewCcp.value) {
    return
  }

  emit('submit', {
    name: name.value.trim(),
    how: how.value.trim(),
    equipment: equipment.value.trim(),
    instructionsAndCalibration: instructionsAndCalibration.value.trim(),
    immediateCorrectiveAction: immediateCorrectiveAction.value.trim(),
    criticalMin: criticalMin.value,
    criticalMax: criticalMax.value,
    unit: unit.value.trim(),
    monitoredDescription: monitoredDescription.value.trim(),
    intervalStart: intervalStart.value,
    intervalRepeatTime: intervalRepeatTime.value,
    verifiers: verifiers.value.map((user) => user.id),
    deviationRecievers: deviationRecievers.value.map((user) => user.id),
    performers: performers.value.map((user) => user.id),
    deputy: deputy.value.map((user) => user.id),
    ccpCorrectiveMeasure: correctiveMeasures.value,
  })
}

function cancel() {
  if (props.isSubmitting) {
    return
  }

  emit('cancel')
}
</script>

<template>
  <div class="create-ccp">
    <div class="ccp-header">
      <h2 class="no-margin">{{ headerTitle }}</h2>
      <div class="create-actions">
        <DesktopButton
          :icon="Save"
          :is-loading="isSubmitting"
          :loading-text="submitLoadingText"
          :content="submitButtonText"
          :on-click="submitCcp"
          :disabled="!canSaveNewCcp || isSubmitting"
        />
        <DesktopButton
          :icon="X"
          content="Avbryt"
          button-color="cherry"
          :on-click="cancel"
          :disabled="isSubmitting"
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
          :disabled="isSubmitting"
          placeholder="F.eks. kjøledisk fisk"
        />
      </label>
      <label class="form-field">
        <span class="navy-subtitle">Enhet</span>
        <input
          v-model="unit"
          class="simple-text-input"
          type="text"
          :disabled="isSubmitting"
          placeholder="F.eks. C"
        />
      </label>
      <label class="form-field">
        <span class="navy-subtitle">Kritisk min</span>
        <input
          v-model.number="criticalMin"
          class="simple-text-input"
          type="number"
          :disabled="isSubmitting"
          placeholder="F.eks. 0"
        />
      </label>
      <label class="form-field">
        <span class="navy-subtitle">Kritisk max</span>
        <input
          v-model.number="criticalMax"
          class="simple-text-input"
          type="number"
          :disabled="isSubmitting"
          placeholder="F.eks. 4"
        />
      </label>
    </div>

    <label class="form-field">
      <span class="navy-subtitle">Hvordan overvåkes det</span>
      <textarea
        v-model="how"
        class="simple-text-input"
        rows="3"
        :disabled="isSubmitting"
        placeholder="Beskriv hvordan dette punktet overvåkes i praksis"
      />
    </label>

    <label class="form-field">
      <span class="navy-subtitle">Utstyr</span>
      <textarea
        v-model="equipment"
        class="simple-text-input"
        rows="3"
        :disabled="isSubmitting"
        placeholder="F.eks. kalibrert termometer og loggskjema"
      />
    </label>

    <label class="form-field">
      <span class="navy-subtitle">Instruks og kalibrering</span>
      <textarea
        v-model="instructionsAndCalibration"
        class="simple-text-input"
        rows="3"
        :disabled="isSubmitting"
        placeholder="Beskriv instruks for bruk og kalibrering"
      />
    </label>

    <label class="form-field">
      <span class="navy-subtitle">Umiddelbart avvikstiltak</span>
      <textarea
        v-model="immediateCorrectiveAction"
        class="simple-text-input"
        rows="3"
        :disabled="isSubmitting"
        placeholder="Hva skal gjøres med en gang ved avvik"
      />
    </label>

    <label class="form-field">
      <span class="navy-subtitle">Hva overvåkes</span>
      <textarea
        v-model="monitoredDescription"
        class="simple-text-input"
        rows="2"
        :disabled="isSubmitting"
        placeholder="F.eks. kjølerom for fisk, kjøtt og meieri"
      />
    </label>

    <div class="people-grid">
      <label class="form-field">
        <span class="navy-subtitle">Godkjennere</span>
        <AddUsers
          :users="availableUsers"
          :set-users="setVerifiers"
          :initial-user-ids="verifiers.map((user) => user.id)"
          :initial-users="verifiers"
          local-only
        />
      </label>
      <label class="form-field">
        <span class="navy-subtitle">Avviksmottakere</span>
        <AddUsers
          :users="availableUsers"
          :set-users="setDeviationRecievers"
          :initial-user-ids="deviationRecievers.map((user) => user.id)"
          :initial-users="deviationRecievers"
          local-only
        />
      </label>
      <label class="form-field">
        <span class="navy-subtitle">Utførere</span>
        <AddUsers
          :users="availableUsers"
          :set-users="setPerformers"
          :initial-user-ids="performers.map((user) => user.id)"
          :initial-users="performers"
          local-only
        />
      </label>
      <label class="form-field">
        <span class="navy-subtitle">Vikarleder</span>
        <AddUsers
          :users="availableUsers"
          :set-users="setDeputy"
          :initial-user-ids="deputy.map((user) => user.id)"
          :initial-users="deputy"
          local-only
        />
      </label>
    </div>

    <CorrectiveMeasures
      :categories="productCategories"
      :measures="correctiveMeasures"
      :set-measures="setCorrectiveMeasures"
      :disabled="isSubmitting"
    />

    <p v-if="criticalMin > criticalMax" class="error-message">
      Kritisk min må være mindre enn eller lik kritisk max.
    </p>
    <p v-if="submitError" class="error-message">
      {{ submitErrorText }}
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
