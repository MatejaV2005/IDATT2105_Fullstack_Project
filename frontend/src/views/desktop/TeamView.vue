<script setup lang="ts">
import TeamMemberAddCard from '@/components/desktop/team/TeamMemberAddCard.vue'
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import Badge from '@/components/desktop/shared/Badge.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import { mockAllUsers, type NewOrganizationUserPayload } from '@/data/mockAllUsers'
import type { TeamAllInfo } from '@/interfaces/api-interfaces'
import { delay } from '@/utils'
import { Edit2, UserMinus } from '@lucide/vue'
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import Loading from '@/components/desktop/shared/Loading.vue'

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

const mockServerState = ref<TeamAllInfo>(mockData.map((user) => ({ ...user })))

const resource = ref<TeamAllInfo>([])
const loading = ref(true)
const error = ref<boolean | null>(null)
const isAddingMember = ref(false)
const addMemberError = ref(false)
const isRemovingMember = ref(false)
const removeMemberError = ref(false)

function cloneTeam(data: TeamAllInfo): TeamAllInfo {
  return data.map((user) => ({
    ...user,
    ccpAssignments: { ...user.ccpAssignments },
    routineAssignments: { ...user.routineAssignments },
    courseProgress: { ...user.courseProgress },
  }))
}

async function fetchTeam() {
  // const response = await fetch('/api/organizations/users')
  // if (!response.ok) {
  //   throw new Error(`Failed to fetch team (${response.status})`)
  // }
  // const data: TeamAllInfo = await response.json()
  await delay(400)
  resource.value = cloneTeam(mockServerState.value)
}

async function addUserToOrganization(payload: NewOrganizationUserPayload) {
  // const response = await fetch('/api/organizations/users', {
  //   method: 'POST',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify({ userId: payload.userId, orgRole: payload.orgRole }),
  // })
  // if (!response.ok) {
  //   throw new Error(`Failed to add user to organization (${response.status})`)
  // }
  await delay(500)

  const existingUser = mockServerState.value.find((entry) => entry.userId === payload.userId)
  if (existingUser) {
    existingUser.orgRole = payload.orgRole
    return
  }

  const directoryUser = mockAllUsers.find((entry) => entry.id === payload.userId)

  mockServerState.value = [
    ...mockServerState.value,
    {
      userId: payload.userId,
      legalName: directoryUser?.legalName || `Bruker ${payload.userId}`,
      orgRole: payload.orgRole,
      ccpAssignments: {
        verifier: 0,
        deviationReceiver: 0,
        performer: 0,
        deputy: 0,
      },
      routineAssignments: {
        verifier: 0,
        deviationReceiver: 0,
        performer: 0,
        deputy: 0,
      },
      mappingPointResponsibilities: 0,
      openReviewedCcpDeviations: 0,
      openReviewedRoutineDeviations: 0,
      courseProgress: {
        completed: 0,
        total: 0,
      },
    },
  ]
}

onMounted(async () => {
  try {
    await fetchTeam()
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

async function handleAddMember(payload: NewOrganizationUserPayload) {
  if (isAddingMember.value) {
    return
  }

  isAddingMember.value = true
  addMemberError.value = false

  try {
    await addUserToOrganization(payload)
    await fetchTeam()
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    addMemberError.value = true
  } finally {
    isAddingMember.value = false
  }
}

async function removeUserFromOrg(userId: number) {
  if (isRemovingMember.value) {
    return
  }

  const shouldRemove = confirm('Sikker på at du vil fjerne bruker fra organisasjonen?')
  if (!shouldRemove) {
    return
  }

  isRemovingMember.value = true
  removeMemberError.value = false

  try {
    // const response = await fetch('/api/organizations/users', {
    //   method: 'DELETE',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify({ userId }),
    // })
    // if (!response.ok) {
    //   throw new Error(`Failed to remove user from organization (${response.status})`)
    // }

    await delay(400)
    mockServerState.value = mockServerState.value.filter((entry) => entry.userId !== userId)
    await fetchTeam()
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    removeMemberError.value = true
  } finally {
    isRemovingMember.value = false
  }
}
</script>

<template>
  <SidebarPageContainer active-page="/desktop/bedrift-teamsammensetning">
    <div class="team-area-container">
      <h1 class="instrument-serif-regular no-margin">Teamsammensettning</h1>
      <TeamMemberAddCard
        :excluded-user-ids="resource.map((user) => user.userId)"
        :is-submitting="isAddingMember"
        @add="handleAddMember"
      />
      <p v-if="addMemberError" class="error-message">
        Klarte ikke å legge til medlem i organisasjonen.
      </p>
      <p v-if="removeMemberError" class="error-message">
        Klarte ikke å fjerne medlem fra organisasjonen.
      </p>
      <Loading v-if="loading" />
      <div v-for="user in resource" :key="user.userId" class="team-user">
        <div class="user-header">
          <div>
            <div class="center-content">
              <h2 class="no-margin">
                {{ user.legalName }}
              </h2>
              <Badge badge-color="navy">
                {{ user.orgRole }}
              </Badge>
            </div>
          </div>
          <DesktopButton
            :icon="UserMinus"
            content="Fjern fra organisasjon"
            :on-click="() => removeUserFromOrg(user.userId)"
            button-color="cherry"
            :disabled="isRemovingMember"
          />
        </div>

        <div class="user-info-grid">
          <div class="user-info-item">
            <span class="navy-subtitle">Opplæring</span>
            <span
              >Fullført: {{ user.courseProgress.completed }} / {{ user.courseProgress.total }}</span
            >
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

.error-message {
  color: #b42318;
  margin: 0;
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
