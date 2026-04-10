<script setup lang="ts">
import api from '@/api/api'
import CcpCard from '@/components/desktop/criticalpoints/CcpCard.vue'
import CcpCreateForm from '@/components/desktop/criticalpoints/CcpCreateForm.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import Paginator from '@/components/desktop/shared/Paginator.vue'
import { useOrgSession } from '@/composables/useOrgSession'
import type {
  CriticalControlPoint,
  CriticalControlPointAllInfo,
  DesktopProductCategory,
  NewCriticalControlPoint,
} from '@/interfaces/api-interfaces'
import type { BasicUserWithAccessLevel } from '@/interfaces/util-interfaces'
import { Plus } from '@lucide/vue'
import { ref, watch } from 'vue'

type CcpApiUser = {
  userId: number
  legalName: string
}

type CcpApiCorrectiveMeasure = {
  id: number
  productCategoryId: number
  productName: string
  measureDescription: string
}

type CcpApiInterval = {
  intervalId: number
  intervalStart: number
  intervalRepeatTime: number
}

type CcpApiResponse = {
  id: number
  name: string
  how: string
  equipment: string
  instructionsAndCalibration: string
  immediateCorrectiveAction: string
  criticalMin: number
  criticalMax: number
  unit: string
  monitoredDescription: string
  interval: CcpApiInterval | null
  repeatText: string | null
  verifiers: CcpApiUser[]
  deviationReceivers: CcpApiUser[]
  performers: CcpApiUser[]
  deputy: CcpApiUser[]
  ccpCorrectiveMeasures: CcpApiCorrectiveMeasure[]
}

type ProductCategoryApiResponse = {
  id: number
  productDescription: string
}

type UserOrgApiResponse = {
  id: number
  legalName: string
  email: string
  accessLevel: BasicUserWithAccessLevel['accessLevel']
}

const resource = ref<CriticalControlPointAllInfo>([])
const organizationUsers = ref<BasicUserWithAccessLevel[]>([])
const productCategories = ref<DesktopProductCategory[]>([])
const loading = ref(true)
const error = ref<boolean | null>(null)
const isCreating = ref(false)
const isCreatingCcp = ref(false)
const createError = ref(false)
const createErrorMessage = ref('')
const editingCcpId = ref<number | null>(null)
const editingPayload = ref<NewCriticalControlPoint | null>(null)
const editingRoleUsers = ref<{
  verifiers: BasicUserWithAccessLevel[]
  deviationRecievers: BasicUserWithAccessLevel[]
  performers: BasicUserWithAccessLevel[]
  deputy: BasicUserWithAccessLevel[]
} | null>(null)
const isUpdatingCcp = ref(false)
const updateError = ref(false)
const updateErrorMessage = ref('')
const { claims } = useOrgSession()
const DEFAULT_INTERVAL_REPEAT_TIME_SECONDS = 86400
let activeFetchId = 0

type UpdateCriticalControlPointPayload = NewCriticalControlPoint & { id: number }

function extractErrorMessage(error: unknown, fallback: string) {
  if (
    typeof error === 'object' &&
    error !== null &&
    'response' in error &&
    typeof error.response === 'object' &&
    error.response !== null &&
    'data' in error.response
  ) {
    const responseData = error.response.data
    if (typeof responseData === 'string' && responseData.trim().length > 0) {
      return responseData
    }
  }

  return error instanceof Error ? error.message : fallback
}

function mapOrgUser(user: UserOrgApiResponse): BasicUserWithAccessLevel {
  return {
    id: user.id,
    legalName: user.legalName,
    email: user.email,
    accessLevel: user.accessLevel,
  }
}

function mapProductCategory(category: ProductCategoryApiResponse): DesktopProductCategory {
  return {
    id: category.id,
    name: category.productDescription,
    description: category.productDescription,
  }
}

function mapCcpUser(user: CcpApiUser) {
  return {
    userId: user.userId,
    userName: user.legalName,
  }
}

