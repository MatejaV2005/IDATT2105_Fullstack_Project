<script setup lang="ts">
import AddUsers from '@/components/desktop/shared/AddUsers.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import UserBadge from '@/components/desktop/shared/UserBadge.vue'
import type { PrerequisiteCategory } from '@/interfaces/api-interfaces'
import type { BasicUserWithAccessLevel } from '@/interfaces/util-interfaces'
import { Edit2, Plus, Save, X } from '@lucide/vue'
import { computed, ref } from 'vue'

type IntervalOption = {
  label: string
  seconds: number
}

type UpdateCategoryPayload = {
  categoryId: number
  categoryName: string
}

type UpdateRoutinePayload = {
  categoryId: number
  routineId: number
  title: string
  description: string
  measures: string
  interval_start: number
  interval_repeat_time: number
  performers: number[]
  deputy: number[]
}

type UpdateStandardPayload = {
  categoryId: number
  standardId: number
  title: string
  description: string
}

type CreateRoutinePayload = {
  categoryId: number
  title: string
  description: string
  measures: string
  interval_start: number
  interval_repeat_time: number
  performers: number[]
  deputy: number[]
}

type CreateStandardPayload = {
  categoryId: number
  title: string
  description: string
}

const props = defineProps<{
  category: PrerequisiteCategory
  repeatIntervalOptions: IntervalOption[]
  disableActions: boolean
  onSaveCategory: (payload: UpdateCategoryPayload) => Promise<void>
  onUpdateRoutine: (payload: UpdateRoutinePayload) => Promise<void>
  onUpdateStandard: (payload: UpdateStandardPayload) => Promise<void>
  onCreateRoutine: (payload: CreateRoutinePayload) => Promise<void>
  onCreateStandard: (payload: CreateStandardPayload) => Promise<void>
  availableUsers?: BasicUserWithAccessLevel[]
}>()

const editingCategory = ref(false)
const editCategoryName = ref('')
const savingCategory = ref(false)
const categorySaveError = ref(false)

const editingPointKey = ref<string | null>(null)
const editPointTitle = ref('')
const editPointMeasures = ref('')
const editPointDescription = ref('')
const editPointIntervalStart = ref('')
const editPointIntervalRepeatTimeSeconds = ref(604800)
const editPointPerformerIds = ref<number[]>([])
const editPointDeputyIds = ref<number[]>([])
const savingPointKey = ref<string | null>(null)
const pointSaveErrorKey = ref<string | null>(null)
const routineScheduleState = ref<
  Record<number, { intervalStart: number; intervalRepeatTime: number }>
>({})

const creatingPointType = ref<'routine' | 'standard' | null>(null)
const createPointTitle = ref('')
const createPointMeasures = ref('')
const createPointDescription = ref('')
const createPointIntervalStart = ref('')
const createPointIntervalRepeatTimeSeconds = ref(604800)
const createPointPerformerIds = ref<number[]>([])
const createPointDeputyIds = ref<number[]>([])
const creatingPoint = ref(false)
const createPointError = ref(false)

const isBusy = computed(
  () => props.disableActions || savingCategory.value || savingPointKey.value !== null,
)

function pointKey(point: PrerequisiteCategory['points'][number]) {
  return point.type === 'routine' ? `routine-${point.routineId}` : `standard-${point.standardId}`
}

function isEditingPoint(point: PrerequisiteCategory['points'][number]) {
  return editingPointKey.value === pointKey(point)
}

function toDateTimeLocal(unixSeconds: number): string {
  const date = new Date(unixSeconds * 1000)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')

  return `${year}-${month}-${day}T${hours}:${minutes}`
}

function toUnixSeconds(dateTimeLocal: string): number | null {
  const timestamp = Date.parse(dateTimeLocal)
  if (Number.isNaN(timestamp)) {
    return null
  }

  return Math.floor(timestamp / 1000)
}

function setEditPointPerformers(users: BasicUserWithAccessLevel[]) {
  editPointPerformerIds.value = users.map((user) => user.id)
}

function setEditPointDeputy(users: BasicUserWithAccessLevel[]) {
  editPointDeputyIds.value = users.map((user) => user.id)
}

function setCreatePointPerformers(users: BasicUserWithAccessLevel[]) {
  createPointPerformerIds.value = users.map((user) => user.id)
}

function setCreatePointDeputy(users: BasicUserWithAccessLevel[]) {
  createPointDeputyIds.value = users.map((user) => user.id)
}

function startEditingCategory() {
  if (isBusy.value || creatingPointType.value !== null) {
    return
  }

  categorySaveError.value = false
  editingCategory.value = true
  editCategoryName.value = props.category.categoryName
}

