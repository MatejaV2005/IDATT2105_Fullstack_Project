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

function showOrganisationSelector() {
  window.alert('Bytte av organisasjon er ikke implementert ennå.')
}
</script>

<template>
  <div class="mobile-layout">
    <div class="app-shell">
      <div class="app-shell__frame">
        <header class="top-app-bar">
          <RouterLink
            class="logo-button"
            to="/mobile/rutiner"
            aria-label="Gå til startsiden"
          >
            <img
              src="@/assets/logo/logo-medium.png"
              :alt="appMeta.companyName"
            >
          </RouterLink>

          <div
            v-if="showTabs"
            class="top-app-bar__center"
          >
            <button
              type="button"
              class="org-switcher"
              aria-label="Bytt organisasjon"
              @click="showOrganisationSelector"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                width="18"
                height="18"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
                aria-hidden="true"
              >
                <path d="M10 12h4" />
                <path d="M10 8h4" />
                <path d="M14 21v-3a2 2 0 0 0-4 0v3" />
                <path d="M6 10H4a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-2" />
                <path d="M6 21V5a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v16" />
              </svg>
              <span class="org-switcher__text">{{ appMeta.currentOrganizationName }}</span>
            </button>
          </div>
          <div
            v-else
            class="top-app-bar__center"
          />

          <RouterLink
            class="profile-button transition"
            to="/mobile/login"
            aria-label="Gå til logg inn"
          >
            <span class="profile-button__avatar">{{ appMeta.userInitials }}</span>
            <span class="profile-button__name">{{ appMeta.userName }}</span>
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
