<script setup lang="ts">
import Badge from '@/components/desktop/shared/Badge.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import { delay } from '@/utils'
import { Check, Eye, Save, X } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'
import Paginator from '@/components/desktop/shared/Paginator.vue'

type DeviationCategory = 'IK_MAT' | 'IK_ALKOHOL' | 'OTHER'
type DeviationReviewStatus = 'OPEN' | 'CLOSED'
type OrgAccessLevel = 'OWNER' | 'MANAGER' | 'WORKER'

type DeviationUser = {
  id: number
  legalName: string
  accessLevel: OrgAccessLevel
}

type DeviationLog = {
  id: number
  ccpRecordId: number | null
  routineRecordId: number | null
  category: DeviationCategory
  reportedBy: DeviationUser
  reviewStatus: DeviationReviewStatus
  reviewedBy: DeviationUser | null
  reviewedAt: string | null
  whatWentWrong: string
  immediateActionTaken: string
  potentialCause: string
  potentialPreventativeMeasure: string
  preventativeMeasureActuallyTaken: string
  deviationReceivers: DeviationUser[]
  createdAt: string
}

type UpdateDeviationPayload = {
  reviewed_by: number
  review_status: 'CLOSED'
  preventative_measure_actually_taken: string
}

const reviewFilter = ref<DeviationReviewStatus | 'ALL'>('OPEN')
const loading = ref(true)
const fetchError = ref(false)
const updateError = ref(false)
const resource = ref<DeviationLog[]>([])

const currentUser: DeviationUser = {
  id: 16,
  legalName: 'Kari Nessa Nordtun',
  accessLevel: 'MANAGER',
}

const mockUsers: DeviationUser[] = [
  { id: 11, legalName: 'Mona Jul', accessLevel: 'OWNER' },
  { id: 12, legalName: 'Jagland', accessLevel: 'MANAGER' },
  { id: 13, legalName: 'Ane Brevik', accessLevel: 'WORKER' },
  { id: 14, legalName: 'Ola Svenneby', accessLevel: 'WORKER' },
  { id: 15, legalName: 'Vedum', accessLevel: 'WORKER' },
  { id: 16, legalName: 'Kari Nessa Nordtun', accessLevel: 'MANAGER' },
]

const mockServerState = ref<DeviationLog[]>([
  {
    id: 301,
    ccpRecordId: 881,
    routineRecordId: null,
    category: 'IK_MAT',
    reportedBy: mockUsers[3]!,
    reviewStatus: 'OPEN',
    reviewedBy: null,
    reviewedAt: null,
    whatWentWrong: 'Kjøledisk for fisk målte 9 C ved åpning.',
    immediateActionTaken: 'Flyttet varer til reservekjøl og varslet vakthavende.',
    potentialCause: 'Kjøledisk ble stående åpen ved nattvask.',
    potentialPreventativeMeasure: 'Legg til sluttkontroll i stengerutine med signatur.',
    preventativeMeasureActuallyTaken: '',
    deviationReceivers: [mockUsers[5]!],
    createdAt: '2026-03-29T08:14:00',
  },
  {
    id: 302,
    ccpRecordId: null,
    routineRecordId: 1204,
    category: 'OTHER',
    reportedBy: mockUsers[2]!,
    reviewStatus: 'OPEN',
    reviewedBy: null,
    reviewedAt: null,
    whatWentWrong: 'Mangler signering på rengjoringsrutine for oppvaskrom.',
    immediateActionTaken: 'Rengjoring ble fullfort og signert manuelt.',
    potentialCause: 'Ny medarbeider kjenner ikke signeringsflyten.',
    potentialPreventativeMeasure: 'Kort opplaring + synlig sjekkliste pa vegg.',
    preventativeMeasureActuallyTaken: '',
    deviationReceivers: [],
    createdAt: '2026-03-30T18:02:00',
  },
  {
    id: 303,
    ccpRecordId: null,
    routineRecordId: 1188,
    category: 'IK_ALKOHOL',
    reportedBy: mockUsers[1]!,
    reviewStatus: 'CLOSED',
    reviewedBy: mockUsers[0]!,
    reviewedAt: '2026-03-25T11:31:00',
    whatWentWrong: 'Utsalg etter skjenketid ble registrert i kassesystem.',
    immediateActionTaken: 'Salg ble stoppet umiddelbart og avvik registrert.',
    potentialCause: 'Manglende varsling i kassasystem ved stenging.',
    potentialPreventativeMeasure: 'Automatisk sperre i kassesystem etter tidspunkt.',
    preventativeMeasureActuallyTaken: 'Sperre er aktivert, og kveldsvakt har faatt ny sjekkliste.',
    deviationReceivers: [mockUsers[0]!, mockUsers[1]!],
    createdAt: '2026-03-24T23:09:00',
  },
])

