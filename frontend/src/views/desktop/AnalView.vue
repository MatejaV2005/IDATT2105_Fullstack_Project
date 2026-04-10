<script setup lang="ts">
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import Paginator from '@/components/desktop/shared/Paginator.vue'
import api from '@/api/api'
import { computed, onMounted, ref } from 'vue'

type AnalyticsOverview = {
  prerequisiteRoutineRecord: {
    completed: number
    failed: number
  }
  ccpRecords: {
    skipped: number
    verified: number
    rejected: number
    waiting: number
  }
  deviations: {
    ikMat: {
      open: number
      closed: number
    }
    ikAlkohol: {
      open: number
      closed: number
    }
    other: {
      open: number
      closed: number
    }
  }
  users: {
    owners: number
    managers: number
    workers: number
  }
  resources: {
    routines: number
    ccps: number
    productCategories: number
  }
}

const orgName = 'Trondfjord Mathus AS'

const resource = ref<AnalyticsOverview | null>(null)
const loading = ref(true)
const error = ref(false)

const totalDeviations = computed(() => {
  if (!resource.value) {
    return 0
  }

  const { ikMat, ikAlkohol, other } = resource.value.deviations
  return ikMat.open + ikMat.closed + ikAlkohol.open + ikAlkohol.closed + other.open + other.closed
})

async function fetchAnalyticsOverview() {
  const response = await api.get<AnalyticsOverview>('/organizations/analysis')
  resource.value = response.data
}

onMounted(async () => {
  try {
    await fetchAnalyticsOverview()
    error.value = false
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    error.value = true
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <SidebarPageContainer active-page="/desktop/bedrift-analyse">
    <div class="analytics-page">
      <Paginator />
      <h1 class="instrument-serif-regular no-margin">Bedriftsanalyse</h1>
      <p class="no-margin org-label">Organisasjon: {{ orgName }}</p>
      <hr class="navy-hr" />

      <Loading v-if="loading" />

      <p v-else-if="error" class="error-message no-margin">Klarte ikke å hente analyseoversikt.</p>

      <template v-else-if="resource">
        <section class="stats-grid">
          <article class="stat-card">
            <h2 class="no-margin">Rutineposter</h2>
            <p class="no-margin">Fullfort: {{ resource.prerequisiteRoutineRecord.completed }}</p>
            <p class="no-margin">Feilet: {{ resource.prerequisiteRoutineRecord.failed }}</p>
          </article>

          <article class="stat-card">
            <h2 class="no-margin">CCP-poster</h2>
            <p class="no-margin">Verifisert: {{ resource.ccpRecords.verified }}</p>
            <p class="no-margin">Venter: {{ resource.ccpRecords.waiting }}</p>
            <p class="no-margin">Avvist: {{ resource.ccpRecords.rejected }}</p>
            <p class="no-margin">Hoppet over: {{ resource.ccpRecords.skipped }}</p>
          </article>

          <article class="stat-card">
            <h2 class="no-margin">Brukere i organisasjon</h2>
            <p class="no-margin">Owners: {{ resource.users.owners }}</p>
            <p class="no-margin">Managers: {{ resource.users.managers }}</p>
            <p class="no-margin">Workers: {{ resource.users.workers }}</p>
          </article>

          <article class="stat-card">
            <h2 class="no-margin">Innhold i systemet</h2>
            <p class="no-margin">Rutiner: {{ resource.resources.routines }}</p>
            <p class="no-margin">CCP-er: {{ resource.resources.ccps }}</p>
            <p class="no-margin">Produktkategorier: {{ resource.resources.productCategories }}</p>
          </article>
        </section>

        <section class="stat-card deviations-card">
          <h2 class="no-margin">Avvik per kategori</h2>
          <p class="no-margin total-deviation">Totalt antall avvik: {{ totalDeviations }}</p>
          <div class="deviation-row header-row">
            <span>Kategori</span>
            <span>Åpne</span>
            <span>Lukkede</span>
          </div>
          <div class="deviation-row">
            <span>IK_MAT</span>
            <span>{{ resource.deviations.ikMat.open }}</span>
            <span>{{ resource.deviations.ikMat.closed }}</span>
          </div>
          <div class="deviation-row">
            <span>IK_ALKOHOL</span>
            <span>{{ resource.deviations.ikAlkohol.open }}</span>
            <span>{{ resource.deviations.ikAlkohol.closed }}</span>
          </div>
          <div class="deviation-row">
            <span>OTHER</span>
            <span>{{ resource.deviations.other.open }}</span>
            <span>{{ resource.deviations.other.closed }}</span>
          </div>
        </section>
      </template>
    </div>
  </SidebarPageContainer>
</template>

<style scoped>
.analytics-page {
  margin-top: 2rem;
  padding-bottom: 3rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.org-label {
  color: var(--blue-navy-80);
}

.stats-grid {
  display: grid;
  gap: 1rem;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.stat-card {
  background-color: var(--white-greek);
  border: 1px solid var(--blue-navy-40);
  border-radius: 1rem;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}

.deviations-card {
  gap: 0.5rem;
}

.total-deviation {
  color: var(--blue-navy-80);
}

.deviation-row {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 0.5rem;
  border-top: 1px solid var(--blue-navy-20);
  padding-top: 0.5rem;
}

.header-row {
  font-weight: 700;
  border-top: 0;
  padding-top: 0.25rem;
}

.error-message {
  color: #b42318;
}

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
