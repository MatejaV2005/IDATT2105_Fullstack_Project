<script setup lang="ts">
import { mockAllUsers, type DirectoryUser } from '@/data/mockAllUsers'
import { delay } from '@/utils'
import { computed, onMounted, ref, watch } from 'vue'

const props = withDefaults(
  defineProps<{
    selectedUserId: number | null
    setSelectedUserId: (userId: number | null) => void
    excludedUserIds?: number[]
    disabled?: boolean
  }>(),
  {
    excludedUserIds: () => [],
    disabled: false,
  },
)

const users = ref<DirectoryUser[]>([])
const searchQuery = ref('')
const isOpen = ref(false)
const isLoading = ref(true)
const errorMessage = ref('')

const selectedUser = computed(() => {
  return users.value.find((user) => user.id === props.selectedUserId) || null
})

const filteredUsers = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()

  return users.value.filter((user) => {
    const isExcluded = props.excludedUserIds.includes(user.id) && user.id !== props.selectedUserId
    const matchesQuery =
      query.length === 0 ||
      user.legalName.toLowerCase().includes(query) ||
      user.email.toLowerCase().includes(query) ||
      user.orgName.toLowerCase().includes(query)

    return !isExcluded && matchesQuery
  })
})

watch(
  selectedUser,
  (user) => {
    searchQuery.value = user ? user.legalName : ''
  },
  { immediate: true },
)

onMounted(async () => {
  try {
    // const response = await fetch('/api/users')
    // if (!response.ok) {
    //   throw new Error(`Failed to fetch users (${response.status})`)
    // }
    // const data: DirectoryUser[] = await response.json()
    await delay(400)
    users.value = mockAllUsers
    errorMessage.value = ''
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    errorMessage.value = 'Klarte ikke å hente brukere.'
  } finally {
    isLoading.value = false
  }
})

function selectUser(user: DirectoryUser) {
  if (props.disabled) {
    return
  }

  props.setSelectedUserId(user.id)
  searchQuery.value = user.legalName
  isOpen.value = false
}

function clearSelection() {
  if (props.disabled) {
    return
  }

  props.setSelectedUserId(null)
  searchQuery.value = ''
  isOpen.value = true
}

function onInput() {
  if (props.disabled) {
    return
  }

  isOpen.value = true
  if (selectedUser.value && searchQuery.value !== selectedUser.value.legalName) {
    props.setSelectedUserId(null)
  }
}
</script>

<template>
  <div class="selector-root">
    <div class="input-row">
      <input
        v-model="searchQuery"
        class="simple-text-input selector-input"
        type="text"
        :disabled="disabled || isLoading"
        placeholder="Velg bruker (alle organisasjoner)"
        @focus="isOpen = true"
        @input="onInput"
      >
      <button
        v-if="selectedUserId !== null"
        class="clear-button"
        type="button"
        :disabled="disabled"
        @click="clearSelection"
      >
        Fjern
      </button>
    </div>

    <p
      v-if="errorMessage"
      class="error-message"
    >
      {{ errorMessage }}
    </p>

    <div
      v-if="isOpen && !isLoading"
      class="dropdown-container"
    >
      <button
        v-for="user in filteredUsers"
        :key="user.id"
        type="button"
        class="dropdown-option"
        :disabled="disabled"
        @click="selectUser(user)"
      >
        <div>
          <div>{{ user.legalName }}</div>
          <div class="option-email">
            {{ user.email }}
          </div>
        </div>
        <span class="option-org">{{ user.orgName }}</span>
      </button>

      <div
        v-if="filteredUsers.length === 0"
        class="empty-state"
      >
        Ingen brukere tilgjengelig.
      </div>
    </div>
  </div>
</template>

<style scoped>
.selector-root {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  width: 100%;
}

.input-row {
  display: flex;
  gap: 0.5rem;
}

.selector-input {
  width: 100%;
}

.clear-button {
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
  border-radius: 0.5rem;
  color: var(--blue-navy);
  padding: 0 0.75rem;
}

.dropdown-container {
  border: 1px solid var(--blue-navy-40);
  border-radius: 0.5rem;
  max-height: 12rem;
  overflow-y: auto;
  background-color: var(--white-greek);
}

.dropdown-option {
  width: 100%;
  border: 0;
  border-bottom: 1px solid var(--blue-navy-20);
  background: transparent;
  padding: 0.6rem 0.75rem;
  display: flex;
  justify-content: space-between;
  text-align: left;
  gap: 0.75rem;
}

.dropdown-option:hover,
.dropdown-option:focus {
  background-color: var(--blue-decor-10);
}

.option-email,
.option-org {
  color: var(--blue-navy-80);
  font-size: 0.9rem;
}

.empty-state {
  padding: 0.75rem;
  color: var(--blue-navy-80);
}

.error-message {
  color: #b42318;
  margin: 0;
}
</style>
