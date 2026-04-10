<script setup lang="ts">
import CcpCard from '@/components/desktop/criticalpoints/CcpCard.vue'
import CcpCreateForm from '@/components/desktop/criticalpoints/CcpCreateForm.vue'
import { getMockProductCategoryName } from '@/data/mockProductCategories'
import { getMockUserNameById } from '@/data/mockUsers'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import Paginator from '@/components/desktop/shared/Paginator.vue'
import type {
  CriticalControlPoint,
  CriticalControlPointAllInfo,
  NewCriticalControlPoint,
} from '@/interfaces/api-interfaces'
import type { BasicUserWithAccessLevel } from '@/interfaces/util-interfaces'
import { delay } from '@/utils'
import { Plus } from '@lucide/vue'
import { onMounted, ref } from 'vue'

const mockData: CriticalControlPointAllInfo = [
  {
    name: 'Renhold av lokaler og utstyr',
    how: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea  commodo consequat. Duis aute irure dolor. Lerum in reprehenderit in voluptate  velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint  occaecat cupidatat non proident, sunt in culpa qui officia deserunt  mollit anim id est laborum',
    equipment:
      'Lorem ipsum esse dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore esse magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation esse ullamco laboris nisi ut aliquip ex ea  commodo consequat. Duis aute irure dolor. Lerum in reprehenderit in voluptate  velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint  occaecat cupidatat non proident, sunt in culpa qui officia deserunt  mollit anim id est laborum',
    instructionsAndCalibration:
      'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea  commodo consequat. Duis aute irure dolor. Lerum in reprehenderit in voluptate  velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint  occaecat cupidatat non proident, sunt in culpa qui officia deserunt  mollit anim id est laborum esse',
    immediateCorrectiveAction:
      'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea  commodo consequat. Duis aute irure dolor. Lerum in reprehenderit in voluptate  velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint  occaecat cupidatat non proident, sunt in culpa qui officia deserunt  mollit anim id est laborum. Duis aute irure dolor. Lerum in reprehenderit in voluptate  velit esse cillum.',
    criticalMin: 2.1,
    criticalMax: 4.2,
    unit: 'C',
    monitoredDescription: '',
    id: 5,
    verifiers: [
      {
        userId: 1234,
        userName: 'Kari Næss Northun',
      },
      {
        userId: 5643,
        userName: 'Stoltenberg',
      },
    ],
    deviationRecievers: [
      {
        userId: 1234,
        userName: 'Simen Velle',
      },
      {
        userId: 5643,
        userName: 'Ola Svenneby',
      },
    ],
    performers: [
      {
        userId: 1234,
        userName: 'Jonas Ghar Støre',
      },
      {
        userId: 5643,
        userName: 'Jens Stoltenberg',
      },
    ],
    deputy: [
      {
        userId: 1234,
        userName: 'Kårw Willoch',
      },
      {
        userId: 5643,
        userName: 'Gro Harlem Brundtland',
      },
    ],
    ccpCorrectiveMeasure: [
      {
        productName: 'Burger',
        measureDescription:
          'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation',
        id: 19,
        productCategoryId: 12,
      },
      {
        productName: 'Fish',
        measureDescription:
          'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation',
        id: 72,
        productCategoryId: 10,
      },
    ],
  },
  {
    name: 'Oppbevaring av ferske råvarer',
    how: 'Kontroll utføres ved åpning, etter varelevering og før stengetid.',
    equipment: 'Kalibrert termometer og loggskjema.',
    instructionsAndCalibration: 'Termometer kontrolleres ukentlig mot referansepunkt.',
    immediateCorrectiveAction: 'Flytt produkter til reservekjøl og varsle ansvarlig leder.',
    criticalMin: 0,
    criticalMax: 4,
    unit: 'C',
    monitoredDescription: 'Kjølerom for fisk, kjøtt og meieri.',
    id: 10,
    verifiers: [
      {
        userId: 3344,
        userName: 'Mona Jul',
      },
    ],
    deviationRecievers: [
      {
        userId: 4499,
        userName: 'Ane Brevik',
      },
    ],
    performers: [
      {
        userId: 9301,
        userName: 'Jagland',
      },
    ],
    deputy: [
      {
        userId: 1199,
        userName: 'Bondevik',
      },
    ],
    ccpCorrectiveMeasure: [
      {
        productName: 'Kylling',
        measureDescription: 'Kasser produkt hvis temperaturgrense er brutt over tid.',
        id: 100,
        productCategoryId: 109,
      },
    ],
  },
  {
    name: 'Varmholding ved servering',
    how: 'Måles hver 30. minutt under service.',
    equipment: 'Digitalt stikktermometer.',
    instructionsAndCalibration: 'Termometer desinfiseres mellom hver måling.',
    immediateCorrectiveAction: 'Øk varme umiddelbart, eller ta produkt ut av salg.',
    criticalMin: 60,
    criticalMax: 90,
    unit: 'C',
    monitoredDescription: 'Supper, sauser og varme retter i buffet.',
    id: 20,
    verifiers: [
      {
        userId: 2222,
        userName: 'Kari Næss Northun',
      },
    ],
    deviationRecievers: [
      {
        userId: 7777,
        userName: 'Simen Velle',
      },
    ],
    performers: [
      {
        userId: 9090,
        userName: 'Jens Stoltenberg',
      },
    ],
    deputy: [
      {
        userId: 8080,
        userName: 'Gro Harlem Brundtland',
      },
    ],
    ccpCorrectiveMeasure: [
      {
        productName: 'Suppe',
        measureDescription: 'Fortsett oppvarming til kritisk grense er oppnådd før servering.',
        id: 10,
        productCategoryId: 91,
      },
      {
        productName: 'Saus',
        measureDescription: 'Skift beholder og dokumenter avvik i logg.',
        id: 11,
        productCategoryId: 98,
      },
    ],
  },
]

