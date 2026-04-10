<script setup lang="ts">
import Paginator from '@/components/desktop/shared/Paginator.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import type { DangerAnalysisAllInfo, DangerRiskCombo } from '@/interfaces/api-interfaces'
import { delay } from '@/utils'
import { Edit2, Plus, Save, Trash2, Upload, X } from '@lucide/vue'
import { onBeforeUnmount, onMounted, ref } from 'vue'

type CreateDangerRiskComboPayload = {
  productCategoryId: number
  danger: string
  dangerCorrectiveMeasure: string
  severityScore: number
  likelihoodScore: number
}

type UpdateDangerRiskComboPayload = {
  productCategoryId: number
  dangerRiskComboId: number
  danger: string
  dangerCorrectiveMeasure: string
  severityScore: number
  likelihoodScore: number
}

type CreateProductCategoryPayload = {
  productName: string
  productDescription: string
}

type UpdateProductCategoryPayload = {
  categoryId: number
  productName: string
  productDescription: string
}

const mockData: DangerAnalysisAllInfo = [
  {
    id: 1,
    productName: 'Kyllingsalat',
    productDescription: 'Salat med varmebehandlet kylling, dressing og grønnsaker.',
    flowchartFileId: 101,
    flowchartFileName: 'kyllingsalat-flyt.jpg',
    flowchartPreviewUrl: null,
    createdAt: '2026-03-05T10:12:00',
    dangerRiskCombos: [
      {
        id: 1,
        danger: 'Utilstrekkelig nedkjoling etter varmebehandling.',
        dangerCorrectiveMeasure:
          'Nedkjol i mindre beholdere, bruk blastchiller og verifiser tid/temperatur.',
        severityScore: 3,
        likelihoodScore: 3,
        createdAt: '2026-03-05T10:20:00',
      },
      {
        id: 2,
        danger: 'Krysskontaminering mellom radig salat og ra kylling.',
        dangerCorrectiveMeasure:
          'Skille soner, egne skjarebrett, tydelig merking og opplaering av ansatte.',
        severityScore: 3,
        likelihoodScore: 2,
        createdAt: '2026-03-05T10:24:00',
      },
    ],
  },
  {
    id: 2,
    productName: 'Husets burger',
    productDescription: 'Burger med storfekjott, briochebrod, ost og tilbehor.',
    flowchartFileId: null,
    flowchartFileName: null,
    flowchartPreviewUrl: null,
    createdAt: '2026-03-06T09:00:00',
    dangerRiskCombos: [
      {
        id: 3,
        danger: 'For lav kjernetemperatur ved steking.',
        dangerCorrectiveMeasure:
          'Innfør krav om temperaturmaling for hver batch og dokumenter i sjekkliste.',
        severityScore: 3,
        likelihoodScore: 2,
        createdAt: '2026-03-06T09:15:00',
      },
    ],
  },
]

const mockServerState = ref<DangerAnalysisAllInfo>(cloneData(mockData))

const resource = ref<DangerAnalysisAllInfo>([])
const loading = ref(true)
const error = ref(false)

const isCreatingCategory = ref(false)
const newCategoryName = ref('')
const newCategoryDescription = ref('')
const isCreatingCategoryRequest = ref(false)
const createCategoryError = ref(false)

const editingCategoryId = ref<number | null>(null)
const editCategoryName = ref('')
const editCategoryDescription = ref('')
const isSavingCategory = ref(false)
const saveCategoryErrorId = ref<number | null>(null)

const creatingForCategoryId = ref<number | null>(null)
const isCreatingCombo = ref(false)
const createComboError = ref(false)

const editingComboKey = ref<string | null>(null)
const isSavingCombo = ref(false)
const saveComboErrorKey = ref<string | null>(null)

const deletingComboKey = ref<string | null>(null)
const deleteComboErrorKey = ref<string | null>(null)

const uploadingFlowchartCategoryId = ref<number | null>(null)
const uploadFlowchartErrorCategoryId = ref<number | null>(null)
const pendingFlowchartFiles = ref<Record<number, File | null>>({})

