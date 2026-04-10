<script setup lang="ts">
import DeviationLogsFilter from '@/components/desktop/deviationlogs/DeviationLogsFilter.vue'
import DeviationReviewCard from '@/components/desktop/deviationlogs/DeviationReviewCard.vue'
import type {
  DeviationLog,
  DeviationReviewStatus,
  DeviationUser,
  UpdateDeviationPayload,
} from '@/components/desktop/deviationlogs/types'
import Loading from '@/components/desktop/shared/Loading.vue'
import Paginator from '@/components/desktop/shared/Paginator.vue'
import { delay } from '@/utils'
import { computed, onMounted, ref } from 'vue'

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

function startReviewingDeviation(deviationId: number) {
  if (submittingReview.value) {
    return
  }

  const deviation = resource.value.find((entry) => entry.id === deviationId)
  if (!deviation || deviation.reviewStatus === 'CLOSED') {
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

        <DeviationLogsFilter
          :review-filter="reviewFilter"
          @set-filter="(filter) => (reviewFilter = filter)"
        />

        <Loading v-if="loading" />
        <p v-else-if="fetchError" class="error-message">Klarte ikke a hente avvik.</p>

        <template v-else>
          <p v-if="filteredResource.length === 0" class="empty-message">
            Ingen avvik i valgt visning.
          </p>

          <DeviationReviewCard
            v-for="deviation in filteredResource"
            :key="deviation.id"
            :deviation="deviation"
            :is-reviewing="reviewingDeviationId === deviation.id"
            :review-text="reviewingDeviationId === deviation.id ? reviewText : ''"
            :submitting-review="submittingReview"
            :update-error="reviewingDeviationId === deviation.id && updateError"
            :format-date-time="formatDateTime"
            @start-review="startReviewingDeviation"
            @cancel-review="cancelReviewingDeviation"
            @submit-review="submitDeviationReview"
            @update-review-text="(value) => (reviewText = value)"
          />
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
}
</style>
