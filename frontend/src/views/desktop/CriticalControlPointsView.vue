<script setup lang="ts">
import CorrectiveMeasures from '@/components/desktop/criticalpoints/CorrectiveMeasures.vue'
import { getMockProductCategoryName } from '@/data/mockProductCategories'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import Paginator from '@/components/desktop/shared/Paginator.vue'
import UserBadge from '@/components/desktop/shared/UserBadge.vue'
import type {
  CriticalControlPointAllInfo,
  NewCriticalControlPoint,
} from '@/interfaces/api-interfaces'
import { delay } from '@/utils'
import { Edit2, Plus, Save, X } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'

const expandedMeasures = ref<Record<string, boolean>>({})

function getMeasureKey(ccpIndex: number, measureIndex: number) {
  return `${ccpIndex}-${measureIndex}`
}

function toggleMeasure(ccpIndex: number, measureIndex: number) {
  const key = getMeasureKey(ccpIndex, measureIndex)
  expandedMeasures.value[key] = !expandedMeasures.value[key]
}

function getMeasureDescription(ccpIndex: number, measureIndex: number, text: string) {
  const key = getMeasureKey(ccpIndex, measureIndex)
  if (expandedMeasures.value[key] || text.length <= 220) {
    return text
  }

  return `${text.slice(0, 220)}...`
}

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

const name = ref('')
const how = ref('')
const equipment = ref('')
const instructionsAndCalibration = ref('')
const immediateCorrectiveAction = ref('')
const criticalMin = ref(0)
const criticalMax = ref(0)
const unit = ref('C')
const monitoredDescription = ref('')
const correctiveMeasures = ref<NewCriticalControlPoint['ccpCorrectiveMeasure']>([])

const canSaveNewCcp = computed(() => {
  return (
    name.value.trim().length > 0 &&
    how.value.trim().length > 0 &&
    equipment.value.trim().length > 0 &&
    instructionsAndCalibration.value.trim().length > 0 &&
    immediateCorrectiveAction.value.trim().length > 0 &&
    unit.value.trim().length > 0 &&
    monitoredDescription.value.trim().length > 0 &&
    Number.isFinite(criticalMin.value) &&
    Number.isFinite(criticalMax.value) &&
    correctiveMeasures.value.length > 0 &&
    criticalMin.value <= criticalMax.value
  )
})

function resetCreateForm() {
  name.value = ''
  how.value = ''
  equipment.value = ''
  instructionsAndCalibration.value = ''
  immediateCorrectiveAction.value = ''
  criticalMin.value = 0
  criticalMax.value = 0
  unit.value = 'C'
  monitoredDescription.value = ''
  correctiveMeasures.value = []
}

function setCorrectiveMeasures(measures: NewCriticalControlPoint['ccpCorrectiveMeasure']) {
  correctiveMeasures.value = measures
}