const formDanger = ref('')
const formCorrectiveMeasure = ref('')
const formSeverityScore = ref(3)
const formLikelihoodScore = ref(3)

function cloneData(data: DangerAnalysisAllInfo): DangerAnalysisAllInfo {
  return data.map((category) => ({
    ...category,
    dangerRiskCombos: category.dangerRiskCombos.map((combo) => ({ ...combo })),
  }))
}

function comboKey(categoryId: number, comboId: number) {
  return `${categoryId}-${comboId}`
}

function scoreLabel(combo: DangerRiskCombo) {
  return combo.severityScore * combo.likelihoodScore
}

function resetForm() {
  formDanger.value = ''
  formCorrectiveMeasure.value = ''
  formSeverityScore.value = 3
  formLikelihoodScore.value = 3
}

async function fetchDangerAnalysis() {
  // const response = await fetch('/api/product-categories/danger-analysis')
  // if (!response.ok) {
  //   throw new Error(`Failed to fetch danger analysis (${response.status})`)
  // }
  // const data: DangerAnalysisAllInfo = await response.json()
  await delay(700)
  resource.value = cloneData(mockServerState.value)
}

async function mockCreateDangerRiskCombo(payload: CreateDangerRiskComboPayload) {
  // const response = await fetch('/api/danger-risk-combos', {
  //   method: 'POST',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify(payload),
  // })
  // if (!response.ok) {
  //   throw new Error(`Failed to create danger/risk combo (${response.status})`)
  // }
  await delay(700)

  const category = mockServerState.value.find((entry) => entry.id === payload.productCategoryId)
  if (!category) {
    throw new Error('Product category not found')
  }

  const nextId =
    mockServerState.value
      .flatMap((entry) => entry.dangerRiskCombos)
      .reduce((maxId, combo) => Math.max(maxId, combo.id), 0) + 1

  category.dangerRiskCombos = [
    ...category.dangerRiskCombos,
    {
      id: nextId,
      danger: payload.danger,
      dangerCorrectiveMeasure: payload.dangerCorrectiveMeasure,
      severityScore: payload.severityScore,
      likelihoodScore: payload.likelihoodScore,
      createdAt: new Date().toISOString(),
    },
  ]
}

async function mockCreateProductCategory(payload: CreateProductCategoryPayload) {
  // const response = await fetch('/api/product-categories', {
  //   method: 'POST',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify(payload),
  // })
  // if (!response.ok) {
  //   throw new Error(`Failed to create product category (${response.status})`)
  // }
  await delay(700)

  const nextId =
    mockServerState.value.length === 0
      ? 1
      : Math.max(...mockServerState.value.map((category) => category.id)) + 1

  mockServerState.value = [
    ...mockServerState.value,
    {
      id: nextId,
      productName: payload.productName,
      productDescription: payload.productDescription,
      flowchartFileId: null,
      flowchartFileName: null,
      flowchartPreviewUrl: null,
      createdAt: new Date().toISOString(),
      dangerRiskCombos: [],
    },
  ]
}

async function mockUpdateProductCategory(payload: UpdateProductCategoryPayload) {
  // const response = await fetch('/api/product-categories', {
  //   method: 'PATCH',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify(payload),
  // })
  // if (!response.ok) {
  //   throw new Error(`Failed to update product category (${response.status})`)
  // }
  await delay(700)

  const categoryIndex = mockServerState.value.findIndex((entry) => entry.id === payload.categoryId)
  if (categoryIndex === -1) {
    throw new Error('Product category not found')
  }

  const currentCategory = mockServerState.value[categoryIndex]
  if (!currentCategory) {
    throw new Error('Product category not found')
  }

  mockServerState.value[categoryIndex] = {
    ...currentCategory,
    productName: payload.productName,
    productDescription: payload.productDescription,
  }
}

