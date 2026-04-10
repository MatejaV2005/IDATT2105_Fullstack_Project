<script setup lang="ts">
import { mockUsers } from '@/data/mockUsers'
import type { BasicUserWithAccessLevel } from '@/interfaces/util-interfaces'
import { delay } from '@/utils'
import { Plus, X } from '@lucide/vue'
import { computed, onMounted, ref, watch } from 'vue'
import DesktopButton from './DesktopButton.vue'
import Loading from './Loading.vue'
import UserSelector from './UserSelector.vue'
import UserBadge from './UserBadge.vue'

const props = defineProps<{
  setUsers: (users: BasicUserWithAccessLevel[]) => void
  initialUserIds?: number[]
}>()

const allUsers = ref<BasicUserWithAccessLevel[]>([])
const selectedUsers = ref<BasicUserWithAccessLevel[]>([])
const selectedUserId = ref<number | null>(null)
const isLoading = ref(true)
const isSubmitting = ref(false)
const errorMessage = ref('')

const availableUsers = computed(() => {
  return allUsers.value.filter((user) => {
    const isAlreadySelected = selectedUsers.value.some((selected) => selected.id === user.id)

    return !isAlreadySelected
  })
})

const canAddSelectedUser = computed(() => {
  if (selectedUserId.value === null) {
    return false
  }

  return availableUsers.value.some((user) => user.id === selectedUserId.value)
})

watch(
  selectedUsers,
  (users) => {
    props.setUsers(users)
  },
  { deep: true },
)

function syncInitialUsers() {
  const initialIds = props.initialUserIds || []
  const sortedInitialIds = [...initialIds].sort((a, b) => a - b)
  const sortedSelectedIds = selectedUsers.value.map((user) => user.id).sort((a, b) => a - b)

  const sameSelection =
    sortedInitialIds.length === sortedSelectedIds.length &&
    sortedInitialIds.every((id, index) => id === sortedSelectedIds[index])

  if (sameSelection) {
    return
  }

  selectedUsers.value = allUsers.value.filter((user) => initialIds.includes(user.id))
}

watch(
  () => props.initialUserIds,
  () => {
    syncInitialUsers()
  },
  { deep: true },
)

onMounted(async () => {
  try {
    // const response = await fetch('/api/organizations/users')
    // if (!response.ok) {
    //   throw new Error(`Failed to fetch users (${response.status})`)
    // }
    // const data = await response.json()
    await delay(400)
    allUsers.value = mockUsers
    syncInitialUsers()
    isLoading.value = false
    errorMessage.value = ''
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    errorMessage.value = 'Klarte ikke å hente brukere.'
  }
})

function addUser(user: BasicUserWithAccessLevel) {
  if (isSubmitting.value) {
    return
  }

  selectedUsers.value = [...selectedUsers.value, user]
  selectedUserId.value = null
}

function setSelectedUserId(userId: number | null) {
  selectedUserId.value = userId
}

function addSelectedUser() {
  if (!canAddSelectedUser.value || selectedUserId.value === null) {
    return
  }

  const user = availableUsers.value.find((entry) => entry.id === selectedUserId.value)
  if (!user) {
    return
  }

  addUser(user)
}

function removeUser(userId: number) {
  if (isSubmitting.value) {
    return
  }

  selectedUsers.value = selectedUsers.value.filter((user) => user.id !== userId)
}
</script>

<template>
  <div class="add-users-container">
    <div class="selector-row">
      <UserSelector
        :users="allUsers"
        :selected-user-id="selectedUserId"
        :set-selected-user-id="setSelectedUserId"
        :excluded-user-ids="selectedUsers.map((user) => user.id)"
        :disabled="isLoading || isSubmitting"
      />
      <DesktopButton
        :icon="Plus"
        content="Legg til"
        :on-click="addSelectedUser"
        :disabled="!canAddSelectedUser || isLoading || isSubmitting"
        :is-loading="isSubmitting"
      />
    </div>

    <Loading v-if="isLoading" />
    <p v-else-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </p>

    <p v-else-if="availableUsers.length === 0" class="empty-state">Ingen brukere tilgjengelig.</p>

    <div class="selected-users">
      <div v-for="user in selectedUsers" :key="user.id" class="selected-user-row">
        <UserBadge :name="user.legalName" :user-id="user.id" />
        <DesktopButton
          :icon="X"
          content="Fjern"
          :on-click="() => removeUser(user.id)"
          button-color="cherry"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.add-users-container {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.selector-row {
  display: flex;
  gap: 0.5rem;
  align-items: flex-start;
}

.empty-state {
  margin: 0;
  color: var(--blue-navy-80);
}

.selected-users {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.selected-user-row {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.25rem;
  background-color: var(--blue-decor-10);
  border: 1px solid var(--blue-decor-40);
  border-radius: 0.5rem;
}

.remove-user {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  border: 1px solid var(--red-cherry-40);
  background-color: var(--red-cherry-20);
  color: var(--red-cherry);
  border-radius: 0.5rem;
  padding: 0.25rem 0.5rem;
}

.error-message {
  color: #b42318;
  margin: 0;
}

@media (max-width: 768px) {
  .selector-row {
    flex-direction: column;
  }
}
</style>