const reviewingDeviationId = ref<number | null>(null)
const submittingReview = ref(false)
const reviewText = ref('')

const filteredResource = computed(() => {
  if (reviewFilter.value === 'ALL') {
    return resource.value
  }

  return resource.value.filter((entry) => entry.reviewStatus === reviewFilter.value)
})

function cloneDeviations(data: DeviationLog[]) {
  return data.map((deviation) => ({
    ...deviation,
    reportedBy: { ...deviation.reportedBy },
    reviewedBy: deviation.reviewedBy ? { ...deviation.reviewedBy } : null,
    deviationReceivers: deviation.deviationReceivers.map((user) => ({ ...user })),
  }))
}

function isCurrentUserManagerOrOwner() {
  return currentUser.accessLevel === 'OWNER' || currentUser.accessLevel === 'MANAGER'
}

function isAssignedReceiver(deviation: DeviationLog) {
  return deviation.deviationReceivers.some((user) => user.id === currentUser.id)
}

function shouldIncludeForCurrentUser(deviation: DeviationLog) {
  if (deviation.deviationReceivers.length > 0) {
    return isAssignedReceiver(deviation)
  }

  return isCurrentUserManagerOrOwner()
}

function getCategoryLabel(category: DeviationCategory) {
  if (category === 'IK_MAT') {
    return 'IK Mat'
  }
  if (category === 'IK_ALKOHOL') {
    return 'IK Alkohol'
  }

  return 'Annet'
}

function getCategoryColor(category: DeviationCategory): 'navy' | 'cherry' {
  return category === 'IK_ALKOHOL' ? 'cherry' : 'navy'
}

function getLinkedRecordText(deviation: DeviationLog) {
  if (deviation.ccpRecordId !== null) {
    return `CCP logg #${deviation.ccpRecordId}`
  }
  if (deviation.routineRecordId !== null) {
    return `Rutine logg #${deviation.routineRecordId}`
  }

  return 'Ikke knyttet til en spesifikk logg'
}

function getReceiversText(deviation: DeviationLog) {
  if (deviation.deviationReceivers.length === 0) {
    return 'Ingen eksplisitte avviksmottakere - ledere og eier mottar avviket'
  }

  return deviation.deviationReceivers.map((user) => user.legalName).join(', ')
}