async function mockUpdateDangerRiskCombo(payload: UpdateDangerRiskComboPayload) {
  // const response = await fetch('/api/danger-risk-combos', {
  //   method: 'PATCH',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify(payload),
  // })
  // if (!response.ok) {
  //   throw new Error(`Failed to update danger/risk combo (${response.status})`)
  // }
  await delay(700)

  const category = mockServerState.value.find((entry) => entry.id === payload.productCategoryId)
  if (!category) {
    throw new Error('Product category not found')
  }

  const comboIndex = category.dangerRiskCombos.findIndex(
    (combo) => combo.id === payload.dangerRiskComboId,
  )
  if (comboIndex === -1) {
    throw new Error('Danger/risk combo not found')
  }

  const currentCombo = category.dangerRiskCombos[comboIndex]
  if (!currentCombo) {
    throw new Error('Danger/risk combo not found')
  }

  category.dangerRiskCombos[comboIndex] = {
    ...currentCombo,
    danger: payload.danger,
    dangerCorrectiveMeasure: payload.dangerCorrectiveMeasure,
    severityScore: payload.severityScore,
    likelihoodScore: payload.likelihoodScore,
  }
}

async function mockDeleteDangerRiskCombo(categoryId: number, dangerRiskComboId: number) {
  // const response = await fetch('/api/danger-risk-combos', {
  //   method: 'DELETE',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify({ dangerRiskComboId }),
  // })
  // if (!response.ok) {
  //   throw new Error(`Failed to delete danger/risk combo (${response.status})`)
  // }
  await delay(500)

  const category = mockServerState.value.find((entry) => entry.id === categoryId)
  if (!category) {
    throw new Error('Product category not found')
  }

  category.dangerRiskCombos = category.dangerRiskCombos.filter(
    (combo) => combo.id !== dangerRiskComboId,
  )
}

async function mockUploadFlowchart(categoryId: number, file: File) {
  const formData = new FormData()
  formData.append('file', file)

  // const response = await fetch(`/api/product-categories/${categoryId}/flowchart`, {
  //   method: 'POST',
  //   body: formData,
  // })
  // if (!response.ok) {
  //   throw new Error(`Failed to upload flowchart (${response.status})`)
  // }
  await delay(900)

  const category = mockServerState.value.find((entry) => entry.id === categoryId)
  if (!category) {
    throw new Error('Product category not found')
  }

  if (category.flowchartPreviewUrl?.startsWith('blob:')) {
    URL.revokeObjectURL(category.flowchartPreviewUrl)
  }

  const nextFileId =
    mockServerState.value
      .map((entry) => entry.flowchartFileId ?? 0)
      .reduce((maxId, fileId) => Math.max(maxId, fileId), 0) + 1

  category.flowchartFileId = nextFileId
  category.flowchartFileName = file.name
  category.flowchartPreviewUrl = URL.createObjectURL(file)
}

function cleanupBlobUrls() {
  for (const category of mockServerState.value) {
    if (category.flowchartPreviewUrl?.startsWith('blob:')) {
      URL.revokeObjectURL(category.flowchartPreviewUrl)
    }
  }
}

function setPendingFlowchartFile(categoryId: number, event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0] ?? null

  const allowedTypes = new Set(['image/jpeg', 'image/jpg', 'image/png'])
  if (file && !allowedTypes.has(file.type)) {
    pendingFlowchartFiles.value[categoryId] = null
    uploadFlowchartErrorCategoryId.value = categoryId
    input.value = ''
    return
  }

  pendingFlowchartFiles.value[categoryId] = file
  if (uploadFlowchartErrorCategoryId.value === categoryId) {
    uploadFlowchartErrorCategoryId.value = null
  }
  input.value = ''
}

function startCreateCombo(categoryId: number) {
  if (isSavingCombo.value || isCreatingCombo.value || deletingComboKey.value !== null) {
    return
  }

  editingComboKey.value = null
  saveComboErrorKey.value = null
  createComboError.value = false
  creatingForCategoryId.value = categoryId
  resetForm()
}

function resetCreateCategoryForm() {
  newCategoryName.value = ''
  newCategoryDescription.value = ''
}

function startCreatingCategory() {
  if (isCreatingCategoryRequest.value) {
    return
  }

  createCategoryError.value = false
  resetCreateCategoryForm()
  isCreatingCategory.value = true
}