function mapApiCcp(ccp: CcpApiResponse): CriticalControlPoint {
  return {
    id: ccp.id,
    name: ccp.name,
    how: ccp.how,
    equipment: ccp.equipment,
    instructionsAndCalibration: ccp.instructionsAndCalibration,
    immediateCorrectiveAction: ccp.immediateCorrectiveAction,
    criticalMin: Number(ccp.criticalMin),
    criticalMax: Number(ccp.criticalMax),
    unit: ccp.unit ?? '',
    monitoredDescription: ccp.monitoredDescription ?? '',
    repeatText: ccp.repeatText,
    intervalStart: ccp.interval?.intervalStart ?? null,
    intervalRepeatTime: ccp.interval?.intervalRepeatTime ?? null,
    verifiers: (ccp.verifiers ?? []).map(mapCcpUser),
    deviationRecievers: (ccp.deviationReceivers ?? []).map(mapCcpUser),
    performers: (ccp.performers ?? []).map(mapCcpUser),
    deputy: (ccp.deputy ?? []).map(mapCcpUser),
    ccpCorrectiveMeasure: (ccp.ccpCorrectiveMeasures ?? []).map((measure) => ({
      id: measure.id,
      productCategoryId: measure.productCategoryId,
      productName: measure.productName,
      measureDescription: measure.measureDescription,
    })),
  }
}

function getCurrentEpochSeconds() {
  return Math.floor(Date.now() / 1000)
}

function buildCreateRequest(payload: NewCriticalControlPoint) {
  return {
    name: payload.name,
    how: payload.how,
    equipment: payload.equipment,
    instructionsAndCalibration: payload.instructionsAndCalibration,
    immediateCorrectiveAction: payload.immediateCorrectiveAction,
    criticalMin: payload.criticalMin,
    criticalMax: payload.criticalMax,
    unit: payload.unit,
    monitoredDescription: payload.monitoredDescription,
    intervalStart: payload.intervalStart ?? getCurrentEpochSeconds(),
    intervalRepeatTime: payload.intervalRepeatTime ?? DEFAULT_INTERVAL_REPEAT_TIME_SECONDS,
    verifierUserIds: payload.verifiers,
    deviationReceiverUserIds: payload.deviationRecievers,
    performerUserIds: payload.performers,
    deputyUserIds: payload.deputy,
    correctiveMeasures: payload.ccpCorrectiveMeasure.map((measure) => ({
      productCategoryId: measure.productCategoryId,
      measureDescription: measure.measureDescription,
    })),
  }
}

function buildUpdateRequest(payload: NewCriticalControlPoint) {
  return {
    name: payload.name,
    how: payload.how,
    equipment: payload.equipment,
    instructionsAndCalibration: payload.instructionsAndCalibration,
    immediateCorrectiveAction: payload.immediateCorrectiveAction,
    criticalMin: payload.criticalMin,
    criticalMax: payload.criticalMax,
    unit: payload.unit,
    monitoredDescription: payload.monitoredDescription,
    intervalStart: payload.intervalStart ?? undefined,
    intervalRepeatTime: payload.intervalRepeatTime ?? undefined,
  }
}

function buildAssignmentsRequest(payload: NewCriticalControlPoint) {
  return {
    verifierUserIds: payload.verifiers,
    deviationReceiverUserIds: payload.deviationRecievers,
    performerUserIds: payload.performers,
    deputyUserIds: payload.deputy,
  }
}

function startCreating() {
  if (isCreatingCcp.value || isUpdatingCcp.value || editingCcpId.value !== null) {
    return
  }

  isCreating.value = true
  createError.value = false
  createErrorMessage.value = ''
}

function cancelCreating() {
  if (isCreatingCcp.value) {
    return
  }

  isCreating.value = false
  createError.value = false
  createErrorMessage.value = ''
}

async function fetchCcps() {
  const fetchId = ++activeFetchId

  loading.value = true
  error.value = false

  try {
    const [ccpResult, usersResult, categoriesResult] = await Promise.allSettled([
      api.get<CcpApiResponse[]>('/haccp/critical-control-points/get-all-info'),
      api.get<UserOrgApiResponse[]>('/organizations/users'),
      (async () => {
        try {
          return await api.get<ProductCategoryApiResponse[]>('/product-categories')
        } catch {
          return api.get<ProductCategoryApiResponse[]>('/api/product-categories')
        }
      })(),
    ])

    if (fetchId !== activeFetchId) {
      return
    }

    if (ccpResult.status !== 'fulfilled') {
      throw ccpResult.reason
    }

    resource.value = Array.isArray(ccpResult.value.data) ? ccpResult.value.data.map(mapApiCcp) : []
    organizationUsers.value =
      usersResult.status === 'fulfilled' && Array.isArray(usersResult.value.data)
        ? usersResult.value.data.map(mapOrgUser)
        : []
    productCategories.value =
      categoriesResult.status === 'fulfilled' && Array.isArray(categoriesResult.value.data)
        ? categoriesResult.value.data.map(mapProductCategory)
        : []

    if (usersResult.status === 'rejected') {
      console.error(extractErrorMessage(usersResult.reason, 'Klarte ikke å hente brukere.'))
    }

    if (categoriesResult.status === 'rejected') {
      console.error(
        extractErrorMessage(categoriesResult.reason, 'Klarte ikke å hente produktkategorier.'),
      )
    }
  } catch (err) {
    if (fetchId !== activeFetchId) {
      return
    }

    resource.value = []
    organizationUsers.value = []
    productCategories.value = []
    error.value = true
    console.error(extractErrorMessage(err, 'Klarte ikke å hente kritiske kontrollpunkter.'))
  } finally {
    if (fetchId === activeFetchId) {
      loading.value = false
    }
  }
}

