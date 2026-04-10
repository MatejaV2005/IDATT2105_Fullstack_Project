<script setup lang="ts">
import Badge from '@/components/desktop/shared/Badge.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import type {
  LearningCourse,
  LearningOrganizationUser,
  LearningResource,
} from '@/interfaces/api-interfaces'
import { Download, Edit2, File, Link, Plus, Save, Trash2, UserPlus, X } from '@lucide/vue'
import { computed, ref } from 'vue'

const props = defineProps<{
  course: LearningCourse
  organizationUsers: LearningOrganizationUser[]
  onSaveCourseDetails: (
    courseId: number,
    payload: { title: string; courseDescription: string },
  ) => Promise<boolean>
  onDeleteCourse: (courseId: number) => Promise<boolean>
  onAddResponsibleUser: (courseId: number, userId: number) => Promise<boolean>
  onRemoveResponsibleUser: (courseId: number, userId: number) => Promise<boolean>
  onAddCourseLink: (courseId: number, link: string) => Promise<boolean>
  onRemoveCourseLink: (courseId: number, linkId: number) => Promise<boolean>
  onUploadCourseFiles: (courseId: number, files: File[]) => Promise<boolean>
  onRemoveCourseFile: (courseId: number, fileId: number) => Promise<boolean>
  onDownloadCourseFile: (courseId: number, fileId: number, fileName: string) => Promise<void>
}>()

const isEditing = ref(false)
const isMutating = ref(false)
const actionError = ref<string | null>(null)

const editTitle = ref('')
const editDescription = ref('')
const selectedResponsibleUserId = ref('')
const linkInput = ref('')
const pendingFiles = ref<File[]>([])

const availableResponsibleUsers = computed(() =>
  props.organizationUsers.filter(
    (user) => !props.course.responsibleUsers.some((responsible) => responsible.userId === user.id),
  ),
)

function startEditing() {
  if (isMutating.value) {
    return
  }

  editTitle.value = props.course.title
  editDescription.value = props.course.courseDescription
  selectedResponsibleUserId.value = ''
  linkInput.value = ''
  pendingFiles.value = []
  actionError.value = null
  isEditing.value = true
}

function cancelEditing() {
  if (isMutating.value) {
    return
  }

  isEditing.value = false
  actionError.value = null
  selectedResponsibleUserId.value = ''
  linkInput.value = ''
  pendingFiles.value = []
}

async function saveCourseDetails() {
  if (
    isMutating.value ||
    editTitle.value.trim().length === 0 ||
    editDescription.value.trim().length === 0
  ) {
    return
  }

  isMutating.value = true
  actionError.value = null

  try {
    const saved = await props.onSaveCourseDetails(props.course.id, {
      title: editTitle.value.trim(),
      courseDescription: editDescription.value.trim(),
    })

    if (!saved) {
      actionError.value = 'Klarte ikke å lagre kurset.'
      return
    }

    isEditing.value = false
  } finally {
    isMutating.value = false
  }
}

async function deleteCourse() {
  if (isMutating.value || !window.confirm(`Slette kurset "${props.course.title}"?`)) {
    return
  }

  isMutating.value = true
  actionError.value = null

  try {
    const deleted = await props.onDeleteCourse(props.course.id)
    if (!deleted) {
      actionError.value = 'Klarte ikke å slette kurset.'
    }
  } finally {
    isMutating.value = false
  }
}

async function addResponsibleUser() {
  const userId = Number(selectedResponsibleUserId.value)
  if (isMutating.value || !userId) {
    return
  }

  isMutating.value = true
  actionError.value = null

  try {
    const added = await props.onAddResponsibleUser(props.course.id, userId)
    if (!added) {
      actionError.value = 'Klarte ikke å legge til ansvarlig.'
      return
    }

    selectedResponsibleUserId.value = ''
  } finally {
    isMutating.value = false
  }
}

async function removeResponsibleUser(userId: number) {
  if (isMutating.value) {
    return
  }

  isMutating.value = true
  actionError.value = null

  try {
    const removed = await props.onRemoveResponsibleUser(props.course.id, userId)
    if (!removed) {
      actionError.value = 'Klarte ikke å fjerne ansvarlig.'
    }
  } finally {
    isMutating.value = false
  }
}

