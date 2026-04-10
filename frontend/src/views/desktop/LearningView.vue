<script setup lang="ts">
import api from '@/api/api'
import LearningCompletionTable from '@/components/desktop/learning/LearningCompletionTable.vue'
import LearningCourseRequirements from '@/components/desktop/learning/LearningCourseRequirements.vue'
import LearningCreateCourseCard from '@/components/desktop/learning/LearningCreateCourseCard.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import { useOrgSession } from '@/composables/useOrgSession'
import type {
  CreateLearningCoursePayload,
  LearningCourse,
  LearningOrganizationUser,
  LearningOverviewResponse,
  LearningUserCourseProgress,
  LearningUserProgress,
} from '@/interfaces/api-interfaces'
import { computed, ref, watch } from 'vue'

interface LearningPageData {
  allCourses: LearningCourse[]
  userProgress: LearningUserProgress[]
}

interface UpdateCourseCompletionPayload {
  userId: number
  courseId: number
  completed: boolean
}

const { claims } = useOrgSession()

const resource = ref<LearningPageData>({ allCourses: [], userProgress: [] })
const organizationUsers = ref<LearningOrganizationUser[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

const isEditingCompletion = ref(false)
const isSavingCompletion = ref(false)
const saveCompletionError = ref(false)
const draftUserProgress = ref<LearningUserProgress[]>([])

const isCreatingCourseRequest = ref(false)
const createCourseError = ref(false)

let activeFetchId = 0

function sortUsers(users: LearningOrganizationUser[]) {
  return [...users].sort((left, right) => left.legalName.localeCompare(right.legalName, 'nb-NO'))
}

function buildLearningPageData(
  overview: LearningOverviewResponse,
  users: LearningOrganizationUser[],
): LearningPageData {
  const progressByUser = new Map(
    overview.userProgress.map((userProgress) => [
      userProgress.userId,
      new Map(
        userProgress.courses.map((courseStatus) => [courseStatus.courseId, courseStatus.completed]),
      ),
    ]),
  )

  const normalizedUsers = sortUsers(users).map((user) => ({
    id: user.id,
    legalName: user.legalName,
    email: user.email,
    courses: overview.allCourses.map<LearningUserCourseProgress>((course) => ({
      courseId: course.id,
      title: course.title,
      completed: progressByUser.get(user.id)?.get(course.id) ?? false,
      hasProgressRecord: progressByUser.get(user.id)?.has(course.id) ?? false,
    })),
  }))

  return {
    allCourses: overview.allCourses,
    userProgress: normalizedUsers,
  }
}

function cloneUserProgress(users: LearningUserProgress[]) {
  return users.map((user) => ({
    ...user,
    courses: user.courses.map((course) => ({ ...course })),
  }))
}

function getErrorMessage(err: unknown, fallback: string) {
  const status = (err as { response?: { status?: number } }).response?.status

  if (status === 403) {
    return 'Du har ikke tilgang til opplæringssiden. Kun owner og manager kan åpne denne siden.'
  }

  return fallback
}

async function fetchLearning() {
  const fetchId = ++activeFetchId

  if (fetchId === activeFetchId) {
    loading.value = true
    error.value = null
  }

  try {
    const [overviewResponse, organizationUsersResponse] = await Promise.all([
      api.get<LearningOverviewResponse>('/courses/overview'),
      api.get<LearningOrganizationUser[]>('/organizations/users'),
    ])

    if (fetchId !== activeFetchId) {
      return
    }

    organizationUsers.value = sortUsers(organizationUsersResponse.data)
    resource.value = buildLearningPageData(overviewResponse.data, organizationUsers.value)
    isEditingCompletion.value = false
    draftUserProgress.value = []
    saveCompletionError.value = false
    createCourseError.value = false
  } catch (err) {
    if (fetchId !== activeFetchId) {
      return
    }

    resource.value = { allCourses: [], userProgress: [] }
    organizationUsers.value = []
    error.value = getErrorMessage(
      err,
      'Kunne ikke hente opplæringsdata fra serveren. Prøv igjen.',
    )
  } finally {
    if (fetchId === activeFetchId) {
      loading.value = false
    }
  }
}

function startEditingCompletion() {
  draftUserProgress.value = cloneUserProgress(resource.value.userProgress)
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

  const course = user.courses.find((entry) => entry.courseId === payload.courseId)
  if (!course) {
    return
  }

  course.completed = !course.completed
}

async function persistCompletionChange(
  originalCourse: LearningUserCourseProgress,
  nextCourse: LearningUserCourseProgress,
  userId: number,
) {
  if (originalCourse.hasProgressRecord) {
    await api.patch(`/courses/${nextCourse.courseId}/progress/${userId}`, nextCourse.completed, {
      headers: {
        'Content-Type': 'application/json',
      },
    })
    return
  }

  if (!nextCourse.completed) {
    return
  }

  await api.post(`/courses/${nextCourse.courseId}/progress/${userId}`)
  await api.patch(`/courses/${nextCourse.courseId}/progress/${userId}`, true, {
    headers: {
      'Content-Type': 'application/json',
    },
  })
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
      const originalCourse = originalUser.courses.find((entry) => entry.courseId === course.courseId)
      if (!originalCourse || originalCourse.completed === course.completed) {
        continue
      }

      updates.push({
        userId: user.id,
        courseId: course.courseId,
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
      const originalUser = resource.value.userProgress.find((entry) => entry.id === payload.userId)
      const originalCourse = originalUser?.courses.find((entry) => entry.courseId === payload.courseId)
      const nextUser = draftUserProgress.value.find((entry) => entry.id === payload.userId)
      const nextCourse = nextUser?.courses.find((entry) => entry.courseId === payload.courseId)

      if (!originalCourse || !nextCourse) {
        continue
      }

      await persistCompletionChange(originalCourse, nextCourse, payload.userId)
    }

    await fetchLearning()
  } catch (err) {
    console.error(err)
    saveCompletionError.value = true
  } finally {
    isSavingCompletion.value = false
  }
}

async function uploadCourseFile(courseId: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)

  await api.post(`/courses/${courseId}/files`, formData)
}

