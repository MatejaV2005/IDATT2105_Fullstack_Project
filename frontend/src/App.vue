<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'

import { appMeta } from './data/mockHaccp'

const route = useRoute()

const tabs = [
  { name: 'rutiner', label: 'Rutiner', to: '/rutiner' },
  { name: 'logging', label: 'Logging', to: '/logging' },
  { name: 'avvik', label: 'Avvik', to: '/avvik' },
]

const showTabs = computed(() => route.name !== 'login')
const isLoginPage = computed(() => route.name === 'login')
</script>

<template>
  <div class="app-shell">
    <div class="app-shell__frame">
      <header class="top-app-bar">
        <div class="top-app-bar__identity">
          <p class="top-app-bar__brand">{{ appMeta.companyName }}</p>
        </div>

        <RouterLink class="avatar-button" to="/login" aria-label="Gå til logg inn">
          <span class="avatar-button__initials">{{ appMeta.userInitials }}</span>
        </RouterLink>
      </header>

      <nav v-if="showTabs" class="top-tabs" aria-label="Hovednavigasjon">
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

      <main class="app-content" :class="{ 'app-content--auth': isLoginPage }">
        <RouterView />
      </main>
    </div>
  </div>
</template>
