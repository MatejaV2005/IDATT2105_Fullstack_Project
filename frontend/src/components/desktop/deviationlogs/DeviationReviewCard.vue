<script setup lang="ts">
import Badge from '@/components/desktop/shared/Badge.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import { Check, Save, X } from '@lucide/vue'
import type { DeviationCategory, DeviationLog } from './types'

const props = defineProps<{
  deviation: DeviationLog
  isReviewing: boolean
  reviewText: string
  submittingReview: boolean
  updateError: boolean
  formatDateTime: (value: string | null) => string
}>()

const emit = defineEmits<{
  startReview: [deviationId: number]
  cancelReview: []
  submitReview: [deviationId: number]
  updateReviewText: [value: string]
}>()

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

function getLinkedRecordText() {
  if (props.deviation.ccpRecordId !== null) {
    return `CCP logg #${props.deviation.ccpRecordId}`
  }
  if (props.deviation.routineRecordId !== null) {
    return `Rutine logg #${props.deviation.routineRecordId}`
  }

  return 'Ikke knyttet til en spesifikk logg'
}

function getReceiversText() {
  if (props.deviation.deviationReceivers.length === 0) {
    return 'Ingen eksplisitte avviksmottakere - ledere og eier mottar avviket'
  }

  return props.deviation.deviationReceivers.map((user) => user.legalName).join(', ')
}
</script>

<template>
  <article
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
      <span class="deviation-date">Opprettet {{ formatDateTime(deviation.createdAt) }}</span>
    </div>

    <div class="details-grid">
      <div class="detail-box">
        <span class="navy-subtitle">Rapportert av</span>
        <span>{{ deviation.reportedBy.legalName }}</span>
      </div>
      <div class="detail-box">
        <span class="navy-subtitle">Kobling</span>
        <span>{{ getLinkedRecordText() }}</span>
      </div>
      <div class="detail-box detail-box-wide">
        <span class="navy-subtitle">Avviksmottakere</span>
        <span>{{ getReceiversText() }}</span>
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

    <div v-else-if="isReviewing" class="deviation-section review-box">
      <span class="navy-subtitle">Forebyggende tiltak som faktisk ble gjort</span>
      <textarea
        :value="reviewText"
        class="simple-text-input"
        rows="4"
        :disabled="submittingReview"
        placeholder="Beskriv tiltaket som faktisk ble gjennomfort"
        @input="emit('updateReviewText', ($event.target as HTMLTextAreaElement).value)"
      />
      <div class="review-actions">
        <DesktopButton
          :icon="Save"
          content="Lukk avvik"
          :on-click="() => emit('submitReview', deviation.id)"
          :is-loading="submittingReview"
          loading-text="Lukker"
          :disabled="reviewText.trim().length === 0"
        />
        <DesktopButton
          :icon="X"
          content="Avbryt"
          button-color="cherry"
          :on-click="() => emit('cancelReview')"
          :disabled="submittingReview"
        />
      </div>
      <p v-if="updateError" class="error-message">Klarte ikke a oppdatere avvik.</p>
    </div>

    <div v-else class="card-footer">
      <DesktopButton
        :icon="Check"
        content="Gjennomga avvik"
        :on-click="() => emit('startReview', deviation.id)"
      />
    </div>
  </article>
</template>

<style scoped>
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

@media (max-width: 768px) {
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