function formatDateTime(value: string | null) {
  if (!value) {
    return '-'
  }

  const date = new Date(value)
  return date.toLocaleString('nb-NO', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

async function fetchDeviations() {
  // const response = await fetch('/api/deviations/received')
  // if (!response.ok) {
  //   throw new Error(`Failed to fetch deviations (${response.status})`)
  // }
  // const data: DeviationLog[] = await response.json()

  await delay(500)
  resource.value = cloneDeviations(mockServerState.value).filter(shouldIncludeForCurrentUser)
}

async function reviewDeviation(deviationId: number, payload: UpdateDeviationPayload) {
  // const response = await fetch(`/api/deviations/${deviationId}`, {
  //   method: 'PUT',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify(payload),
  // })
  // if (!response.ok) {
  //   throw new Error(`Failed to update deviation (${response.status})`)
  // }

  await delay(700)

  const deviationIndex = mockServerState.value.findIndex((entry) => entry.id === deviationId)
  if (deviationIndex === -1) {
    throw new Error('Deviation not found')
  }

  const currentDeviation = mockServerState.value[deviationIndex]
  if (!currentDeviation) {
    throw new Error('Deviation not found')
  }

  if (currentDeviation.reviewStatus === 'CLOSED') {
    throw new Error('Deviation is already closed')
  }

  const reviewedByUser = mockUsers.find((user) => user.id === payload.reviewed_by)
  if (!reviewedByUser) {
    throw new Error('Reviewer not found')
  }

  mockServerState.value[deviationIndex] = {
    ...currentDeviation,
    reviewStatus: payload.review_status,
    preventativeMeasureActuallyTaken: payload.preventative_measure_actually_taken,
    reviewedBy: reviewedByUser,
    reviewedAt: new Date().toISOString(),
  }
}

function startReviewingDeviation(deviation: DeviationLog) {
  if (submittingReview.value || deviation.reviewStatus === 'CLOSED') {
    return
  }

  reviewingDeviationId.value = deviation.id
  reviewText.value = ''
  updateError.value = false
}

function cancelReviewingDeviation() {
  if (submittingReview.value) {
    return
  }

  reviewingDeviationId.value = null
  reviewText.value = ''
  updateError.value = false
}

async function submitDeviationReview(deviationId: number) {
  if (submittingReview.value || reviewText.value.trim().length === 0) {
    return
  }

  submittingReview.value = true
  updateError.value = false

  try {
    await reviewDeviation(deviationId, {
      reviewed_by: currentUser.id,
      review_status: 'CLOSED',
      preventative_measure_actually_taken: reviewText.value.trim(),
    })
    await fetchDeviations()
    reviewingDeviationId.value = null
    reviewText.value = ''
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    updateError.value = true
  } finally {
    submittingReview.value = false
  }
}

onMounted(async () => {
  try {
    await fetchDeviations()
    fetchError.value = false
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    fetchError.value = true
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="deviation-logs-page">
    <main>
      <div class="deviation-logs-page-container">
        <Paginator />
        <h1 class="instrument-serif-regular no-margin">Gjennomga mottatte avvik</h1>
        <hr class="navy-hr" />

        <div class="filter-row">
          <DesktopButton
            content="Apne"
            :icon="Check"
            :on-click="() => (reviewFilter = 'OPEN')"
            :disabled="reviewFilter === 'OPEN'"
          />
          <DesktopButton
            content="Lukkede"
            :icon="X"
            :on-click="() => (reviewFilter = 'CLOSED')"
            :disabled="reviewFilter === 'CLOSED'"
            button-color="navy"
          />
          <DesktopButton
            content="Alle"
            :icon="Eye"
            :on-click="() => (reviewFilter = 'ALL')"
            :disabled="reviewFilter === 'ALL'"
            button-color="navy"
          />
        </div>

        <Loading v-if="loading" />
        <p v-else-if="fetchError" class="error-message">Klarte ikke a hente avvik.</p>

        <template v-else>
          <p v-if="filteredResource.length === 0" class="empty-message">
            Ingen avvik i valgt visning.
          </p>

          <article
            v-for="deviation in filteredResource"
            :key="deviation.id"
            class="deviation-card"
            :class="deviation.reviewStatus === 'CLOSED' ? 'deviation-card-closed' : ''"
          >
            <div class="deviation-header-row">
              <div class="deviation-header-meta">
                <Badge :badge-color="getCategoryColor(deviation.category)">
                  {{ getCategoryLabel(deviation.category) }}
                </Badge>
                <span class="deviation-id">#{{ deviation.id }}</span>
                <span class="deviation-status">{{
                  deviation.reviewStatus === 'OPEN' ? 'Apent' : 'Lukket'
                }}</span>
              </div>
              <span class="deviation-date"
                >Opprettet {{ formatDateTime(deviation.createdAt) }}</span
              >
            </div>

            <div class="details-grid">
              <div class="detail-box">
                <span class="navy-subtitle">Rapportert av</span>
                <span>{{ deviation.reportedBy.legalName }}</span>
              </div>
              <div class="detail-box">
                <span class="navy-subtitle">Kobling</span>
                <span>{{ getLinkedRecordText(deviation) }}</span>
              </div>
              <div class="detail-box detail-box-wide">
                <span class="navy-subtitle">Avviksmottakere</span>
                <span>{{ getReceiversText(deviation) }}</span>
              </div>
            </div>

            <div class="deviation-section">
              <span class="navy-subtitle">Hva gikk galt</span>
              <p class="no-margin">{{ deviation.whatWentWrong }}</p>
            </div>

            <div class="deviation-section">
              <span class="navy-subtitle">Umiddelbar handling</span>
              <p class="no-margin">{{ deviation.immediateActionTaken }}</p>
            </div>

            <div class="deviation-section">
              <span class="navy-subtitle">Potensiell arsak</span>
              <p class="no-margin">{{ deviation.potentialCause }}</p>
            </div>

            <div class="deviation-section">
              <span class="navy-subtitle">Potensielt forebyggende tiltak</span>
              <p class="no-margin">{{ deviation.potentialPreventativeMeasure }}</p>
            </div>

            <div v-if="deviation.reviewStatus === 'CLOSED'" class="deviation-section resolved-box">
              <span class="navy-subtitle">Forebyggende tiltak som faktisk ble gjort</span>
              <p class="no-margin">{{ deviation.preventativeMeasureActuallyTaken }}</p>
              <p class="resolved-meta no-margin">
                Gjennomgatt av {{ deviation.reviewedBy?.legalName || '-' }}
                {{ formatDateTime(deviation.reviewedAt) }}
              </p>
            </div>

            <div
              v-else-if="reviewingDeviationId === deviation.id"
              class="deviation-section review-box"
            >
              <span class="navy-subtitle">Forebyggende tiltak som faktisk ble gjort</span>
              <textarea
                v-model="reviewText"
                class="simple-text-input"
                rows="4"
                :disabled="submittingReview"
                placeholder="Beskriv tiltaket som faktisk ble gjennomfort"
              />
              <div class="review-actions">
                <DesktopButton
                  :icon="Save"
                  content="Lukk avvik"
                  :on-click="() => submitDeviationReview(deviation.id)"
                  :is-loading="submittingReview"
                  loading-text="Lukker"
                  :disabled="reviewText.trim().length === 0"
                />
                <DesktopButton
                  :icon="X"
                  content="Avbryt"
                  button-color="cherry"
                  :on-click="cancelReviewingDeviation"
                  :disabled="submittingReview"
                />
              </div>
              <p v-if="updateError" class="error-message">Klarte ikke a oppdatere avvik.</p>
            </div>

            <div v-else class="card-footer">
              <DesktopButton
                :icon="Check"
                content="Gjennomga avvik"
                :on-click="() => startReviewingDeviation(deviation)"
              />
            </div>
          </article>
        </template>
      </div>
    </main>
  </div>
</template>

<style scoped>
.deviation-logs-page {
  overflow: scroll;
}

main {
  display: flex;
  margin-top: 5rem;
  padding-bottom: 5rem;
  padding-left: 2rem;
  padding-right: 2rem;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  width: 100%;
}

.deviation-logs-page-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  width: calc(3 / 5 * 100%);
}

.filter-row {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.deviation-card {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  border-radius: 1rem;
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
  padding: 1rem;
}

.deviation-card-closed {
  background-color: var(--blue-light-20);
}

.deviation-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--blue-navy-20);
}

.deviation-header-meta {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  flex-wrap: wrap;
}

.deviation-id,
.deviation-status,
.deviation-date {
  color: var(--blue-navy-80);
}

.details-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.75rem;
}

.detail-box {
  display: flex;
  flex-direction: column;
  gap: 0.2rem;
  background-color: var(--blue-light-20);
  border: 1px solid var(--blue-navy-20);
  border-radius: 0.5rem;
  padding: 0.6rem;
}

.detail-box-wide {
  grid-column: span 2;
}

.deviation-section {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.resolved-box,
.review-box {
  border: 1px solid var(--blue-navy-20);
  border-radius: 0.5rem;
  background-color: var(--blue-light-20);
  padding: 0.75rem;
}

.resolved-meta {
  color: var(--blue-navy-80);
}

.review-actions {
  display: flex;
  gap: 0.5rem;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
}

.error-message {
  color: #b42318;
  margin: 0;
}

.empty-message {
  margin: 0;
  color: var(--blue-navy-80);
}

@media (max-width: 1200px) {
  .deviation-logs-page-container {
    width: calc(4 / 5 * 100%);
  }
}

@media (max-width: 768px) {
  .deviation-logs-page-container {
    width: 100%;
  }

  .deviation-header-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .details-grid {
    grid-template-columns: 1fr;
  }

  .detail-box-wide {
    grid-column: span 1;
  }

  .review-actions {
    flex-direction: column;
  }
}
</style>
