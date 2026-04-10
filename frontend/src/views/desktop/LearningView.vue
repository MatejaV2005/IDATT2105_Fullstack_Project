<script setup lang="ts">
import Badge from '@/components/desktop/shared/Badge.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import type { LearningAllInfo } from '@/interfaces/api-interfaces'
import { delay } from '@/utils'
import { Check, Edit2, File, Link, Plus, Save, X } from '@lucide/vue'
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
const isCreatingCourse = ref(false)
const isCreatingCourseRequest = ref(false)
const createCourseError = ref(false)
const createCourseTitle = ref('')
const createCourseDescription = ref('')
const createCourseLinkInput = ref('')
const createCourseLinks = ref<string[]>([])
const createCourseResourceFiles = ref<File[]>([])

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

function hasCompletedCourse(
  user: { courses: { name: string; completed: boolean; uniqueId: number }[] },
  courseId: number,
) {
  return user.courses.some((course) => course.uniqueId === courseId && course.completed)
}

function startCreatingCourse() {
  if (isCreatingCourseRequest.value) {
    return
  }

  createCourseError.value = false
  createCourseTitle.value = ''
  createCourseDescription.value = ''
  createCourseLinkInput.value = ''
  createCourseLinks.value = []
  createCourseResourceFiles.value = []
  isCreatingCourse.value = true
}

function cancelCreatingCourse(force = false) {
  if (isCreatingCourseRequest.value && !force) {
    return
  }

  isCreatingCourse.value = false
  createCourseError.value = false
  createCourseTitle.value = ''
  createCourseDescription.value = ''
  createCourseLinkInput.value = ''
  createCourseLinks.value = []
  createCourseResourceFiles.value = []
}

function addCourseLink() {
  const value = createCourseLinkInput.value.trim()
  if (value.length === 0) {
    return
  }

  createCourseLinks.value = [...createCourseLinks.value, value]
  createCourseLinkInput.value = ''
}

function removeCourseLink(index: number) {
  createCourseLinks.value = createCourseLinks.value.filter(
    (_, currentIndex) => currentIndex !== index,
  )
}

function setCourseResourceFiles(event: Event) {
  const input = event.target as HTMLInputElement | null
  if (!input?.files) {
    return
  }

  const selectedFiles = Array.from(input.files)
  const existingKeys = new Set(
    createCourseResourceFiles.value.map((file) => `${file.name}-${file.size}-${file.lastModified}`),
  )

  for (const file of selectedFiles) {
    const fileKey = `${file.name}-${file.size}-${file.lastModified}`
    if (!existingKeys.has(fileKey)) {
      createCourseResourceFiles.value = [...createCourseResourceFiles.value, file]
      existingKeys.add(fileKey)
    }
  }

  input.value = ''
}

function removeCourseResourceFile(index: number) {
  createCourseResourceFiles.value = createCourseResourceFiles.value.filter(
    (_, currentIndex) => currentIndex !== index,
  )
}

