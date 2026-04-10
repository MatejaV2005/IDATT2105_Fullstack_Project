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
      <select
        v-model="selectedRole"
        class="simple-text-input role-select"
        :disabled="isSubmitting"
      >
        <option value="OWNER">OWNER</option>
        <option value="MANAGER">MANAGER</option>
        <option value="WORKER">WORKER</option>
      </select>
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

.role-select {
  width: 100%;
}
</style>