function cancelEditingCategory() {
  if (savingCategory.value) {
    return
  }

  editingCategory.value = false
  editCategoryName.value = ''
  categorySaveError.value = false
}

async function saveCategory() {
  if (
    !editingCategory.value ||
    savingCategory.value ||
    editCategoryName.value.trim().length === 0
  ) {
    return
  }

  savingCategory.value = true
  categorySaveError.value = false

  try {
    await props.onSaveCategory({
      categoryId: props.category.id,
      categoryName: editCategoryName.value.trim(),
    })
    editingCategory.value = false
    editCategoryName.value = ''
  } catch {
    categorySaveError.value = true
  } finally {
    savingCategory.value = false
  }
}

function startEditingPoint(point: PrerequisiteCategory['points'][number]) {
  if (isBusy.value || creatingPointType.value !== null) {
    return
  }

  editingCategory.value = false
  categorySaveError.value = false
  pointSaveErrorKey.value = null
  editingPointKey.value = pointKey(point)
  editPointTitle.value = point.title

  if (point.type === 'routine') {
    const schedule = routineScheduleState.value[point.routineId] ?? {
      intervalStart: point.intervalStart ?? Math.floor(Date.now() / 1000),
      intervalRepeatTime: point.intervalRepeatTime ?? 604800,
    }
    routineScheduleState.value[point.routineId] = schedule
    editPointMeasures.value = point.measures
    editPointDescription.value = point.description
    editPointIntervalStart.value = toDateTimeLocal(schedule.intervalStart)
    editPointIntervalRepeatTimeSeconds.value = schedule.intervalRepeatTime
    editPointPerformerIds.value = point.performers.map((user) => user.userId)
    editPointDeputyIds.value = point.deputy.map((user) => user.userId)
  } else {
    editPointMeasures.value = ''
    editPointDescription.value = point.description
    editPointIntervalStart.value = ''
    editPointIntervalRepeatTimeSeconds.value = 604800
    editPointPerformerIds.value = []
    editPointDeputyIds.value = []
  }
}

function cancelEditingPoint() {
  if (savingPointKey.value !== null) {
    return
  }

  editingPointKey.value = null
  pointSaveErrorKey.value = null
  editPointTitle.value = ''
  editPointMeasures.value = ''
  editPointDescription.value = ''
  editPointIntervalStart.value = ''
  editPointIntervalRepeatTimeSeconds.value = 604800
  editPointPerformerIds.value = []
  editPointDeputyIds.value = []
}

async function savePoint(point: PrerequisiteCategory['points'][number]) {
  const key = pointKey(point)
  if (
    editingPointKey.value !== key ||
    savingPointKey.value !== null ||
    editPointTitle.value.trim().length === 0
  ) {
    return
  }

  savingPointKey.value = key
  pointSaveErrorKey.value = null

  try {
    if (point.type === 'routine') {
      const intervalStart = toUnixSeconds(editPointIntervalStart.value)
      const repeatSeconds = Math.floor(editPointIntervalRepeatTimeSeconds.value)

      if (
        editPointMeasures.value.trim().length === 0 ||
        intervalStart === null ||
        repeatSeconds <= 0 ||
        editPointPerformerIds.value.length === 0 ||
        editPointDeputyIds.value.length === 0
      ) {
        throw new Error('Invalid routine payload')
      }

      routineScheduleState.value[point.routineId] = {
        intervalStart,
        intervalRepeatTime: repeatSeconds,
      }

      await props.onUpdateRoutine({
        categoryId: props.category.id,
        routineId: point.routineId,
        title: editPointTitle.value.trim(),
        description: editPointDescription.value.trim(),
        measures: editPointMeasures.value.trim(),
        interval_start: intervalStart,
        interval_repeat_time: repeatSeconds,
        performers: editPointPerformerIds.value,
        deputy: editPointDeputyIds.value,
      })
    } else {
      if (editPointDescription.value.trim().length === 0) {
        throw new Error('Invalid standard payload')
      }

      await props.onUpdateStandard({
        categoryId: props.category.id,
        standardId: point.standardId,
        title: editPointTitle.value.trim(),
        description: editPointDescription.value.trim(),
      })
    }

    cancelEditingPoint()
  } catch {
    pointSaveErrorKey.value = key
  } finally {
    savingPointKey.value = null
  }
}

