<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { computed, onMounted } from 'vue'

import OrganizationSwitcher from '@/components/OrganizationSwitcher.vue'
import { useOrgSession } from '@/composables/useOrgSession'
import { getPostAuthRoute } from '@/utils/auth-routing'

import ProfileNavbarButton from './ProfileNavbarButton.vue'

const { claims, currentUserInitials, currentUserName, ensureOrgSessionLoaded, isAuthenticated, organizations } =
  useOrgSession()

const authenticatedRoute = computed(() => getPostAuthRoute(claims.value, organizations.value))
const profileRoute = computed(() => (isAuthenticated.value ? authenticatedRoute.value : '/auth'))
const logoRoute = computed(() => (isAuthenticated.value ? authenticatedRoute.value : '/auth'))
const profileLabel = computed(() => currentUserName.value || 'Logg inn')

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
    <OrganizationSwitcher
      variant="desktop"
      redirect-to="/desktop/users/me"
    />
    <ProfileNavbarButton
      :username="profileLabel"
      :initials="currentUserInitials"
      :to="profileRoute"
    />
  </div>
</template>
<style scoped>
#navbar {
  display: flex;
  height: 6rem !important;
  max-height: 6rem;
  min-height: 6rem;
  background-color: var(--white-greek);
  border-bottom: 1px solid var(--blue-navy-40);
  align-items: center;
  justify-content: space-between;
  img {
    height: 2.25rem;
  }
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
    max-height: 4rem;
    min-height: 4rem;
  }
}
</style>