function cloneCcps(data: CriticalControlPointAllInfo): CriticalControlPointAllInfo {
  return data.map((ccp) => ({
    ...ccp,
    verifiers: ccp.verifiers.map((user) => ({ ...user })),
    deviationRecievers: ccp.deviationRecievers.map((user) => ({ ...user })),
    performers: ccp.performers.map((user) => ({ ...user })),
    deputy: ccp.deputy.map((user) => ({ ...user })),
    ccpCorrectiveMeasure: ccp.ccpCorrectiveMeasure.map((measure) => ({ ...measure })),
  }))
}

const mockServerState = ref<CriticalControlPointAllInfo>(cloneCcps(mockData))

const resource = ref<CriticalControlPointAllInfo>([])
const loading = ref(true)
const error = ref<boolean | null>(null)
const isCreating = ref(false)
const isCreatingCcp = ref(false)
const createError = ref(false)
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

type UpdateCriticalControlPointPayload = NewCriticalControlPoint & { id: number }

function startCreating() {
  if (isCreatingCcp.value || isUpdatingCcp.value || editingCcpId.value !== null) {
    return
  }

  isCreating.value = true
  createError.value = false
}

function cancelCreating() {
  if (isCreatingCcp.value) {
    return
  }

  isCreating.value = false
  createError.value = false
}

async function fetchCcps() {
  // const response = await fetch('/api/haccp/critical-control-points/get-all-info')
  // if (!response.ok) {
  //   throw new Error(`Failed to fetch critical control points (${response.status})`)
  // }
  // const data: CriticalControlPointAllInfo = await response.json()

  await delay(500)
  resource.value = cloneCcps(mockServerState.value)
}

function mapUsersByIds(ids: number[]) {
  return ids.map((id) => ({
    userId: id,
    userName: getMockUserNameById(id),
  }))
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
    verifiers: ccp.verifiers.map((user) => user.userId),
    deviationRecievers: ccp.deviationRecievers.map((user) => user.userId),
    performers: ccp.performers.map((user) => user.userId),
    deputy: ccp.deputy.map((user) => user.userId),
    ccpCorrectiveMeasure: ccp.ccpCorrectiveMeasure.map((measure) => ({
      productCategoryId: measure.productCategoryId,
      measureDescription: measure.measureDescription,
    })),
  }
}

