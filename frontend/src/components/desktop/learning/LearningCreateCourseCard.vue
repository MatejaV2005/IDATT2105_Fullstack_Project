<script setup lang="ts">
import Badge from '@/components/desktop/shared/Badge.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import { File, Link, Plus, Save, X } from '@lucide/vue'
import { ref } from 'vue'

interface CreateCoursePayload {
  title: string
  description: string
  links: string[]
  resources: File[]
}

const props = defineProps<{
  isSubmitting: boolean
  createError: boolean
  onCreate: (payload: CreateCoursePayload) => Promise<boolean>
}>()

const isOpen = ref(false)
const title = ref('')
const description = ref('')
const linkInput = ref('')
const links = ref<string[]>([])
const resourceFiles = ref<File[]>([])

function resetForm() {
  title.value = ''
  description.value = ''
  linkInput.value = ''
  links.value = []
  resourceFiles.value = []
}

function startCreating() {
  if (props.isSubmitting) {
    return
  }

  resetForm()
  isOpen.value = true
}

function cancelCreating() {
  if (props.isSubmitting) {
    return
  }

  resetForm()
  isOpen.value = false
}

function addLink() {
  const value = linkInput.value.trim()
  if (value.length === 0) {
    return
  }

  links.value = [...links.value, value]
  linkInput.value = ''
}

function removeLink(index: number) {
  links.value = links.value.filter((_, currentIndex) => currentIndex !== index)
}

function setResourceFiles(event: Event) {
  const input = event.target as HTMLInputElement | null
  if (!input?.files) {
    return
  }

  const selectedFiles = Array.from(input.files)
  const existingKeys = new Set(
    resourceFiles.value.map((file) => `${file.name}-${file.size}-${file.lastModified}`),
  )

  for (const file of selectedFiles) {
    const fileKey = `${file.name}-${file.size}-${file.lastModified}`
    if (!existingKeys.has(fileKey)) {
      resourceFiles.value = [...resourceFiles.value, file]
      existingKeys.add(fileKey)
    }
  }

  input.value = ''
}

function removeResourceFile(index: number) {
  resourceFiles.value = resourceFiles.value.filter((_, currentIndex) => currentIndex !== index)
}

async function createCourse() {
  if (
    props.isSubmitting ||
    title.value.trim().length === 0 ||
    description.value.trim().length === 0
  ) {
    return
  }

  const created = await props.onCreate({
    title: title.value.trim(),
    description: description.value.trim(),
    links: [...links.value],
    resources: [...resourceFiles.value],
  })

  if (created) {
    resetForm()
    isOpen.value = false
  }
}
</script>

<template>
  <DesktopButton v-if="!isOpen" :icon="Plus" content="Legg til kurs" :on-click="startCreating" />

  <div v-else class="course create-course-card">
    <h3 class="no-margin">Nytt kurs</h3>

    <label class="create-course-field">
      <span class="navy-subtitle">Tittel</span>
      <input
        v-model="title"
        class="simple-text-input course-input"
        type="text"
        placeholder="F.eks. Internkontroll grunnkurs"
        :disabled="isSubmitting"
      />
    </label>

    <label class="create-course-field">
      <span class="navy-subtitle">Beskrivelse</span>
      <textarea
        v-model="description"
        class="simple-text-input course-textarea"
        rows="4"
        placeholder="Beskriv hva kurset går ut på"
        :disabled="isSubmitting"
      />
    </label>

    <label class="create-course-field">
      <span class="navy-subtitle">Lenker</span>
      <div class="link-row">
        <input
          v-model="linkInput"
          class="simple-text-input course-input"
          type="text"
          placeholder="https://example.no/kurs"
          :disabled="isSubmitting"
        />
        <DesktopButton
          :icon="Plus"
          content="Legg til lenke"
          :on-click="addLink"
          :disabled="isSubmitting"
        />
      </div>
      <div class="resource-container">
        <Badge
          v-for="(link, index) in links"
          :key="`new-course-link-${index}-${link}`"
          badge-color="navy"
          :icon="Link"
        >
          {{ link }}
          <DesktopButton
            :icon="X"
            content="Fjern"
            button-color="boring-ghost"
            :disabled="isSubmitting"
            :on-click="() => removeLink(index)"
            class="inline-remove-button"
          />
        </Badge>
      </div>
    </label>

    <label class="create-course-field">
      <span class="navy-subtitle">Ressurser (filer)</span>
      <input type="file" multiple :disabled="isSubmitting" @change="setResourceFiles" />
      <div class="resource-container">
        <Badge
          v-for="(file, index) in resourceFiles"
          :key="`new-course-file-${file.name}-${file.lastModified}-${index}`"
          badge-color="navy"
          :icon="File"
        >
          {{ file.name }}
          <DesktopButton
            :icon="X"
            content="Fjern"
            button-color="boring-ghost"
            :disabled="isSubmitting"
            :on-click="() => removeResourceFile(index)"
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
        :is-loading="isSubmitting"
        loading-text="Oppretter"
        class="completion-action-button"
      />
      <DesktopButton
        :icon="X"
        content="Avbryt"
        button-color="cherry"
        :on-click="cancelCreating"
        :disabled="isSubmitting"
        class="completion-action-button"
      />
    </div>

    <p v-if="createError" class="completion-error">Klarte ikke å opprette kurs.</p>
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

.resource-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.inline-remove-button {
  margin-left: 0.35rem;
  padding: 0.05rem 0.45rem;
  border-radius: 0.35rem;
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

@media (max-width: 768px) {
  .course {
    padding: 0.75rem;
  }

  .completion-action-row {
    flex-direction: column;
  }
}
</style>
