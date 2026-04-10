<script setup lang="ts">
import AddOneUser from '@/components/desktop/shared/AddOneUser.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import type { NewOrganizationUserPayload, TeamDirectoryUser } from '@/interfaces/api-interfaces'
import type { OrgAccessLevel } from '@/interfaces/util-interfaces'
import { Plus } from '@lucide/vue'
import { computed, ref } from 'vue'

const props = defineProps<{
  excludedUserIds: number[]
  isSubmitting: boolean
  availableUsers: TeamDirectoryUser[]
  isLoadingUsers: boolean
  usersErrorMessage: string
}>()

const emit = defineEmits<{
  add: [payload: NewOrganizationUserPayload]
}>()

const selectedUserId = ref<number | null>(null)
const selectedRole = ref<OrgAccessLevel>('WORKER')

const canAdd = computed(() => selectedUserId.value !== null)

function setSelectedUserId(userId: number | null) {
  selectedUserId.value = userId
}

function submitAdd() {
  if (!canAdd.value || selectedUserId.value === null || props.isSubmitting) {
    return
  }

  emit('add', {
    userId: selectedUserId.value,
    orgRole: selectedRole.value,
  })

  selectedUserId.value = null
  selectedRole.value = 'WORKER'
}
</script>

<template>
  <div class="add-member-card">
    <h2 class="no-margin">
      Legg til medlem
    </h2>

    <label class="form-field">
      <span class="navy-subtitle">Bruker</span>
      <AddOneUser
        :selected-user-id="selectedUserId"
        :set-selected-user-id="setSelectedUserId"
        :users="availableUsers"
        :excluded-user-ids="excludedUserIds"
        :disabled="isSubmitting"
        :is-loading="isLoadingUsers"
        :error-message="usersErrorMessage"
      />
    </label>

    <label class="form-field">
      <span class="navy-subtitle">Rolle</span>
      <div class="role-select-shell">
        <select
          v-model="selectedRole"
          class="simple-text-input role-select"
          :disabled="isSubmitting"
        >
          <option value="OWNER">OWNER</option>
          <option value="MANAGER">MANAGER</option>
          <option value="WORKER">WORKER</option>
        </select>
      </div>
    </label>

    <DesktopButton
      :icon="Plus"
      content="Legg til i team"
      :on-click="submitAdd"
      :disabled="!canAdd || isSubmitting"
      :is-loading="isSubmitting"
      loading-text="Legger til"
    />
  </div>
</template>

<style scoped>
.add-member-card {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  border: 1px solid var(--blue-navy-40);
  border-radius: 0.75rem;
  background-color: var(--white-greek);
  padding: 0.75rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.role-select-shell {
  position: relative;
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

.role-select {
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

.role-select:focus {
  outline: 2px solid var(--blue-decor);
  outline-offset: 2px;
}
</style>
