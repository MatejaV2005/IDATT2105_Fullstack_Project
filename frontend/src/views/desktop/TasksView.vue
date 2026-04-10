<script setup lang="ts">
import api from '@/api/api'
import Badge from '@/components/desktop/shared/Badge.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import { useOrgSession } from '@/composables/useOrgSession'
import type { TasksOverviewInfo } from '@/interfaces/api-interfaces'
import { SendHorizonal } from '@lucide/vue'
import { ref, watch } from 'vue'

const resource = ref<TasksOverviewInfo | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

const { claims } = useOrgSession()
let activeFetchId = 0

function getErrorMessage(err: unknown, fallback: string) {
  const status = (err as { response?: { status?: number } }).response?.status

  if (status === 403) {
    return 'Du har ikke tilgang til oppgaveoversikten i denne organisasjonen.'
  }

  return fallback
}

async function fetchTaskOverview() {
  const fetchId = ++activeFetchId
  loading.value = true
  error.value = null

  try {
    const [ccpResponse, deviationResponse] = await Promise.all([
      api.get<number>('/ccps/verification-count'),
      api.get<number>('/deviations/deviation-review-count'),
    ])

    if (fetchId !== activeFetchId) {
      return
    }

    resource.value = {
      remainingCcpVerifications: Number(ccpResponse.data) || 0,
      remainingDeviationReviews: Number(deviationResponse.data) || 0,
    }
  } catch (err) {
    if (fetchId === activeFetchId) {
      resource.value = null
      error.value = getErrorMessage(err, 'Klarte ikke å hente oppgaver.')
      console.error(err)
    }
  } finally {
    if (fetchId === activeFetchId) {
      loading.value = false
    }
  }
}

watch(() => claims.value?.orgId ?? null, () => {
  void fetchTaskOverview()
}, { immediate: true })
</script>

<template>
  <SidebarPageContainer active-page="/desktop/oppgaver-oversikt">
    <div class="tasks-page-container">
      <h1 class="instrument-serif-regular no-margin">Oversikt over mine oppgaver</h1>
      <Loading v-if="loading" />
      <p v-else-if="error" class="tasks-error-message">{{ error }}</p>

      <template v-else-if="resource">
        <div class="task-card">
          <div class="task-card-header">
            <div class="task-card-title">Verifiser logging av <b>kritiske kontrollpunkt</b></div>
            <Badge badge-color="cherry">
              <b>{{ resource.remainingCcpVerifications }}</b> CCP logger som verifiseres
            </Badge>
          </div>
          <RouterLink class="task-card-footer" to="/desktop/oppgaver-oversikt/kontrollpunkt-logger">
            <DesktopButton :icon="SendHorizonal" content="Gå til logging av CCP's" />
          </RouterLink>
        </div>

        <div class="task-card">
          <div class="task-card-header">
            <div class="task-card-title">Gjennomgang av rapporterte <b>avvik</b></div>
            <Badge badge-color="cherry">
              <b>{{ resource.remainingDeviationReviews }}</b> avvik som må gjennomgås
            </Badge>
          </div>
          <RouterLink class="task-card-footer" to="/desktop/oppgaver-oversikt/avvik">
            <DesktopButton :icon="SendHorizonal" content="Gå til rapporterte avvik" />
          </RouterLink>
        </div>
      </template>
    </div>
  </SidebarPageContainer>
</template>
<style scoped>
.tasks-page-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border-radius: 1rem;
  padding: 1rem;
  margin-top: 2rem;
}

.task-card-footer {
  display: flex;
  justify-content: end;
}

.task-card {
  background-color: var(--white-greek);
  border: 1px solid var(--blue-navy-40);
  border-radius: 1rem;
  padding: 1rem;
  height: 12rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 1rem;
  overflow-wrap: anywhere;
}

.task-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--blue-navy-40);
}

.task-card-title {
  display: flex;
  gap: 0.5rem;
  align-items: center;
}

.tasks-error-message {
  color: #b42318;
  margin: 0;
}

@media (max-width: 1200px) {
  .tasks-page-container {
    margin-top: 1rem;
    padding: 0.75rem;
  }

  .task-card {
    height: auto;
    min-height: 12rem;
  }
}

@media (max-width: 768px) {
  .tasks-page-container {
    padding: 0.5rem;
  }

  .task-card {
    padding: 0.75rem;
    min-height: 0;
  }

  .task-card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 0.75rem;
  }

  .task-card-title {
    flex-wrap: wrap;
    justify-content: flex-start;
  }

  .task-card-footer {
    width: 100%;
    justify-content: stretch;
  }

  .task-card-footer :deep(.navy-button) {
    width: 100%;
  }
}
</style>