function startEditingCategory(category: {
  id: number
  productName: string
  productDescription: string
}) {
  if (isSavingCategory.value || isCreatingCategoryRequest.value) {
    return
  }

  saveCategoryErrorId.value = null
  editingCategoryId.value = category.id
  editCategoryName.value = category.productName
  editCategoryDescription.value = category.productDescription
}

function cancelEditingCategory() {
  if (isSavingCategory.value) {
    return
  }

  editingCategoryId.value = null
  editCategoryName.value = ''
  editCategoryDescription.value = ''
  saveCategoryErrorId.value = null
}

function resetEditingCategoryState() {
  editingCategoryId.value = null
  editCategoryName.value = ''
  editCategoryDescription.value = ''
  saveCategoryErrorId.value = null
}

async function saveCategory(categoryId: number) {
  if (
    isSavingCategory.value ||
    editingCategoryId.value !== categoryId ||
    editCategoryName.value.trim().length === 0 ||
    editCategoryDescription.value.trim().length === 0
  ) {
    return
  }

  isSavingCategory.value = true
  saveCategoryErrorId.value = null

  try {
    await mockUpdateProductCategory({
      categoryId,
      productName: editCategoryName.value.trim(),
      productDescription: editCategoryDescription.value.trim(),
    })
    await fetchDangerAnalysis()
    resetEditingCategoryState()
  } catch {
    saveCategoryErrorId.value = categoryId
  } finally {
    isSavingCategory.value = false
  }
}

function cancelCreatingCategory() {
  if (isCreatingCategoryRequest.value) {
    return
  }

  isCreatingCategory.value = false
  createCategoryError.value = false
  resetCreateCategoryForm()
}

async function createCategory() {
  if (
    isCreatingCategoryRequest.value ||
    newCategoryName.value.trim().length === 0 ||
    newCategoryDescription.value.trim().length === 0
  ) {
    return
  }

  isCreatingCategoryRequest.value = true
  createCategoryError.value = false

  try {
    await mockCreateProductCategory({
      productName: newCategoryName.value.trim(),
      productDescription: newCategoryDescription.value.trim(),
    })
    await fetchDangerAnalysis()
    isCreatingCategory.value = false
    resetCreateCategoryForm()
  } catch {
    createCategoryError.value = true
  } finally {
    isCreatingCategoryRequest.value = false
  }
}

function cancelCreateCombo() {
  if (isCreatingCombo.value) {
    return
  }

  creatingForCategoryId.value = null
  createComboError.value = false
  resetForm()
}

async function createCombo() {
  if (
    creatingForCategoryId.value === null ||
    isCreatingCombo.value ||
    formDanger.value.trim().length === 0 ||
    formCorrectiveMeasure.value.trim().length === 0
  ) {
    return
  }

  isCreatingCombo.value = true
  createComboError.value = false

  try {
    await mockCreateDangerRiskCombo({
      productCategoryId: creatingForCategoryId.value,
      danger: formDanger.value.trim(),
      dangerCorrectiveMeasure: formCorrectiveMeasure.value.trim(),
      severityScore: Math.max(1, Math.min(3, formSeverityScore.value)),
      likelihoodScore: Math.max(1, Math.min(3, formLikelihoodScore.value)),
    })
    await fetchDangerAnalysis()
    creatingForCategoryId.value = null
    resetForm()
  } catch {
    createComboError.value = true
  } finally {
    isCreatingCombo.value = false
  }
}

function startEditCombo(categoryId: number, combo: DangerRiskCombo) {
  if (isSavingCombo.value || isCreatingCombo.value || deletingComboKey.value !== null) {
    return
  }

  creatingForCategoryId.value = null
  createComboError.value = false
  editingComboKey.value = comboKey(categoryId, combo.id)
  saveComboErrorKey.value = null
  formDanger.value = combo.danger
  formCorrectiveMeasure.value = combo.dangerCorrectiveMeasure
  formSeverityScore.value = combo.severityScore
  formLikelihoodScore.value = combo.likelihoodScore
}

