<script setup lang="ts">
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import UserBadge from '@/components/desktop/shared/UserBadge.vue'
import type { CcpLogRecord, VerificationStatus } from '@/interfaces/api-interfaces'
import { Ban, Check, SkipForward } from '@lucide/vue'

const props = defineProps<{
  record: CcpLogRecord
  isLoading: boolean
  loadingStatus: VerificationStatus | null
  isDisabled: boolean
}>()

const emit = defineEmits<{
  setStatus: [payload: { recordId: number; verificationStatus: VerificationStatus }]
}>()

function isOutsideRange(record: CcpLogRecord) {
  return record.value < record.min || record.value > record.max
}

function getRangeStatusLabel(record: CcpLogRecord) {
  return isOutsideRange(record) ? 'Utenfor kritisk omrade' : 'Innenfor kritisk omrade'
}

function requestVerificationStatus(verificationStatus: VerificationStatus) {
  if (props.isDisabled || props.isLoading) {
    return
  }

  emit('setStatus', { recordId: props.record.id, verificationStatus })
}
</script>

<template>
  <div class="record-card">
    <div class="record-overview">
      <div class="record-metric-card">
        <span class="record-label">Malt verdi</span>
        <p class="record-value no-margin">{{ props.record.value }} {{ props.record.unit }}</p>
      </div>

      <div class="record-metric-card">
        <span class="record-label">Kritisk omrade</span>
        <p class="record-range no-margin">
          {{ props.record.min }} - {{ props.record.max }} {{ props.record.unit }}
        </p>
      </div>

      <div class="record-metric-card">
        <span class="record-label">Status</span>
        <span
          :class="[
            'record-status-pill',
            isOutsideRange(props.record) ? 'record-status-pill-danger' : 'record-status-pill-ok',
          ]"
        >
          {{ getRangeStatusLabel(props.record) }}
        </span>
      </div>

      <div class="record-performed-by">
        <span class="record-label">Utfort av</span>
        <UserBadge
          :name="props.record.performedBy.legalName"
          :user-id="props.record.performedBy.id"
        />
      </div>
    </div>

    <div class="record-comment-block">
      <span class="record-label">Kommentar</span>
      <p class="record-comment no-margin">
        {{ props.record.comment || 'Ingen kommentar' }}
      </p>
    </div>

    <div class="record-actions">
      <DesktopButton
        :icon="Ban"
        content="Reject"
        button-color="cherry"
        :on-click="() => requestVerificationStatus('REJECTED')"
        :is-loading="props.isLoading && props.loadingStatus === 'REJECTED'"
        loading-text="Rejecting..."
        :disabled="props.isDisabled || props.isLoading"
      />
      <DesktopButton
        :icon="Check"
        content="Verify"
        button-color="blue-decor"
        :on-click="() => requestVerificationStatus('VERIFIED')"
        :is-loading="props.isLoading && props.loadingStatus === 'VERIFIED'"
        loading-text="Verifying..."
        :disabled="props.isDisabled || props.isLoading"
      />
      <DesktopButton
        :icon="SkipForward"
        content="Skip"
        :on-click="() => requestVerificationStatus('SKIPPED')"
        :is-loading="props.isLoading && props.loadingStatus === 'SKIPPED'"
        loading-text="Skipping..."
        button-color="boring-ghost"
        :disabled="props.isDisabled || props.isLoading"
      />
    </div>
  </div>
</template>

<style scoped>
.record-card {
  padding: 1rem;
  border-radius: 0.5rem;
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
  display: flex;
  flex-direction: column;
  gap: 1rem;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}

.record-overview {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr)) minmax(220px, auto);
  gap: 1rem;
}

.record-metric-card,
.record-performed-by {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 0.75rem;
  border-radius: 0.5rem;
  border: 1px solid var(--blue-navy-20);
  background-color: var(--blue-light-10);
}

.record-label {
  font-size: 0.8rem;
  letter-spacing: 0.02em;
  text-transform: uppercase;
  color: var(--blue-navy-60);
}

.record-value {
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--black-no-face);
}

.record-range {
  font-weight: 600;
  color: var(--blue-navy);
}

.record-status-pill {
  width: fit-content;
  border-radius: 999px;
  padding: 0.2rem 0.55rem;
  font-size: 0.8rem;
  font-weight: 700;
  line-height: 1.3;
}

.record-status-pill-ok {
  color: #165d2d;
  background-color: var(--green-light-20);
}

.record-status-pill-danger {
  color: #b42318;
  background-color: #fde9ea;
}

.record-comment-block {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  padding: 0.75rem;
  border-radius: 0.5rem;
  border: 1px solid var(--blue-navy-20);
  background-color: var(--white-greek);
}

.record-comment {
  color: var(--black-no-face);
}

.record-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  justify-content: end;
}

@media (max-width: 1200px) {
  .record-overview {
    grid-template-columns: 1fr 1fr;
  }
}

@media (max-width: 768px) {
  .record-overview {
    grid-template-columns: 1fr;
  }

  .record-performed-by {
    align-items: flex-start;
  }

  .record-actions :deep(.navy-button) {
    width: 100%;
  }
}
</style>
