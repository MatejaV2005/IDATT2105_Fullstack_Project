<script setup lang="ts">
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import Badge from '@/components/desktop/shared/Badge.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import type { TeamAllInfo } from '@/interfaces/api-interfaces'
import { delay } from '@/utils'
import { Edit2 } from '@lucide/vue'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const mockData: TeamAllInfo = [
  {
    userId: 11,
    legalName: 'Mona Jul',
    orgRole: 'OWNER',
    ccpAssignments: {
      verifier: 3,
      deviationReceiver: 2,
      performer: 1,
      deputy: 0,
    },
    routineAssignments: {
      verifier: 2,
      deviationReceiver: 2,
      performer: 4,
      deputy: 1,
    },
    mappingPointResponsibilities: 3,
    openReviewedCcpDeviations: 1,
    openReviewedRoutineDeviations: 2,
    courseProgress: {
      completed: 4,
      total: 5,
    },
  },
  {
    userId: 12,
    legalName: 'Jagland',
    orgRole: 'MANAGER',
    ccpAssignments: {
      verifier: 4,
      deviationReceiver: 1,
      performer: 2,
      deputy: 1,
    },
    routineAssignments: {
      verifier: 3,
      deviationReceiver: 0,
      performer: 3,
      deputy: 2,
    },
    mappingPointResponsibilities: 2,
    openReviewedCcpDeviations: 0,
    openReviewedRoutineDeviations: 1,
    courseProgress: {
      completed: 5,
      total: 5,
    },
  },
  {
    userId: 13,
    legalName: 'Ane Brevik',
    orgRole: 'WORKER',
    ccpAssignments: {
      verifier: 0,
      deviationReceiver: 1,
      performer: 2,
      deputy: 1,
    },
    routineAssignments: {
      verifier: 1,
      deviationReceiver: 1,
      performer: 2,
      deputy: 0,
    },
    mappingPointResponsibilities: 1,
    openReviewedCcpDeviations: 2,
    openReviewedRoutineDeviations: 1,
    courseProgress: {
      completed: 2,
      total: 5,
    },
  },
  {
    userId: 14,
    legalName: 'Ola Svenneby',
    orgRole: 'WORKER',
    ccpAssignments: {
      verifier: 1,
      deviationReceiver: 2,
      performer: 2,
      deputy: 1,
    },
    routineAssignments: {
      verifier: 2,
      deviationReceiver: 1,
      performer: 2,
      deputy: 0,
    },
    mappingPointResponsibilities: 4,
    openReviewedCcpDeviations: 1,
    openReviewedRoutineDeviations: 0,
    courseProgress: {
      completed: 3,
      total: 5,
    },
  },
]

const resource = ref<TeamAllInfo>([])
const loading = ref(true)
const error = ref<boolean | null>(null)

onMounted(async () => {
  try {
    // const response = await fetch('/api/users/team')
    // const data = await response.json()
    await delay(2000)
    const data = mockData
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

function goToUser(userId: number) {
  router.push(`/desktop/users/${userId}`)
}
</script>

<template>
  <SidebarPageContainer active-page="/desktop/bedrift-teamsammensetning">
    <div class="team-area-container">
      <h1 class="instrument-serif-regular no-margin">
        Teamsammensettning
      </h1>
      <Loading v-if="loading"/>
      <div
        v-for="user in resource"
        :key="user.userId"
        class="team-user"
      >
        <div class="user-header">
          <div>
            <RouterLink
              class="user-link"
              :to="`/desktop/users/${user.userId}`"
            >
              <h2 class="no-margin">
                {{ user.legalName }}
              </h2>
              <Badge badge-color="navy">
                {{ user.orgRole }}
              </Badge>
            </RouterLink>
          </div>
          <DesktopButton
            :icon="Edit2"
            content="Rediger"
            :on-click="() => goToUser(user.userId)"
          />
        </div>

        <div class="user-info-grid">
          <div class="user-info-item">
            <span class="navy-subtitle">Opplæring</span>
            <span>Fullført: {{ user.courseProgress.completed }} / {{ user.courseProgress.total }}</span>
          </div>
          <div class="user-info-item">
            <span class="navy-subtitle">CCP roller</span>
            <div class="mini-stat-grid">
              <span>Verifiserer: {{ user.ccpAssignments.verifier }}</span>
              <span>Avviksmottaker: {{ user.ccpAssignments.deviationReceiver }}</span>
              <span>Utfører: {{ user.ccpAssignments.performer }}</span>
              <span>Vikar: {{ user.ccpAssignments.deputy }}</span>
            </div>
          </div>
          <div class="user-info-item">
            <span class="navy-subtitle">Rutine roller</span>
            <div class="mini-stat-grid">
              <span>Verifiserer: {{ user.routineAssignments.verifier }}</span>
              <span>Avviksmottaker: {{ user.routineAssignments.deviationReceiver }}</span>
              <span>Utfører: {{ user.routineAssignments.performer }}</span>
              <span>Vikar: {{ user.routineAssignments.deputy }}</span>
            </div>
          </div>
          <div class="user-info-item">
            <span class="navy-subtitle">IK ansvar</span>
            <span>Kartleggingspunkter: {{ user.mappingPointResponsibilities }}</span>
            <span>Åpne avvik (CCP): {{ user.openReviewedCcpDeviations }}</span>
            <span>Åpne avvik (rutiner): {{ user.openReviewedRoutineDeviations }}</span>
            <span>
              Totalt åpne avvik:
              {{ user.openReviewedCcpDeviations + user.openReviewedRoutineDeviations }}
            </span>
          </div>
        </div>
      </div>
    </div>
  </SidebarPageContainer>
</template>

<style scoped>
.team-area-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border-radius: 1rem;
  padding: 1rem;
  margin-top: 2rem;
}

.team-user {
  background-color: var(--white-greek);
  border-radius: 0.75rem;
  border: 1px solid var(--blue-navy-40);
  padding: 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.user-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 0.75rem;
  border-bottom: 1px solid var(--blue-navy-40);
  padding-bottom: 0.75rem;
}

.user-badges {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
  margin-top: 0.5rem;
}

.user-link {
  color: var(--blue-navy);
  text-decoration: none;
  display: flex;
  gap: 1rem;
}

.user-link:hover,
.user-link:focus {
  text-decoration: underline;
}

.user-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.user-info-item {
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
  border: 1px solid var(--blue-navy-20);
  border-radius: 0.5rem;
  padding: 0.6rem;
  background-color: var(--blue-light-20);
}

.mini-stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 0.25rem 0.75rem;
}

@media (max-width: 1200px) {
  .team-area-container {
    margin-top: 1rem;
    padding: 0.5rem;
  }
}

@media (max-width: 768px) {
  .team-area-container {
    padding: 0;
  }

  .team-user {
    padding: 0.6rem;
  }

  .user-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .user-info-grid {
    grid-template-columns: 1fr;
  }

  .mini-stat-grid {
    grid-template-columns: 1fr;
  }
}
</style>
