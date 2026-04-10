<script setup lang="ts">
import api from '@/api/api'
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import Paginator from '@/components/desktop/shared/Paginator.vue'
import { clearOrgSession, useOrgSession } from '@/composables/useOrgSession'
import type { OrganizationSettings } from '@/interfaces/api-interfaces'
import { Save, Trash2, TriangleAlert, X } from '@lucide/vue'
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const { claims, currentUserRole } = useOrgSession()

const organization = ref<OrganizationSettings | null>(null)
const isLoading = ref(true)
const isSaving = ref(false)
const isDeleting = ref(false)
const errorMessage = ref('')
const saveMessage = ref('')
const showDeleteConfirmation = ref(false)

const form = ref({
  orgName: '',
  orgAddress: '',
  alcoholEnabled: false,
  foodEnabled: false,
})

const createdAtText = computed(() => {
  if (!organization.value?.createdAt) {
    return 'Ukjent'
  }

  const date = new Date(organization.value.createdAt)
  if (Number.isNaN(date.getTime())) {
    return organization.value.createdAt
  }

  return new Intl.DateTimeFormat('nb-NO', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(date)
})

const isDirty = computed(() => {
  if (!organization.value) {
    return false
  }

  return (
    form.value.orgName !== organization.value.orgName ||
    form.value.orgAddress !== organization.value.orgAddress ||
    form.value.alcoholEnabled !== organization.value.alcoholEnabled ||
    form.value.foodEnabled !== organization.value.foodEnabled
  )
})

async function loadOrganization() {
  if (!claims.value?.orgId || currentUserRole.value !== 'OWNER') {
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const response = await api.get<OrganizationSettings>('/organizations')
    organization.value = response.data
    form.value = {
      orgName: response.data.orgName,
      orgAddress: response.data.orgAddress,
      alcoholEnabled: response.data.alcoholEnabled,
      foodEnabled: response.data.foodEnabled,
    }
  } catch {
    errorMessage.value = 'Klarte ikke å hente bedriftsinnstillinger.'
  } finally {
    isLoading.value = false
  }
}

async function saveOrganization() {
  if (!organization.value || !isDirty.value || isSaving.value) {
    return
  }

  saveMessage.value = ''
  errorMessage.value = ''
  isSaving.value = true

  try {
    const response = await api.patch<OrganizationSettings>('/organizations', {
      orgName: form.value.orgName.trim(),
      orgAddress: form.value.orgAddress.trim(),
      alcoholEnabled: form.value.alcoholEnabled,
      foodEnabled: form.value.foodEnabled,
    })

    organization.value = response.data
    form.value = {
      orgName: response.data.orgName,
      orgAddress: response.data.orgAddress,
      alcoholEnabled: response.data.alcoholEnabled,
      foodEnabled: response.data.foodEnabled,
    }
    saveMessage.value = 'Bedriftsinnstillingene er oppdatert.'
  } catch {
    errorMessage.value = 'Klarte ikke å lagre bedriftsinnstillinger.'
  } finally {
    isSaving.value = false
  }
}

function resetForm() {
  if (!organization.value) {
    return
  }

  form.value = {
    orgName: organization.value.orgName,
    orgAddress: organization.value.orgAddress,
    alcoholEnabled: organization.value.alcoholEnabled,
    foodEnabled: organization.value.foodEnabled,
  }
  saveMessage.value = ''
  errorMessage.value = ''
}

async function deleteOrganization() {
  if (isDeleting.value) {
    return
  }

  errorMessage.value = ''
  isDeleting.value = true

  try {
    await api.delete('/organizations')
    try {
      await api.post('/auth/logout')
    } catch {
      // The local session is still cleared below even if logout fails after deletion.
    }
    clearOrgSession()
    await router.replace('/auth')
  } catch {
    errorMessage.value = 'Klarte ikke å slette organisasjonen.'
  } finally {
    isDeleting.value = false
  }
}

watch(
  () => [claims.value?.orgId ?? null, currentUserRole.value] as const,
  ([nextOrgId, nextRole], [previousOrgId, previousRole]) => {
    if (!nextOrgId) {
      void router.replace('/desktop/no-org')
      return
    }

    if (nextRole !== 'OWNER') {
      void router.replace(nextRole === 'WORKER' ? '/desktop/oppgaver-oversikt' : '/desktop/bedrift-analyse')
      return
    }

    if (nextOrgId !== previousOrgId || nextRole !== previousRole) {
      void loadOrganization()
    }
  },
)

onMounted(async () => {
  if (!claims.value?.orgId) {
    await router.replace('/desktop/no-org')
    return
  }

  if (currentUserRole.value !== 'OWNER') {
    await router.replace(currentUserRole.value === 'WORKER' ? '/desktop/oppgaver-oversikt' : '/desktop/bedrift-analyse')
    return
  }

  await loadOrganization()
})
</script>

<template>
  <SidebarPageContainer active-page="/desktop/bedrift-innstillinger">
    <section class="settings-page">
      <Paginator />
      <h1 class="instrument-serif-regular no-margin">Bedrift Innstillinger</h1>
      <p class="page-lead no-margin">
        Oppdater navn, adresse og hvilke internkontrollområder organisasjonen dekker.
      </p>
      <hr class="navy-hr">

      <Loading v-if="isLoading" />

      <p
        v-else-if="errorMessage && !organization"
        class="error-message no-margin"
      >
        {{ errorMessage }}
      </p>

      <template v-else-if="organization">
        <article class="settings-card">
          <div class="settings-card__header">
            <div>
              <h2 class="no-margin">Organisasjon</h2>
              <p class="settings-card__subtext no-margin">
                Opprettet {{ createdAtText }}
              </p>
            </div>
            <DesktopButton
              content="Tilbakestill"
              :icon="X"
              button-color="grey"
              :disabled="!isDirty || isSaving || isDeleting"
              :on-click="resetForm"
            />
          </div>

          <div class="settings-grid">
            <label class="field">
              <span class="field__label">Navn</span>
              <input
                v-model="form.orgName"
                class="simple-text-input"
                type="text"
                :disabled="isSaving || isDeleting"
              >
            </label>

            <label class="field">
              <span class="field__label">Adresse</span>
              <input
                v-model="form.orgAddress"
                class="simple-text-input"
                type="text"
                :disabled="isSaving || isDeleting"
              >
            </label>

            <label class="field field--readonly">
              <span class="field__label">Organisasjonsnummer</span>
              <input
                :value="organization.orgNumber"
                class="simple-text-input"
                type="text"
                disabled
              >
            </label>
          </div>

          <div class="toggles">
            <label class="toggle-card">
              <span class="toggle-card__title">IK Mat</span>
              <span class="toggle-card__hint">Aktiver matrelaterte rutiner, avvik og oppfølging.</span>
              <input
                v-model="form.foodEnabled"
                type="checkbox"
                :disabled="isSaving || isDeleting"
              >
            </label>

            <label class="toggle-card">
              <span class="toggle-card__title">IK Alkohol</span>
              <span class="toggle-card__hint">Aktiver alkoholrelaterte kartlegginger og avvik.</span>
              <input
                v-model="form.alcoholEnabled"
                type="checkbox"
                :disabled="isSaving || isDeleting"
              >
            </label>
          </div>

          <p
            v-if="saveMessage"
            class="success-message no-margin"
          >
            {{ saveMessage }}
          </p>
          <p
            v-if="errorMessage"
            class="error-message no-margin"
          >
            {{ errorMessage }}
          </p>

          <div class="settings-card__actions">
            <DesktopButton
              content="Lagre endringer"
              loading-text="Lagrer..."
              :icon="Save"
              :is-loading="isSaving"
              :disabled="!isDirty || isDeleting"
              :on-click="saveOrganization"
            />
          </div>
        </article>

        <article class="settings-card settings-card--danger">
          <div class="settings-card__header">
            <div>
              <h2 class="no-margin">Slett organisasjon</h2>
              <p class="settings-card__subtext no-margin">
                Dette fjerner organisasjonen og tilhørende organisasjonsdata, men ikke brukerkontoene.
              </p>
            </div>
          </div>

          <div
            v-if="showDeleteConfirmation"
            class="danger-confirmation"
          >
            <div class="danger-confirmation__warning">
              <TriangleAlert />
              <p class="no-margin">
                Er du sikker på at du vil slette organisasjonen? Denne handlingen kan ikke angres.
              </p>
            </div>
            <div class="danger-confirmation__actions">
              <DesktopButton
                content="Avbryt"
                :icon="X"
                button-color="grey"
                :disabled="isDeleting"
                :on-click="() => { showDeleteConfirmation = false }"
              />
              <DesktopButton
                content="Bekreft sletting"
                loading-text="Sletter..."
                :icon="Trash2"
                button-color="cherry"
                :is-loading="isDeleting"
                :on-click="deleteOrganization"
              />
            </div>
          </div>

          <div
            v-else
            class="settings-card__actions"
          >
            <DesktopButton
              content="Slett organisasjon"
              :icon="Trash2"
              button-color="cherry"
              :disabled="isDeleting"
              :on-click="() => { showDeleteConfirmation = true }"
            />
          </div>
        </article>
      </template>
    </section>
  </SidebarPageContainer>
</template>

<style scoped>
.settings-page {
  margin-top: 2rem;
  padding-bottom: 3rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.page-lead {
  color: var(--blue-navy-80);
}

.settings-card {
  background-color: var(--white-greek);
  border: 1px solid var(--blue-navy-40);
  border-radius: 1rem;
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.settings-card--danger {
  border-color: color-mix(in srgb, var(--red-cherry) 35%, var(--blue-navy-40));
}

.settings-card__header {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: flex-start;
}

.settings-card__subtext {
  color: var(--blue-navy-80);
}

.settings-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.field--readonly {
  grid-column: span 2;
}

.field__label {
  font-weight: 600;
  color: var(--blue-navy);
}

.toggles {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.toggle-card {
  border: 1px solid var(--blue-navy-40);
  border-radius: 0.85rem;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  background-color: var(--white-greek);
}

.toggle-card input {
  accent-color: var(--blue-navy);
  width: 1.1rem;
  height: 1.1rem;
}

.toggle-card__title {
  font-weight: 700;
  color: var(--blue-navy);
}

.toggle-card__hint {
  color: var(--blue-navy-80);
}

.settings-card__actions {
  display: flex;
  justify-content: flex-end;
}

.danger-confirmation {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.danger-confirmation__warning {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
  padding: 1rem;
  border-radius: 0.85rem;
  background-color: color-mix(in srgb, var(--red-cherry) 10%, white);
  color: var(--red-cherry);
}

.danger-confirmation__warning :deep(svg) {
  width: 1.1rem;
  height: 1.1rem;
  flex-shrink: 0;
  margin-top: 0.15rem;
}

.danger-confirmation__actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

.success-message {
  color: #0f7b6c;
}

@media (max-width: 900px) {
  .settings-grid,
  .toggles {
    grid-template-columns: 1fr;
  }

  .field--readonly {
    grid-column: span 1;
  }

  .settings-card__header,
  .danger-confirmation__actions {
    flex-direction: column;
  }

  .settings-card__actions,
  .danger-confirmation__actions {
    align-items: stretch;
  }
}
</style>
