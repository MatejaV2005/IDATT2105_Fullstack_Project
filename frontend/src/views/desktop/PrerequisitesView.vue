<script setup lang="ts">
import api from '@/api/api'
import PrerequisiteCategoryCard from '@/components/desktop/prerequisites/PrerequisiteCategoryCard.vue'
import PrerequisiteCreateCategoryCard from '@/components/desktop/prerequisites/PrerequisiteCreateCategoryCard.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import Paginator from '@/components/desktop/shared/Paginator.vue'
import { useOrgSession } from '@/composables/useOrgSession'
import type { PrerequisiteCategoryAllInfo } from '@/interfaces/api-interfaces'
import type { BasicUserWithAccessLevel } from '@/interfaces/util-interfaces'
import { Plus } from '@lucide/vue'
import { ref, watch } from 'vue'

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

type CreateCategoryPayload = {
  categoryName: string
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

type ApiPrerequisiteUser = {
  userId: number
  legalName: string
}

type ApiPrerequisiteInterval = {
  intervalId: number
  intervalStart: number
  intervalRepeatTime: number
} | null

type ApiRoutinePoint = {
  id: number
  title: string
  type: 'routine'
  description: string
  measures: string
  repeatText: string
  interval: ApiPrerequisiteInterval
  verifiers: ApiPrerequisiteUser[]
  deviationReceivers: ApiPrerequisiteUser[]
  performers: ApiPrerequisiteUser[]
  deputy: ApiPrerequisiteUser[]
}

type ApiStandardPoint = {
  id: number
  title: string
  type: 'standard'
  description: string
}

type ApiPrerequisitePoint = ApiRoutinePoint | ApiStandardPoint

type ApiPrerequisiteCategory = {
  id: number
  categoryName: string
  points: ApiPrerequisitePoint[]
}

const repeatIntervalOptions = [
  { label: 'Hver dag', seconds: 86400 },
  { label: 'Hver 7. dag (1 uke)', seconds: 604800 },
  { label: 'Hver 14. dag (2 uker)', seconds: 1209600 },
  { label: 'Hver 30. dag', seconds: 2592000 },
]

const { claims } = useOrgSession()

const resource = ref<PrerequisiteCategoryAllInfo>([])
const availableUsers = ref<BasicUserWithAccessLevel[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

const isCreatingCategory = ref(false)
const newCategoryName = ref('')
const isCreatingCategoryRequest = ref(false)
const createCategoryError = ref(false)

let activeFetchId = 0

function sortUsers(users: BasicUserWithAccessLevel[]) {
  return [...users].sort((left, right) => left.legalName.localeCompare(right.legalName, 'nb-NO'))
}

function mapPrerequisiteUser(user: ApiPrerequisiteUser) {
  return {
    userId: user.userId,
    legalName: user.legalName,
  }
}

function mapPrerequisiteCategory(category: ApiPrerequisiteCategory): PrerequisiteCategoryAllInfo[number] {
  return {
    id: category.id,
    categoryName: category.categoryName,
    points: category.points.map((point) => {
      if (point.type === 'routine') {
        return {
          type: 'routine' as const,
          routineId: point.id,
          title: point.title,
          description: point.description,
          measures: point.measures,
          repeatText: point.repeatText,
          verifiers: point.verifiers.map(mapPrerequisiteUser),
          deviationRecievers: point.deviationReceivers.map(mapPrerequisiteUser),
          performers: point.performers.map(mapPrerequisiteUser),
          deputy: point.deputy.map(mapPrerequisiteUser),
          intervalStart: point.interval?.intervalStart ?? null,
          intervalRepeatTime: point.interval?.intervalRepeatTime ?? null,
        }
      }

      return {
        type: 'standard' as const,
        standardId: point.id,
        title: point.title,
        description: point.description,
        resources: [],
      }
    }),
  }
}

function getErrorMessage(err: unknown, fallback: string) {
  const status = (err as { response?: { status?: number } }).response?.status

  if (status === 403) {
    return 'Du har ikke tilgang til grunnforutsetninger. Kun owner og manager kan endre denne siden.'
  }

  return fallback
}

async function fetchPrerequisites() {
  const fetchId = ++activeFetchId
  loading.value = true
  error.value = null

  try {
    const [prerequisiteResponse, usersResponse] = await Promise.all([
      api.get<ApiPrerequisiteCategory[]>('/prerequisite-categories/get-all-info'),
      api.get<BasicUserWithAccessLevel[]>('/organizations/users'),
    ])

    if (fetchId !== activeFetchId) {
      return
    }

    resource.value = prerequisiteResponse.data.map(mapPrerequisiteCategory)
    availableUsers.value = sortUsers(usersResponse.data)
    createCategoryError.value = false
  } catch (err) {
    if (fetchId !== activeFetchId) {
      return
    }

    resource.value = []
    availableUsers.value = []
    error.value = getErrorMessage(
      err,
      'Kunne ikke hente grunnforutsetninger fra serveren. Prøv igjen.',
    )
  } finally {
    if (fetchId === activeFetchId) {
      loading.value = false
    }
  }
}

async function handleSaveCategory(payload: UpdateCategoryPayload) {
  await api.patch('/prerequisite-categories/update-category', {
    categoryId: payload.categoryId,
    categoryName: payload.categoryName,
  })
  await fetchPrerequisites()
}

async function handleUpdateRoutine(payload: UpdateRoutinePayload) {
  await api.patch('/prerequisite-categories/update-routine', {
    routineId: payload.routineId,
    categoryId: payload.categoryId,
    title: payload.title,
    description: payload.description,
    measures: payload.measures,
    intervalStart: payload.interval_start,
    intervalRepeatTime: payload.interval_repeat_time,
  })

  await api.put(`/prerequisite-routines/${payload.routineId}/assignments`, {
    verifierUserIds: [],
    deviationReceiverUserIds: payload.deputy,
    performerUserIds: payload.performers,
    deputyUserIds: payload.deputy,
  })

  await fetchPrerequisites()
}

async function handleUpdateStandard(payload: UpdateStandardPayload) {
  await api.patch('/prerequisite-categories/update-standard', {
    standardId: payload.standardId,
    categoryId: payload.categoryId,
    title: payload.title,
    description: payload.description,
  })
  await fetchPrerequisites()
}

async function handleCreateRoutine(payload: CreateRoutinePayload) {
  await api.post('/prerequisite-categories/create-routine', {
    categoryId: payload.categoryId,
    title: payload.title,
    description: payload.description,
    measures: payload.measures,
    intervalStart: payload.interval_start,
    intervalRepeatTime: payload.interval_repeat_time,
    verifierUserIds: [],
    deviationReceiverUserIds: payload.deputy,
    performerUserIds: payload.performers,
    deputyUserIds: payload.deputy,
  })
  await fetchPrerequisites()
}

async function handleCreateStandard(payload: CreateStandardPayload) {
  await api.post('/prerequisite-categories/create-standard', {
    categoryId: payload.categoryId,
    title: payload.title,
    description: payload.description,
  })
  await fetchPrerequisites()
}

function startCreatingCategory() {
  if (isCreatingCategoryRequest.value) {
    return
  }

  isCreatingCategory.value = true
  createCategoryError.value = false
  newCategoryName.value = ''
}

function cancelCreatingCategory() {
  if (isCreatingCategoryRequest.value) {
    return
  }

  isCreatingCategory.value = false
  createCategoryError.value = false
  newCategoryName.value = ''
}

async function createCategory() {
  if (
    !isCreatingCategory.value ||
    isCreatingCategoryRequest.value ||
    newCategoryName.value.trim().length === 0
  ) {
    return
  }

  isCreatingCategoryRequest.value = true
  createCategoryError.value = false

  try {
    const payload: CreateCategoryPayload = { categoryName: newCategoryName.value.trim() }
    await api.post('/prerequisite-categories/create-category', payload)
    await fetchPrerequisites()
    isCreatingCategory.value = false
    newCategoryName.value = ''
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    createCategoryError.value = true
  } finally {
    isCreatingCategoryRequest.value = false
  }
}

function setNewCategoryName(value: string) {
  newCategoryName.value = value
}

watch(
  () => claims.value?.orgId ?? null,
  (orgId) => {
    if (!orgId) {
      resource.value = []
      availableUsers.value = []
      loading.value = false
      error.value = null
      return
    }

    void fetchPrerequisites()
  },
  { immediate: true },
)
</script>

<template>
  <div class="con">
    <main>
      <div class="main-no-sidebar-container">
        <Paginator />
        <h1 class="instrument-serif-regular no-margin">Grunnforutsetninger</h1>
        <hr class="navy-hr" />

        <DesktopButton
          v-if="!loading && !isCreatingCategory"
          :icon="Plus"
          content="kategori"
          :on-click="startCreatingCategory"
          :disabled="isCreatingCategoryRequest"
        />

        <Loading v-if="loading" />

        <p v-else-if="error" class="error-message">
          {{ error }}
        </p>

        <template v-else>
          <PrerequisiteCreateCategoryCard
            v-if="isCreatingCategory"
            :new-category-name="newCategoryName"
            :is-loading="isCreatingCategoryRequest"
            :has-error="createCategoryError"
            :set-new-category-name="setNewCategoryName"
            :on-create="createCategory"
            :on-cancel="cancelCreatingCategory"
          />

          <PrerequisiteCategoryCard
            v-for="category in resource"
            :key="`category-${category.id}`"
            :category="category"
            :available-users="availableUsers"
            :repeat-interval-options="repeatIntervalOptions"
            :disable-actions="isCreatingCategory || isCreatingCategoryRequest"
            :on-save-category="handleSaveCategory"
            :on-update-routine="handleUpdateRoutine"
            :on-update-standard="handleUpdateStandard"
            :on-create-routine="handleCreateRoutine"
            :on-create-standard="handleCreateStandard"
          />
        </template>
      </div>
    </main>
  </div>
</template>

<style scoped>
.con {
  overflow: scroll;
}

main {
  margin-top: 5rem;
  padding-bottom: 5rem;
  padding-left: 2rem;
  padding-right: 2rem;
}

.error-message {
  color: #b42318;
  margin: 0;
}
</style>
