<script setup lang="ts">
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import type { LearningCourse, LearningUserProgress } from '@/interfaces/api-interfaces'
import { Check, Edit2, X } from '@lucide/vue'

const props = defineProps<{
  allCourses: LearningCourse[]
  users: LearningUserProgress[]
  isEditingCompletion: boolean
  isSavingCompletion: boolean
  saveCompletionError: boolean
}>()

const emit = defineEmits<{
  startEditing: []
  saveChanges: []
  cancelEditing: []
  toggleCompletion: [payload: { userId: number; courseId: number }]
}>()

function hasCompletedCourse(user: LearningUserProgress, courseId: number) {
  return user.courses.some((course) => course.courseId === courseId && course.completed)
}
</script>

<template>
  <div class="course-completion">
    <table>
      <thead>
        <tr>
          <th>Bruker</th>
          <th v-for="course in allCourses" :key="course.id">
            {{ course.title }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="user in users" :key="user.id">
          <td>{{ user.legalName }}</td>
          <td v-for="course in allCourses" :key="`${user.id}-${course.id}`">
            <button
              v-if="isEditingCompletion"
              class="completion-chip completion-toggle-chip"
              :class="hasCompletedCourse(user, course.id) ? 'is-complete' : 'is-incomplete'"
              :disabled="isSavingCompletion"
              @click="emit('toggleCompletion', { userId: user.id, courseId: course.id })"
            >
              {{ hasCompletedCourse(user, course.id) ? 'Fullført' : 'Ikke fullført' }}
            </button>
            <span
              v-else
              class="completion-chip"
              :class="hasCompletedCourse(user, course.id) ? 'is-complete' : 'is-incomplete'"
            >
              {{ hasCompletedCourse(user, course.id) ? 'Fullført' : 'Ikke fullført' }}
            </span>
          </td>
        </tr>
      </tbody>
    </table>
    <div class="completion-actions">
      <DesktopButton
        v-if="!isEditingCompletion"
        content="Rediger"
        :icon="Edit2"
        class="navy-button-flat-top"
        :on-click="() => emit('startEditing')"
      />
      <div v-else class="completion-action-row">
        <DesktopButton
          content="Lagre"
          :icon="Check"
          :on-click="() => emit('saveChanges')"
          :is-loading="isSavingCompletion"
          loading-text="Lagrer"
          class="completion-action-button"
        />
        <DesktopButton
          content="Avbryt"
          :icon="X"
          :on-click="() => emit('cancelEditing')"
          :disabled="isSavingCompletion"
          button-color="boring-ghost"
          class="completion-action-button"
        />
      </div>
      <p v-if="saveCompletionError" class="completion-error">
        Klarte ikke oppdatere kursstatus. Prøv igjen.
      </p>
    </div>
  </div>
</template>

<style scoped>
.course-completion {
  max-height: 26rem;
  overflow: auto;
  border: 1px solid var(--blue-navy-40);
  border-radius: 0.75rem;
  background-color: var(--white-greek);
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 40rem;
}

th,
td {
  text-align: left;
  padding: 0.75rem;
  border-bottom: 1px solid var(--blue-navy-20);
}

thead th {
  position: sticky;
  top: 0;
  background-color: var(--white-greek);
  z-index: 1;
}

.completion-chip {
  display: inline-flex;
  flex-wrap: wrap;
  border-radius: 999px;
  padding: 0.15rem 0.6rem;
  font-size: 0.85rem;
  font-weight: 600;
}

.completion-toggle-chip {
  border: 0;
}

.completion-toggle-chip:hover:not(:disabled) {
  box-shadow: 0rem 0.2rem 0.35rem var(--black-no-face-20);
}

.completion-toggle-chip:disabled {
  opacity: 0.7;
}

.is-complete {
  color: #0f5132;
  background-color: #d1e7dd;
}

.is-incomplete {
  color: #842029;
  background-color: #f8d7da;
}

.completion-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.completion-action-row {
  display: flex;
  gap: 0.5rem;
}

.completion-action-button {
  width: 100%;
  border-radius: 0;
}

.completion-error {
  margin: 0;
  color: var(--red-cherry);
  padding: 0 0.5rem 0.5rem;
}

.navy-button-flat-top {
  width: 100%;
  border-radius: 0.7rem;
  border-top-right-radius: 0;
  border-top-left-radius: 0;
}

@media (max-width: 1200px) {
  table {
    min-width: 34rem;
  }
}

@media (max-width: 768px) {
  .course-completion {
    max-height: 22rem;
  }

  table {
    min-width: 28rem;
  }

  th,
  td {
    padding: 0.6rem;
    font-size: 0.9rem;
  }

  .completion-chip {
    font-size: 0.8rem;
  }

  .completion-action-row {
    flex-direction: column;
  }
}
</style>