function buildCreateCourseFormData(payload: CreateLearningCoursePayload) {
  const formData = new FormData()
  formData.append('title', payload.title)
  formData.append('description', payload.description)

  for (const link of payload.links) {
    formData.append('links', link)
  }

  for (const file of payload.resources) {
    formData.append('resources', file)
  }

  return formData
}

async function handleCreateCourse(payload: CreateLearningCoursePayload): Promise<boolean> {
  if (isCreatingCourseRequest.value) {
    return false
  }

  isCreatingCourseRequest.value = true
  createCourseError.value = false

  try {
    const createResponse = await api.post<{ id: number }>(
      '/courses/create-course',
      buildCreateCourseFormData(payload),
    )

    const courseId = createResponse.data.id

    const assignmentResults = await Promise.allSettled(
      organizationUsers.value.map((user) => api.post(`/courses/${courseId}/progress/${user.id}`)),
    )

    const failedAssignments = assignmentResults.filter((result) => result.status === 'rejected')
    if (failedAssignments.length > 0) {
      console.warn(
        `Course ${courseId} was created, but ${failedAssignments.length} course assignments failed.`,
        failedAssignments,
      )
    }

    await fetchLearning()
    return true
  } catch (err) {
    console.error(err)
    createCourseError.value = true
    return false
  } finally {
    isCreatingCourseRequest.value = false
  }
}

async function saveCourseDetails(
  courseId: number,
  payload: { title: string; courseDescription: string },
): Promise<boolean> {
  try {
    await api.patch(`/courses/${courseId}`, payload)
    await fetchLearning()
    return true
  } catch (err) {
    console.error(err)
    return false
  }
}

