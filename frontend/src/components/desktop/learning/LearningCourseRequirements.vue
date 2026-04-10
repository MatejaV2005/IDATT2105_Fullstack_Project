<script setup lang="ts">
import Badge from '@/components/desktop/shared/Badge.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import type { LearningCourse } from '@/interfaces/api-interfaces'
import { Edit2, File, Link } from '@lucide/vue'

defineProps<{
  allCourses: LearningCourse[]
}>()
</script>

<template>
  <div v-for="course in allCourses" :key="course.uniqueId" class="course">
    <div class="course-header">
      <h2 class="no-margin">
        {{ course.name }}
      </h2>
      <DesktopButton content="Edit" :icon="Edit2" />
    </div>
    <div>
      <span class="navy-subtitle"> Beskrivelse: </span>
      <span>
        {{ course.description }}
      </span>
    </div>
    <div>
      <span class="navy-subtitle"> Ressurser: </span>
      <div class="resource-container">
        <Badge
          v-for="resource in course.resources"
          :key="resource.id"
          badge-color="navy"
          :icon="resource.type === 'link' ? Link : File"
        >
          {{ resource.name }}
        </Badge>
      </div>
    </div>
  </div>
</template>

<style scoped>
.course {
  width: 100%;
  background-color: var(--white-greek);
  border-radius: 1rem;
  padding: 1rem;
  border: 1px solid var(--blue-navy-40);
  display: flex;
  gap: 1rem;
  flex-direction: column;
  .course-header {
    display: flex;
    justify-content: space-between;
    padding-bottom: 1rem;
    border-bottom: 1px solid var(--blue-navy-40);
  }
}

.resource-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}

@media (max-width: 768px) {
  .course {
    padding: 0.75rem;

    .course-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 0.5rem;
    }
  }
}
</style>
