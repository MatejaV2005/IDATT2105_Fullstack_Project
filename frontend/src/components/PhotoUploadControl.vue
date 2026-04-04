<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'

import type { UploadedPhoto } from '@/types/uploads'

import AppIcon from './AppIcon.vue'

const props = withDefaults(
  defineProps<{
    label: string
    description?: string
    helperText?: string
    variant?: 'icon' | 'panel'
    multiple?: boolean
    maxFiles?: number
  }>(),
  {
    description: '',
    helperText: '',
    variant: 'icon',
    multiple: false,
    maxFiles: 1,
  },
)

const photos = defineModel<UploadedPhoto[]>({ default: [] })

const showOptions = ref(false)
const libraryInput = ref<HTMLInputElement | null>(null)
const cameraInput = ref<HTMLInputElement | null>(null)
const triggerButton = ref<HTMLButtonElement | null>(null)
const menuElement = ref<HTMLDivElement | null>(null)
const menuStyle = ref<Record<string, string>>({})

const previewLabel = computed(() => {
  if (!photos.value.length) {
    return props.multiple ? 'Ingen bilder valgt ennå' : 'Ingen bildevedlegg ennå'
  }

  return props.multiple ? `${photos.value.length} bilder lastet opp` : `${photos.value[0]?.name ?? '1 bilde'}`
})

function revokeEntries(entries: UploadedPhoto[]) {
  for (const entry of entries) {
    URL.revokeObjectURL(entry.previewUrl)
  }
}

function updatePhotos(nextPhotos: UploadedPhoto[]) {
  const previousPhotos = photos.value
  const nextUrls = new Set(nextPhotos.map((photo) => photo.previewUrl))
  const removedPhotos = previousPhotos.filter((photo) => !nextUrls.has(photo.previewUrl))

  revokeEntries(removedPhotos)
  photos.value = nextPhotos
}

function createPhotoEntries(fileList: FileList | null) {
  if (!fileList) {
    return
  }

  const created = Array.from(fileList)
    .filter((file) => file.type.startsWith('image/'))
    .map((file) => ({
      id: `${file.name}-${file.lastModified}-${Math.random().toString(36).slice(2, 8)}`,
      name: file.name,
      previewUrl: URL.createObjectURL(file),
      file,
    }))

  if (!created.length) {
    return
  }

  const mergedPhotos = props.multiple
    ? [...photos.value, ...created].slice(0, props.maxFiles)
    : created.slice(0, 1)

  const discardedPhotos = props.multiple
    ? created.slice(Math.max(0, props.maxFiles - photos.value.length))
    : created.slice(1)

  revokeEntries(discardedPhotos)
  updatePhotos(mergedPhotos)
}

function onLibraryChange(event: Event) {
  const target = event.target as HTMLInputElement
  createPhotoEntries(target.files)
  target.value = ''
}

function onCameraChange(event: Event) {
  const target = event.target as HTMLInputElement
  createPhotoEntries(target.files)
  target.value = ''
}

function openLibrary() {
  showOptions.value = false
  libraryInput.value?.click()
}

function openCamera() {
  showOptions.value = false
  cameraInput.value?.click()
}

function removePhoto(photoId: string) {
  updatePhotos(photos.value.filter((photo) => photo.id !== photoId))
}

async function positionMenu() {
  if (!triggerButton.value) {
    return
  }

  const rect = triggerButton.value.getBoundingClientRect()

  await nextTick()

  const menuWidth = menuElement.value?.offsetWidth ?? 176
  const menuHeight = menuElement.value?.offsetHeight ?? 108
  const viewportPadding = 12
  const gap = 8

  let left = rect.right - menuWidth
  left = Math.min(left, window.innerWidth - menuWidth - viewportPadding)
  left = Math.max(left, viewportPadding)

  const shouldOpenAbove = rect.bottom + gap + menuHeight > window.innerHeight - viewportPadding
  const top = shouldOpenAbove
    ? Math.max(viewportPadding, rect.top - menuHeight - gap)
    : rect.bottom + gap

  menuStyle.value = {
    left: `${left}px`,
    top: `${top}px`,
  }
}

async function toggleOptions() {
  showOptions.value = !showOptions.value

  if (showOptions.value) {
    await positionMenu()
  }
}

function onDocumentPointerDown(event: PointerEvent) {
  const target = event.target as Node | null

  if (!showOptions.value) {
    return
  }

  if (triggerButton.value?.contains(target) || menuElement.value?.contains(target)) {
    return
  }

  showOptions.value = false
}

function onWindowChange() {
  if (showOptions.value) {
    void positionMenu()
  }
}

function onEscape(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    showOptions.value = false
  }
}

onMounted(() => {
  document.addEventListener('pointerdown', onDocumentPointerDown)
  window.addEventListener('resize', onWindowChange)
  window.addEventListener('scroll', onWindowChange, true)
  window.addEventListener('keydown', onEscape)
})

onBeforeUnmount(() => {
  revokeEntries(photos.value)
  document.removeEventListener('pointerdown', onDocumentPointerDown)
  window.removeEventListener('resize', onWindowChange)
  window.removeEventListener('scroll', onWindowChange, true)
  window.removeEventListener('keydown', onEscape)
})
</script>

<template>
  <div
    class="photo-upload"
    :class="[`photo-upload--${variant}`]"
  >
    <div class="photo-upload__trigger-wrap">
      <button
        ref="triggerButton"
        class="card-icon-button photo-upload__trigger"
        type="button"
        :aria-label="label"
        @click="toggleOptions"
      >
        <AppIcon name="camera" />
      </button>
    </div>

    <Teleport to="body">
      <div
        v-if="showOptions"
        ref="menuElement"
        class="photo-upload__menu"
        :style="menuStyle"
      >
        <button
          class="photo-upload__option"
          type="button"
          @click="openCamera"
        >
          Ta bilde
        </button>
        <button
          class="photo-upload__option"
          type="button"
          @click="openLibrary"
        >
          Velg fra album
        </button>
      </div>
    </Teleport>

    <div
      v-if="variant === 'panel'"
      class="photo-upload__copy"
    >
      <p class="card-title upload-card__title">
        {{ label }}
      </p>
      <p
        v-if="description"
        class="muted-copy"
      >
        {{ description }}
      </p>
    </div>

    <p
      v-if="variant === 'icon'"
      class="photo-upload__status"
    >
      {{ previewLabel }}
    </p>

    <div
      v-if="photos.length"
      class="photo-upload__preview-grid"
      :class="{ 'photo-upload__preview-grid--compact': variant === 'icon' }"
    >
      <figure
        v-for="photo in photos"
        :key="photo.id"
        class="photo-upload__preview"
      >
        <img
          :src="photo.previewUrl"
          :alt="photo.name"
          class="photo-upload__image"
        >
        <button
          class="photo-upload__remove"
          type="button"
          :aria-label="`Fjern ${photo.name}`"
          @click="removePhoto(photo.id)"
        >
          ×
        </button>
      </figure>
    </div>

    <p
      v-if="variant === 'panel'"
      class="helper-text"
    >
      {{ helperText || previewLabel }}
    </p>

    <input
      ref="libraryInput"
      class="visually-hidden"
      type="file"
      accept="image/*"
      :multiple="multiple"
      @change="onLibraryChange"
    >
    <input
      ref="cameraInput"
      class="visually-hidden"
      type="file"
      accept="image/*"
      capture="environment"
      :multiple="multiple"
      @change="onCameraChange"
    >
  </div>
</template>
