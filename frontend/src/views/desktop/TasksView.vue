<script setup lang="ts">
import Badge from '@/components/desktop/shared/Badge.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import type { TasksOverviewInfo } from '@/interfaces/api-interfaces'
import { delay } from '@/utils'
import { SendHorizonal } from '@lucide/vue'
import { onMounted, ref } from 'vue'

const resource = ref<TasksOverviewInfo | null>(null)
const loading = ref(true)
const error = ref<boolean | null>(null)

onMounted(async () => {
  try {
    // const ccpResponse = await fetch('/api/ccps/verification-count')
    // const deviationResponse = await fetch('/api/deviation-review-count')
    // if (!ccpResponse.ok || !deviationResponse.ok) {
    //   throw new Error('Failed to fetch task overview')
    // }
    // const ccpData = await ccpResponse.json()
    // const deviationData = await deviationResponse.json()
    await delay(2000)

    const data: TasksOverviewInfo = {
      remainingCcpVerifications: 10,
      remainingDeviationReviews: 8,
    }

    resource.value = data
    loading.value = false
    error.value = false
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    error.value = true
  }
})
</script>

<template>
  <SidebarPageContainer active-page="/desktop/oppgaver-oversikt">
    <div class="tasks-page-container">
      <h1 class="instrument-serif-regular no-margin">Oversikt over mine oppgaver</h1>
      <Loading v-if="loading" />
      <p v-else-if="error" class="tasks-error-message">Klarte ikke å hente oppgaver.</p>

      <template v-else-if="resource">
        <div class="task-card">
          <div class="task-card-header">
            <div class="task-card-title">Verifiser logging av <b>krtiske kontrollpunkt</b></div>
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