async function deleteCourse(courseId: number): Promise<boolean> {
  try {
    await api.delete(`/courses/${courseId}`)
    await fetchLearning()
    return true
  } catch (err) {
    console.error(err)
    return false
  }
}

async function addResponsibleUser(courseId: number, userId: number): Promise<boolean> {
  try {
    await api.post(`/courses/${courseId}/responsible/${userId}`)
    await fetchLearning()
    return true
  } catch (err) {
    console.error(err)
    return false
  }
}

async function removeResponsibleUser(courseId: number, userId: number): Promise<boolean> {
  try {
    await api.delete(`/courses/${courseId}/responsible/${userId}`)
    await fetchLearning()
    return true
  } catch (err) {
    console.error(err)
    return false
  }
}

async function addCourseLink(courseId: number, link: string): Promise<boolean> {
  try {
    await api.post(`/courses/${courseId}/links`, { link })
    await fetchLearning()
    return true
  } catch (err) {
    console.error(err)
    return false
  }
}

async function removeCourseLink(courseId: number, linkId: number): Promise<boolean> {
  try {
    await api.delete(`/courses/${courseId}/links/${linkId}`)
    await fetchLearning()
    return true
  } catch (err) {
    console.error(err)
    return false
  }
}

async function uploadCourseFiles(courseId: number, files: File[]): Promise<boolean> {
  try {
    for (const file of files) {
      await uploadCourseFile(courseId, file)
    }

    await fetchLearning()
    return true
  } catch (err) {
    console.error(err)
    return false
  }
}

async function removeCourseFile(courseId: number, fileId: number): Promise<boolean> {
  try {
    await api.delete(`/courses/${courseId}/files/${fileId}`)
    await fetchLearning()
    return true
  } catch (err) {
    console.error(err)
    return false
  }
}

async function downloadCourseFile(courseId: number, fileId: number, fileName: string) {
  const response = await api.get(`/courses/${courseId}/files/${fileId}`, {
    responseType: 'blob',
  })

  const blobUrl = URL.createObjectURL(response.data)
  const link = document.createElement('a')
  link.href = blobUrl
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(blobUrl)
}

const hasCourses = computed(() => resource.value.allCourses.length > 0)

watch(
  () => claims.value?.orgId ?? null,
  () => {
    void fetchLearning()
  },
  { immediate: true },
)
</script>

<template>
  <SidebarPageContainer active-page="/desktop/bedrift-opplaering">
    <div class="learning-area-container">
      <h1 class="instrument-serif-regular no-margin">Opplæring</h1>

      <Loading v-if="loading" />

      <div v-else-if="error" class="learning-error-banner">
        <p>{{ error }}</p>
      </div>

      <template v-else>
        <span class="navy-subtitle">Godkjenning</span>
        <LearningCompletionTable
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
        <p v-if="!hasCourses" class="learning-empty-state">Ingen kurs funnet for denne organisasjonen.</p>
        <LearningCourseRequirements
          v-else
          :all-courses="resource.allCourses"
          :organization-users="organizationUsers"
          :on-save-course-details="saveCourseDetails"
          :on-delete-course="deleteCourse"
          :on-add-responsible-user="addResponsibleUser"
          :on-remove-responsible-user="removeResponsibleUser"
          :on-add-course-link="addCourseLink"
          :on-remove-course-link="removeCourseLink"
          :on-upload-course-files="uploadCourseFiles"
          :on-remove-course-file="removeCourseFile"
          :on-download-course-file="downloadCourseFile"
        />

        <LearningCreateCourseCard
          :is-submitting="isCreatingCourseRequest"
          :create-error="createCourseError"
          :on-create="handleCreateCourse"
        />
      </template>
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

.learning-error-banner,
.learning-empty-state {
  margin: 0;
  background-color: var(--red-cherry-20);
  border: 1px solid var(--red-cherry-40);
  color: var(--red-cherry);
  border-radius: 0.75rem;
  padding: 0.85rem 1rem;
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
