<script setup lang="ts">
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import SignOutButton from '@/components/desktop/shared/SignOutButton.vue'
import type { MeInfo } from '@/interfaces/api-interfaces'
import { delay } from '@/utils'
import { Edit2, Save, X } from '@lucide/vue'
import { computed, onMounted, ref } from 'vue'

const resource = ref<MeInfo | null>(null)
const loading = ref(true)
const error = ref<boolean | null>(null)

const legalName = ref('')
const email = ref('')
const isEditing = ref(false)
const isSaving = ref(false)
const saveError = ref(false)

const mockData: MeInfo = {
  id: 1,
  legalName: 'Mona Jul',
  email: 'mona.jul@example.com',
  createdAt: '2026-03-01 10:15:00',
}

const canSave = computed(() => {
  return legalName.value.trim().length > 0 && email.value.trim().length > 0 && !isSaving.value
})

onMounted(async () => {
  try {
    // const response = await fetch('/api/me')
    // if (!response.ok) {
    //     throw new Error(`Failed to get user (${response.status})`)
    // }
    // const data = await response.json()
    await delay(2000)
    const data = mockData
    resource.value = data
    legalName.value = data.legalName
    email.value = data.email
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

async function saveChanges() {
  if (!resource.value || !canSave.value || !isEditing.value) {
    return
  }

  saveError.value = false
  isSaving.value = true

  try {
    const payload = {
      legalName: legalName.value,
      email: email.value,
    }
    // const response = await fetch('/api/users/me', {
    //     method: 'PUT',
    //     headers: { 'Content-Type': 'application/json' },
    //     body: JSON.stringify(payload),
    // })
    // if (!response.ok) {
    //     throw new Error(`Failed to update user (${response.status})`)
    // }
    await delay(2000)
    resource.value = {
      ...resource.value,
      legalName: payload.legalName,
      email: payload.email,
    }
    isEditing.value = false
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    saveError.value = true
  } finally {
    isSaving.value = false
  }
}

function startEditing() {
  if (isSaving.value) {
    return
  }
  saveError.value = false
  isEditing.value = true
}

function cancelEditing() {
  if (!resource.value || isSaving.value) {
    return
  }
  legalName.value = resource.value.legalName
  email.value = resource.value.email
  saveError.value = false
  isEditing.value = false
}
</script>

<template>
  <SidebarPageContainer>
    <div class="me-area-container">
      <h1 class="instrument-serif-regular no-margin">
        Min profil
      </h1>

      <Loading v-if="loading" />
      <p
        v-else-if="error"
        class="error-message"
      >
        Klarte ikke å hente brukerdata.
      </p>

      <div
        v-else-if="resource"
        class="profile-card"
      >
        <div class="useless-data-container">
          <div class="row">
            <span class="navy-subtitle">Bruker-ID</span>
            <span>{{ resource.id }}</span>
          </div>
          <div class="row">
            <span class="navy-subtitle">Opprettet</span>
            <span>{{ resource.createdAt }}</span>
          </div>
        </div>

        <label class="field">
          <span class="navy-subtitle">Juridisk navn</span>
          <input
            v-model="legalName"
            class="simple-text-input"
            type="text"
            :disabled="!isEditing || isSaving"
          >
        </label>

        <label class="field">
          <span class="navy-subtitle">E-post</span>
          <input
            v-model="email"
            class="simple-text-input"
            type="email"
            :disabled="!isEditing || isSaving"
          >
        </label>

        <Loading v-if="isSaving" />
        <div
          v-else
          class="actions"
        >
          <DesktopButton
            v-if="!isEditing"
            :icon="Edit2"
            content="Rediger"
            :on-click="startEditing"
          />
          <DesktopButton
            v-if="isEditing"
            :icon="Save"
            content="Lagre"
            :on-click="saveChanges"
          />
          <DesktopButton
            v-if="isEditing"
            :icon="X"
            content="Avbryt"
            button-color="cherry"
            :on-click="cancelEditing"
          />
          <SignOutButton v-if="!isEditing" />
        </div>

        <p
          v-if="saveError"
          class="error-message"
        >
          Kunne ikke oppdatere profil.
        </p>
      </div>
    </div>
  </SidebarPageContainer>
</template>

<style scoped>
.me-area-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border-radius: 1rem;
  padding: 1rem;
  margin-top: 2rem;
}

.profile-card {
  background-color: var(--white-greek);
  border: 1px solid var(--blue-navy-40);
  border-radius: 0.75rem;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  width: 100%;
  height: 100%;
  min-height: 0;
}

.row {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  width: 100%;
  background-color: var(--blue-decor-10);
  padding: 0.5rem 1.5rem;
  border-radius: 1rem;
  border: 1px solid var(--blue-decor-40);
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.actions {
  display: flex;
  gap: 0.75rem;
}

.error-message {
  color: #b42318;
  margin: 0;
}

.useless-data-container {
  display: flex;
  gap: 1rem;
  width: 100%;
  justify-content: stretch;
}

@media (max-width: 1200px) {
  .me-area-container {
    margin-top: 1rem;
    padding: 0.75rem;
    .useless-data-container {
      flex-direction: column;
    }
  }
  .actions {
    flex-direction: column;
  }
}

@media (max-width: 768px) {
  .me-area-container {
    padding: 0.5rem;
  }
}
</style>
