<script setup lang="ts">
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import UserBadge from '@/components/desktop/shared/UserBadge.vue'
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import type { DangerAnalysisCollaboratorsAllInfo } from '@/interfaces/api-interfaces'
import { delay } from '@/utils'
import { Plus, SendHorizonal } from '@lucide/vue'
import { onMounted, ref } from 'vue'

function hello() {
  alert('hello')
}

const resource = ref<DangerAnalysisCollaboratorsAllInfo>([])
const loading = ref(true)
const error = ref<boolean | null>(null)

onMounted(async () => {
  try {
    // const response = await fetch('/api/haccp/danger-analysis/collaborators')
    // if (!response.ok) {
    //     throw new Error(`Failed to update user (${response.status})`)
    // }
    // const data = await response.json()
    await delay(2000)
    const data: DangerAnalysisCollaboratorsAllInfo = [
      { userId: 123, legalName: 'Einar Gerherdsen' },
      { userId: 456, legalName: 'Kari Naess Northun' },
    ]
    resource.value = data
    loading.value = false
    error.value = false
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    error.value = true
  }
})
</script>

<template>
  <SidebarPageContainer active-page="/desktop/haccp">
    <div class="haccp-area-container">
      <h1 class="instrument-serif-regular no-margin">
        HACCP
      </h1>
      <div class="haccp-part">
        <div class="part-header">
          <div class="header-first">
            <div class="header-num">
              1
            </div>
            Grunnforutsetningene
          </div>
          <UserBadge
            name="Kun deg"
            :user-id="-1"
          />
        </div>
        <RouterLink
          class="part-bottom"
          to="/desktop/haccp/prerequisites"
        >
          <DesktopButton
            :icon="SendHorizonal"
            content="Endre"
          />
        </RouterLink>
      </div>
      <div class="haccp-part">
        <div class="part-header">
          <div class="header-first">
            <div class="header-num">
              2
            </div>
            Fareanalyse
          </div>
          <div class="user-container">
            <Loading v-if="loading" class="loading-in-user-container" />
            <span v-else-if="error">Kunne ikke hente medlemmer</span>
            <UserBadge
              v-for="collaborator in resource"
              :key="collaborator.userId"
              :name="collaborator.legalName"
              :user-id="collaborator.userId"
            />
            <div v-if="resource.length > 0" class="vertical-divider" />
            <DesktopButton
              :on-click="hello"
              content="Legg til medlem"
              :icon="Plus"
            />
            <vr />
          </div>
        </div>
        <RouterLink
          class="part-bottom"
          to="/desktop/haccp/danger-analysis"
        >
          <DesktopButton
            :icon="SendHorizonal"
            content="Endre"
          />
        </RouterLink>
      </div>
      <div
        to="haccp/ccps"
        class="haccp-part"
      >
        <div class="part-header">
          <div class="header-first">
            <div class="header-num">
              3
            </div>
            Kritiske punkter
          </div>
          <UserBadge
            name="Kun deg"
            :user-id="-1"
          />
        </div>
        <RouterLink
          class="part-bottom"
          to="/desktop/haccp/ccps"
        >
          <DesktopButton
            :icon="SendHorizonal"
            content="Endre"
          />
        </RouterLink>
      </div>
    </div>
  </SidebarPageContainer>
</template>
<style scoped>
.user-container {
  display: flex;
  gap: 0.25rem;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  .loading-in-user-container {
    margin-right: .5rem;
  }
}
.haccp-area-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border-radius: 1rem;
  padding: 1rem;
  margin-top: 2rem;
}
.part-bottom {
  display: flex;
  justify-content: end;
}
.haccp-part {
  background-color: var(--white-greek);
  border: 1px solid var(--blue-navy-40);
  border-radius: 1rem;
  padding: 1rem;
  height: 12rem;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 1rem;
  overflow-wrap: anywhere;
  .part-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-bottom: 1rem;
    border-bottom: 1px solid var(--blue-navy-40);
    .header-first {
      display: flex;
      gap: 0.5rem;
      justify-content: center;
      align-items: center;
    }
    .header-num {
      background-color: var(--blue-decor-20);
      border: 1px solid var(--blue-decor);
      border-radius: 1rem;
      width: 2rem;
      height: 2rem;
      display: flex;
      justify-content: center;
      align-items: center;
      color: var(--blue-decor);
    }
  }
}

@media (max-width: 1200px) {
  .haccp-area-container {
    margin-top: 1rem;
    padding: 0.75rem;
  }

  .haccp-part {
    height: auto;
    min-height: 12rem;
  }
}

@media (max-width: 768px) {
  .haccp-area-container {
    padding: 0.5rem;
  }

  .haccp-part {
    padding: 0.75rem;
    min-height: 0;

    .part-header {
      flex-direction: column;
      align-items: flex-start;
      gap: 0.75rem;

      .header-first {
        flex-wrap: wrap;
        justify-content: flex-start;
      }
    }
  }

  .part-bottom {
    width: 100%;
    justify-content: stretch;
  }

  .part-bottom :deep(.navy-button) {
    width: 100%;
  }
}
</style>