async function addCourseLink() {
  const nextLink = linkInput.value.trim()
  if (isMutating.value || nextLink.length === 0) {
    return
  }

  isMutating.value = true
  actionError.value = null

  try {
    const added = await props.onAddCourseLink(props.course.id, nextLink)
    if (!added) {
      actionError.value = 'Klarte ikke å legge til lenke.'
      return
    }

    linkInput.value = ''
  } finally {
    isMutating.value = false
  }
}

async function removeResource(resource: LearningResource) {
  if (isMutating.value) {
    return
  }

  isMutating.value = true
  actionError.value = null

  try {
    const removed =
      resource.type === 'link'
        ? await props.onRemoveCourseLink(props.course.id, resource.id)
        : await props.onRemoveCourseFile(props.course.id, resource.id)

    if (!removed) {
      actionError.value =
        resource.type === 'link'
          ? 'Klarte ikke å fjerne lenke.'
          : 'Klarte ikke å fjerne fil.'
    }
  } finally {
    isMutating.value = false
  }
}

function setPendingFiles(event: Event) {
  const input = event.target as HTMLInputElement | null
  if (!input?.files) {
    return
  }

  const selected = Array.from(input.files)
  const existingKeys = new Set(
    pendingFiles.value.map((file) => `${file.name}-${file.size}-${file.lastModified}`),
  )

  for (const file of selected) {
    const fileKey = `${file.name}-${file.size}-${file.lastModified}`
    if (!existingKeys.has(fileKey)) {
      pendingFiles.value = [...pendingFiles.value, file]
      existingKeys.add(fileKey)
    }
  }

  input.value = ''
}

function removePendingFile(index: number) {
  pendingFiles.value = pendingFiles.value.filter((_, currentIndex) => currentIndex !== index)
}

async function uploadPendingFiles() {
  if (isMutating.value || pendingFiles.value.length === 0) {
    return
  }

  isMutating.value = true
  actionError.value = null

  try {
    const uploaded = await props.onUploadCourseFiles(props.course.id, pendingFiles.value)
    if (!uploaded) {
      actionError.value = 'Klarte ikke å laste opp filer.'
      return
    }

    pendingFiles.value = []
  } finally {
    isMutating.value = false
  }
}

async function handleResourceClick(resource: LearningResource) {
  try {
    if (resource.type === 'link') {
      const href = /^https?:\/\//i.test(resource.name) ? resource.name : `https://${resource.name}`
      window.open(href, '_blank', 'noopener,noreferrer')
      return
    }

    await props.onDownloadCourseFile(props.course.id, resource.id, resource.name)
  } catch (err) {
    console.error(err)
    actionError.value =
      resource.type === 'link'
        ? 'Klarte ikke å åpne lenken.'
        : 'Klarte ikke å laste ned filen.'
  }
}
</script>

