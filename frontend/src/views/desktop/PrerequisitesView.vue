<script setup lang="ts">
import PrerequisiteCategoryCard from '@/components/desktop/prerequisites/PrerequisiteCategoryCard.vue'
import PrerequisiteCreateCategoryCard from '@/components/desktop/prerequisites/PrerequisiteCreateCategoryCard.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import Paginator from '@/components/desktop/shared/Paginator.vue'
import { mockAllUsers } from '@/data/mockAllUsers'
import type { PrerequisiteCategoryAllInfo } from '@/interfaces/api-interfaces'
import { delay } from '@/utils'
import { Plus } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'

type PrerequisitePoint = PrerequisiteCategoryAllInfo[number]['points'][number]
type RoutinePoint = Extract<PrerequisitePoint, { type: 'routine' }>
type StandardPoint = Extract<PrerequisitePoint, { type: 'standard' }>
type PrerequisiteUser = RoutinePoint['performers'][number]

type UpdateCategoryPayload = {
  categoryId: number
  categoryName: string
}

type UpdateRoutinePayload = {
  categoryId: number
  routineId: number
  title: string
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

const mockData: PrerequisiteCategoryAllInfo = [
  {
    categoryName: 'Renhold av lokaler og utstyr',
    id: 1,
    points: [
      {
        title: 'Vask gulvene',
        type: 'routine',
        measures: 'Terje må vaske gulvene ekstra godt neste gang og ta 50 push-ups',
        repeatText: 'Hver mandag kl. 17:00',
        routineId: 1,
        deviationRecievers: [
          { userId: 16, legalName: 'Kari Nessa Nordtun' },
          { userId: 14, legalName: 'Ola Svenneby' },
        ],
        performers: [
          { userId: 11, legalName: 'Mona Jul' },
          { userId: 12, legalName: 'Jagland' },
        ],
        deputy: [
          { userId: 14, legalName: 'Ola Svenneby' },
          { userId: 16, legalName: 'Kari Nessa Nordtun' },
        ],
      },
      {
        title: 'Hold rent',
        type: 'standard',
        description:
          'Vask 1 skal kun brukes for mat, mens vask 2 skal brukes for å vaske tallerkener',
        resources: [],
        standardId: 1,
      },
    ],
  },
  {
    categoryName: 'God personlig hygiene hos ansatte ',
    points: [],
    id: 8,
  },
]

const repeatIntervalOptions = [
  { label: 'Hver dag', seconds: 86400 },
  { label: 'Hver 7. dag (1 uke)', seconds: 604800 },
  { label: 'Hver 14. dag (2 uker)', seconds: 1209600 },
  { label: 'Hver 30. dag', seconds: 2592000 },
]

function clonePrerequisites(data: PrerequisiteCategoryAllInfo): PrerequisiteCategoryAllInfo {
  return data.map((category) => ({
    id: category.id,
    categoryName: category.categoryName,
    points: category.points.map((point) => {
      if (point.type === 'routine') {
        return {
          ...point,
          deviationRecievers: point.deviationRecievers.map((user) => ({ ...user })),
          performers: point.performers.map((user) => ({ ...user })),
          deputy: point.deputy.map((user) => ({ ...user })),
        }
      }

      return { ...point }
    }),
  }))
}

const mockServerState = ref<PrerequisiteCategoryAllInfo>(clonePrerequisites(mockData))
const resource = ref<PrerequisiteCategoryAllInfo>([])
const loading = ref(true)
const error = ref<boolean | null>(null)

const isCreatingCategory = ref(false)
const newCategoryName = ref('')
const isCreatingCategoryRequest = ref(false)
const createCategoryError = ref(false)

const allSelectableUsers = computed<PrerequisiteUser[]>(() => {
  const users = new Map<number, string>()

  for (const user of mockAllUsers) {
    users.set(user.id, user.legalName)
  }

  for (const category of mockServerState.value) {
    for (const point of category.points) {
      if (point.type === 'routine') {
        for (const user of point.performers) {
          users.set(user.userId, user.legalName)
        }
        for (const user of point.deputy) {
          users.set(user.userId, user.legalName)
        }
      }
    }
  }

  return Array.from(users.entries()).map(([userId, legalName]) => ({ userId, legalName }))
})

function formatRepeatTime(seconds: number): string {
  if (seconds % 604800 === 0) {
    const weeks = seconds / 604800
    return weeks === 1 ? 'Hver uke' : `Hver ${weeks} uker`
  }

  if (seconds % 86400 === 0) {
    const days = seconds / 86400
    return days === 1 ? 'Hver dag' : `Hver ${days} dager`
  }

  if (seconds % 3600 === 0) {
    const hours = seconds / 3600
    return hours === 1 ? 'Hver time' : `Hver ${hours} timer`
  }

  if (seconds % 60 === 0) {
    const minutes = seconds / 60
    return minutes === 1 ? 'Hvert minutt' : `Hvert ${minutes}. minutt`
  }

  return `Hvert ${seconds}. sekund`
}

function formatIntervalText(intervalStart: number, intervalRepeatTime: number): string {
  const dateLabel = new Date(intervalStart * 1000).toLocaleString('nb-NO')
  return `${formatRepeatTime(intervalRepeatTime)} (start: ${dateLabel})`
}

function resolveUserById(userId: number): PrerequisiteUser {
  const user = allSelectableUsers.value.find((candidate) => candidate.userId === userId)
  if (user) {
    return {
      userId: user.userId,
      legalName: user.legalName,
    }
  }

  return {
    userId,
    legalName: `Bruker ${userId}`,
  }
}

function mapIdsToUsers(userIds: number[]): PrerequisiteUser[] {
  return userIds.map((userId) => resolveUserById(userId))
}

async function fetchPrerequisites() {
  // const response = await fetch('/api/prerequisite-categories/get-all-info')
  // if (!response.ok) {
  //   throw new Error(`Failed to fetch prerequisites (${response.status})`)
  // }
  // const data: PrerequisiteCategoryAllInfo = await response.json()

  await delay(500)
  resource.value = clonePrerequisites(mockServerState.value)
}

async function mockUpdateCategory(payload: UpdateCategoryPayload) {
  // await fetch('/api/prerequisite-categories/update-category', { method: 'PATCH', body: JSON.stringify(payload) })
  await delay(700)

  const categoryIndex = mockServerState.value.findIndex(
    (category) => category.id === payload.categoryId,
  )
  if (categoryIndex === -1) {
    throw new Error('Category not found')
  }

  const currentCategory = mockServerState.value[categoryIndex]
  if (!currentCategory) {
    throw new Error('Category not found')
  }

  mockServerState.value[categoryIndex] = {
    ...currentCategory,
    categoryName: payload.categoryName,
  }
}

async function mockUpdateRoutine(payload: UpdateRoutinePayload) {
  // await fetch('/api/prerequisite-categories/update-routine', { method: 'PATCH', body: JSON.stringify(payload) })
  await delay(700)

  const category = mockServerState.value.find((entry) => entry.id === payload.categoryId)
  if (!category) {
    throw new Error('Category not found')
  }

  const routineIndex = category.points.findIndex(
    (point) => point.type === 'routine' && point.routineId === payload.routineId,
  )
  const routine = category.points[routineIndex] as RoutinePoint | undefined

  if (!routine || routine.type !== 'routine') {
    throw new Error('Routine not found')
  }

  category.points[routineIndex] = {
    ...routine,
    title: payload.title,
    measures: payload.measures,
    repeatText: formatIntervalText(payload.interval_start, payload.interval_repeat_time),
    performers: mapIdsToUsers(payload.performers),
    deputy: mapIdsToUsers(payload.deputy),
  }
}

async function mockUpdateStandard(payload: UpdateStandardPayload) {
  // await fetch('/api/prerequisite-categories/update-standard', { method: 'PATCH', body: JSON.stringify(payload) })
  await delay(700)

  const category = mockServerState.value.find((entry) => entry.id === payload.categoryId)
  if (!category) {
    throw new Error('Category not found')
  }

  const standardIndex = category.points.findIndex(
    (point) => point.type === 'standard' && point.standardId === payload.standardId,
  )
  const standard = category.points[standardIndex] as StandardPoint | undefined

  if (!standard || standard.type !== 'standard') {
    throw new Error('Standard not found')
  }

  category.points[standardIndex] = {
    ...standard,
    title: payload.title,
    description: payload.description,
  }
}

async function mockCreateCategory(payload: CreateCategoryPayload) {
  // await fetch('/api/prerequisite-categories/create-category', { method: 'POST', body: JSON.stringify(payload) })
  await delay(700)

  const nextId =
    mockServerState.value.length === 0
      ? 1
      : Math.max(...mockServerState.value.map((category) => category.id)) + 1

  mockServerState.value = [
    ...mockServerState.value,
    {
      id: nextId,
      categoryName: payload.categoryName,
      points: [],
    },
  ]
}

async function mockCreateRoutine(payload: CreateRoutinePayload) {
  // await fetch('/api/prerequisite-categories/create-routine', { method: 'POST', body: JSON.stringify(payload) })
  await delay(700)

  const category = mockServerState.value.find((entry) => entry.id === payload.categoryId)
  if (!category) {
    throw new Error('Category not found')
  }

  const nextRoutineId =
    mockServerState.value
      .flatMap((entry) => entry.points)
      .filter((point): point is RoutinePoint => point.type === 'routine')
      .reduce((maxId, point) => Math.max(maxId, point.routineId), 0) + 1

  category.points = [
    ...category.points,
    {
      type: 'routine',
      routineId: nextRoutineId,
      title: payload.title,
      measures: payload.measures,
      repeatText: formatIntervalText(payload.interval_start, payload.interval_repeat_time),
      deviationRecievers: mapIdsToUsers(payload.deputy),
      performers: mapIdsToUsers(payload.performers),
      deputy: mapIdsToUsers(payload.deputy),
    },
  ]
}

async function mockCreateStandard(payload: CreateStandardPayload) {
  // await fetch('/api/prerequisite-categories/create-standard', { method: 'POST', body: JSON.stringify(payload) })
  await delay(700)

  const category = mockServerState.value.find((entry) => entry.id === payload.categoryId)
  if (!category) {
    throw new Error('Category not found')
  }

  const nextStandardId =
    mockServerState.value
      .flatMap((entry) => entry.points)
      .filter((point): point is StandardPoint => point.type === 'standard')
      .reduce((maxId, point) => Math.max(maxId, point.standardId), 0) + 1

  category.points = [
    ...category.points,
    {
      type: 'standard',
      standardId: nextStandardId,
      title: payload.title,
      description: payload.description,
      resources: [],
    },
  ]
}

async function handleSaveCategory(payload: UpdateCategoryPayload) {
  await mockUpdateCategory(payload)
  await fetchPrerequisites()
}

async function handleUpdateRoutine(payload: UpdateRoutinePayload) {
  await mockUpdateRoutine(payload)
  await fetchPrerequisites()
}

async function handleUpdateStandard(payload: UpdateStandardPayload) {
  await mockUpdateStandard(payload)
  await fetchPrerequisites()
}

async function handleCreateRoutine(payload: CreateRoutinePayload) {
  await mockCreateRoutine(payload)
  await fetchPrerequisites()
}

async function handleCreateStandard(payload: CreateStandardPayload) {
  await mockCreateStandard(payload)
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
    await mockCreateCategory({ categoryName: newCategoryName.value.trim() })
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

onMounted(async () => {
  try {
    await fetchPrerequisites()
    error.value = false
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    error.value = true
  } finally {
    loading.value = false
  }
})
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

        <PrerequisiteCreateCategoryCard
          v-if="!loading && isCreatingCategory"
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
          :repeat-interval-options="repeatIntervalOptions"
          :disable-actions="isCreatingCategory || isCreatingCategoryRequest"
          :on-save-category="handleSaveCategory"
          :on-update-routine="handleUpdateRoutine"
          :on-update-standard="handleUpdateStandard"
          :on-create-routine="handleCreateRoutine"
          :on-create-standard="handleCreateStandard"
        />
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
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  width: 100%;
}

.main-no-sidebar-container {
  width: calc(3 / 5 * 100%);
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

@media (max-width: 768px) {
  .main-no-sidebar-container {
    width: 100%;
  }
}
</style>
