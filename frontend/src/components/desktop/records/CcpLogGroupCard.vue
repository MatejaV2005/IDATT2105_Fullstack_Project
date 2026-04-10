<script setup lang="ts">
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import type { CcpLogsGroup, VerificationStatus } from '@/interfaces/api-interfaces'
import { Ban, Check, SkipForward } from '@lucide/vue'
import { computed } from 'vue'
import CcpLogRecordCard from './CcpLogRecordCard.vue'

const props = defineProps<{
  ccp: CcpLogsGroup
  isBulkLoading: boolean
  bulkLoadingStatus: VerificationStatus | null
  recordLoadingStatusById: Record<number, VerificationStatus | null>
}>()

const emit = defineEmits<{
  setRecordStatus: [payload: { recordId: number; verificationStatus: VerificationStatus }]
  setAllRecordsStatus: [payload: { ccpId: number; verificationStatus: VerificationStatus }]
}>()

const isAnyRecordLoadingInGroup = computed(() =>
  props.ccp.records.some((record) => props.recordLoadingStatusById[record.id]),
)

const disableRecordActions = computed(() => props.isBulkLoading)
const disableBulkActions = computed(() => props.isBulkLoading || isAnyRecordLoadingInGroup.value)

function requestSetAllRecordsStatus(verificationStatus: VerificationStatus) {
  if (disableBulkActions.value) {
    return
  }

  emit('setAllRecordsStatus', { ccpId: props.ccp.id, verificationStatus })
}
</script>

<template>
  <div class="ccp-log-group">
    <div class="ccp-log-group-header">
      <h2 class="no-margin">
        {{ props.ccp.name }}
      </h2>
      <div class="bulk-actions">
        <DesktopButton
          :icon="Ban"
          content="Reject all"
          button-color="cherry"
          :on-click="() => requestSetAllRecordsStatus('REJECTED')"
          :is-loading="props.isBulkLoading && props.bulkLoadingStatus === 'REJECTED'"
          loading-text="Rejecting all..."
          :disabled="disableBulkActions || props.ccp.records.length === 0"
        />
        <DesktopButton
          :icon="Check"
          content="Verify all"
          button-color="blue-decor"
          :on-click="() => requestSetAllRecordsStatus('VERIFIED')"
          :is-loading="props.isBulkLoading && props.bulkLoadingStatus === 'VERIFIED'"
          loading-text="Verifying all..."
          :disabled="disableBulkActions || props.ccp.records.length === 0"
        />
        <DesktopButton
          :icon="SkipForward"
          content="Skip all"
          :on-click="() => requestSetAllRecordsStatus('SKIPPED')"
          :is-loading="props.isBulkLoading && props.bulkLoadingStatus === 'SKIPPED'"
          loading-text="Skipping all..."
          :disabled="disableBulkActions || props.ccp.records.length === 0"
          button-color="boring-ghost"
        />
      </div>
    </div>

    <div class="record-list">
      <div v-if="props.ccp.records.length === 0" class="no-records-message">
        Alle logger er gjennomgatt. Bra!
      </div>
      <CcpLogRecordCard
        v-for="record in props.ccp.records"
        :key="record.id"
        :record="record"
        :is-loading="
          props.recordLoadingStatusById[record.id] !== null &&
          props.recordLoadingStatusById[record.id] !== undefined
        "
        :loading-status="props.recordLoadingStatusById[record.id] || null"
        :is-disabled="disableRecordActions"
        @set-status="emit('setRecordStatus', $event)"
      />
    </div>
  </div>
</template>

<style scoped>
.ccp-log-group {
  border-radius: 1rem;
  padding: 1rem;
  display: flex;
  gap: 1rem;
  flex-direction: column;
  border: 1px solid var(--blue-decor);
  background-color: var(--blue-decor-10);
  box-shadow: 0 8px 22px rgba(8, 43, 74, 0.08);
}

.ccp-log-group-header {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  align-items: center;
}

.bulk-actions {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.no-records-message {
  color: var(--blue-navy);
}

@media (max-width: 768px) {
  .ccp-log-group-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .bulk-actions :deep(.navy-button) {
    width: 100%;
  }
}
</style>
