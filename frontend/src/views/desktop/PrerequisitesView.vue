<script setup lang="ts">
import { mockAllUsers } from '@/data/mockAllUsers'
import AddUsers from '@/components/desktop/shared/AddUsers.vue'
import Badge from '@/components/desktop/shared/Badge.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import Paginator from '@/components/desktop/shared/Paginator.vue'
import UserBadge from '@/components/desktop/shared/UserBadge.vue'
import type { PrerequisiteCategoryAllInfo } from '@/interfaces/api-interfaces'
import type { BasicUserWithAccessLevel } from '@/interfaces/util-interfaces'
import { delay } from '@/utils'
import { Edit2, File, Link, Plus, Save, X } from '@lucide/vue'
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
  links: string[]
  resources: string[]
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
          {
            userId: 16,
            legalName: 'Kari Nessa Nordtun',
          },
          {
            userId: 14,
            legalName: 'Ola Svenneby',
          },
        ],
        performers: [
          {
            userId: 11,
            legalName: 'Mona Jul',
          },
          {
            userId: 12,
            legalName: 'Jagland',
          },
        ],
        deputy: [
          {
            userId: 14,
            legalName: 'Ola Svenneby',
          },
          {
            userId: 16,
            legalName: 'Kari Nessa Nordtun',
          },
        ],
      },
      {
        title: 'Hold rent',
        type: 'standard',
        description:
          'Vask 1 skal kun brukes for mat, mens vask 2 skal brukes for å vaske tallerkener',
        resources: [
          {
            id: 1,
            name: 'https://mattilsynet.no/hygieneguide',
            type: 'link',
          },
          {
            id: 2,
            name: 'renhold-standard.pdf',
            type: 'file',
          },
        ],
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

      return {
        ...point,
        resources: point.resources.map((resource) => ({ ...resource })),
      }
    }),
  }))
}

const mockServerState = ref<PrerequisiteCategoryAllInfo>(clonePrerequisites(mockData))
const resource = ref<PrerequisiteCategoryAllInfo>([])
const loading = ref(true)
const error = ref<boolean | null>(null)

const editingCategoryIndex = ref<number | null>(null)
const editCategoryName = ref('')
const savingCategoryIndex = ref<number | null>(null)
const categorySaveErrorIndex = ref<number | null>(null)
const isCreatingCategory = ref(false)
const newCategoryName = ref('')
const isCreatingCategoryRequest = ref(false)
const createCategoryError = ref(false)

const creatingPointTarget = ref<{ categoryIndex: number; type: 'routine' | 'standard' } | null>(
  null,
)
const createPointTitle = ref('')
const createPointMeasures = ref('')
const createPointDescription = ref('')
const createStandardLinkInput = ref('')
const createStandardLinks = ref<string[]>([])
const createStandardResourceFiles = ref<File[]>([])
const createPointIntervalStart = ref('')
const createPointIntervalRepeatTimeSeconds = ref(604800)
const createPointPerformerIds = ref<number[]>([])
const createPointDeputyIds = ref<number[]>([])
const creatingPointKey = ref<string | null>(null)
const createPointErrorKey = ref<string | null>(null)

const editingPointTarget = ref<{ categoryIndex: number; pointIndex: number } | null>(null)
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
  Record<string, { intervalStart: number; intervalRepeatTime: number }>
>({})

const repeatIntervalOptions = [
  { label: 'Hver dag', seconds: 86400 },
  { label: 'Hver 7. dag (1 uke)', seconds: 604800 },
  { label: 'Hver 14. dag (2 uker)', seconds: 1209600 },
  { label: 'Hver 30. dag', seconds: 2592000 },
]

