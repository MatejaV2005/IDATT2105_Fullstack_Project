<script setup lang="ts">
import LearningCompletionTable from '@/components/desktop/learning/LearningCompletionTable.vue'
import LearningCourseRequirements from '@/components/desktop/learning/LearningCourseRequirements.vue'
import LearningCreateCourseCard from '@/components/desktop/learning/LearningCreateCourseCard.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import type { LearningAllInfo } from '@/interfaces/api-interfaces'
import { delay } from '@/utils'
import { onMounted, ref } from 'vue'

const mockData: LearningAllInfo = {
  allCourses: [
    {
      name: 'Serveringskurs',
      description:
        "Dere må lese here pdf'en & skrive en oppsummering. Dere må også gjøre alle oppgaver i NDLA sitt kurs og levere det inn til oss.",
      resources: [
        {
          name: 'www.ndla.no/random_stuff',
          type: 'link',
          id: 1,
        },
        {
          name: 'erna_sin_spise_guide.pdf',
          type: 'file',
          id: 2,
        },
      ],
      responsible: ['Simen Velle', 'Vedum'],
      uniqueId: 1234,
    },
    {
      name: 'Drikkekurs',
      description:
        "Dere må lese here pdf'en & skrive en oppsummering. Dere må også gjøre alle oppgaver i NDLA sitt kurs og levere det inn til oss.",
      resources: [
        {
          name: 'www.ndla.no/random_stuff',
          type: 'link',
          id: 9,
        },
        {
          name: 'erna_sin_spise_guide.pdf',
          type: 'file',
          id: 4,
        },
      ],
      responsible: ['Simen Velle', 'Ola svenneby'],
      uniqueId: 9876,
    },
    {
      name: 'Drikkekurs',
      description:
        "Dere må lese here pdf'en & skrive en oppsummering. Dere må også gjøre alle oppgaver i NDLA sitt kurs og levere det inn til oss.",
      resources: [
        {
          name: 'www.ndla.no/random_stuff',
          type: 'link',
          id: 23,
        },
        {
          name: 'erna_sin_spise_guide.pdf',
          type: 'file',
          id: 58,
        },
      ],
      responsible: ['Simen Velle', 'Ola svenneby'],
      uniqueId: 9816,
    },
  ],
  userProgress: [
    {
      id: 11,
      legalName: 'Mona Jul',
      email: 'mona.jul@example.com',
      courses: [
        {
          name: 'Serveringskurs',
          completed: true,
          uniqueId: 1234,
        },
        {
          name: 'Drikkekurs',
          completed: false,
          uniqueId: 9876,
        },
      ],
    },
    {
      id: 12,
      legalName: 'Jagland',
      email: 'jagland@example.com',
      courses: [
        {
          name: 'Serveringskurs',
          completed: true,
          uniqueId: 1234,
        },
        {
          name: 'Drikkekurs',
          completed: true,
          uniqueId: 9876,
        },
      ],
    },
    {
      id: 13,
      legalName: 'Bent Hoie',
      email: 'bent.hoie@example.com',
      courses: [
        {
          name: 'Serveringskurs',
          completed: false,
          uniqueId: 1234,
        },
        {
          name: 'Drikkekurs',
          completed: true,
          uniqueId: 9876,
        },
      ],
    },
    {
      id: 14,
      legalName: 'Bondevik',
      email: 'bondevik@example.com',
      courses: [
        {
          name: 'Serveringskurs',
          completed: false,
          uniqueId: 1234,
        },
        {
          name: 'Drikkekurs',
          completed: false,
          uniqueId: 9876,
        },
      ],
    },
  ],
}

interface UpdateCourseCompletionPayload {
  userId: number
  courseId: number
  completed: boolean
}

interface CreateCoursePayload {
  title: string
  description: string
  links: string[]
  resources: File[]
}

function cloneLearningData(data: LearningAllInfo): LearningAllInfo {
  return {
    allCourses: data.allCourses.map((course) => ({
      ...course,
      resources: course.resources.map((resource) => ({ ...resource })),
      responsible: [...course.responsible],
    })),
    userProgress: data.userProgress.map((user) => ({
      ...user,
      courses: user.courses.map((course) => ({ ...course })),
    })),
  }
}

const mockServerState = ref<LearningAllInfo>(cloneLearningData(mockData))

const resource = ref<LearningAllInfo>({ allCourses: [], userProgress: [] })
const loading = ref(true)
const error = ref<boolean | null>(null)

const isEditingCompletion = ref(false)
const isSavingCompletion = ref(false)
const saveCompletionError = ref(false)
const draftUserProgress = ref<LearningAllInfo['userProgress']>([])

const isCreatingCourseRequest = ref(false)
const createCourseError = ref(false)

async function fetchLearning() {
  // const response = await fetch('/api/learning/get-all-info')
  // if (!response.ok) {
  //   throw new Error(`Failed to fetch learning data (${response.status})`)
  // }
  // const data: LearningAllInfo = await response.json()
  await delay(500)
  resource.value = cloneLearningData(mockServerState.value)
}

async function updateCourseCompletion(payload: UpdateCourseCompletionPayload) {
  // const response = await fetch('/api/courses/course-progress', {
  //   method: 'PUT',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify(payload),
  // })
  // if (!response.ok) {
  //   throw new Error(`Failed to update course completion (${response.status})`)
  // }
  await delay(220)

  const userEntry = mockServerState.value.userProgress.find((entry) => entry.id === payload.userId)
  if (!userEntry) {
    return
  }

  const courseEntry = userEntry.courses.find((entry) => entry.uniqueId === payload.courseId)
  if (!courseEntry) {
    return
  }

  courseEntry.completed = payload.completed
}