<template>
  <div class="course">
    <div class="course-header">
      <div class="course-heading">
        <template v-if="!isEditing">
          <h2 class="no-margin">
            {{ course.title }}
          </h2>
        </template>
        <label v-else class="course-field">
          <span class="navy-subtitle">Tittel</span>
          <input
            v-model="editTitle"
            class="simple-text-input"
            :disabled="isMutating"
            type="text"
          />
        </label>
      </div>
      <DesktopButton
        v-if="!isEditing"
        content="Rediger"
        :icon="Edit2"
        :on-click="startEditing"
      />
      <div v-else class="course-actions">
        <DesktopButton
          content="Lagre"
          :icon="Save"
          :is-loading="isMutating"
          loading-text="Lagrer"
          :on-click="saveCourseDetails"
        />
        <DesktopButton
          content="Avbryt"
          :icon="X"
          button-color="boring-ghost"
          :disabled="isMutating"
          :on-click="cancelEditing"
        />
        <DesktopButton
          content="Slett"
          :icon="Trash2"
          button-color="cherry"
          :disabled="isMutating"
          :on-click="deleteCourse"
        />
      </div>
    </div>

    <div>
      <span class="navy-subtitle">Beskrivelse:</span>
      <textarea
        v-if="isEditing"
        v-model="editDescription"
        class="simple-text-input course-textarea"
        rows="4"
        :disabled="isMutating"
      />
      <span v-else>{{ course.courseDescription }}</span>
    </div>

    <div>
      <span class="navy-subtitle">Ansvarlige:</span>
      <div class="resource-container">
        <Badge
          v-for="responsibleUser in course.responsibleUsers"
          :key="responsibleUser.userId"
          badge-color="grey"
          class="resource-badge"
        >
          {{ responsibleUser.legalName }}
          <DesktopButton
            v-if="isEditing"
            :icon="X"
            content="Fjern"
            button-color="boring-ghost"
            :disabled="isMutating"
            :on-click="() => removeResponsibleUser(responsibleUser.userId)"
            class="inline-remove-button"
          />
        </Badge>
      </div>
      <div v-if="isEditing" class="inline-editor-row">
        <select
          v-model="selectedResponsibleUserId"
          class="simple-text-input responsible-select"
          :disabled="isMutating || availableResponsibleUsers.length === 0"
        >
          <option value="">Velg ansvarlig</option>
          <option
            v-for="user in availableResponsibleUsers"
            :key="user.id"
            :value="String(user.id)"
          >
            {{ user.legalName }}
          </option>
        </select>
        <DesktopButton
          :icon="UserPlus"
          content="Legg til ansvarlig"
          :disabled="isMutating || selectedResponsibleUserId.length === 0"
          :on-click="addResponsibleUser"
        />
      </div>
    </div>

    <div>
      <span class="navy-subtitle">Ressurser:</span>
      <div class="resource-container">
        <div
          v-for="resource in course.resources"
          :key="`${resource.type}-${resource.id}`"
          class="resource-item"
        >
          <button
            class="resource-button"
            type="button"
            :disabled="isMutating"
            @click="handleResourceClick(resource)"
          >
            <Badge
              badge-color="navy"
              :icon="resource.type === 'link' ? Link : File"
              class="resource-badge"
            >
              {{ resource.name }}
              <Download v-if="resource.type === 'file'" class="resource-action-icon" />
            </Badge>
          </button>
          <DesktopButton
            v-if="isEditing"
            :icon="Trash2"
            content="Fjern"
            button-color="boring-ghost"
            :disabled="isMutating"
            :on-click="() => removeResource(resource)"
            class="inline-remove-button"
          />
        </div>
      </div>

      <div v-if="isEditing" class="resource-editor">
        <div class="inline-editor-row">
          <input
            v-model="linkInput"
            class="simple-text-input"
            type="text"
            placeholder="https://example.no/kurs"
            :disabled="isMutating"
          />
          <DesktopButton
            :icon="Plus"
            content="Legg til lenke"
            :disabled="isMutating || linkInput.trim().length === 0"
            :on-click="addCourseLink"
          />
        </div>

        <div class="file-upload-row">
          <input type="file" multiple :disabled="isMutating" @change="setPendingFiles" />
          <DesktopButton
            :icon="Plus"
            content="Last opp filer"
            :disabled="isMutating || pendingFiles.length === 0"
            :on-click="uploadPendingFiles"
          />
        </div>

        <div v-if="pendingFiles.length > 0" class="resource-container">
          <Badge
            v-for="(file, index) in pendingFiles"
            :key="`${file.name}-${file.lastModified}-${index}`"
            badge-color="grey"
            :icon="File"
            class="resource-badge"
          >
            {{ file.name }}
            <DesktopButton
              :icon="X"
              content="Fjern"
              button-color="boring-ghost"
              :disabled="isMutating"
              :on-click="() => removePendingFile(index)"
              class="inline-remove-button"
            />
          </Badge>
        </div>
      </div>
    </div>

    <p v-if="actionError" class="course-error">
      {{ actionError }}
    </p>
  </div>
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
}

.course-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: flex-start;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--blue-navy-40);
}

.course-heading {
  flex: 1;
  min-width: 0;
}

.course-field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.course-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.course-textarea {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  resize: vertical;
}

.resource-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  margin-top: 0.5rem;
}

.resource-item {
  display: flex;
  gap: 0.35rem;
  align-items: center;
}

.resource-button {
  border: 0;
  padding: 0;
  background: transparent;
  cursor: pointer;
}

.resource-button:disabled {
  cursor: default;
}

.resource-badge {
  max-width: 100%;
  overflow-wrap: anywhere;
}

.resource-action-icon {
  width: 0.95rem;
  height: 0.95rem;
}

.inline-editor-row,
.file-upload-row {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  margin-top: 0.75rem;
  flex-wrap: wrap;
}

.resource-editor {
  margin-top: 0.5rem;
}

.responsible-select {
  min-width: 14rem;
}

.inline-remove-button {
  margin-left: 0.25rem;
}

.course-error {
  margin: 0;
  color: var(--red-cherry);
}

@media (max-width: 768px) {
  .course {
    padding: 0.75rem;
  }

  .course-header {
    flex-direction: column;
  }

  .course-actions {
    width: 100%;
  }
}
</style>
