<script setup lang="ts">
import LearningCourseCard from '@/components/desktop/learning/LearningCourseCard.vue'
import type {
  LearningCourse,
  LearningOrganizationUser,
} from '@/interfaces/api-interfaces'

defineProps<{
  allCourses: LearningCourse[]
  organizationUsers: LearningOrganizationUser[]
  onSaveCourseDetails: (
    courseId: number,
    payload: { title: string; courseDescription: string },
  ) => Promise<boolean>
  onDeleteCourse: (courseId: number) => Promise<boolean>
  onAddResponsibleUser: (courseId: number, userId: number) => Promise<boolean>
  onRemoveResponsibleUser: (courseId: number, userId: number) => Promise<boolean>
  onAddCourseLink: (courseId: number, link: string) => Promise<boolean>
  onRemoveCourseLink: (courseId: number, linkId: number) => Promise<boolean>
  onUploadCourseFiles: (courseId: number, files: File[]) => Promise<boolean>
  onRemoveCourseFile: (courseId: number, fileId: number) => Promise<boolean>
  onDownloadCourseFile: (courseId: number, fileId: number, fileName: string) => Promise<void>
}>()
</script>

<template>
  <LearningCourseCard
    v-for="course in allCourses"
    :key="course.id"
    :course="course"
    :organization-users="organizationUsers"
    :on-save-course-details="onSaveCourseDetails"
    :on-delete-course="onDeleteCourse"
    :on-add-responsible-user="onAddResponsibleUser"
    :on-remove-responsible-user="onRemoveResponsibleUser"
    :on-add-course-link="onAddCourseLink"
    :on-remove-course-link="onRemoveCourseLink"
    :on-upload-course-files="onUploadCourseFiles"
    :on-remove-course-file="onRemoveCourseFile"
    :on-download-course-file="onDownloadCourseFile"
  />
</template>
