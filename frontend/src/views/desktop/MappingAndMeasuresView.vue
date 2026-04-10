<script setup lang="ts">
import api from '@/api/api'
import MappingPointCard from '@/components/desktop/alcohol/MappingPointCard.vue'
import MappingPointCreateCard from '@/components/desktop/alcohol/MappingPointCreateCard.vue'
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import { useOrgSession } from '@/composables/useOrgSession'
import type { MappingPoint, MappingPointAllInfo } from '@/interfaces/api-interfaces'
import { Plus } from '@lucide/vue'
import { ref, watch } from 'vue'

const resource = ref<MappingPointAllInfo>([])
const loading = ref(true)
const error = ref<string | null>(null)

const { claims } = useOrgSession()
let activeFetchId = 0

const isCreating = ref(false)
const isCreatingPoint = ref(false)
const createError = ref(false)

const savingPointId = ref<number | null>(null)
const deletingPointId = ref<number | null>(null)
const saveErrorPointId = ref<number | null>(null)
const deleteErrorPointId = ref<number | null>(null)

function getErrorMessage(err: unknown, fallback: string) {
  const status = (err as { response?: { status?: number } }).response?.status

  if (status === 403) {
    return 'Du har ikke tilgang til kartlegging og tiltak. Kun owner og manager kan endre punkter.'
  }

  return fallback
}

async function fetchMappingPoints() {
  const fetchId = ++activeFetchId
  loading.value = true
  error.value = null

  try {
    const response = await api.get<MappingPointAllInfo>('/mapping-points')
    if (fetchId !== activeFetchId) {
      return
    }

    resource.value = response.data
  } catch (err) {
    if (fetchId === activeFetchId) {
      resource.value = []
      error.value = getErrorMessage(err, 'Klarte ikke å hente kartleggingspunkter.')
      console.error(err)
    }
  } finally {
    if (fetchId === activeFetchId) {
      loading.value = false
    }
  }
}

watch(() => claims.value?.orgId ?? null, () => {
  void fetchMappingPoints()
}, { immediate: true })

function startCreating() {
  if (isCreatingPoint.value || savingPointId.value !== null || deletingPointId.value !== null) {
    return
  }
  isCreating.value = true
  createError.value = false
}

function cancelCreating() {
  if (isCreatingPoint.value) {
    return
  }
  isCreating.value = false
  createError.value = false
}

async function createPoint(payload: Omit<MappingPoint, 'id'>) {
  if (isCreatingPoint.value) {
    return
  }

  isCreatingPoint.value = true
  createError.value = false

  try {
    const response = await api.post<MappingPoint>('/mapping-points', {
      ...payload,
      dots: Math.max(0, Math.trunc(payload.dots)),
    })

    resource.value = [...resource.value, response.data]
    isCreating.value = false
  } catch (err) {
    console.error(err)
    createError.value = true
  } finally {
    isCreatingPoint.value = false
  }
}

async function savePoint(payload: MappingPoint) {
  if (savingPointId.value !== null || deletingPointId.value !== null) {
    return
  }

  savingPointId.value = payload.id
  saveErrorPointId.value = null
  deleteErrorPointId.value = null

  try {
    const response = await api.patch<MappingPoint>(`/mapping-points/${payload.id}`, {
      law: payload.law,
      dots: Math.max(0, Math.trunc(payload.dots)),
      title: payload.title,
      challenges: payload.challenges,
      measures: payload.measures,
      responsibleText: payload.responsibleText,
    })

    resource.value = resource.value.map((point) =>
      point.id === payload.id ? response.data : point,
    )
  } catch (err) {
    console.error(err)
    saveErrorPointId.value = payload.id
  } finally {
    savingPointId.value = null
  }
}

async function deletePoint(payload: MappingPoint) {
  if (savingPointId.value !== null || deletingPointId.value !== null) {
    return
  }

  deletingPointId.value = payload.id
  deleteErrorPointId.value = null
  saveErrorPointId.value = null

  try {
    await api.delete(`/mapping-points/${payload.id}`)
    resource.value = resource.value.filter((point) => point.id !== payload.id)
  } catch (err) {
    console.error(err)
    deleteErrorPointId.value = payload.id
  } finally {
    deletingPointId.value = null
  }
}
</script>

<template>
  <SidebarPageContainer active-page="/desktop/ik-alkohol-kartlegging-og-tiltak">
    <div class="mapping-area-container">
      <div class="header-row">
        <h1 class="instrument-serif-regular no-margin">
          Kartlegging & tiltak
        </h1>
        <DesktopButton
          v-if="!isCreating"
          content="Nytt punkt"
          :icon="Plus"
          :on-click="startCreating"
        />
      </div>

      <Loading v-if="loading" />
      <p
        v-else-if="error"
        class="error-message"
      >
        {{ error }}
      </p>

      <template v-else>
        <MappingPointCreateCard
          v-if="isCreating"
          :is-creating="isCreatingPoint"
          :create-error="createError"
          @cancel="cancelCreating"
          @create="createPoint"
        />

        <MappingPointCard
          v-for="point in resource"
          :key="point.id"
          :point="point"
          :is-saving="savingPointId === point.id"
          :is-deleting="deletingPointId === point.id"
          :save-error="saveErrorPointId === point.id"
          :delete-error="deleteErrorPointId === point.id"
          @save="savePoint"
          @delete-point="deletePoint"
        />
      </template>
    </div>
  </SidebarPageContainer>
</template>

<style scoped>
.mapping-area-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border-radius: 1rem;
  padding: 1rem;
  margin-top: 2rem;
}

.header-row {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
}

.error-message {
  color: #b42318;
  margin: 0;
}

@media (max-width: 1200px) {
  .mapping-area-container {
    margin-top: 1rem;
    padding: 0.5rem;
  }
}

@media (max-width: 768px) {
  .mapping-area-container {
    padding: 0;
  }

  .header-row {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
