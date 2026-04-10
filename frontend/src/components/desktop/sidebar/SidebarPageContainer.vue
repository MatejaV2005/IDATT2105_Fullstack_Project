<script setup lang="ts">
import { useOrgSession } from '@/composables/useOrgSession'
import SidebarMenuOption from './SidebarMenuOption.vue'
import {
  ChartColumnIncreasing,
  PersonStanding,
  Brain,
  Settings,
  Beer,
  CookingPot,
  CheckCheck,
  Building2,
} from '@lucide/vue'

withDefaults(
  defineProps<{
  activePage?: string
  navigationMode?: 'full' | 'no-org'
}>(),
  {
    navigationMode: 'full',
  },
)

const { currentUserRole } = useOrgSession()
</script>

<template>
  <div class="sidebar-page-container">
    <div class="sidebar">
      <template v-if="navigationMode === 'no-org'">
        <div>
          <div>
            <span class="accent-title">Meg</span>
            <SidebarMenuOption
              to="/desktop/users/me"
              name="Min profil"
              :is-selected="activePage === '/desktop/users/me'"
              :icon="PersonStanding"
            />
            <SidebarMenuOption
              to="/desktop/no-org"
              name="Opprett / bli med i organisasjon"
              :is-selected="activePage === '/desktop/no-org'"
              :icon="Building2"
            />
          </div>
        </div>
      </template>
      <template v-else-if="currentUserRole === 'WORKER'">
        <div>
          <div>
            <span class="accent-title">Meg</span>
            <SidebarMenuOption
              to="/desktop/users/me"
              name="Min profil"
              :is-selected="activePage === '/desktop/users/me'"
              :icon="PersonStanding"
            />
            <SidebarMenuOption
              to="/desktop/oppgaver-oversikt"
              name="Mine oppgaver"
              :is-selected="activePage === '/desktop/oppgaver-oversikt'"
              :icon="CheckCheck"
            />
          </div>
        </div>
      </template>
      <template v-else>
        <div>
          <div>
            <span class="accent-title"> Regulering </span>
            <SidebarMenuOption
              to="/desktop/haccp"
              name="IK-MAT HACCP"
              :is-selected="activePage === '/desktop/haccp'"
              :icon="CookingPot"
            />
            <SidebarMenuOption
              to="/desktop/ik-alkohol-kartlegging-og-tiltak"
              name="IK-ALKOHOL kartlegging"
              :is-selected="activePage === '/desktop/ik-alkohol-kartlegging-og-tiltak'"
              :icon="Beer"
            />
          </div>
          <hr class="no-margin">
          <div>
            <span class="accent-title"> BEDRIFT </span>
            <SidebarMenuOption
              to="/desktop/bedrift-teamsammensetning"
              name="Teamsammensetning"
              :is-selected="activePage === '/desktop/bedrift-teamsammensetning'"
              :icon="PersonStanding"
            />
            <SidebarMenuOption
              to="/desktop/bedrift-analyse"
              name="Analyse"
              :is-selected="activePage === '/desktop/bedrift-analyse'"
              :icon="ChartColumnIncreasing"
            />
            <SidebarMenuOption
              to="/desktop/bedrift-opplaering"
              name="Opplæring"
              :is-selected="activePage === '/desktop/bedrift-opplaering'"
              :icon="Brain"
            />
          </div>
          <hr class="no-margin">
          <div>
            <span class="accent-title">Meg</span>
            <SidebarMenuOption
              to="/desktop/oppgaver-oversikt"
              name="Mine oppgaver"
              :is-selected="activePage === '/desktop/oppgaver-oversikt'"
              :icon="CheckCheck"
            />
          </div>
        </div>
        <SidebarMenuOption
          v-if="currentUserRole === 'OWNER'"
          to="/desktop/bedrift-innstillinger"
          name="Bedrift Innstillinger"
          :is-selected="activePage === '/desktop/bedrift-innstillinger'"
          :icon="Settings"
        />
      </template>
    </div>
    <main class="main-view">
      <slot />
    </main>
  </div>
</template>

<style scoped>
.sidebar-page-container {
  display: grid;
  grid-template-columns: 2rem 1fr 1rem 1fr 1rem 1fr 1rem 1fr 1rem 1fr 2rem;
  height: 100%;
  min-height: 0;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  grid-column: span 2;
  width: 100%;
  background-color: var(--white-greek);
  border-right: 1px solid var(--blue-navy-40);
  display: flex;
  /* align-items: center; */
  justify-content: space-between;
  flex-direction: column;
  > div {
    display: flex;
    flex-direction: column;
    > div {
      display: flex;
      flex-direction: column;
      gap: 0.5rem;
    }
    > hr {
      border-width: 0px;
      border-top: 1px solid var(--blue-navy-40);
    }
  }
}
@media (min-width: 1200px) {
  /* Styles for devices larger than 1200px */
  .sidebar {
    padding: 4rem 2rem;
    > div {
      gap: 1rem;
    }
  }
}
@media (min-width: 768px) and (max-width: 1200px) {
  /* Styles for devices between 768px and 1200px */
  .sidebar {
    padding: 2rem 1rem;
    > div {
      gap: 1rem;
    }
  }
}
@media (min-width: 576px) and (max-width: 768px) {
  /* Styles for devices between 576px and 768px */
  .sidebar {
    padding-left: 0.5rem;
    padding-right: 0.5rem;
    > div {
      gap: 0.5rem;
    }
  }
}
@media (min-width: 0px) and (max-width: 576px) {
  /* Styles for devices between 0px and 576px */
  .sidebar {
    padding-left: 0.25rem;
    padding-right: 0.25rem;
    font-size: small;
    .accent-title {
      font-weight: 600;
    }
    > div {
      gap: 0.25rem;
    }
  }
}

.main-view {
  grid-column: span 9;
  padding-right: 2rem;
  padding-left: 1rem;
  width: 100%;
  /* background-color: blue; */
  min-height: 0;
  overflow-y: auto;
}
.accent-title {
  color: var(--blue-navy);
}
</style>