const allSelectableUsers = computed<PrerequisiteUser[]>(() => {
  const users = new Map<number, string>()

  for (const user of mockAllUsers) {
    users.set(user.id, user.legalName)
  }

  for (const category of resource.value) {
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

function getPointKey(categoryIndex: number, pointIndex: number) {
  return `${categoryIndex}-${pointIndex}`
}

function getCreatePointKey(categoryIndex: number, type: 'routine' | 'standard') {
  return `${categoryIndex}-${type}`
}

function isEditingPoint(categoryIndex: number, pointIndex: number) {
  return (
    editingPointTarget.value?.categoryIndex === categoryIndex &&
    editingPointTarget.value?.pointIndex === pointIndex
  )
}

function isCreatingPointInCategory(categoryIndex: number) {
  return creatingPointTarget.value?.categoryIndex === categoryIndex
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
  // await fetch('/api/prerequisite-categories/update-category', {
  //   method: 'PATCH',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify(payload),
  // })
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
  // await fetch('/api/prerequisite-categories/update-routine', {
  //   method: 'PATCH',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify(payload),
  // })
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

  const categoryIndex = mockServerState.value.findIndex((entry) => entry.id === payload.categoryId)
  const pointKey = `${categoryIndex}-${routineIndex}`
  routineScheduleState.value[pointKey] = {
    intervalStart: payload.interval_start,
    intervalRepeatTime: payload.interval_repeat_time,
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
  // await fetch('/api/prerequisite-categories/update-standard', {
  //   method: 'PATCH',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify(payload),
  // })
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
  // await fetch('/api/prerequisite-categories/create-category', {
  //   method: 'POST',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify(payload),
  // })
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
  // await fetch('/api/prerequisite-categories/create-routine', {
  //   method: 'POST',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify(payload),
  // })
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
  // await fetch('/api/prerequisite-categories/create-standard', {
  //   method: 'POST',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify(payload),
  // })
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

  const nextResourceIdStart =
    mockServerState.value
      .flatMap((entry) => entry.points)
      .filter((point): point is StandardPoint => point.type === 'standard')
      .flatMap((point) => point.resources)
      .reduce((maxId, resource) => Math.max(maxId, resource.id), 0) + 1

  const resources = [
    ...payload.links.map((link, index) => ({
      id: nextResourceIdStart + index,
      name: link,
      type: 'link' as const,
    })),
    ...payload.resources.map((fileName, index) => ({
      id: nextResourceIdStart + payload.links.length + index,
      name: fileName,
      type: 'file' as const,
    })),
  ]

  category.points = [
    ...category.points,
    {
      type: 'standard',
      standardId: nextStandardId,
      title: payload.title,
      description: payload.description,
      resources,
    },
  ]
}

function startCreatingCategory() {
  if (
    isCreatingCategoryRequest.value ||
    creatingPointKey.value !== null ||
    creatingPointTarget.value !== null ||
    savingCategoryIndex.value !== null ||
    savingPointKey.value !== null
  ) {
    return
  }

  isCreatingCategory.value = true
  createCategoryError.value = false
  newCategoryName.value = ''
  editingCategoryIndex.value = null
  editingPointTarget.value = null
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
    await mockCreateCategory({
      categoryName: newCategoryName.value.trim(),
    })
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

function startEditingCategory(categoryIndex: number) {
  if (
    isCreatingCategory.value ||
    creatingPointTarget.value !== null ||
    savingCategoryIndex.value !== null ||
    savingPointKey.value !== null
  ) {
    return
  }

  editingPointTarget.value = null
  pointSaveErrorKey.value = null
  categorySaveErrorIndex.value = null
  editCategoryName.value = resource.value[categoryIndex]?.categoryName ?? ''
  editingCategoryIndex.value = categoryIndex
}

function cancelEditingCategory() {
  if (savingCategoryIndex.value !== null) {
    return
  }

  editingCategoryIndex.value = null
  editCategoryName.value = ''
  categorySaveErrorIndex.value = null
}

async function saveCategory(categoryIndex: number) {
  if (
    savingCategoryIndex.value !== null ||
    editingCategoryIndex.value !== categoryIndex ||
    editCategoryName.value.trim().length === 0
  ) {
    return
  }

  savingCategoryIndex.value = categoryIndex
  categorySaveErrorIndex.value = null

  try {
    const current = mockServerState.value[categoryIndex]

    if (!current) {
      throw new Error('Category not found')
    }

    await mockUpdateCategory({
      categoryId: current.id,
      categoryName: editCategoryName.value.trim(),
    })

    await fetchPrerequisites()
    editingCategoryIndex.value = null
    editCategoryName.value = ''
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    categorySaveErrorIndex.value = categoryIndex
  } finally {
    savingCategoryIndex.value = null
  }
}

function startEditingPoint(categoryIndex: number, pointIndex: number) {
  if (
    isCreatingCategory.value ||
    creatingPointTarget.value !== null ||
    savingCategoryIndex.value !== null ||
    savingPointKey.value !== null
  ) {
    return
  }

  const point = resource.value[categoryIndex]?.points[pointIndex]
  if (!point) {
    return
  }

  editingCategoryIndex.value = null
  categorySaveErrorIndex.value = null

  editPointTitle.value = point.title
  if (point.type === 'routine') {
    const pointKey = getPointKey(categoryIndex, pointIndex)
    const currentSchedule = routineScheduleState.value[pointKey] ?? {
      intervalStart: Math.floor(Date.now() / 1000),
      intervalRepeatTime: 604800,
    }
    routineScheduleState.value[pointKey] = currentSchedule

    editPointMeasures.value = point.measures
    editPointIntervalStart.value = toDateTimeLocal(currentSchedule.intervalStart)
    editPointIntervalRepeatTimeSeconds.value = currentSchedule.intervalRepeatTime
    editPointPerformerIds.value = point.performers.map((user) => user.userId)
    editPointDeputyIds.value = point.deputy.map((user) => user.userId)
    editPointDescription.value = ''
  } else {
    editPointMeasures.value = ''
    editPointDescription.value = point.description
    editPointIntervalStart.value = ''
    editPointIntervalRepeatTimeSeconds.value = 604800
    editPointPerformerIds.value = []
    editPointDeputyIds.value = []
  }

  pointSaveErrorKey.value = null
  editingPointTarget.value = { categoryIndex, pointIndex }
}

function cancelEditingPoint() {
  if (savingPointKey.value !== null) {
    return
  }

  editingPointTarget.value = null
  editPointTitle.value = ''
  editPointMeasures.value = ''
  editPointDescription.value = ''
  editPointIntervalStart.value = ''
  editPointIntervalRepeatTimeSeconds.value = 604800
  editPointPerformerIds.value = []
  editPointDeputyIds.value = []
  pointSaveErrorKey.value = null
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

function addCreateStandardLink() {
  const value = createStandardLinkInput.value.trim()
  if (value.length === 0) {
    return
  }

  createStandardLinks.value = [...createStandardLinks.value, value]
  createStandardLinkInput.value = ''
}

function removeCreateStandardLink(index: number) {
  createStandardLinks.value = createStandardLinks.value.filter(
    (_, currentIndex) => currentIndex !== index,
  )
}

function setCreateStandardResourceFiles(event: Event) {
  const input = event.target as HTMLInputElement | null
  if (!input?.files) {
    createStandardResourceFiles.value = []
    return
  }

  createStandardResourceFiles.value = Array.from(input.files)
}

function startCreatingPoint(categoryIndex: number, type: 'routine' | 'standard') {
  if (
    isCreatingCategory.value ||
    isCreatingCategoryRequest.value ||
    savingCategoryIndex.value !== null ||
    savingPointKey.value !== null ||
    creatingPointKey.value !== null
  ) {
    return
  }

  const category = resource.value[categoryIndex]
  if (!category) {
    return
  }

  creatingPointTarget.value = { categoryIndex, type }
  createPointErrorKey.value = null
  editCategoryName.value = ''
  editingCategoryIndex.value = null
  editingPointTarget.value = null
  pointSaveErrorKey.value = null

  createPointTitle.value = ''
  createPointMeasures.value = ''
  createPointDescription.value = ''
  createStandardLinkInput.value = ''
  createStandardLinks.value = []
  createStandardResourceFiles.value = []
  createPointIntervalStart.value = toDateTimeLocal(Math.floor(Date.now() / 1000))
  createPointIntervalRepeatTimeSeconds.value = 604800
  createPointPerformerIds.value = []
  createPointDeputyIds.value = []

  if (type === 'routine' && category.points.length > 0) {
    const lastRoutine = [...category.points].reverse().find((point) => point.type === 'routine')
    if (lastRoutine && lastRoutine.type === 'routine') {
      createPointPerformerIds.value = lastRoutine.performers.map((user) => user.userId)
      createPointDeputyIds.value = lastRoutine.deputy.map((user) => user.userId)
    }
  }
}

function cancelCreatingPoint() {
  if (creatingPointKey.value !== null) {
    return
  }

  creatingPointTarget.value = null
  createPointTitle.value = ''
  createPointMeasures.value = ''
  createPointDescription.value = ''
  createStandardLinkInput.value = ''
  createStandardLinks.value = []
  createStandardResourceFiles.value = []
  createPointIntervalStart.value = ''
  createPointIntervalRepeatTimeSeconds.value = 604800
  createPointPerformerIds.value = []
  createPointDeputyIds.value = []
  createPointErrorKey.value = null
}

async function createPoint(categoryIndex: number) {
  const target = creatingPointTarget.value
  if (!target || target.categoryIndex !== categoryIndex) {
    return
  }

  const createKey = getCreatePointKey(target.categoryIndex, target.type)
  if (creatingPointKey.value !== null || createPointTitle.value.trim().length === 0) {
    return
  }

  if (target.type === 'routine') {
    const intervalStart = toUnixSeconds(createPointIntervalStart.value)
    const repeatSeconds = Math.floor(createPointIntervalRepeatTimeSeconds.value)
    if (
      createPointMeasures.value.trim().length === 0 ||
      intervalStart === null ||
      repeatSeconds <= 0 ||
      createPointPerformerIds.value.length === 0 ||
      createPointDeputyIds.value.length === 0
    ) {
      return
    }
  } else if (createPointDescription.value.trim().length === 0) {
    return
  }

  creatingPointKey.value = createKey
  createPointErrorKey.value = null

  try {
    const category = resource.value[categoryIndex]
    if (!category) {
      throw new Error('Category not found')
    }

    if (target.type === 'routine') {
      const intervalStart = toUnixSeconds(createPointIntervalStart.value)
      if (intervalStart === null) {
        throw new Error('Invalid interval_start')
      }

      await mockCreateRoutine({
        categoryId: category.id,
        title: createPointTitle.value.trim(),
        measures: createPointMeasures.value.trim(),
        interval_start: intervalStart,
        interval_repeat_time: Math.floor(createPointIntervalRepeatTimeSeconds.value),
        performers: createPointPerformerIds.value,
        deputy: createPointDeputyIds.value,
      })
    } else {
      await mockCreateStandard({
        categoryId: category.id,
        title: createPointTitle.value.trim(),
        description: createPointDescription.value.trim(),
        links: [...createStandardLinks.value],
        resources: createStandardResourceFiles.value.map((file) => file.name),
      })
    }

    await fetchPrerequisites()
    creatingPointTarget.value = null
    createPointTitle.value = ''
    createPointMeasures.value = ''
    createPointDescription.value = ''
    createPointIntervalStart.value = ''
    createPointIntervalRepeatTimeSeconds.value = 604800
    createPointPerformerIds.value = []
    createPointDeputyIds.value = []
    createPointErrorKey.value = null
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    createPointErrorKey.value = createKey
  } finally {
    creatingPointKey.value = null
  }
}

async function savePoint(categoryIndex: number, pointIndex: number) {
  const point = resource.value[categoryIndex]?.points[pointIndex]
  if (
    !point ||
    !isEditingPoint(categoryIndex, pointIndex) ||
    savingPointKey.value !== null ||
    editPointTitle.value.trim().length === 0
  ) {
    return
  }

  if (point.type === 'routine') {
    const intervalStart = toUnixSeconds(editPointIntervalStart.value)
    const repeatSeconds = Math.floor(editPointIntervalRepeatTimeSeconds.value)

    if (
      editPointMeasures.value.trim().length === 0 ||
      intervalStart === null ||
      repeatSeconds <= 0
    ) {
      return
    }

    if (editPointPerformerIds.value.length === 0 || editPointDeputyIds.value.length === 0) {
      return
    }
  }

  if (point.type === 'standard' && editPointDescription.value.trim().length === 0) {
    return
  }

  const pointKey = getPointKey(categoryIndex, pointIndex)
  savingPointKey.value = pointKey
  pointSaveErrorKey.value = null

  try {
    const category = mockServerState.value[categoryIndex]
    if (!category) {
      throw new Error('Category not found')
    }

    const serverPoint = category.points[pointIndex]
    if (!serverPoint) {
      throw new Error('Point not found')
    }

    if (serverPoint.type === 'routine') {
      const intervalStart = toUnixSeconds(editPointIntervalStart.value)
      if (intervalStart === null) {
        throw new Error('Invalid interval_start')
      }

      const repeatSeconds = Math.floor(editPointIntervalRepeatTimeSeconds.value)
      if (repeatSeconds <= 0) {
        throw new Error('Invalid interval_repeat_time')
      }

      await mockUpdateRoutine({
        categoryId: category.id,
        routineId: serverPoint.routineId,
        title: editPointTitle.value.trim(),
        measures: editPointMeasures.value.trim(),
        interval_start: intervalStart,
        interval_repeat_time: repeatSeconds,
        performers: editPointPerformerIds.value,
        deputy: editPointDeputyIds.value,
      })
    } else {
      await mockUpdateStandard({
        categoryId: category.id,
        standardId: serverPoint.standardId,
        title: editPointTitle.value.trim(),
        description: editPointDescription.value.trim(),
      })
    }

    await fetchPrerequisites()
    editingPointTarget.value = null
    editPointTitle.value = ''
    editPointMeasures.value = ''
    editPointDescription.value = ''
    editPointIntervalStart.value = ''
    editPointIntervalRepeatTimeSeconds.value = 604800
    editPointPerformerIds.value = []
    editPointDeputyIds.value = []
    pointSaveErrorKey.value = null
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    pointSaveErrorKey.value = pointKey
  } finally {
    savingPointKey.value = null
  }
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
        <div v-if="!loading && isCreatingCategory" class="create-category-card">
          <h2 class="no-margin">Ny kategori</h2>
          <label class="create-category-field">
            <span class="navy-subtitle">Kategorinavn</span>
            <input
              v-model="newCategoryName"
              class="simple-text-input category-input"
              :disabled="isCreatingCategoryRequest"
              type="text"
              placeholder="F.eks. Renhold og hygiene"
            />
          </label>
          <div class="edit-actions">
            <DesktopButton
              :icon="Save"
              content="Opprett"
              :on-click="createCategory"
              :disabled="newCategoryName.trim().length === 0"
              :is-loading="isCreatingCategoryRequest"
              loading-text="Oppretter"
            />
            <DesktopButton
              :icon="X"
              content="Avbryt"
              button-color="cherry"
              :on-click="cancelCreatingCategory"
              :disabled="isCreatingCategoryRequest"
            />
          </div>
          <p v-if="createCategoryError" class="error-message no-margin">
            Klarte ikke å opprette kategori.
          </p>
        </div>
        <div
          v-for="(prerequisite, categoryIndex) in resource"
          :key="`category-${prerequisite.id}`"
          class="prerequisite-category"
        >
          <div class="prerequisite-category-header">
            <div class="category-edit-wrap">
              <h2 v-if="editingCategoryIndex !== categoryIndex" class="no-margin">
                {{ prerequisite.categoryName }}
              </h2>
              <input
                v-else
                v-model="editCategoryName"
                class="simple-text-input category-input"
                :disabled="savingCategoryIndex === categoryIndex"
                type="text"
                placeholder="Kategorinavn"
              />
            </div>
            <DesktopButton
              v-if="editingCategoryIndex !== categoryIndex"
              :icon="Edit2"
              content="Rediger"
              :on-click="() => startEditingCategory(categoryIndex)"
            />
            <div v-else class="edit-actions">
              <DesktopButton
                :icon="Save"
                content="Lagre"
                :disabled="editCategoryName.trim().length === 0"
                :on-click="() => saveCategory(categoryIndex)"
              />
              <DesktopButton
                :icon="X"
                content="Avbryt"
                button-color="cherry"
                :on-click="cancelEditingCategory"
              />
            </div>
          </div>

          <p v-if="categorySaveErrorIndex === categoryIndex" class="error-message no-margin">
            Klarte ikke å oppdatere kategori.
          </p>

          <div v-if="prerequisite.points.length === 0">Oi... Her var det tomt</div>

          <div class="point-container">
            <div
              v-for="(point, pointIndex) in prerequisite.points"
              :key="
                point.type === 'routine'
                  ? `routine-${prerequisite.id}-${point.routineId}`
                  : `standard-${prerequisite.id}-${point.standardId}`
              "
              class="point"
            >
              <div class="point-header">
                <div>
                  <div v-if="point.type === 'routine'" class="point-dot-routine" />
                  <div v-if="point.type === 'standard'" class="point-dot-standard" />
                  <h3 v-if="!isEditingPoint(categoryIndex, pointIndex)" class="no-margin">
                    {{ point.title }}
                  </h3>
                  <input
                    v-else
                    v-model="editPointTitle"
                    class="simple-text-input point-title-input"
                    :disabled="savingPointKey === `${categoryIndex}-${pointIndex}`"
                    type="text"
                    placeholder="Tittel"
                  />
                </div>
                <div>
                  <span
                    v-if="point.type === 'routine' && !isEditingPoint(categoryIndex, pointIndex)"
                  >
                    {{ point.repeatText }}
                  </span>
                  <div
                    v-if="point.type === 'routine' && isEditingPoint(categoryIndex, pointIndex)"
                    class="interval-edit-fields"
                  >
                    <label class="interval-field">
                      <span class="navy-subtitle">Intervallstart</span>
                      <input
                        v-model="editPointIntervalStart"
                        class="simple-text-input repeat-input"
                        :disabled="savingPointKey === `${categoryIndex}-${pointIndex}`"
                        type="datetime-local"
                        placeholder="Velg starttid"
                      />
                    </label>
                    <label class="interval-field">
                      <span class="navy-subtitle">Repetering (sek)</span>
                      <select
                        v-model.number="editPointIntervalRepeatTimeSeconds"
                        class="simple-text-input repeat-input"
                        :disabled="savingPointKey === `${categoryIndex}-${pointIndex}`"
                      >
                        <option
                          v-for="intervalOption in repeatIntervalOptions"
                          :key="`repeat-${intervalOption.seconds}`"
                          :value="intervalOption.seconds"
                        >
                          {{ intervalOption.label }}
                        </option>
                        <option
                          v-if="
                            !repeatIntervalOptions.some(
                              (intervalOption) =>
                                intervalOption.seconds === editPointIntervalRepeatTimeSeconds,
                            )
                          "
                          :value="editPointIntervalRepeatTimeSeconds"
                        >
                          Tilpasset ({{ editPointIntervalRepeatTimeSeconds }} sek)
                        </option>
                      </select>
                    </label>
                  </div>

                  <DesktopButton
                    v-if="!isEditingPoint(categoryIndex, pointIndex)"
                    :icon="Edit2"
                    content="Rediger"
                    :on-click="() => startEditingPoint(categoryIndex, pointIndex)"
                  />
                  <div v-else class="edit-actions">
                    <DesktopButton
                      :icon="Save"
                      content="Lagre"
                      :on-click="() => savePoint(categoryIndex, pointIndex)"
                    />
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
                <span v-if="!isEditingPoint(categoryIndex, pointIndex)">
                  Avvikstiltak: {{ point.measures }}
                </span>
                <div v-else>
                  <h4 class="navy-subtitle no-margin">Avvikstiltak</h4>
                  <textarea
                    v-model="editPointMeasures"
                    class="simple-text-input point-textarea"
                    :disabled="savingPointKey === `${categoryIndex}-${pointIndex}`"
                    rows="4"
                    placeholder="Beskriv avvikstiltak"
                  />
                </div>

                <div>
                  <span class="navy-subtitle">Vikarleder</span>
                  <div v-if="!isEditingPoint(categoryIndex, pointIndex)" class="user-parent">
                    <UserBadge
                      v-for="deputy in point.deputy"
                      :key="deputy.userId"
                      :name="deputy.legalName"
                      :user-id="123"
                    />
                  </div>
                  <AddUsers
                    v-else
                    :set-users="setEditPointDeputy"
                    :initial-user-ids="editPointDeputyIds"
                  />
                </div>

                <div>
                  <span class="navy-subtitle">De som skal gjøre det</span>
                  <div v-if="!isEditingPoint(categoryIndex, pointIndex)" class="user-parent">
                    <UserBadge
                      v-for="performer in point.performers"
                      :key="performer.userId"
                      :name="performer.legalName"
                      :user-id="123"
                    />
                  </div>
                  <AddUsers
                    v-else
                    :set-users="setEditPointPerformers"
                    :initial-user-ids="editPointPerformerIds"
                  />
                </div>
              </template>

              <template v-else>
                <span v-if="!isEditingPoint(categoryIndex, pointIndex)">
                  {{ point.description }}
                </span>
                <div v-else>
                  <h4 class="navy-subtitle no-margin">Beskrivelse</h4>
                  <textarea
                    v-model="editPointDescription"
                    class="simple-text-input point-textarea"
                    :disabled="savingPointKey === `${categoryIndex}-${pointIndex}`"
                    rows="4"
                    placeholder="Beskriv standarden"
                  />
                </div>

                <div v-if="point.resources.length > 0" class="standard-resources">
                  <span class="navy-subtitle">Ressurser</span>
                  <div class="resource-container">
                    <Badge
                      v-for="resource in point.resources"
                      :key="resource.id"
                      badge-color="navy"
                      :icon="resource.type === 'link' ? Link : File"
                    >
                      {{ resource.name }}
                    </Badge>
                  </div>
                </div>
              </template>

              <Loading v-if="savingPointKey === `${categoryIndex}-${pointIndex}`" />
              <p
                v-if="pointSaveErrorKey === `${categoryIndex}-${pointIndex}`"
                class="error-message no-margin"
              >
                Klarte ikke å oppdatere punkt.
              </p>
            </div>
          </div>

          <div class="add-point-container">
            <DesktopButton
              :icon="Plus"
              content="Legg til rutine"
              :on-click="() => startCreatingPoint(categoryIndex, 'routine')"
              button-color="blue-decor"
              :disabled="
                isCreatingCategory ||
                isCreatingCategoryRequest ||
                creatingPointKey !== null ||
                (creatingPointTarget !== null && !isCreatingPointInCategory(categoryIndex))
              "
            />
            <DesktopButton
              button-color="cherry"
              :icon="Plus"
              content="Legg til standard"
              :on-click="() => startCreatingPoint(categoryIndex, 'standard')"
              :disabled="
                isCreatingCategory ||
                isCreatingCategoryRequest ||
                creatingPointKey !== null ||
                (creatingPointTarget !== null && !isCreatingPointInCategory(categoryIndex))
              "
            />
          </div>

          <div v-if="isCreatingPointInCategory(categoryIndex)" class="create-point-card">
            <h3 class="no-margin">
              Ny {{ creatingPointTarget?.type === 'routine' ? 'rutine' : 'standard' }}
            </h3>

            <label class="create-category-field">
              <span class="navy-subtitle">Tittel</span>
              <input
                v-model="createPointTitle"
                class="simple-text-input category-input"
                :disabled="creatingPointKey !== null"
                type="text"
                placeholder="F.eks. Vask av benker"
              />
            </label>

            <template v-if="creatingPointTarget?.type === 'routine'">
              <div class="interval-edit-fields">
                <label class="interval-field">
                  <span class="navy-subtitle">Intervallstart</span>
                  <input
                    v-model="createPointIntervalStart"
                    class="simple-text-input repeat-input"
                    :disabled="creatingPointKey !== null"
                    type="datetime-local"
                    placeholder="Velg starttid"
                  />
                </label>
                <label class="interval-field">
                  <span class="navy-subtitle">Repetering</span>
                  <select
                    v-model.number="createPointIntervalRepeatTimeSeconds"
                    class="simple-text-input repeat-input"
                    :disabled="creatingPointKey !== null"
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

              <label class="create-category-field">
                <span class="navy-subtitle">Avvikstiltak</span>
                <textarea
                  v-model="createPointMeasures"
                  class="simple-text-input point-textarea"
                  :disabled="creatingPointKey !== null"
                  rows="4"
                  placeholder="Hva skal gjøres ved avvik?"
                />
              </label>

              <div>
                <span class="navy-subtitle">Vikarleder</span>
                <AddUsers
                  :set-users="setCreatePointDeputy"
                  :initial-user-ids="createPointDeputyIds"
                />
              </div>

              <div>
                <span class="navy-subtitle">De som skal gjøre det</span>
                <AddUsers
                  :set-users="setCreatePointPerformers"
                  :initial-user-ids="createPointPerformerIds"
                />
              </div>
            </template>

            <div v-else class="create-category-field">
              <label class="create-category-field">
                <span class="navy-subtitle">Beskrivelse</span>
                <textarea
                  v-model="createPointDescription"
                  class="simple-text-input point-textarea"
                  :disabled="creatingPointKey !== null"
                  rows="4"
                  placeholder="Beskrivelse av standard"
                />
              </label>

              <label class="create-category-field">
                <span class="navy-subtitle">Lenker</span>
                <div class="create-link-row">
                  <input
                    v-model="createStandardLinkInput"
                    class="simple-text-input category-input"
                    :disabled="creatingPointKey !== null"
                    type="text"
                    placeholder="https://example.no/standard"
                  />
                  <DesktopButton
                    :icon="Plus"
                    content="Legg til lenke"
                    :on-click="addCreateStandardLink"
                    :disabled="creatingPointKey !== null"
                  />
                </div>
                <div class="resource-container">
                  <Badge
                    v-for="(link, linkIndex) in createStandardLinks"
                    :key="`create-standard-link-${linkIndex}-${link}`"
                    badge-color="navy"
                    :icon="Link"
                  >
                    {{ link }}
                    <button
                      class="remove-resource-button"
                      :disabled="creatingPointKey !== null"
                      @click="removeCreateStandardLink(linkIndex)"
                    >
                      x
                    </button>
                  </Badge>
                </div>
              </label>

              <label class="create-category-field">
                <span class="navy-subtitle">Ressurser (filer)</span>
                <input
                  type="file"
                  multiple
                  :disabled="creatingPointKey !== null"
                  @change="setCreateStandardResourceFiles"
                />
                <div class="resource-container">
                  <Badge
                    v-for="file in createStandardResourceFiles"
                    :key="`create-standard-file-${file.name}`"
                    badge-color="navy"
                    :icon="File"
                  >
                    {{ file.name }}
                  </Badge>
                </div>
              </label>
            </div>

            <div class="edit-actions">
              <DesktopButton
                :icon="Save"
                content="Opprett"
                :on-click="() => createPoint(categoryIndex)"
                :is-loading="
                  creatingPointKey === getCreatePointKey(categoryIndex, creatingPointTarget!.type)
                "
                loading-text="Oppretter"
              />
              <DesktopButton
                :icon="X"
                content="Avbryt"
                button-color="cherry"
                :on-click="cancelCreatingPoint"
                :disabled="creatingPointKey !== null"
              />
            </div>

            <p
              v-if="
                createPointErrorKey === getCreatePointKey(categoryIndex, creatingPointTarget!.type)
              "
              class="error-message no-margin"
            >
              Klarte ikke å opprette punkt.
            </p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>
<style scoped>
.con {
  overflow: scroll;
}
.add-point-container {
  display: flex;
  width: 100%;
  gap: 1rem;
  > button {
    width: 100%;
  }
}
.user-parent {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}
.prerequisite-category {
  border-radius: 1rem;
  padding: 1rem;
  display: flex;
  gap: 1rem;
  flex-direction: column;
  border: 1px solid var(--blue-decor);
  background-color: var(--blue-decor-10);
  .prerequisite-category-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 0.75rem;
  }
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

.create-category-card {
  border-radius: 0.75rem;
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
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

.create-category-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.create-link-row {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.standard-resources {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.resource-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.remove-resource-button {
  border: 0;
  background-color: transparent;
  color: var(--blue-navy);
  padding: 0;
  margin-left: 0.3rem;
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

.interval-field {
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
  > div {
    display: flex;
    flex-direction: row;
    justify-content: center;
    align-items: center;
    gap: 0.5rem;
    /* background-color: red; */
  }
}
.point-dot-routine {
  width: 0.5rem;
  height: 0.5rem;
  background-color: var(--blue-decor);
}
.point-dot-standard {
  width: 0.5rem;
  height: 0.5rem;
  background-color: var(--red-cherry);
}
main {
  display: flex;
  margin-top: 5rem;
  padding-bottom: 5rem;
  padding-left: 2rem;
  padding-right: 2rem;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  width: 100%;
  .main-no-sidebar-container {
    width: calc(3 / 5 * 100%);
    /* background-color: var(--white-greek);
        border-radius: 1rem;
        padding: 1rem;
        border: 1px solid var(--blue-navy-40); */
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
  .point-container {
    display: flex;
    gap: 1rem;
    flex-direction: column;
    .point {
      padding: 1rem;
      border-radius: 0.5rem;
      border: 1px solid var(--blue-navy-40);
      background-color: var(--white-greek);
    }
  }
}
hr {
  border-color: var(--blue-navy-40);
  border-width: 1px;
}
@media (max-width: 768px) {
  main > form {
    width: 100%;
  }

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

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