function toCreatePayload(ccp: CriticalControlPoint): NewCriticalControlPoint {
  return {
    name: ccp.name,
    how: ccp.how,
    equipment: ccp.equipment,
    instructionsAndCalibration: ccp.instructionsAndCalibration,
    immediateCorrectiveAction: ccp.immediateCorrectiveAction,
    criticalMin: ccp.criticalMin,
    criticalMax: ccp.criticalMax,
    unit: ccp.unit,
    monitoredDescription: ccp.monitoredDescription,
    intervalStart: ccp.intervalStart ?? null,
    intervalRepeatTime: ccp.intervalRepeatTime ?? null,
    verifiers: ccp.verifiers.map((user) => user.userId),
    deviationRecievers: ccp.deviationRecievers.map((user) => user.userId),
    performers: ccp.performers.map((user) => user.userId),
    deputy: ccp.deputy.map((user) => user.userId),
    ccpCorrectiveMeasure: ccp.ccpCorrectiveMeasure.map((measure) => ({
      id: measure.id,
      productCategoryId: measure.productCategoryId,
      measureDescription: measure.measureDescription,
      productName: measure.productName,
    })),
  }
}

async function createCcp(payload: NewCriticalControlPoint) {
  await api.post('/haccp/critical-control-points', buildCreateRequest(payload))
}

async function syncCorrectiveMeasures(
  ccpId: number,
  currentMeasures: CriticalControlPoint['ccpCorrectiveMeasure'],
  nextMeasures: NewCriticalControlPoint['ccpCorrectiveMeasure'],
) {
  const nextMeasureIds = new Set(
    nextMeasures
      .map((measure) => measure.id)
      .filter((id): id is number => typeof id === 'number'),
  )

  for (const measure of currentMeasures) {
    if (!nextMeasureIds.has(measure.id)) {
      await api.delete(`/haccp/critical-control-points/corrective-measures/${measure.id}`)
    }
  }

  const currentMeasuresById = new Map(currentMeasures.map((measure) => [measure.id, measure]))

  for (const measure of nextMeasures) {
    const trimmedDescription = measure.measureDescription.trim()
    if (typeof measure.id !== 'number') {
      await api.post(`/haccp/critical-control-points/${ccpId}/corrective-measures`, {
        productCategoryId: measure.productCategoryId,
        measureDescription: trimmedDescription,
      })
      continue
    }

    const currentMeasure = currentMeasuresById.get(measure.id)
    if (
      !currentMeasure ||
      currentMeasure.productCategoryId !== measure.productCategoryId ||
      currentMeasure.measureDescription !== trimmedDescription
    ) {
      await api.patch(`/haccp/critical-control-points/corrective-measures/${measure.id}`, {
        productCategoryId: measure.productCategoryId,
        measureDescription: trimmedDescription,
      })
    }
  }
}

async function updateCcp(payload: UpdateCriticalControlPointPayload) {
  const currentCcp = resource.value.find((ccp) => ccp.id === payload.id)
  if (!currentCcp) {
    throw new Error('Critical control point not found')
  }

  await api.patch(`/haccp/critical-control-points/${payload.id}`, buildUpdateRequest(payload))
  await api.put(
    `/haccp/critical-control-points/${payload.id}/assignments`,
    buildAssignmentsRequest(payload),
  )
  await syncCorrectiveMeasures(payload.id, currentCcp.ccpCorrectiveMeasure, payload.ccpCorrectiveMeasure)
}

function toRoleUser(userId: number, fallbackName: string): BasicUserWithAccessLevel {
  const existingUser = organizationUsers.value.find((user) => user.id === userId)
  if (existingUser) {
    return { ...existingUser }
  }

  return {
    id: userId,
    legalName: fallbackName,
    email: '',
    accessLevel: 'WORKER',
  }
}

