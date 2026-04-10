<script setup lang="ts">
import api from '@/api/api'
import TeamMemberAddCard from '@/components/desktop/team/TeamMemberAddCard.vue'
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import Badge from '@/components/desktop/shared/Badge.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import { useOrgSession } from '@/composables/useOrgSession'
import type {
  NewOrganizationUserPayload,
  TeamAllInfo,
  TeamDirectoryUser,
} from '@/interfaces/api-interfaces'
import type { OrgAccessLevel } from '@/interfaces/util-interfaces'
import { Save, UserMinus } from '@lucide/vue'
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
const roleUpdateError = ref(false)
const isUpdatingRoleUserId = ref<number | null>(null)
const directoryErrorMessage = ref('')
const isLoadingUsers = ref(false)
const draftRoles = ref<Record<number, OrgAccessLevel>>({})

const canManageMembers = computed(() => currentUserRole.value === 'OWNER')
let activeFetchId = 0

function getErrorMessage(err: unknown, fallback: string) {
  const status = (err as { response?: { status?: number } }).response?.status

  if (status === 403) {
    return 'Du har ikke tilgang til teamsiden. Kun owner og manager kan åpne denne siden.'
  }

  return fallback
}

function syncDraftRoles() {
  draftRoles.value = Object.fromEntries(
    resource.value.map((user) => [user.userId, user.orgRole as OrgAccessLevel]),
  )
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
    syncDraftRoles()

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
    draftRoles.value = {}
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

async function updateUserRole(userId: number) {
  if (isUpdatingRoleUserId.value !== null) {
    return
  }

  const user = resource.value.find((entry) => entry.userId === userId)
  const nextRole = draftRoles.value[userId]

  if (!user || !nextRole || user.orgRole === nextRole) {
    return
  }

  isUpdatingRoleUserId.value = userId
  roleUpdateError.value = false

  try {
    await api.patch('/organizations/users', {
      userId,
      role: nextRole,
    })
    await fetchTeam()
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    roleUpdateError.value = true
    draftRoles.value[userId] = user.orgRole as OrgAccessLevel
  } finally {
    isUpdatingRoleUserId.value = null
  }
}

watch(
  () => [claims.value?.orgId, currentUserRole.value] as const,
  ([orgId]) => {
    if (!orgId) {
      resource.value = []
      availableUsers.value = []
      draftRoles.value = {}
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
      <p v-if="roleUpdateError" class="error-message">
        Klarte ikke å oppdatere rolle i organisasjonen.
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
              <Badge
                v-if="!canManageMembers"
                badge-color="navy"
              >
                {{ user.orgRole }}
              </Badge>
              <div
                v-else
                class="role-edit"
              >
                <div class="role-select-shell role-edit__field">
                  <select
                    v-model="draftRoles[user.userId]"
                    class="simple-text-input role-edit__select"
                    :disabled="isUpdatingRoleUserId === user.userId"
                  >
                    <option value="OWNER">OWNER</option>
                    <option value="MANAGER">MANAGER</option>
                    <option value="WORKER">WORKER</option>
                  </select>
                </div>
                <DesktopButton
                  :icon="Save"
                  content="Lagre rolle"
                  button-color="grey"
                  :on-click="() => updateUserRole(user.userId)"
                  :disabled="
                    isUpdatingRoleUserId !== null ||
                    draftRoles[user.userId] === undefined ||
                    draftRoles[user.userId] === user.orgRole
                  "
                  :is-loading="isUpdatingRoleUserId === user.userId"
                  loading-text="Lagrer"
                />
              </div>
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

.role-edit {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.role-select-shell {
  position: relative;
  min-width: 11rem;
}

.role-select-shell::after {
  content: '';
  position: absolute;
  top: 50%;
  right: 0.9rem;
  width: 0.5rem;
  height: 0.5rem;
  border-right: 2px solid var(--blue-navy);
  border-bottom: 2px solid var(--blue-navy);
  transform: translateY(-70%) rotate(45deg);
  pointer-events: none;
}

.role-edit__field {
  flex: 0 1 12rem;
}

.role-edit__select {
  min-width: 11rem;
  width: 100%;
  appearance: none;
  border: 1px solid var(--blue-navy-40);
  border-radius: 0.75rem;
  background-color: var(--blue-light-20);
  color: var(--blue-navy);
  padding: 0.8rem 2.6rem 0.8rem 0.95rem;
  font-weight: 600;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.role-edit__select:focus {
  outline: 2px solid var(--blue-decor);
  outline-offset: 2px;
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