function startCreatingPoint(type: 'routine' | 'standard') {
  if (isBusy.value || creatingPoint.value) {
    return
  }

  cancelEditingPoint()
  cancelEditingCategory()

  creatingPointType.value = type
  createPointError.value = false
  createPointTitle.value = ''
  createPointMeasures.value = ''
  createPointDescription.value = ''
  createPointIntervalStart.value = toDateTimeLocal(Math.floor(Date.now() / 1000))
  createPointIntervalRepeatTimeSeconds.value = 604800
  createPointPerformerIds.value = []
  createPointDeputyIds.value = []
}

function cancelCreatingPoint() {
  creatingPointType.value = null
  createPointError.value = false
  createPointTitle.value = ''
  createPointMeasures.value = ''
  createPointDescription.value = ''
  createPointIntervalStart.value = ''
  createPointIntervalRepeatTimeSeconds.value = 604800
  createPointPerformerIds.value = []
  createPointDeputyIds.value = []
}

async function createPoint() {
  if (
    creatingPointType.value === null ||
    creatingPoint.value ||
    createPointTitle.value.trim().length === 0
  ) {
    return
  }

  creatingPoint.value = true
  createPointError.value = false

  try {
    if (creatingPointType.value === 'routine') {
      const intervalStart = toUnixSeconds(createPointIntervalStart.value)
      const repeatSeconds = Math.floor(createPointIntervalRepeatTimeSeconds.value)

      if (
        createPointMeasures.value.trim().length === 0 ||
        createPointDescription.value.trim().length === 0 ||
        intervalStart === null ||
        repeatSeconds <= 0 ||
        createPointPerformerIds.value.length === 0 ||
        createPointDeputyIds.value.length === 0
      ) {
        throw new Error('Invalid routine payload')
      }

      await props.onCreateRoutine({
        categoryId: props.category.id,
        title: createPointTitle.value.trim(),
        description: createPointDescription.value.trim(),
        measures: createPointMeasures.value.trim(),
        interval_start: intervalStart,
        interval_repeat_time: repeatSeconds,
        performers: createPointPerformerIds.value,
        deputy: createPointDeputyIds.value,
      })
    } else {
      if (createPointDescription.value.trim().length === 0) {
        throw new Error('Invalid standard payload')
      }

      await props.onCreateStandard({
        categoryId: props.category.id,
        title: createPointTitle.value.trim(),
        description: createPointDescription.value.trim(),
      })
    }

    cancelCreatingPoint()
  } catch {
    createPointError.value = true
  } finally {
    creatingPoint.value = false
  }
}
</script>