function startEditingCcp(ccpId: number) {
  if (isCreating.value || isCreatingCcp.value || isUpdatingCcp.value) {
    return
  }

  const ccp = resource.value.find((entry) => entry.id === ccpId)
  if (!ccp) {
    return
  }

  editingCcpId.value = ccpId
  editingPayload.value = toCreatePayload(ccp)
  editingRoleUsers.value = {
    verifiers: ccp.verifiers.map((user) => toRoleUser(user.userId, user.userName)),
    deviationRecievers: ccp.deviationRecievers.map((user) => toRoleUser(user.userId, user.userName)),
    performers: ccp.performers.map((user) => toRoleUser(user.userId, user.userName)),
    deputy: ccp.deputy.map((user) => toRoleUser(user.userId, user.userName)),
  }
  updateError.value = false
  updateErrorMessage.value = ''
}

function cancelEditingCcp() {
  if (isUpdatingCcp.value) {
    return
  }

  editingCcpId.value = null
  editingPayload.value = null
  editingRoleUsers.value = null
  updateError.value = false
  updateErrorMessage.value = ''
}

async function submitEditCcp(payload: NewCriticalControlPoint) {
  if (isUpdatingCcp.value || editingCcpId.value === null) {
    return
  }

  isUpdatingCcp.value = true
  updateError.value = false

  try {
    await updateCcp({
      id: editingCcpId.value,
      ...payload,
    })
    await fetchCcps()
    editingCcpId.value = null
    editingPayload.value = null
    editingRoleUsers.value = null
  } catch (err) {
    const message = extractErrorMessage(err, 'Klarte ikke å oppdatere kritisk kontrollpunkt.')
    console.error(message)
    updateError.value = true
    updateErrorMessage.value = message
  } finally {
    isUpdatingCcp.value = false
  }
}

async function addCcp(payload: NewCriticalControlPoint) {
  if (isCreatingCcp.value) {
    return
  }

  isCreatingCcp.value = true
  createError.value = false

  try {
    await createCcp(payload)
    await fetchCcps()
    isCreating.value = false
  } catch (err) {
    const message = extractErrorMessage(err, 'Klarte ikke å opprette kritisk kontrollpunkt.')
    console.error(message)
    createError.value = true
    createErrorMessage.value = message
  } finally {
    isCreatingCcp.value = false
  }
}

watch(
  () => claims.value?.orgId ?? null,
  () => {
    isCreating.value = false
    editingCcpId.value = null
    editingPayload.value = null
    editingRoleUsers.value = null
    createError.value = false
    createErrorMessage.value = ''
    updateError.value = false
    updateErrorMessage.value = ''
    void fetchCcps()
  },
  { immediate: true },
)
</script>

<template>
  <div class="con">
    <main>
      <div class="main-no-sidebar-container">
        <Paginator />
        <h1 class="instrument-serif-regular no-margin">Kritiske punkter</h1>
        <hr class="navy-hr" />
        <DesktopButton
          v-if="!isCreating && editingCcpId === null && !loading"
          :icon="Plus"
          content="Legg til CCP"
          :on-click="startCreating"
        />
        <CcpCreateForm
          v-if="isCreating"
          mode="create"
          :is-submitting="isCreatingCcp"
          :submit-error="createError"
          :submit-error-text="createErrorMessage"
          :available-users="organizationUsers"
          :product-categories="productCategories"
          @submit="addCcp"
          @cancel="cancelCreating"
        />
        <Loading v-if="loading" />
        <p v-else-if="error && !isCreating" class="error-message">
          Klarte ikke å hente kritiske kontrollpunkter.
        </p>

        <template v-if="!loading && !error">
          <template v-for="ccp in resource" :key="ccp.id">
            <CcpCreateForm
              v-if="editingCcpId === ccp.id"
              mode="edit"
              :initial-payload="editingPayload"
              :initial-role-users="editingRoleUsers"
              :is-submitting="isUpdatingCcp"
              :submit-error="updateError"
              :submit-error-text="updateErrorMessage"
              :available-users="organizationUsers"
              :product-categories="productCategories"
              @submit="submitEditCcp"
              @cancel="cancelEditingCcp"
            />
            <CcpCard v-else :ccp="ccp" @edit="startEditingCcp" />
          </template>
        </template>
      </div>
    </main>
  </div>
</template>

<style scoped>
.con {
  overflow: scroll;
}

.error-message {
  color: #b42318;
  margin: 0;
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
  .main-no-sidebar-container {
    width: calc(5 / 5 * 100%);
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
}

hr {
  border-color: var(--blue-navy-40);
  border-width: 1px;
}

@media (max-width: 768px) {
  main {
    padding-left: 1rem;
    padding-right: 1rem;
    .main-no-sidebar-container {
      width: 100%;
    }
  }
}
</style>