function cancelEditCombo() {
  if (isSavingCombo.value) {
    return
  }

  editingComboKey.value = null
  saveComboErrorKey.value = null
  resetForm()
}

async function saveCombo(categoryId: number, comboId: number) {
  const key = comboKey(categoryId, comboId)

  if (
    editingComboKey.value !== key ||
    isSavingCombo.value ||
    formDanger.value.trim().length === 0 ||
    formCorrectiveMeasure.value.trim().length === 0
  ) {
    return
  }

  isSavingCombo.value = true
  saveComboErrorKey.value = null

  try {
    await mockUpdateDangerRiskCombo({
      productCategoryId: categoryId,
      dangerRiskComboId: comboId,
      danger: formDanger.value.trim(),
      dangerCorrectiveMeasure: formCorrectiveMeasure.value.trim(),
      severityScore: Math.max(1, Math.min(3, formSeverityScore.value)),
      likelihoodScore: Math.max(1, Math.min(3, formLikelihoodScore.value)),
    })
    await fetchDangerAnalysis()
    cancelEditCombo()
  } catch {
    saveComboErrorKey.value = key
  } finally {
    isSavingCombo.value = false
  }
}

async function deleteCombo(categoryId: number, comboId: number) {
  if (isSavingCombo.value || isCreatingCombo.value || deletingComboKey.value !== null) {
    return
  }

  const shouldDelete = confirm('Sikker på at du vil slette farepunktet?')
  if (!shouldDelete) {
    return
  }

  const key = comboKey(categoryId, comboId)
  deletingComboKey.value = key
  deleteComboErrorKey.value = null

  try {
    await mockDeleteDangerRiskCombo(categoryId, comboId)
    await fetchDangerAnalysis()
    if (editingComboKey.value === key) {
      cancelEditCombo()
    }
  } catch {
    deleteComboErrorKey.value = key
  } finally {
    deletingComboKey.value = null
  }
}

async function uploadFlowchart(categoryId: number) {
  const file = pendingFlowchartFiles.value[categoryId]
  if (!file || uploadingFlowchartCategoryId.value !== null) {
    return
  }

  uploadingFlowchartCategoryId.value = categoryId
  uploadFlowchartErrorCategoryId.value = null

  try {
    await mockUploadFlowchart(categoryId, file)
    pendingFlowchartFiles.value[categoryId] = null
    await fetchDangerAnalysis()
  } catch {
    uploadFlowchartErrorCategoryId.value = categoryId
  } finally {
    uploadingFlowchartCategoryId.value = null
  }
}

