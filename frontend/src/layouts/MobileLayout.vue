<script setup lang="ts">
import OrganizationSwitcher from '@/components/OrganizationSwitcher.vue'
import ViewModeSwitcher from '@/components/ViewModeSwitcher.vue'
import { useOrgSession } from '@/composables/useOrgSession'
import { computed, onMounted } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'

const route = useRoute()
const {
  claims,
  currentOrganization,
  currentUserInitials,
  currentUserName,
  ensureOrgSessionLoaded,
  isAuthenticated,
  organizations,
} = useOrgSession()

const tabs = [
  { name: 'rutiner', label: 'Rutiner', to: '/mobile/rutiner' },
  { name: 'logging', label: 'Logging', to: '/mobile/logging' },
  { name: 'kartlegging og tiltak', label: 'Kartlegging og tiltak', to: '/mobile/kartlegging-og-tiltak' },
  { name: 'avvik', label: 'Avvik', to: '/mobile/avvik' },
]

const showTabs = computed(() => route.name !== 'login')
const isLoginPage = computed(() => route.name === 'login')
const profileLink = computed(() => (isAuthenticated.value ? '/desktop/users/me' : '/auth'))
const profileName = computed(() => currentUserName.value || 'Logg inn')
const profileInitials = computed(() => currentUserInitials.value)
const showViewModeSwitcher = computed(() => showTabs.value && currentOrganization.value !== null)

onMounted(() => {
  void ensureOrgSessionLoaded()
})
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
              alt="Grimni"
            >
          </RouterLink>

          <div
            v-if="showTabs"
            class="top-app-bar__center"
          >
            <div class="top-app-bar__center-stack">
              <OrganizationSwitcher
                variant="mobile"
                redirect-to="/mobile/rutiner"
              />
              <ViewModeSwitcher
                v-if="showViewModeSwitcher"
                variant="mobile"
              />
            </div>
          </div>
          <div
            v-else
            class="top-app-bar__center"
          />

          <RouterLink
            class="profile-button transition"
            :to="profileLink"
            aria-label="Gå til logg inn"
          >
            <span class="profile-button__avatar">{{ profileInitials }}</span>
            <span class="profile-button__name">{{ profileName }}</span>
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
<style scoped>
.top-app-bar__center-stack {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  min-width: 0;
  width: 100%;
}
</style>