async function createCourse() {
  if (
    !isCreatingCourse.value ||
    isCreatingCourseRequest.value ||
    createCourseTitle.value.trim().length === 0 ||
    createCourseDescription.value.trim().length === 0
  ) {
    return
  }

  isCreatingCourseRequest.value = true
  createCourseError.value = false

  try {
    await mockCreateCourse({
      title: createCourseTitle.value.trim(),
      description: createCourseDescription.value.trim(),
      links: [...createCourseLinks.value],
      resources: [...createCourseResourceFiles.value],
    })
    await fetchLearning()
    cancelCreatingCourse(true)
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    createCourseError.value = true
  } finally {
    isCreatingCourseRequest.value = false
  }
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

function toggleDraftCourseCompletion(userId: number, courseId: number) {
  const user = draftUserProgress.value.find((entry) => entry.id === userId)
  if (!user) {
    return
  }

  const course = user.courses.find((entry) => entry.uniqueId === courseId)
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
</script>

<template>
  <SidebarPageContainer active-page="/desktop/bedrift-opplaering">
    <div class="learning-area-container">
      <h1 class="instrument-serif-regular no-margin">Opplæring</h1>
      <span class="navy-subtitle">Godkjenning</span>
      <Loading v-if="loading" />
      <div v-if="!loading" class="course-completion">
        <table>
          <thead>
            <tr>
              <th>Bruker</th>
              <th v-for="course in resource.allCourses" :key="course.uniqueId">
                {{ course.name }}
              </th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="user in isEditingCompletion ? draftUserProgress : resource.userProgress"
              :key="user.id"
            >
              <td>{{ user.legalName }}</td>
              <td v-for="course in resource.allCourses" :key="`${user.id}-${course.uniqueId}`">
                <button
                  v-if="isEditingCompletion"
                  class="completion-chip completion-toggle-chip"
                  :class="
                    hasCompletedCourse(user, course.uniqueId) ? 'is-complete' : 'is-incomplete'
                  "
                  :disabled="isSavingCompletion"
                  @click="toggleDraftCourseCompletion(user.id, course.uniqueId)"
                >
                  {{ hasCompletedCourse(user, course.uniqueId) ? 'Fullført' : 'Ikke fullført' }}
                </button>
                <span
                  v-else
                  class="completion-chip"
                  :class="
                    hasCompletedCourse(user, course.uniqueId) ? 'is-complete' : 'is-incomplete'
                  "
                >
                  {{ hasCompletedCourse(user, course.uniqueId) ? 'Fullført' : 'Ikke fullført' }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
        <div class="completion-actions">
          <DesktopButton
            v-if="!isEditingCompletion"
            content="Rediger"
            :icon="Edit2"
            class="navy-button-flat-top"
            :on-click="startEditingCompletion"
          />
          <div v-else class="completion-action-row">
            <DesktopButton
              content="Lagre"
              :icon="Check"
              :on-click="saveCompletionChanges"
              :is-loading="isSavingCompletion"
              loading-text="Lagrer"
              class="completion-action-button"
            />
            <DesktopButton
              content="Avbryt"
              :icon="X"
              :on-click="cancelCompletionEditing"
              :disabled="isSavingCompletion"
              button-color="boring-ghost"
              class="completion-action-button"
            />
          </div>
          <p v-if="saveCompletionError" class="completion-error">
            Klarte ikke oppdatere kursstatus. Prøv igjen.
          </p>
        </div>
      </div>
      <span class="navy-subtitle">Opplæringskrav</span>
      <Loading v-if="loading" />
      <div v-for="course in resource.allCourses" :key="course.uniqueId" class="course">
        <div class="course-header">
          <h2 class="no-margin">
            {{ course.name }}
          </h2>
          <DesktopButton content="Edit" :icon="Edit2" />
        </div>
        <div>
          <span class="navy-subtitle"> Beskrivelse: </span>
          <span>
            {{ course.description }}
          </span>
        </div>
        <div>
          <span class="navy-subtitle"> Ressurser: </span>
          <div class="resource-container">
            <Badge
              v-for="resource in course.resources"
              :key="resource.id"
              badge-color="navy"
              :icon="resource.type === 'link' ? Link : File"
            >
              {{ resource.name }}
            </Badge>
          </div>
        </div>
      </div>
      <DesktopButton
        v-if="!isCreatingCourse"
        :icon="Plus"
        content="Legg til kurs"
        :on-click="startCreatingCourse"
      />

      <div v-else class="course create-course-card">
        <h3 class="no-margin">Nytt kurs</h3>

        <label class="create-course-field">
          <span class="navy-subtitle">Tittel</span>
          <input
            v-model="createCourseTitle"
            class="simple-text-input course-input"
            type="text"
            placeholder="F.eks. Internkontroll grunnkurs"
            :disabled="isCreatingCourseRequest"
          />
        </label>

        <label class="create-course-field">
          <span class="navy-subtitle">Beskrivelse</span>
          <textarea
            v-model="createCourseDescription"
            class="simple-text-input course-textarea"
            rows="4"
            placeholder="Beskriv hva kurset går ut på"
            :disabled="isCreatingCourseRequest"
          />
        </label>

        <label class="create-course-field">
          <span class="navy-subtitle">Lenker</span>
          <div class="link-row">
            <input
              v-model="createCourseLinkInput"
              class="simple-text-input course-input"
              type="text"
              placeholder="https://example.no/kurs"
              :disabled="isCreatingCourseRequest"
            />
            <DesktopButton
              :icon="Plus"
              content="Legg til lenke"
              :on-click="addCourseLink"
              :disabled="isCreatingCourseRequest"
            />
          </div>
          <div class="resource-container">
            <Badge
              v-for="(link, index) in createCourseLinks"
              :key="`new-course-link-${index}-${link}`"
              badge-color="navy"
              :icon="Link"
            >
              {{ link }}
              <DesktopButton
                :icon="X"
                content="Fjern"
                button-color="boring-ghost"
                :disabled="isCreatingCourseRequest"
                :on-click="() => removeCourseLink(index)"
                class="inline-remove-button"
              />
            </Badge>
          </div>
        </label>

        <label class="create-course-field">
          <span class="navy-subtitle">Ressurser (filer)</span>
          <input
            type="file"
            multiple
            :disabled="isCreatingCourseRequest"
            @change="setCourseResourceFiles"
          />
          <div class="resource-container">
            <Badge
              v-for="(file, index) in createCourseResourceFiles"
              :key="`new-course-file-${file.name}-${file.lastModified}-${index}`"
              badge-color="navy"
              :icon="File"
            >
              {{ file.name }}
              <DesktopButton
                :icon="X"
                content="Fjern"
                button-color="boring-ghost"
                :disabled="isCreatingCourseRequest"
                :on-click="() => removeCourseResourceFile(index)"
                class="inline-remove-button"
              />
            </Badge>
          </div>
        </label>

        <div class="completion-action-row">
          <DesktopButton
            :icon="Save"
            content="Opprett"
            :on-click="createCourse"
            :is-loading="isCreatingCourseRequest"
            loading-text="Oppretter"
            class="completion-action-button"
          />
          <DesktopButton
            :icon="X"
            content="Avbryt"
            button-color="cherry"
            :on-click="cancelCreatingCourse"
            :disabled="isCreatingCourseRequest"
            class="completion-action-button"
          />
        </div>

        <p v-if="createCourseError" class="completion-error">Klarte ikke å opprette kurs.</p>
      </div>
    </div>
  </SidebarPageContainer>
</template>

<style scoped>
.course {
  width: 100%;
  background-color: var(--white-greek);
  border-radius: 1rem;
  padding: 1rem;
  border: 1px solid var(--blue-navy-40);
  display: flex;
  gap: 1rem;
  flex-direction: column;
  .course-header {
    display: flex;
    justify-content: space-between;
    /* background-color: red; */
    /* height: 2rem; */
    padding-bottom: 1rem;
    border-bottom: 1px solid var(--blue-navy-40);
  }
}
.resource-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.create-course-card {
  gap: 0.75rem;
}

.create-course-field {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.course-input {
  height: 2.2rem;
}

.course-textarea {
  resize: vertical;
}

.link-row {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.inline-remove-button {
  margin-left: 0.35rem;
  padding: 0.05rem 0.45rem;
  border-radius: 0.35rem;
}
.learning-area-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border-radius: 1rem;
  padding: 1rem;
  margin-top: 2rem;
}
.navy-button-flat-top {
  width: 100%;
  border-radius: 0.7rem;
  border-top-right-radius: 0;
  border-top-left-radius: 0;
}

.completion-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.completion-action-row {
  display: flex;
  gap: 0.5rem;
}

.completion-action-button {
  width: 100%;
  border-radius: 0;
}

.completion-error {
  margin: 0;
  color: var(--red-cherry);
  padding: 0 0.5rem 0.5rem;
}

.course-completion {
  max-height: 26rem;
  overflow: auto;
  border: 1px solid var(--blue-navy-40);
  border-radius: 0.75rem;
  background-color: var(--white-greek);
  border: 1px solid var(--blue-navy-40);
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 40rem;
}

th,
td {
  text-align: left;
  padding: 0.75rem;
  border-bottom: 1px solid var(--blue-navy-20);
}

thead th {
  position: sticky;
  top: 0;
  background-color: var(--white-greek);
  z-index: 1;
}

.completion-chip {
  display: inline-flex;
  flex-wrap: wrap;
  border-radius: 999px;
  padding: 0.15rem 0.6rem;
  font-size: 0.85rem;
  font-weight: 600;
}

.completion-toggle-chip {
  border: 0;
}

.completion-toggle-chip:hover:not(:disabled) {
  box-shadow: 0rem 0.2rem 0.35rem var(--black-no-face-20);
}

.completion-toggle-chip:disabled {
  opacity: 0.7;
}

.is-complete {
  color: #0f5132;
  background-color: #d1e7dd;
}

.is-incomplete {
  color: #842029;
  background-color: #f8d7da;
}

@media (max-width: 1200px) {
  .learning-area-container {
    margin-top: 1rem;
    padding: 0.75rem;
  }

  table {
    min-width: 34rem;
  }
}

@media (max-width: 768px) {
  .learning-area-container {
    padding: 0.5rem;
  }

  .course {
    padding: 0.75rem;

    .course-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 0.5rem;
    }
  }

  .course-completion {
    max-height: 22rem;
  }

  table {
    min-width: 28rem;
  }

  th,
  td {
    padding: 0.6rem;
    font-size: 0.9rem;
  }

  .completion-chip {
    font-size: 0.8rem;
  }

  .completion-action-row {
    flex-direction: column;
  }
}
</style>
