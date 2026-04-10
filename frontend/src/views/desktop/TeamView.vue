<script setup lang="ts">
import api from '@/api/api'
import TeamMemberAddCard from '@/components/desktop/team/TeamMemberAddCard.vue'
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import Badge from '@/components/desktop/shared/Badge.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import { useOrgSession } from '@/composables/useOrgSession'
import type { NewOrganizationUserPayload, TeamAllInfo, TeamDirectoryUser } from '@/interfaces/api-interfaces'
import { UserMinus } from '@lucide/vue'
import { computed, ref, watch } from 'vue'
const { claims, currentUserRole } = useOrgSession()

const resource = ref<TeamAllInfo>([])
const availableUsers = ref<TeamDirectoryUser[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const isAddingMember = ref(false)
const addMemberError = ref(false)
const isRemovingMember = ref(false)
const removeMemberError = ref(false)
const directoryErrorMessage = ref('')
const isLoadingUsers = ref(false)

const canManageMembers = computed(() => currentUserRole.value === 'OWNER')
let activeFetchId = 0

function getErrorMessage(err: unknown, fallback: string) {
  const status = (err as { response?: { status?: number } }).response?.status

  if (status === 403) {
    return 'Du har ikke tilgang til teamsiden. Kun owner og manager kan åpne denne siden.'
  }

  return fallback
}

async function fetchTeam() {
  const fetchId = ++activeFetchId
  loading.value = true
  error.value = null
  directoryErrorMessage.value = ''
  resource.value = []
  availableUsers.value = []

  try {
    const teamResponse = await api.get<TeamAllInfo>('/organizations/team-overview')
    if (fetchId !== activeFetchId) {
      return
    }

    resource.value = teamResponse.data

    if (canManageMembers.value) {
      isLoadingUsers.value = true
      try {
        const directoryResponse = await api.get<TeamDirectoryUser[]>('/organizations/user-directory')
        if (fetchId !== activeFetchId) {
          return
        }
        availableUsers.value = directoryResponse.data
      } catch (err) {
        if (fetchId !== activeFetchId) {
          return
        }
        availableUsers.value = []
        directoryErrorMessage.value = getErrorMessage(err, 'Klarte ikke å hente brukere.')
      } finally {
        if (fetchId === activeFetchId) {
          isLoadingUsers.value = false
        }
      }
    } else {
      availableUsers.value = []
      isLoadingUsers.value = false
    }
  } catch (err) {
    if (fetchId !== activeFetchId) {
      return
    }

    resource.value = []
    availableUsers.value = []
    error.value = getErrorMessage(err, 'Klarte ikke å hente teamsammensetningen.')
    isLoadingUsers.value = false
  } finally {
    if (fetchId === activeFetchId) {
      loading.value = false
    }
  }
}

async function handleAddMember(payload: NewOrganizationUserPayload) {
  if (isAddingMember.value) {
    return
  }

  isAddingMember.value = true
  addMemberError.value = false

  try {
    await api.post('/organizations/users', {
      userId: payload.userId,
      role: payload.orgRole,
    })
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
    await api.delete('/organizations/users', {
      data: { userId },
    })
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

watch(
  () => [claims.value?.orgId, currentUserRole.value] as const,
  ([orgId]) => {
    if (!orgId) {
      resource.value = []
      availableUsers.value = []
      loading.value = false
      error.value = null
      return
    }

    void fetchTeam()
  },
  { immediate: true },
)
</script>

<template>
  <SidebarPageContainer active-page="/desktop/bedrift-teamsammensetning">
    <div class="team-area-container">
      <h1 class="instrument-serif-regular no-margin">Teamsammensettning</h1>
      <TeamMemberAddCard
        v-if="canManageMembers"
        :excluded-user-ids="resource.map((user) => user.userId)"
        :is-submitting="isAddingMember"
        :available-users="availableUsers"
        :is-loading-users="isLoadingUsers"
        :users-error-message="directoryErrorMessage"
        @add="handleAddMember"
      />
      <p v-if="addMemberError" class="error-message">
        Klarte ikke å legge til medlem i organisasjonen.
      </p>
      <p v-if="removeMemberError" class="error-message">
        Klarte ikke å fjerne medlem fra organisasjonen.
      </p>
      <p
        v-if="error"
        class="error-message"
      >
        {{ error }}
      </p>
      <Loading v-else-if="loading" />
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
            v-if="canManageMembers"
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
