<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'

import { useOrgSession } from '@/composables/useOrgSession'

const props = withDefaults(
  defineProps<{
    redirectTo: string
    variant?: 'desktop' | 'mobile'
  }>(),
  {
    variant: 'desktop',
  },
)

const router = useRouter()
const rootRef = ref<HTMLElement | null>(null)
const isOpen = ref(false)

const {
  organizations,
  currentOrganization,
  isAuthenticated,
  isLoadingOrganizations,
  isSwitchingOrganization,
  orgErrorMessage,
  ensureOrgSessionLoaded,
  switchOrganization,
} = useOrgSession()

const buttonLabel = computed(() => currentOrganization.value?.orgName ?? 'Ingen organisasjon')
const buttonRole = computed(() => currentOrganization.value?.orgRole ?? '')

function handleDocumentClick(event: MouseEvent) {
  if (!rootRef.value) {
    return
  }

  const target = event.target
  if (target instanceof Node && !rootRef.value.contains(target)) {
    isOpen.value = false
  }
}

async function toggleMenu() {
  if (!isAuthenticated.value) {
    return
  }

  if (!isOpen.value) {
    await ensureOrgSessionLoaded()
  }

  isOpen.value = !isOpen.value
}

async function handleSelect(organizationId: number) {
  try {
    await switchOrganization(organizationId)
    isOpen.value = false
    await router.push(props.redirectTo)
  } catch {
    // Error state is handled by the shared org session state.
  }
}

onMounted(() => {
  void ensureOrgSessionLoaded()
  document.addEventListener('mousedown', handleDocumentClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('mousedown', handleDocumentClick)
})
</script>

<template>
  <div
    v-if="isAuthenticated"
    ref="rootRef"
    class="org-switcher-shell"
  >
    <button
      type="button"
      class="org-switcher-button"
      :class="`org-switcher-button--${variant}`"
      aria-label="Bytt organisasjon"
      :aria-expanded="isOpen"
      :disabled="isSwitchingOrganization"
      @click="toggleMenu"
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
      <span class="org-switcher-button__text">{{ buttonLabel }}</span>
      <span
        v-if="buttonRole"
        class="org-switcher-button__role"
      >{{ buttonRole }}</span>
    </button>

    <div
      v-if="isOpen"
      class="org-switcher-menu"
      :class="`org-switcher-menu--${variant}`"
    >
      <p class="org-switcher-menu__title">
        Bytt organisasjon
      </p>

      <p
        v-if="isLoadingOrganizations"
        class="org-switcher-menu__state"
      >
        Laster organisasjoner...
      </p>

      <p
        v-else-if="orgErrorMessage"
        class="org-switcher-menu__state org-switcher-menu__state--error"
      >
        {{ orgErrorMessage }}
      </p>

      <p
        v-else-if="organizations.length === 0"
        class="org-switcher-menu__state"
      >
        Ingen tilgjengelige organisasjoner.
      </p>

      <ul
        v-else
        class="org-switcher-menu__list"
      >
        <li
          v-for="organization in organizations"
          :key="organization.id"
        >
          <button
            type="button"
            class="org-switcher-menu__item"
            :class="{ 'org-switcher-menu__item--current': organization.isCurrent }"
            :disabled="organization.isCurrent || isSwitchingOrganization"
            @click="handleSelect(organization.id)"
          >
            <span class="org-switcher-menu__item-main">
              <span class="org-switcher-menu__item-name">{{ organization.orgName }}</span>
              <span class="org-switcher-menu__item-meta">{{ organization.orgRole }}</span>
            </span>
            <span class="org-switcher-menu__item-status">
              {{ organization.isCurrent ? 'Aktiv' : 'Bytt' }}
            </span>
          </button>
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.org-switcher-shell {
  position: relative;
}

.org-switcher-button {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border: 1px solid var(--blue-navy-20);
  color: var(--blue-navy);
  background: var(--white-greek);
  cursor: pointer;
}

.org-switcher-button:disabled {
  cursor: wait;
  opacity: 0.8;
}

.org-switcher-button--desktop {
  padding: 1rem 1.25rem;
  border-radius: 1rem;
  min-width: 240px;
  justify-content: center;
}

.org-switcher-button--mobile {
  padding: 8px 12px;
  border-radius: 999px;
  max-width: min(52vw, 360px);
}

.org-switcher-button__text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
  font-size: 14px;
  line-height: 20px;
}

.org-switcher-button__role {
  flex: 0 0 auto;
  padding: 3px 8px;
  border-radius: 999px;
  background: var(--blue-light-20);
  color: var(--blue-navy);
  font-weight: 700;
  font-size: 11px;
  line-height: 16px;
  letter-spacing: 0.04em;
}

.org-switcher-menu {
  position: absolute;
  top: calc(100% + 10px);
  width: min(360px, 92vw);
  border: 1px solid var(--blue-navy-20);
  border-radius: 1rem;
  background: var(--white-greek);
  box-shadow: 0 12px 24px rgba(15, 34, 66, 0.08);
  padding: 14px;
  z-index: 40;
}

.org-switcher-menu--desktop {
  left: 50%;
  transform: translateX(-50%);
}

.org-switcher-menu--mobile {
  left: 50%;
  transform: translateX(-50%);
}

.org-switcher-menu__title {
  margin: 0 0 10px;
  color: var(--blue-navy);
  font-weight: 700;
  font-size: 14px;
  line-height: 20px;
}

.org-switcher-menu__state {
  margin: 0;
  color: var(--blue-navy-60);
  font-weight: 500;
  font-size: 13px;
  line-height: 18px;
}

.org-switcher-menu__state--error {
  color: var(--red-cherry);
}

.org-switcher-menu__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.org-switcher-menu__item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid var(--blue-navy-20);
  border-radius: 0.875rem;
  background: var(--white-greek);
  color: var(--blue-navy);
  text-align: left;
  padding: 12px;
  cursor: pointer;
}

.org-switcher-menu__item:disabled {
  cursor: default;
}

.org-switcher-menu__item--current {
  background: var(--blue-light-10);
  border-color: var(--blue-decor-40);
}

.org-switcher-menu__item-main {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.org-switcher-menu__item-name {
  font-weight: 600;
  font-size: 14px;
  line-height: 20px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.org-switcher-menu__item-meta {
  color: var(--blue-navy-60);
  font-weight: 500;
  font-size: 12px;
  line-height: 18px;
}

.org-switcher-menu__item-status {
  flex: 0 0 auto;
  color: var(--blue-navy-60);
  font-weight: 700;
  font-size: 12px;
  line-height: 18px;
}

@media (max-width: 767px) {
  .org-switcher-button--mobile {
    max-width: min(44vw, 220px);
  }
}

@media (max-width: 430px) {
  .org-switcher-button--mobile {
    max-width: min(38vw, 150px);
    padding: 8px 10px;
  }

  .org-switcher-button__role {
    display: none;
  }

  .org-switcher-menu--mobile {
    width: min(320px, calc(100vw - 24px));
  }
}
</style>
