<script setup lang="ts">
import { appMeta } from '@/data/mockHaccp'
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'

const route = useRoute()

const tabs = [
  { name: 'rutiner', label: 'Rutiner', to: '/mobile/rutiner' },
  { name: 'logging', label: 'Logging', to: '/mobile/logging' },
  { name: 'kartlegging og tiltak', label: 'Kartlegging og tiltak', to: '/mobile/kartlegging-og-tiltak' },
  { name: 'avvik', label: 'Avvik', to: '/mobile/avvik' },
]

const showTabs = computed(() => route.name !== 'login')
const isLoginPage = computed(() => route.name === 'login')
</script>

<template>
  <div class="mobile-layout">
    <div class="app-shell">
      <div class="app-shell__frame">
        <header class="top-app-bar">
          <div class="top-app-bar__identity">
            <p class="top-app-bar__brand">
              {{ appMeta.companyName }}
            </p>
          </div>

          <RouterLink
            class="avatar-button"
            to="/mobile/login"
            aria-label="Gå til logg inn"
          >
            <span class="avatar-button__initials">{{ appMeta.userInitials }}</span>
          </RouterLink>
        </header>

        <nav
          v-if="showTabs"
          class="top-tabs"
          aria-label="Hovednavigasjon"
        >
          <RouterLink
            v-for="tab in tabs"
            :key="tab.name"
            :to="tab.to"
            class="top-tabs__link"
            :class="{ 'top-tabs__link--active': route.name === tab.name }"
          >
            {{ tab.label }}
          </RouterLink>
        </nav>

        <main
          class="app-content"
          :class="{ 'app-content--auth': isLoginPage }"
        >
          <RouterView />
        </main>
      </div>
    </div>
  </div>
</template>

<style src="@/assets/mobile.css"></style>