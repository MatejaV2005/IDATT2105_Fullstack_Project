<script setup lang="ts">
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <SidebarPageContainer activePage="/desktop/haccp">
    <div class="haccp-area-container">
      <h1 class="instrument-serif-regular no-margin">HACCP</h1>
    </div>
  </SidebarPageContainer>
</template>
<style scoped>
  .haccp-area-container {
    display: flex;
    flex-direction: column;
    gap: 1rem;
    border-radius: 1rem;
    padding: 1rem;
    margin-top: 2rem;
  }

</style>
