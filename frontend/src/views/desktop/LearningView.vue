<script setup lang="ts">
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <SidebarPageContainer activePage="/desktop/bedrift-opplaering">
    <div>some content</div>
    <div>some content</div>
    <div>some content</div>
    <div>some content</div>
    <div>some content</div>
  </SidebarPageContainer>
</template>
<style scoped></style>