onMounted(async () => {
  try {
    await fetchDangerAnalysis()
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

onBeforeUnmount(() => {
  cleanupBlobUrls()
})
</script>

<template>
  <div class="con">
    <main>
      <div class="main-no-sidebar-container">
        <Paginator />
        <h1 class="instrument-serif-regular no-margin">Fareanalyse</h1>
        <hr class="navy-hr" />

        <DesktopButton
          v-if="!loading && !isCreatingCategory"
          :icon="Plus"
          content="produktkategori"
          :on-click="startCreatingCategory"
          :disabled="isCreatingCategoryRequest"
        />

        <div v-if="!loading && isCreatingCategory" class="create-category-card">
          <h2 class="no-margin">Ny produktkategori</h2>
          <label class="create-category-field">
            <span class="navy-subtitle">Produktnavn</span>
            <input
              v-model="newCategoryName"
              class="simple-text-input"
              :disabled="isCreatingCategoryRequest"
              type="text"
              placeholder="F.eks. Fiskekaker"
            />
          </label>
          <label class="create-category-field">
            <span class="navy-subtitle">Produktbeskrivelse</span>
            <textarea
              v-model="newCategoryDescription"
              class="simple-text-input"
              :disabled="isCreatingCategoryRequest"
              rows="3"
              placeholder="Beskriv produktet og prosessen kort"
            />
          </label>
          <div class="combo-actions">
            <DesktopButton
              :icon="Save"
              content="Opprett"
              :on-click="createCategory"
              :is-loading="isCreatingCategoryRequest"
              loading-text="Oppretter"
              :disabled="
                newCategoryName.trim().length === 0 || newCategoryDescription.trim().length === 0
              "
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
            Klarte ikke å opprette produktkategori.
          </p>
        </div>

        <Loading v-if="loading" />

        <p v-else-if="error" class="error-message no-margin">Klarte ikke å hente fareanalysen.</p>

        <div
          v-for="category in resource"
          :key="`danger-category-${category.id}`"
          class="danger-category-card"
        >
          <div class="danger-category-header">
            <div class="danger-category-copy">
              <template v-if="editingCategoryId !== category.id">
                <h2 class="no-margin">{{ category.productName }}</h2>
                <p class="danger-category-description no-margin">
                  {{ category.productDescription }}
                </p>
              </template>
              <template v-else>
                <label class="create-category-field">
                  <span class="navy-subtitle">Produktnavn</span>
                  <input
                    v-model="editCategoryName"
                    type="text"
                    class="simple-text-input"
                    :disabled="isSavingCategory"
                  />
                </label>
                <label class="create-category-field">
                  <span class="navy-subtitle">Produktbeskrivelse</span>
                  <textarea
                    v-model="editCategoryDescription"
                    class="simple-text-input"
                    rows="3"
                    :disabled="isSavingCategory"
                  />
                </label>
              </template>
            </div>
            <div class="combo-actions">
              <DesktopButton
                v-if="editingCategoryId !== category.id"
                :icon="Edit2"
                content="Rediger kategori"
                :disabled="isSavingCategory || isCreatingCategoryRequest"
                :on-click="() => startEditingCategory(category)"
              />
              <template v-else>
                <DesktopButton
                  :icon="Save"
                  content="Lagre"
                  :is-loading="isSavingCategory"
                  loading-text="Lagrer"
                  :disabled="
                    editCategoryName.trim().length === 0 ||
                    editCategoryDescription.trim().length === 0
                  "
                  :on-click="() => saveCategory(category.id)"
                />
                <DesktopButton
                  :icon="X"
                  content="Avbryt"
                  button-color="boring-ghost"
                  :disabled="isSavingCategory"
                  :on-click="cancelEditingCategory"
                />
              </template>
            </div>
          </div>
          <p v-if="saveCategoryErrorId === category.id" class="error-message no-margin">
            Klarte ikke å lagre produktkategorien.
          </p>

          <div class="flowchart-box">
            <span class="navy-subtitle">Flytdiagram</span>
            <span class="flowchart-current">
              {{
                category.flowchartFileName
                  ? `Navaerende fil: ${category.flowchartFileName}`
                  : 'Ingen flytdiagram lastet opp ennå.'
              }}
            </span>
            <div v-if="category.flowchartPreviewUrl" class="flowchart-preview">
              <a
                :href="category.flowchartPreviewUrl"
                target="_blank"
                rel="noopener noreferrer"
                class="flowchart-link"
              >
                Vis storre bilde
              </a>
              <img
                :src="category.flowchartPreviewUrl"
                :alt="`Flytdiagram for ${category.productName}`"
                class="flowchart-image"
              />
            </div>
            <div class="flowchart-actions">
              <input
                type="file"
                class="simple-text-input"
                :disabled="uploadingFlowchartCategoryId !== null"
                accept=".jpeg,.jpg,.png"
                @change="setPendingFlowchartFile(category.id, $event)"
              />
              <DesktopButton
                :icon="Upload"
                content="Last opp flytdiagram"
                :is-loading="uploadingFlowchartCategoryId === category.id"
                loading-text="Laster opp"
                :disabled="
                  uploadingFlowchartCategoryId !== null || !pendingFlowchartFiles[category.id]
                "
                :on-click="() => uploadFlowchart(category.id)"
              />
            </div>
            <p v-if="pendingFlowchartFiles[category.id]" class="flowchart-pending no-margin">
              Klar for opplasting: {{ pendingFlowchartFiles[category.id]?.name }}
            </p>
            <p
              v-if="uploadFlowchartErrorCategoryId === category.id"
              class="error-message no-margin"
            >
              Klarte ikke å laste opp flytdiagram. Kun .jpeg, .jpg og .png er tillatt.
            </p>
          </div>

          <hr class="navy-hr" />

          <div class="danger-combo-stack">
            <div v-if="category.dangerRiskCombos.length === 0" class="empty-state">
              Ingen farepunkter registrert for denne produktkategorien ennå.
            </div>

            <article
              v-for="combo in category.dangerRiskCombos"
              :key="`danger-combo-${combo.id}`"
              class="danger-combo-card"
            >
              <template v-if="editingComboKey !== comboKey(category.id, combo.id)">
                <div class="combo-main-row">
                  <div class="combo-copy">
                    <h3 class="no-margin">{{ combo.danger }}</h3>
                    <p class="no-margin">
                      <span class="navy-subtitle">Tiltak:</span> {{ combo.dangerCorrectiveMeasure }}
                    </p>
                  </div>
                  <div class="combo-score-box">
                    <span>Alvorlighet: {{ combo.severityScore }}</span>
                    <span>Sannsynlighet: {{ combo.likelihoodScore }}</span>
                    <span class="combo-risk-label">Risiko: {{ scoreLabel(combo) }}</span>
                  </div>
                </div>
                <div class="combo-actions">
                  <DesktopButton
                    :icon="Edit2"
                    content="Rediger"
                    :disabled="isCreatingCombo || deletingComboKey !== null || isSavingCombo"
                    :on-click="() => startEditCombo(category.id, combo)"
                  />
                  <DesktopButton
                    :icon="Trash2"
                    content="Slett"
                    button-color="cherry"
                    :is-loading="deletingComboKey === comboKey(category.id, combo.id)"
                    loading-text="Sletter"
                    :disabled="isCreatingCombo || isSavingCombo || deletingComboKey !== null"
                    :on-click="() => deleteCombo(category.id, combo.id)"
                  />
                </div>
                <p
                  v-if="deleteComboErrorKey === comboKey(category.id, combo.id)"
                  class="error-message no-margin"
                >
                  Klarte ikke å slette farepunktet.
                </p>
              </template>

              <template v-else>
                <div class="combo-edit-grid">
                  <label>
                    <span class="navy-subtitle">Fare</span>
                    <input
                      v-model="formDanger"
                      type="text"
                      class="simple-text-input"
                      :disabled="isSavingCombo"
                    />
                  </label>
                  <label>
                    <span class="navy-subtitle">Tiltak</span>
                    <textarea
                      v-model="formCorrectiveMeasure"
                      class="simple-text-input"
                      rows="3"
                      :disabled="isSavingCombo"
                    />
                  </label>
                  <div class="combo-score-inputs">
                    <label>
                      <span class="navy-subtitle">Alvorlighet (1-3)</span>
                      <input
                        v-model.number="formSeverityScore"
                        type="number"
                        min="1"
                        max="3"
                        class="simple-text-input"
                        :disabled="isSavingCombo"
                      />
                    </label>
                    <label>
                      <span class="navy-subtitle">Sannsynlighet (1-3)</span>
                      <input
                        v-model.number="formLikelihoodScore"
                        type="number"
                        min="1"
                        max="3"
                        class="simple-text-input"
                        :disabled="isSavingCombo"
                      />
                    </label>
                  </div>
                </div>
                <div class="combo-actions">
                  <DesktopButton
                    :icon="Save"
                    content="Lagre"
                    :is-loading="isSavingCombo"
                    loading-text="Lagrer"
                    :on-click="() => saveCombo(category.id, combo.id)"
                  />
                  <DesktopButton
                    :icon="X"
                    content="Avbryt"
                    button-color="boring-ghost"
                    :disabled="isSavingCombo"
                    :on-click="cancelEditCombo"
                  />
                </div>
                <p
                  v-if="saveComboErrorKey === comboKey(category.id, combo.id)"
                  class="error-message no-margin"
                >
                  Klarte ikke å lagre farepunktet.
                </p>
              </template>
            </article>

            <div v-if="creatingForCategoryId === category.id" class="danger-create-card">
              <h3 class="no-margin">Nytt farepunkt</h3>
              <div class="combo-edit-grid">
                <label>
                  <span class="navy-subtitle">Fare</span>
                  <input
                    v-model="formDanger"
                    type="text"
                    class="simple-text-input"
                    :disabled="isCreatingCombo"
                  />
                </label>
                <label>
                  <span class="navy-subtitle">Korrigerende tiltak</span>
                  <textarea
                    v-model="formCorrectiveMeasure"
                    class="simple-text-input"
                    rows="3"
                    :disabled="isCreatingCombo"
                  />
                </label>
                <div class="combo-score-inputs">
                  <label>
                    <span class="navy-subtitle">Alvorlighet (1-3)</span>
                    <input
                      v-model.number="formSeverityScore"
                      type="number"
                      min="1"
                      max="3"
                      class="simple-text-input"
                      :disabled="isCreatingCombo"
                    />
                  </label>
                  <label>
                    <span class="navy-subtitle">Sannsynlighet (1-3)</span>
                    <input
                      v-model.number="formLikelihoodScore"
                      type="number"
                      min="1"
                      max="3"
                      class="simple-text-input"
                      :disabled="isCreatingCombo"
                    />
                  </label>
                </div>
              </div>
              <div class="combo-actions">
                <DesktopButton
                  :icon="Save"
                  content="Opprett"
                  :is-loading="isCreatingCombo"
                  loading-text="Oppretter"
                  :on-click="createCombo"
                />
                <DesktopButton
                  :icon="X"
                  content="Avbryt"
                  button-color="cherry"
                  :disabled="isCreatingCombo"
                  :on-click="cancelCreateCombo"
                />
              </div>
              <p v-if="createComboError" class="error-message no-margin">
                Klarte ikke å opprette farepunktet.
              </p>
            </div>

            <DesktopButton
              v-else
              :icon="Plus"
              content="Legg til farepunkt"
              :disabled="
                isSavingCombo || deletingComboKey !== null || uploadingFlowchartCategoryId !== null
              "
              :on-click="() => startCreateCombo(category.id)"
            />
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

.danger-category-card {
  border-radius: 1rem;
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
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

.create-category-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.danger-category-header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: start;
}

.danger-category-copy {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.danger-category-description {
  margin-top: 0.5rem;
  color: var(--blue-navy-80);
}

.flowchart-box {
  border: 1px solid var(--blue-navy-20);
  border-radius: 0.75rem;
  padding: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.flowchart-current {
  color: var(--blue-navy-80);
}

.flowchart-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: center;
}

.flowchart-pending {
  color: var(--blue-decor);
}

.flowchart-preview {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.flowchart-link {
  color: var(--blue-decor);
  width: fit-content;
}

.flowchart-image {
  width: min(100%, 28rem);
  border-radius: 0.75rem;
  border: 1px solid var(--blue-navy-20);
}

.danger-combo-stack {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.danger-combo-card,
.danger-create-card {
  border: 1px solid var(--blue-navy-20);
  border-radius: 0.75rem;
  padding: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.combo-main-row {
  display: flex;
  justify-content: space-between;
  align-items: start;
  gap: 1rem;
}

.combo-copy {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.combo-score-box {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  border: 1px solid var(--blue-navy-20);
  border-radius: 0.75rem;
  padding: 0.5rem 0.75rem;
  min-width: 9.5rem;
}

.combo-risk-label {
  color: var(--blue-decor);
  font-weight: 600;
}

.combo-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

.combo-edit-grid {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.combo-score-inputs {
  display: grid;
  gap: 0.5rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.empty-state {
  color: var(--blue-navy-60);
}

.error-message {
  color: #b42318;
}

@media (max-width: 768px) {
  .main-no-sidebar-container {
    width: 100%;
  }

  .combo-main-row {
    flex-direction: column;
  }

  .combo-score-inputs {
    grid-template-columns: 1fr;
  }
}
</style>