<template>
  <div class="prerequisite-category">
    <div class="prerequisite-category-header">
      <div class="category-edit-wrap">
        <h2 v-if="!editingCategory" class="no-margin">{{ category.categoryName }}</h2>
        <input
          v-else
          v-model="editCategoryName"
          class="simple-text-input category-input"
          :disabled="savingCategory"
          type="text"
          placeholder="Kategorinavn"
        />
      </div>
      <DesktopButton
        v-if="!editingCategory"
        :icon="Edit2"
        content="Rediger"
        :on-click="startEditingCategory"
        :disabled="isBusy || creatingPointType !== null"
      />
      <div v-else class="edit-actions">
        <DesktopButton
          :icon="Save"
          content="Lagre"
          :disabled="editCategoryName.trim().length === 0"
          :on-click="saveCategory"
        />
        <DesktopButton
          :icon="X"
          content="Avbryt"
          button-color="cherry"
          :on-click="cancelEditingCategory"
        />
      </div>
    </div>

    <p v-if="categorySaveError" class="error-message no-margin">
      Klarte ikke å oppdatere kategori.
    </p>

    <div v-if="category.points.length === 0">Oi... Her var det tomt</div>

    <div class="point-container">
      <div v-for="point in category.points" :key="pointKey(point)" class="point">
        <div class="point-header">
          <div>
            <div v-if="point.type === 'routine'" class="point-dot-routine" />
            <div v-if="point.type === 'standard'" class="point-dot-standard" />
            <h3 v-if="!isEditingPoint(point)" class="no-margin">{{ point.title }}</h3>
            <input
              v-else
              v-model="editPointTitle"
              class="simple-text-input point-title-input"
              :disabled="savingPointKey === pointKey(point)"
              type="text"
              placeholder="Tittel"
            />
          </div>

          <div>
            <span v-if="point.type === 'routine' && !isEditingPoint(point)">{{
              point.repeatText
            }}</span>
            <div
              v-if="point.type === 'routine' && isEditingPoint(point)"
              class="interval-edit-fields"
            >
              <label class="interval-field">
                <span class="navy-subtitle">Intervallstart</span>
                <input
                  v-model="editPointIntervalStart"
                  class="simple-text-input repeat-input"
                  :disabled="savingPointKey === pointKey(point)"
                  type="datetime-local"
                />
              </label>
              <label class="interval-field">
                <span class="navy-subtitle">Repetering</span>
                <select
                  v-model.number="editPointIntervalRepeatTimeSeconds"
                  class="simple-text-input repeat-input"
                  :disabled="savingPointKey === pointKey(point)"
                >
                  <option
                    v-for="intervalOption in repeatIntervalOptions"
                    :key="`repeat-${intervalOption.seconds}`"
                    :value="intervalOption.seconds"
                  >
                    {{ intervalOption.label }}
                  </option>
                </select>
              </label>
            </div>

            <DesktopButton
              v-if="!isEditingPoint(point)"
              :icon="Edit2"
              content="Rediger"
              :on-click="() => startEditingPoint(point)"
              :disabled="isBusy || creatingPointType !== null"
            />
            <div v-else class="edit-actions">
              <DesktopButton :icon="Save" content="Lagre" :on-click="() => savePoint(point)" />
              <DesktopButton
                :icon="X"
                content="Avbryt"
                button-color="cherry"
                :on-click="cancelEditingPoint"
              />
            </div>
          </div>
        </div>

        <template v-if="point.type === 'routine'">
          <span v-if="!isEditingPoint(point)">Avvikstiltak: {{ point.measures }}</span>
          <div v-else>
            <h4 class="navy-subtitle no-margin">Beskrivelse</h4>
            <textarea
              v-model="editPointDescription"
              class="simple-text-input point-textarea"
              :disabled="savingPointKey === pointKey(point)"
              rows="3"
              placeholder="Beskriv rutinen"
            />
            <h4 class="navy-subtitle no-margin">Avvikstiltak</h4>
            <textarea
              v-model="editPointMeasures"
              class="simple-text-input point-textarea"
              :disabled="savingPointKey === pointKey(point)"
              rows="4"
              placeholder="Beskriv avvikstiltak"
            />
          </div>

          <div>
            <span class="navy-subtitle">Vikarleder</span>
            <div v-if="!isEditingPoint(point)" class="user-parent">
              <UserBadge
                v-for="deputy in point.deputy"
                :key="deputy.userId"
                :name="deputy.legalName"
                :user-id="deputy.userId"
              />
            </div>
            <AddUsers
              v-else
              :set-users="setEditPointDeputy"
              :initial-user-ids="editPointDeputyIds"
              :users="availableUsers"
            />
          </div>

          <div>
            <span class="navy-subtitle">De som skal gjore det</span>
            <div v-if="!isEditingPoint(point)" class="user-parent">
              <UserBadge
                v-for="performer in point.performers"
                :key="performer.userId"
                :name="performer.legalName"
                :user-id="performer.userId"
              />
            </div>
            <AddUsers
              v-else
              :set-users="setEditPointPerformers"
              :initial-user-ids="editPointPerformerIds"
              :users="availableUsers"
            />
          </div>
        </template>

        <template v-else>
          <span v-if="!isEditingPoint(point)">{{ point.description }}</span>
          <div v-else>
            <h4 class="navy-subtitle no-margin">Beskrivelse</h4>
            <textarea
              v-model="editPointDescription"
              class="simple-text-input point-textarea"
              :disabled="savingPointKey === pointKey(point)"
              rows="4"
              placeholder="Beskriv standarden"
            />
          </div>
        </template>

        <Loading v-if="savingPointKey === pointKey(point)" />
        <p v-if="pointSaveErrorKey === pointKey(point)" class="error-message no-margin">
          Klarte ikke a oppdatere punkt.
        </p>
      </div>
    </div>

    <div class="add-point-container">
      <DesktopButton
        :icon="Plus"
        content="Legg til rutine"
        :on-click="() => startCreatingPoint('routine')"
        button-color="blue-decor"
        :disabled="isBusy || (creatingPointType !== null && creatingPointType !== 'routine')"
      />
      <DesktopButton
        :icon="Plus"
        content="Legg til standard"
        button-color="cherry"
        :on-click="() => startCreatingPoint('standard')"
        :disabled="isBusy || (creatingPointType !== null && creatingPointType !== 'standard')"
      />
    </div>

    <div v-if="creatingPointType !== null" class="create-point-card">
      <h3 class="no-margin">Ny {{ creatingPointType === 'routine' ? 'rutine' : 'standard' }}</h3>
      <label class="create-field">
        <span class="navy-subtitle">Tittel</span>
        <input
          v-model="createPointTitle"
          class="simple-text-input category-input"
          :disabled="creatingPoint"
          type="text"
          placeholder="F.eks. Vask av benker"
        />
      </label>

      <template v-if="creatingPointType === 'routine'">
        <div class="interval-edit-fields">
          <label class="interval-field">
            <span class="navy-subtitle">Intervallstart</span>
            <input
              v-model="createPointIntervalStart"
              class="simple-text-input repeat-input"
              :disabled="creatingPoint"
              type="datetime-local"
            />
          </label>
          <label class="interval-field">
            <span class="navy-subtitle">Repetering</span>
            <select
              v-model.number="createPointIntervalRepeatTimeSeconds"
              class="simple-text-input repeat-input"
              :disabled="creatingPoint"
            >
              <option
                v-for="intervalOption in repeatIntervalOptions"
                :key="`create-repeat-${intervalOption.seconds}`"
                :value="intervalOption.seconds"
              >
                {{ intervalOption.label }}
              </option>
            </select>
          </label>
        </div>

        <label class="create-field">
          <span class="navy-subtitle">Beskrivelse</span>
          <textarea
            v-model="createPointDescription"
            class="simple-text-input point-textarea"
            :disabled="creatingPoint"
            rows="3"
            placeholder="Beskriv rutinen"
          />
        </label>

        <label class="create-field">
          <span class="navy-subtitle">Avvikstiltak</span>
          <textarea
            v-model="createPointMeasures"
            class="simple-text-input point-textarea"
            :disabled="creatingPoint"
            rows="4"
            placeholder="Hva skal gjores ved avvik?"
          />
        </label>

        <div>
          <span class="navy-subtitle">Vikarleder</span>
          <AddUsers
            :set-users="setCreatePointDeputy"
            :initial-user-ids="createPointDeputyIds"
            :users="availableUsers"
          />
        </div>

        <div>
          <span class="navy-subtitle">De som skal gjore det</span>
          <AddUsers
            :set-users="setCreatePointPerformers"
            :initial-user-ids="createPointPerformerIds"
            :users="availableUsers"
          />
        </div>
      </template>

      <label v-else class="create-field">
        <span class="navy-subtitle">Beskrivelse</span>
        <textarea
          v-model="createPointDescription"
          class="simple-text-input point-textarea"
          :disabled="creatingPoint"
          rows="4"
          placeholder="Beskrivelse av standard"
        />
      </label>

      <div class="edit-actions">
        <DesktopButton
          :icon="Save"
          content="Opprett"
          :on-click="createPoint"
          :is-loading="creatingPoint"
          loading-text="Oppretter"
        />
        <DesktopButton
          :icon="X"
          content="Avbryt"
          button-color="cherry"
          :on-click="cancelCreatingPoint"
          :disabled="creatingPoint"
        />
      </div>

      <p v-if="createPointError" class="error-message no-margin">Klarte ikke a opprette punkt.</p>
    </div>
  </div>
