<script setup lang="ts">
import MainNavbar from '@/components/desktop/navbar/MainNavbar.vue';
import { RouterView } from 'vue-router'
</script>

<template>
  <div class="desktop-layout">
    <MainNavbar />
    <RouterView />
  </div>
</template>

<style src="@/assets/desktop.css"></style>