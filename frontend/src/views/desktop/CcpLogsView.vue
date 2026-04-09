<script setup lang="ts">
import CcpLogGroupCard from '@/components/desktop/records/CcpLogGroupCard.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import Paginator from '@/components/desktop/shared/Paginator.vue'
import type { CcpLogsAllInfo, VerificationStatus } from '@/interfaces/api-interfaces'
import { delay } from '@/utils'
import { onMounted, ref } from 'vue'

const resource = ref<CcpLogsAllInfo>([])
const loading = ref(true)
const error = ref<boolean | null>(null)
const changingStatusByRecordId = ref<Record<number, VerificationStatus | null>>({})
const changingStatusByGroupId = ref<Record<number, VerificationStatus | null>>({})
const mutateError = ref('')

const mockData: CcpLogsAllInfo = [
  {
    id: 1,
    name: 'Kjøleskap temperatur',
    records: [
      {
        id: 11,
        value: 2.4,
        min: 2,
        max: 4,
        unit: 'C',
        comment: 'Kjøleskapet lager en rar lyd',
        performedBy: {
          id: 5,
          legalName: 'Per Willy Amundsen',
        },
      },
      {
        id: 12,
        value: 8,
        min: 2,
        max: 4,
        unit: 'C',
        comment: 'Midlertidig høy temperatur etter varelevering',
        performedBy: {
          id: 4,
          legalName: 'Ane Brevik',
        },
      },
    ],
  },
  {
    id: 2,
    name: 'Varmholding buffet',
    records: [
      {
        id: 21,
        value: 66,
        min: 60,
        max: 90,
        unit: 'C',
        comment: 'Normal måling',
        performedBy: {
          id: 7,
          legalName: 'Mona Jul',
        },
      },
    ],
  },
]

// ! Purely for mocking db
function cloneCcpLogs(data: CcpLogsAllInfo): CcpLogsAllInfo {
  return data.map((group) => ({
    ...group,
    records: group.records.map((record) => ({
      ...record,
      performedBy: { ...record.performedBy },
    })),
  }))
}

const mockServerState = ref<CcpLogsAllInfo>(cloneCcpLogs(mockData)) // we use a state to ensure we can mock removing logs

async function fetchCcpLogs() {
  // const response = await fetch('/api/cpps/logs')
  // if (!response.ok) {
  //   throw new Error(`Failed to fetch ccp logs (${response.status})`)
  // }
  // const data: CcpLogsAllInfo = await response.json()

  await delay(400)
  resource.value = cloneCcpLogs(mockServerState.value)
}

async function setVerificationStatus(recordId: number, verificationStatus: VerificationStatus) {
  // const response = await fetch('/api/cpps/logs/set-verification-status', {
  //   method: 'PUT',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify({
  //     id: recordId,
  //     verificationStatus: verificationStatus,
  //   }),
  // })
  // if (!response.ok) {
  //   throw new Error(`Failed to update verification status (${response.status})`)
  // }

  await delay(700)
  mockServerState.value = mockServerState.value.map((group) => ({
    ...group,
    records: group.records.filter((record) => record.id !== recordId),
  }))
}

onMounted(async () => {
  try {
    await fetchCcpLogs()
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

async function updateSingleRecordStatus(payload: {
  recordId: number
  verificationStatus: VerificationStatus
}) {
  const { recordId, verificationStatus } = payload
  if (
    changingStatusByRecordId.value[recordId] ||
    Object.keys(changingStatusByGroupId.value).length > 0
  ) {
    return
  }

  mutateError.value = ''
  changingStatusByRecordId.value[recordId] = verificationStatus

  try {
    await setVerificationStatus(recordId, verificationStatus)
    await fetchCcpLogs()
  } catch (err) {
    mutateError.value = 'Klarte ikke å oppdatere verifiseringsstatus. Prøv igjen.'
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
  } finally {
    delete changingStatusByRecordId.value[recordId]
  }
}

async function updateGroupRecordsStatus(payload: {
  ccpId: number
  verificationStatus: VerificationStatus
}) {
  const { ccpId, verificationStatus } = payload
  if (
    changingStatusByGroupId.value[ccpId] ||
    Object.keys(changingStatusByGroupId.value).length > 0 ||
    Object.keys(changingStatusByRecordId.value).length > 0
  ) {
    return
  }

  const group = resource.value.find((entry) => entry.id === ccpId)
  if (!group || group.records.length === 0) {
    return
  }

  mutateError.value = ''
  changingStatusByGroupId.value[ccpId] = verificationStatus

  try {
    await Promise.all(
      group.records.map((record) => setVerificationStatus(record.id, verificationStatus)),
    )
    await fetchCcpLogs()
  } catch (err) {
    mutateError.value = 'Klarte ikke å oppdatere verifiseringsstatus for alle logger. Prøv igjen.'
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
  } finally {
    delete changingStatusByGroupId.value[ccpId]
  }
}
</script>

<template>
  <div class="ccp-logs-page">
    <main>
      <div class="ccp-logs-container">
        <Paginator />
        <h1 class="instrument-serif-regular no-margin">Kritiske kontrollpunkt logger</h1>
        <hr class="navy-hr" />

        <Loading v-if="loading" />
        <p v-else-if="error" class="error-message">Klarte ikke a hente logger.</p>

        <template v-else>
          <p v-if="mutateError" class="error-message">
            {{ mutateError }}
          </p>

          <CcpLogGroupCard
            v-for="ccp in resource"
            :key="ccp.id"
            :ccp="ccp"
            :is-bulk-loading="
              changingStatusByGroupId[ccp.id] !== null &&
              changingStatusByGroupId[ccp.id] !== undefined
            "
            :bulk-loading-status="changingStatusByGroupId[ccp.id] || null"
            :record-loading-status-by-id="changingStatusByRecordId"
            @set-record-status="updateSingleRecordStatus"
            @set-all-records-status="updateGroupRecordsStatus"
          />
        </template>
      </div>
    </main>
  </div>
</template>

<style scoped>
.ccp-logs-page {
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

.ccp-logs-container {
  width: calc(3 / 5 * 100%);
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.error-message {
  color: #b42318;
  margin: 0;
}

hr {
  border-color: var(--blue-navy-40);
  border-width: 1px;
}

@media (max-width: 768px) {
  .ccp-logs-container {
    width: 100%;
  }
}
</style>
