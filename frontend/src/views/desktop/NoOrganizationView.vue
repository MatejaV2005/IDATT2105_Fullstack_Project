<script setup lang="ts">
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import { ensureOrgSessionLoaded, useOrgSession } from '@/composables/useOrgSession'
import { getPostAuthRoute } from '@/utils/auth-routing'
import { computed, onMounted } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

const { claims, organizations } = useOrgSession()
const userEmail = computed(() => claims.value?.email ?? '')
const router = useRouter()

onMounted(() => {
  void (async () => {
    if (!claims.value) {
      await router.replace('/auth')
      return
    }

    await ensureOrgSessionLoaded(true)
    const nextRoute = getPostAuthRoute(claims.value, organizations.value)
    if (nextRoute !== '/desktop/no-org') {
      await router.replace(nextRoute)
    }
  })()
})
</script>

<template>
  <SidebarPageContainer
    active-page="/desktop/no-org"
    navigation-mode="no-org"
  >
    <main class="no-org-shell">
      <section class="no-org-page">
        <h1 class="instrument-serif-regular no-margin">
          Du er logget inn, men har ingen organisasjon ennå
        </h1>

        <div class="no-org-grid">
          <article class="no-org-card">
            <p class="no-org-card__eyebrow">
              Join an org
            </p>
            <h2 class="no-margin">
              Be om å bli lagt til
            </h2>
            <p class="no-margin">
              Fortell manager eller owner at du har opprettet bruker i Grimni, og be dem legge deg
              til i organisasjonen med denne e-posten:
            </p>
            <p class="no-org-card__email no-margin">
              {{ userEmail || 'Ingen e-post tilgjengelig' }}
            </p>
          </article>

          <article class="no-org-card">
            <p class="no-org-card__eyebrow">
              Create an org
            </p>
            <h2 class="no-margin">
              Opprett en ny organisasjon
            </h2>
            <p class="no-margin">
              Hvis du skal sette opp en ny bedrift i Grimni, kan du opprette organisasjonen selv og
              bli owner med en gang.
            </p>
            <RouterLink
              class="no-org-card__button transition"
              to="/desktop/create-org"
            >
              Gå til opprett organisasjon
            </RouterLink>
          </article>
        </div>
      </section>
    </main>
  </SidebarPageContainer>
</template>

<style scoped>
.no-org-shell {
  width: 100%;
  display: flex;
  justify-content: center;
  padding: 2rem;
}

.no-org-page {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  width: min(1100px, 100%);
  padding: 1rem;
  margin-top: 2rem;
}

.no-org-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 1rem;
}

.no-org-card {
  background-color: var(--white-greek);
  border: 1px solid var(--blue-navy-40);
  border-radius: 1rem;
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.no-org-card__eyebrow {
  margin: 0;
  color: var(--blue-navy-80);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 0.85rem;
}

.no-org-card__email {
  font-weight: 600;
  color: var(--blue-navy);
}

.no-org-card__button {
  width: fit-content;
  border: 1px solid var(--blue-navy);
  border-radius: 0.5rem;
  background-color: var(--blue-navy);
  color: var(--white-greek);
  padding: 0.65rem 1rem;
  text-decoration: none;
}

.no-org-card__button:hover,
.no-org-card__button:focus {
  background-color: var(--blue-navy-20);
  color: var(--blue-navy);
}

@media (max-width: 900px) {
  .no-org-shell {
    padding: 1rem;
  }

  .no-org-grid {
    grid-template-columns: 1fr;
  }
}
</style>