function startCreating() {
  if (isCreatingCcp.value) {
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
  resetCreateForm()
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
    verifiers: [],
    deviationRecievers: [],
    performers: [],
    deputy: [],
    ccpCorrectiveMeasure: payload.ccpCorrectiveMeasure.map((measure) => ({
      id: Math.floor(Math.random() * 1000000),
      productCategoryId: measure.productCategoryId,
      productName: getMockProductCategoryName(measure.productCategoryId),
      measureDescription: measure.measureDescription,
    })),
  }

  mockServerState.value = [...mockServerState.value, newCcp]
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

async function addCcp() {
  if (isCreatingCcp.value || !canSaveNewCcp.value) {
    return
  }

  isCreatingCcp.value = true
  createError.value = false

  const payload: NewCriticalControlPoint = {
    name: name.value.trim(),
    how: how.value.trim(),
    equipment: equipment.value.trim(),
    instructionsAndCalibration: instructionsAndCalibration.value.trim(),
    immediateCorrectiveAction: immediateCorrectiveAction.value.trim(),
    criticalMin: criticalMin.value,
    criticalMax: criticalMax.value,
    unit: unit.value.trim(),
    monitoredDescription: monitoredDescription.value.trim(),
    verifiers: [],
    deviationRecievers: [],
    performers: [],
    deputy: [],
    ccpCorrectiveMeasure: correctiveMeasures.value,
  }

  try {
    await createCcp(payload)
    await fetchCcps()
    isCreating.value = false
    resetCreateForm()
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
        <h1 class="instrument-serif-regular no-margin">
          Kritiske punkter
        </h1>
        <hr class="navy-hr">
        <DesktopButton
          v-if="!isCreating && !loading"
          :icon="Plus"
          content="Legg til CCP"
          :on-click="startCreating"
        />
        <Loading v-if="loading" />
        <p
          v-else-if="error"
          class="error-message"
        >
          Klarte ikke å hente kritiske kontrollpunkter.
        </p>

        <template v-else>
          <div
            v-if="isCreating"
            class="create-ccp"
          >
            <div class="ccp-header">
              <h2 class="no-margin">
                Nytt kritisk kontrollpunkt
              </h2>
              <div class="create-actions">
                <DesktopButton
                  :icon="Save"
                  :is-loading="isCreatingCcp"
                  loading-text="Lagrer"
                  content="Lagre"
                  :on-click="addCcp"
                  :disabled="!canSaveNewCcp || isCreatingCcp"
                />
                <DesktopButton
                  :icon="X"
                  content="Avbryt"
                  button-color="cherry"
                  :on-click="cancelCreating"
                  :disabled="isCreatingCcp"
                />
              </div>
            </div>

            <div class="ccp-grid">
              <label class="form-field">
                <span class="navy-subtitle">Navn</span>
                <input
                  v-model="name"
                  class="simple-text-input"
                  type="text"
                  :disabled="isCreatingCcp"
                  placeholder="F.eks. kjøledisk fisk"
                >
              </label>
              <label class="form-field">
                <span class="navy-subtitle">Enhet</span>
                <input
                  v-model="unit"
                  class="simple-text-input"
                  type="text"
                  :disabled="isCreatingCcp"
                  placeholder="F.eks. C"
                >
              </label>
              <label class="form-field">
                <span class="navy-subtitle">Kritisk min</span>
                <input
                  v-model.number="criticalMin"
                  class="simple-text-input"
                  type="number"
                  :disabled="isCreatingCcp"
                  placeholder="F.eks. 0"
                >
              </label>
              <label class="form-field">
                <span class="navy-subtitle">Kritisk max</span>
                <input
                  v-model.number="criticalMax"
                  class="simple-text-input"
                  type="number"
                  :disabled="isCreatingCcp"
                  placeholder="F.eks. 4"
                >
              </label>
            </div>

            <label class="form-field">
              <span class="navy-subtitle">Hvordan overvåkes det</span>
              <textarea
                v-model="how"
                class="simple-text-input"
                rows="3"
                :disabled="isCreatingCcp"
                placeholder="Beskriv hvordan dette punktet overvåkes i praksis"
              />
            </label>

            <label class="form-field">
              <span class="navy-subtitle">Utstyr</span>
              <textarea
                v-model="equipment"
                class="simple-text-input"
                rows="3"
                :disabled="isCreatingCcp"
                placeholder="F.eks. kalibrert termometer og loggskjema"
              />
            </label>

            <label class="form-field">
              <span class="navy-subtitle">Instruks og kalibrering</span>
              <textarea
                v-model="instructionsAndCalibration"
                class="simple-text-input"
                rows="3"
                :disabled="isCreatingCcp"
                placeholder="Beskriv instruks for bruk og kalibrering"
              />
            </label>

            <label class="form-field">
              <span class="navy-subtitle">Umiddelbart avvikstiltak</span>
              <textarea
                v-model="immediateCorrectiveAction"
                class="simple-text-input"
                rows="3"
                :disabled="isCreatingCcp"
                placeholder="Hva skal gjøres med en gang ved avvik"
              />
            </label>

            <label class="form-field">
              <span class="navy-subtitle">Hva overvåkes</span>
              <textarea
                v-model="monitoredDescription"
                class="simple-text-input"
                rows="2"
                :disabled="isCreatingCcp"
                placeholder="F.eks. kjølerom for fisk, kjøtt og meieri"
              />
            </label>

            <CorrectiveMeasures
              :measures="correctiveMeasures"
              :set-measures="setCorrectiveMeasures"
              :disabled="isCreatingCcp"
            />

            <p
              v-if="criticalMin > criticalMax"
              class="error-message"
            >
              Kritisk min må være mindre enn eller lik kritisk max.
            </p>
            <p
              v-if="createError"
              class="error-message"
            >
              Klarte ikke å opprette kritisk kontrollpunkt.
            </p>
          </div>

          <div
            v-for="(ccp, index) in resource"
            :key="`${ccp.name}-${index}`"
            class="ccp"
          >
            <div class="ccp-header">
              <div>
                <h2 class="no-margin">
                  {{ ccp.name }}
                </h2>
              </div>
              <DesktopButton
                :icon="Edit2"
                content="Rediger"
                disabled
              />
            </div>

            <div class="ccp-grid">
              <div class="info-card">
                <h3 class="no-margin">
                  Hvordan overvåkes det
                </h3>
                <span>{{ ccp.how }}</span>
              </div>
              <div class="info-card">
                <h3 class="no-margin">
                  Utstyr
                </h3>
                <span>{{ ccp.equipment }}</span>
              </div>
              <div class="info-card">
                <h3 class="no-margin">
                  Instruks og kalibrering
                </h3>
                <span>{{ ccp.instructionsAndCalibration }}</span>
              </div>
              <div class="info-card">
                <h3 class="no-margin">
                  Umiddelbart avvikstiltak
                </h3>
                <span>{{ ccp.immediateCorrectiveAction }}</span>
              </div>
            </div>

            <div class="thresholds">
              <span class="navy-subtitle">Kritisk grense:</span>
              <span>{{ ccp.criticalMin }} - {{ ccp.criticalMax }} {{ ccp.unit }}</span>
            </div>

            <div
              v-if="ccp.monitoredDescription"
              class="info-card"
            >
              <h3 class="no-margin">
                Hva overvåkes
              </h3>
              <span>{{ ccp.monitoredDescription }}</span>
            </div>

            <div class="people-grid">
              <div>
                <span class="navy-subtitle">Godkjennere</span>
                <div class="user-parent">
                  <UserBadge
                    v-for="verifier in ccp.verifiers"
                    :key="verifier.userId"
                    :name="verifier.userName"
                    :user-id="verifier.userId"
                  />
                </div>
              </div>
              <div>
                <span class="navy-subtitle">Avviksmottakere</span>
                <div class="user-parent">
                  <UserBadge
                    v-for="deviationReciever in ccp.deviationRecievers"
                    :key="deviationReciever.userId"
                    :name="deviationReciever.userName"
                    :user-id="deviationReciever.userId"
                  />
                </div>
              </div>
              <div>
                <span class="navy-subtitle">Utførere</span>
                <div class="user-parent">
                  <UserBadge
                    v-for="performer in ccp.performers"
                    :key="performer.userId"
                    :name="performer.userName"
                    :user-id="performer.userId"
                  />
                </div>
              </div>
              <div>
                <span class="navy-subtitle">Vikarleder</span>
                <div class="user-parent">
                  <UserBadge
                    v-for="deputy in ccp.deputy"
                    :key="deputy.userId"
                    :name="deputy.userName"
                    :user-id="deputy.userId"
                  />
                </div>
              </div>
            </div>

            <div>
              <span class="navy-subtitle">Korrigerende tiltak</span>
              <div class="measure-container">
                <div
                  v-for="(measure, measureIndex) in ccp.ccpCorrectiveMeasure"
                  :key="`${measure.productName}-${measureIndex}`"
                  class="measure-card"
                >
                  <h3 class="no-margin">
                    {{ measure.productName }}
                  </h3>
                  <span>{{
                    getMeasureDescription(index, measureIndex, measure.measureDescription)
                  }}</span>
                  <button
                    v-if="measure.measureDescription.length > 220"
                    class="show-more-button"
                    type="button"
                    @click="toggleMeasure(index, measureIndex)"
                  >
                    {{
                      expandedMeasures[getMeasureKey(index, measureIndex)]
                        ? 'Vis mindre'
                        : 'Vis mer'
                    }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </template>
      </div>
    </main>
  </div>
</template>

<style scoped>
.con {
  overflow: scroll;
}

.user-parent {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.create-ccp {
  border-radius: 1rem;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
}

.create-actions {
  display: flex;
  gap: 0.5rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.form-field textarea.simple-text-input {
  width: 100%;
  resize: vertical;
  min-height: 3rem;
  padding: 0.5rem;
  box-sizing: border-box;
  text-indent: 0;
}

.error-message {
  color: #b42318;
  margin: 0;
}

.ccp {
  border-radius: 1rem;
  padding: 1rem;
  display: flex;
  gap: 1rem;
  flex-direction: column;
  border: 1px solid var(--blue-decor);
  background-color: var(--blue-decor-10);
}

.ccp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.ccp-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.info-card {
  padding: 1rem;
  border-radius: 0.5rem;
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.thresholds {
  padding: 1rem;
  border-radius: 0.5rem;
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.people-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.measure-container {
  display: flex;
  gap: 1rem;
  > div {
    max-width: 60%;
  }
}

.measure-card {
  padding: 1rem;
  border-radius: 0.5rem;
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.show-more-button {
  border: 0;
  background: transparent;
  color: var(--blue-decor);
  padding: 0;
  width: fit-content;
  cursor: pointer;
}

.show-more-button:hover,
.show-more-button:focus {
  text-decoration: underline;
}

main {
  display: flex;
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

@media (max-width: 1200px) {
  .ccp-grid,
  .people-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  main {
    padding-left: 1rem;
    padding-right: 1rem;
    .main-no-sidebar-container {
      width: 100%;
    }
  }

  .ccp-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .create-actions {
    width: 100%;
    flex-direction: column;
  }

  .measure-container {
    flex-direction: column;
  }
}
</style>