</template>

<style scoped>
.prerequisite-category {
  border-radius: 1rem;
  padding: 1rem;
  display: flex;
  gap: 1rem;
  flex-direction: column;
  border: 1px solid var(--blue-decor);
  background-color: var(--blue-decor-10);
}

.prerequisite-category-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
}

.category-edit-wrap {
  min-width: 0;
  flex: 1;
}

.category-input {
  width: 100%;
  max-width: 28rem;
  min-height: 2rem;
}

.point-container {
  display: flex;
  gap: 1rem;
  flex-direction: column;
}

.point {
  padding: 1rem;
  border-radius: 0.5rem;
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
}

.point-title-input {
  min-height: 2rem;
  min-width: 14rem;
}

.repeat-input {
  min-height: 2rem;
  min-width: 12rem;
}

.interval-edit-fields {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.interval-field,
.create-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.point-textarea {
  width: 100%;
  max-width: 100%;
  resize: vertical;
  box-sizing: border-box;
}

.edit-actions {
  display: flex;
  gap: 0.5rem;
}

.error-message {
  color: #b42318;
}

.point-header {
  display: flex;
  gap: 0.5rem;
  justify-content: space-between;
}

.point-header > div {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
}

.point-dot-routine,
.point-dot-standard {
  width: 0.5rem;
  height: 0.5rem;
}

.point-dot-routine {
  background-color: var(--blue-decor);
}

.point-dot-standard {
  background-color: var(--red-cherry);
}

.user-parent {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.add-point-container {
  display: flex;
  width: 100%;
  gap: 1rem;
}

.add-point-container > button {
  width: 100%;
}

.create-point-card {
  border-radius: 0.75rem;
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

@media (max-width: 768px) {
  .prerequisite-category-header,
  .point-header,
  .point-header > div {
    flex-direction: column;
    align-items: flex-start;
  }

  .edit-actions {
    width: 100%;
    flex-wrap: wrap;
  }
}
</style>
