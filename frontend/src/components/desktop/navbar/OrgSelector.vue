<script setup lang="ts">
import { mockMeOrganizations, type MeOrganization } from '@/data/mockOrganizations'
import { delay } from '@/utils'
import { onMounted, ref } from 'vue'

const props = defineProps({
  currentBusinessName: String,
})

const organizations = ref<MeOrganization[]>([])
const isLoading = ref(true)
const hasError = ref(false)
const isOpen = ref(false)

async function fetchMyOrganizations() {
  // const response = await fetch('/api/me/orgs')
  // if (!response.ok) {
  //   throw new Error(`Failed to fetch organizations (${response.status})`)
  // }
  // const data: MeOrganization[] = await response.json()
  await delay(350)
  organizations.value = [...mockMeOrganizations]
}

async function changeOrganization(org: MeOrganization) {
  // const response = await fetch('/api/me/change-org', {
  //   method: 'POST',
  //   headers: { 'Content-Type': 'application/json' },
  //   body: JSON.stringify({ orgId: org.id }),
  // })
  // if (!response.ok) {
  //   throw new Error(`Failed to change organization (${response.status})`)
  // }
  await delay(200)
  alert(`Selected ${org.name} org`)
  isOpen.value = false
}

function toggleOrgList() {
  if (isLoading.value || hasError.value) {
    return
  }

  isOpen.value = !isOpen.value
}

onMounted(async () => {
  try {
    await fetchMyOrganizations()
    hasError.value = false
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    hasError.value = true
  } finally {
    isLoading.value = false
  }
})
</script>
<template>
  <div v-if="!!currentBusinessName" class="org-selector">
    <button class="org-selector-button" @click="toggleOrgList">
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width="24"
        height="24"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
        class="lucide lucide-building2-icon lucide-building-2"
      >
        <path d="M10 12h4" />
        <path d="M10 8h4" />
        <path d="M14 21v-3a2 2 0 0 0-4 0v3" />
        <path d="M6 10H4a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-2" />
        <path d="M6 21V5a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v16" />
      </svg>
      <div>
        {{ currentBusinessName }}
      </div>
    </button>

    <div v-if="isOpen" class="org-selector-menu">
      <p v-if="isLoading" class="org-selector-info">Henter organisasjoner...</p>
      <p v-else-if="hasError" class="org-selector-error">Klarte ikke hente organisasjoner.</p>
      <ul v-else class="org-selector-list">
        <li v-for="org in organizations" :key="org.id">
          <button
            class="org-option"
            :class="{ 'org-option-active': org.name === props.currentBusinessName }"
            @click="changeOrganization(org)"
          >
            {{ org.name }}
          </button>
        </li>
      </ul>
    </div>
  </div>
</template>
<style scoped>
.org-selector {
  position: relative;
}

.org-selector-button {
  color: black;
  background-color: var(--white-greek);
  cursor: pointer;
  border: 0;
  text-decoration: none;
  gap: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem 2rem;
  border-radius: 1rem;
}
.org-selector-button:hover {
  box-shadow: 0rem 0.25rem 0.25rem rgba(0, 0, 0, 0.2);
}

.org-selector-menu {
  position: absolute;
  top: calc(100% + 0.25rem);
  left: 0;
  min-width: 16rem;
  border-radius: 0.75rem;
  background-color: var(--white-greek);
  border: 1px solid var(--blue-navy-20);
  box-shadow: 0rem 0.5rem 1rem var(--black-no-face-20);
  z-index: 20;
  padding: 0.5rem;
}

.org-selector-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.org-option {
  width: 100%;
  text-align: left;
  padding: 0.5rem 0.75rem;
  border: 0;
  border-radius: 0.5rem;
  background-color: transparent;
  color: var(--blue-navy);
  border: 1px solid var(--blue-decor-40)
}

.org-option:hover {
  background-color: var(--blue-decor-20);
}

.org-option-active {
  background-color: var(--blue-decor-10);
  font-weight: 600;
}

.org-selector-info,
.org-selector-error {
  margin: 0;
  padding: 0.5rem;
}

.org-selector-error {
  color: var(--red-cherry);
}
</style>