async function createCcp(payload: NewCriticalControlPoint) {
  // const response = await fetch('/api/haccp/critical-control-points', {
  //   method: 'POST',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify(payload),
  // })
  // if (!response.ok) {
  //   throw new Error(`Failed to create critical control point (${response.status})`)
  // }

  await delay(700)

  const newCcp = {
    id: Math.floor(Math.random() * 1000000),
    name: payload.name,
    how: payload.how,
    equipment: payload.equipment,
    instructionsAndCalibration: payload.instructionsAndCalibration,
    immediateCorrectiveAction: payload.immediateCorrectiveAction,
    criticalMin: payload.criticalMin,
    criticalMax: payload.criticalMax,
    unit: payload.unit,
    monitoredDescription: payload.monitoredDescription,
    verifiers: mapUsersByIds(payload.verifiers),
    deviationRecievers: mapUsersByIds(payload.deviationRecievers),
    performers: mapUsersByIds(payload.performers),
    deputy: mapUsersByIds(payload.deputy),
    ccpCorrectiveMeasure: payload.ccpCorrectiveMeasure.map((measure) => ({
      id: Math.floor(Math.random() * 1000000),
      productCategoryId: measure.productCategoryId,
      productName: getMockProductCategoryName(measure.productCategoryId),
      measureDescription: measure.measureDescription,
    })),
  }

  mockServerState.value = [...mockServerState.value, newCcp]
}

async function updateCcp(payload: UpdateCriticalControlPointPayload) {
  // const response = await fetch('/api/haccp/critical-control-points', {
  //   method: 'PUT',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify(payload),
  // })
  // if (!response.ok) {
  //   throw new Error(`Failed to update critical control point (${response.status})`)
  // }

  await delay(700)

  const ccpIndex = mockServerState.value.findIndex((ccp) => ccp.id === payload.id)
  if (ccpIndex === -1) {
    throw new Error('Critical control point not found')
  }

  const currentCcp = mockServerState.value[ccpIndex]
  if (!currentCcp) {
    throw new Error('Critical control point not found')
  }

  mockServerState.value[ccpIndex] = {
    ...currentCcp,
    name: payload.name,
    how: payload.how,
    equipment: payload.equipment,
    instructionsAndCalibration: payload.instructionsAndCalibration,
    immediateCorrectiveAction: payload.immediateCorrectiveAction,
    criticalMin: payload.criticalMin,
    criticalMax: payload.criticalMax,
    unit: payload.unit,
    monitoredDescription: payload.monitoredDescription,
    verifiers: mapUsersByIds(payload.verifiers),
    deviationRecievers: mapUsersByIds(payload.deviationRecievers),
    performers: mapUsersByIds(payload.performers),
    deputy: mapUsersByIds(payload.deputy),
    ccpCorrectiveMeasure: payload.ccpCorrectiveMeasure.map((measure, index) => {
      const existing = currentCcp.ccpCorrectiveMeasure.find(
        (entry) => entry.productCategoryId === measure.productCategoryId,
      )

      return {
        id: existing?.id ?? Date.now() + index,
        productCategoryId: measure.productCategoryId,
        productName: getMockProductCategoryName(measure.productCategoryId),
        measureDescription: measure.measureDescription,
      }
    }),
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
    verifiers: ccp.verifiers.map((user) => ({
      id: user.userId,
      legalName: user.userName,
      email: '',
      accessLevel: 'WORKER',
    })),
    deviationRecievers: ccp.deviationRecievers.map((user) => ({
      id: user.userId,
      legalName: user.userName,
      email: '',
      accessLevel: 'WORKER',
    })),
    performers: ccp.performers.map((user) => ({
      id: user.userId,
      legalName: user.userName,
      email: '',
      accessLevel: 'WORKER',
    })),
    deputy: ccp.deputy.map((user) => ({
      id: user.userId,
      legalName: user.userName,
      email: '',
      accessLevel: 'WORKER',
    })),
  }
  updateError.value = false
}

function cancelEditingCcp() {
  if (isUpdatingCcp.value) {
    return
  }

  editingCcpId.value = null
  editingPayload.value = null
  editingRoleUsers.value = null
  updateError.value = false
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
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    updateError.value = true
  } finally {
    isUpdatingCcp.value = false
  }
}

onMounted(async () => {
  try {
    await fetchCcps()
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
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    createError.value = true
  } finally {
    isCreatingCcp.value = false
  }
}
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
        <Loading v-if="loading" />
        <p v-else-if="error" class="error-message">Klarte ikke å hente kritiske kontrollpunkter.</p>

        <template v-else>
          <CcpCreateForm
            v-if="isCreating"
            mode="create"
            :is-submitting="isCreatingCcp"
            :submit-error="createError"
            @submit="addCcp"
            @cancel="cancelCreating"
          />

          <template v-for="ccp in resource" :key="ccp.id">
            <CcpCreateForm
              v-if="editingCcpId === ccp.id"
              mode="edit"
              :initial-payload="editingPayload"
              :initial-role-users="editingRoleUsers"
              :is-submitting="isUpdatingCcp"
              :submit-error="updateError"
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