async function mockCreateCourse(payload: CreateCoursePayload) {
  // const formData = new FormData()
  // formData.append('title', payload.title)
  // formData.append('description', payload.description)
  // payload.links.forEach((link) => formData.append('links', link))
  // payload.resources.forEach((file) => formData.append('resources', file))
  // const response = await fetch('/api/learning/create-course', {
  //   method: 'POST',
  //   body: formData,
  // })
  // if (!response.ok) {
  //   throw new Error(`Failed to create course (${response.status})`)
  // }
  await delay(600)

  const nextCourseId =
    mockServerState.value.allCourses.length === 0
      ? 1
      : Math.max(...mockServerState.value.allCourses.map((course) => course.uniqueId)) + 1

  const nextResourceIdStart =
    mockServerState.value.allCourses
      .flatMap((course) => course.resources)
      .reduce((maxId, resource) => Math.max(maxId, resource.id), 0) + 1

  const resources = [
    ...payload.links.map((link, index) => ({
      id: nextResourceIdStart + index,
      name: link,
      type: 'link' as const,
    })),
    ...payload.resources.map((file, index) => ({
      id: nextResourceIdStart + payload.links.length + index,
      name: file.name,
      type: 'file' as const,
    })),
  ]

  mockServerState.value.allCourses = [
    ...mockServerState.value.allCourses,
    {
      uniqueId: nextCourseId,
      name: payload.title,
      description: payload.description,
      resources,
      responsible: [],
    },
  ]

  mockServerState.value.userProgress = mockServerState.value.userProgress.map((user) => ({
    ...user,
    courses: [
      ...user.courses,
      {
        uniqueId: nextCourseId,
        name: payload.title,
        completed: false,
      },
    ],
  }))
}

function startEditingCompletion() {
  draftUserProgress.value = resource.value.userProgress.map((user) => ({
    ...user,
    courses: user.courses.map((course) => ({ ...course })),
  }))
  saveCompletionError.value = false
  isEditingCompletion.value = true
}

function cancelCompletionEditing() {
  if (isSavingCompletion.value) {
    return
  }

  isEditingCompletion.value = false
  draftUserProgress.value = []
  saveCompletionError.value = false
}

function toggleDraftCourseCompletion(payload: { userId: number; courseId: number }) {
  const user = draftUserProgress.value.find((entry) => entry.id === payload.userId)
  if (!user) {
    return
  }

  const course = user.courses.find((entry) => entry.uniqueId === payload.courseId)
  if (!course) {
    return
  }

  course.completed = !course.completed
}

async function saveCompletionChanges() {
  if (isSavingCompletion.value) {
    return
  }

  const updates: UpdateCourseCompletionPayload[] = []

  for (const user of draftUserProgress.value) {
    const originalUser = resource.value.userProgress.find((entry) => entry.id === user.id)
    if (!originalUser) {
      continue
    }

    for (const course of user.courses) {
      const originalCourse = originalUser.courses.find(
        (entry) => entry.uniqueId === course.uniqueId,
      )
      if (!originalCourse || originalCourse.completed === course.completed) {
        continue
      }

      updates.push({
        userId: user.id,
        courseId: course.uniqueId,
        completed: course.completed,
      })
    }
  }

  if (updates.length === 0) {
    isEditingCompletion.value = false
    draftUserProgress.value = []
    return
  }

  isSavingCompletion.value = true
  saveCompletionError.value = false

  try {
    for (const payload of updates) {
      await updateCourseCompletion(payload)
    }
    await fetchLearning()
    isEditingCompletion.value = false
    draftUserProgress.value = []
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    saveCompletionError.value = true
  } finally {
    isSavingCompletion.value = false
  }
}

async function handleCreateCourse(payload: CreateCoursePayload): Promise<boolean> {
  if (isCreatingCourseRequest.value) {
    return false
  }

  isCreatingCourseRequest.value = true
  createCourseError.value = false

  try {
    await mockCreateCourse(payload)
    await fetchLearning()
    return true
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    createCourseError.value = true
    return false
  } finally {
    isCreatingCourseRequest.value = false
  }
}

onMounted(async () => {
  try {
    await fetchLearning()
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
  <SidebarPageContainer active-page="/desktop/bedrift-opplaering">
    <div class="learning-area-container">
      <h1 class="instrument-serif-regular no-margin">Opplæring</h1>

      <span class="navy-subtitle">Godkjenning</span>
      <Loading v-if="loading" />
      <LearningCompletionTable
        v-else
        :all-courses="resource.allCourses"
        :users="isEditingCompletion ? draftUserProgress : resource.userProgress"
        :is-editing-completion="isEditingCompletion"
        :is-saving-completion="isSavingCompletion"
        :save-completion-error="saveCompletionError"
        @start-editing="startEditingCompletion"
        @save-changes="saveCompletionChanges"
        @cancel-editing="cancelCompletionEditing"
        @toggle-completion="toggleDraftCourseCompletion"
      />

      <span class="navy-subtitle">Opplæringskrav</span>
      <Loading v-if="loading" />
      <LearningCourseRequirements v-else :all-courses="resource.allCourses" />

      <LearningCreateCourseCard
        :is-submitting="isCreatingCourseRequest"
        :create-error="createCourseError"
        :on-create="handleCreateCourse"
      />
    </div>
  </SidebarPageContainer>
</template>

<style scoped>
.learning-area-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border-radius: 1rem;
  padding: 1rem;
  margin-top: 2rem;
}

@media (max-width: 1200px) {
  .learning-area-container {
    margin-top: 1rem;
    padding: 0.75rem;
  }
}

@media (max-width: 768px) {
  .learning-area-container {
    padding: 0.5rem;
  }
}
</style>
