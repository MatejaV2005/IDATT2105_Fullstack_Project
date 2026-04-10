<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { computed, onMounted } from 'vue'

import OrganizationSwitcher from '@/components/OrganizationSwitcher.vue'
import ViewModeSwitcher from '@/components/ViewModeSwitcher.vue'
import { useOrgSession } from '@/composables/useOrgSession'
import { getPostAuthRoute } from '@/utils/auth-routing'

import ProfileNavbarButton from './ProfileNavbarButton.vue'

const {
  claims,
  currentOrganization,
  currentUserInitials,
  currentUserName,
  ensureOrgSessionLoaded,
  isAuthenticated,
  organizations,
} = useOrgSession()

const authenticatedRoute = computed(() => getPostAuthRoute(claims.value, organizations.value))
const profileRoute = computed(() => (isAuthenticated.value ? '/desktop/users/me' : '/auth'))
const logoRoute = computed(() => (isAuthenticated.value ? authenticatedRoute.value : '/auth'))
const profileLabel = computed(() => currentUserName.value || 'Logg inn')
const showViewModeSwitcher = computed(() => isAuthenticated.value && currentOrganization.value !== null)

onMounted(() => {
  void ensureOrgSessionLoaded()
})
</script>
<template>
  <div id="navbar">
    <RouterLink
      :to="logoRoute"
      class="logo-button"
    >
      <img
        src="@/assets/logo/logo-medium.png"
        alt="Logo"
      >
    </RouterLink>
    <div class="navbar-center">
      <OrganizationSwitcher
        variant="desktop"
        redirect-to="/desktop/users/me"
      />
      <ViewModeSwitcher
        v-if="showViewModeSwitcher"
        variant="desktop"
      />
    </div>
    <ProfileNavbarButton
      :username="profileLabel"
      :initials="currentUserInitials"
      :to="profileRoute"
    />
  </div>
</template>
<style scoped>
#navbar {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  column-gap: 1rem;
  height: 6.25rem !important;
  max-height: 6.25rem;
  min-height: 6.25rem;
  background-color: var(--white-greek);
  border-bottom: 1px solid var(--blue-navy-40);
  align-items: center;
  img {
    height: 2.25rem;
  }
}

.logo-button {
  display: inline-flex;
  align-items: center;
  justify-self: start;
}

.navbar-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.3rem;
  min-width: 0;
}

.navbar-center > * {
  min-width: 0;
}

@media (min-width: 1200px) {
  #navbar {
    padding-left: 2rem;
    padding-right: 2rem;
  }
}
@media (max-width: 1200px) {
  #navbar {
    padding-left: 0.5rem;
    padding-right: 0.5rem;
    max-height: 5.4rem;
    min-height: 5.4rem;
    column-gap: 0.6rem;
  }
}
</style>
