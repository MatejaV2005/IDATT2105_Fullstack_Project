<script setup lang="ts">
import DeviationLogsFilter from '@/components/desktop/deviationlogs/DeviationLogsFilter.vue'
import DeviationReviewCard from '@/components/desktop/deviationlogs/DeviationReviewCard.vue'
import api from '@/api/api'
import type {
  DeviationCategory,
  DeviationLog,
  DeviationReviewStatus,
  UpdateDeviationPayload,
} from '@/components/desktop/deviationlogs/types'
import { useOrgSession } from '@/composables/useOrgSession'
import Loading from '@/components/desktop/shared/Loading.vue'
import Paginator from '@/components/desktop/shared/Paginator.vue'
import { computed, ref, watch } from 'vue'

interface ApiDeviationResponse {
  id: number
  category: string
  reviewStatus: string
  createdAt: string
  reviewedAt: string | null
  reportedById: number | null
  reportedByName: string | null
  reviewedById: number | null
  reviewedByName: string | null
  whatWentWrong: string
  immediateActionTaken: string
  potentialCause: string
  potentialPreventativeMeasure: string
  preventativeMeasureActuallyTaken: string
}

const reviewFilter = ref<DeviationReviewStatus | 'ALL'>('OPEN')
const loading = ref(true)
const fetchError = ref<string | null>(null)
const updateError = ref(false)
const resource = ref<DeviationLog[]>([])
const { claims } = useOrgSession()
let activeFetchId = 0

function normalizeCategory(category: string): DeviationCategory {
  if (category === 'IK_MAT' || category === 'IK_ALKOHOL' || category === 'OTHER') {
    return category
  }

  return 'OTHER'
}

const reviewingDeviationId = ref<number | null>(null)
const submittingReview = ref(false)
const reviewText = ref('')

const filteredResource = computed(() => {
  if (reviewFilter.value === 'ALL') {
    return resource.value
  }

  return resource.value.filter((entry) => entry.reviewStatus === reviewFilter.value)
})

function mapApiDeviation(apiDeviation: ApiDeviationResponse): DeviationLog {
  return {
    id: apiDeviation.id,
    ccpRecordId: null,
    routineRecordId: null,
    category: normalizeCategory(apiDeviation.category),
    reportedBy: {
      id: apiDeviation.reportedById ?? 0,
      legalName: apiDeviation.reportedByName ?? 'Ukjent',
      accessLevel: 'WORKER',
    },
    reviewStatus: apiDeviation.reviewStatus === 'CLOSED' ? 'CLOSED' : 'OPEN',
    reviewedBy:
      apiDeviation.reviewedById !== null || apiDeviation.reviewedByName
        ? {
            id: apiDeviation.reviewedById ?? 0,
            legalName: apiDeviation.reviewedByName ?? 'Ukjent',
            accessLevel: 'WORKER',
          }
        : null,
    reviewedAt: apiDeviation.reviewedAt,
    whatWentWrong: apiDeviation.whatWentWrong,
    immediateActionTaken: apiDeviation.immediateActionTaken,
    potentialCause: apiDeviation.potentialCause,
    potentialPreventativeMeasure: apiDeviation.potentialPreventativeMeasure,
    preventativeMeasureActuallyTaken: apiDeviation.preventativeMeasureActuallyTaken ?? '',
    deviationReceivers: [],
    createdAt: apiDeviation.createdAt,
  }
}

function getErrorMessage(err: unknown, fallback: string) {
  const status = (err as { response?: { status?: number } }).response?.status

  if (status === 403) {
    return 'Du har ikke tilgang til mottatte avvik.'
  }

  return fallback
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
  const fetchId = ++activeFetchId
  loading.value = true
  fetchError.value = null

  try {
    const response = await api.get<ApiDeviationResponse[]>('/deviations/received')
    if (fetchId !== activeFetchId) {
      return
    }

    resource.value = Array.isArray(response.data) ? response.data.map(mapApiDeviation) : []
  } catch (err) {
    if (fetchId === activeFetchId) {
      resource.value = []
      fetchError.value = getErrorMessage(err, 'Klarte ikke a hente avvik.')
      console.error(err)
    }
  } finally {
    if (fetchId === activeFetchId) {
      loading.value = false
    }
  }
}

async function reviewDeviation(deviationId: number, payload: UpdateDeviationPayload) {
  await api.put(`/deviations/${deviationId}`, payload)
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
      preventativeMeasureActuallyTaken: reviewText.value.trim(),
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

watch(() => claims.value?.orgId ?? null, () => {
  void fetchDeviations()
}, { immediate: true })
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
        <p v-else-if="fetchError" class="error-message">{{ fetchError }}</p>

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
