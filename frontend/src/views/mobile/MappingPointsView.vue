<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { CircleAlert, EyeOff, Eye } from '@lucide/vue'
import SectionHeading from '@/components/SectionHeading.vue'
import api from '@/api/api.js'

interface MappingPoint {
  id: number
  law: string
  dots: number
  title: string
  challenges: string
  measures: string
  responsibleText: string
}

const mockData: MappingPoint[] = [
  {
    id: 1,
    law: 'AL § 1-5',
    dots: 8,
    title: 'Salg eller utlevering til person som er under 18 år',
    challenges:
      'Mindreårige kunder bruker lånt/falskt ID-kort. Kassapersonalet sjekker ikke legitimasjon eller vet ikke hvordan man foretar legitimasjonskontroll. Det er vanskelig å fastslå alder. Ansatte synes det er ubehagelig å spørre om legitimasjon, det er enklere å la være. Mindreårige handler sammen med eldre venner og de eldre vennene kjøper/betaler.',
    measures:
      'Instruks om å sjekke legitimasjon på alle som ser ut som de er under 25 år. Oppslag i kassen om at alle som er yngre enn 25 år blir bedt om å vise legitimasjon. Heng opp en oversikt som viser de ansatte hvilket årstall man må være født i for å være over 18 år. Er personalet i tvil om alder, har den ansatte rett plikt til å spørre om (men ikke kreve) legitimasjon. Hvis personen ikke viser legitimasjon, skal salg nektes. Steg-for-steg rutiner for hva man ser etter ved legitimasjonskontroll: Hold legitimasjonen i hånden. Sjekk om det er helt, og kjennes ekte ut. Ved å stryke tommelen diagonalt over ID-kortet kjenner man om det er i flukt eller forhøyet på riktige steder. Bruk god tid. Ligner bildet? Stemmer alderen? Spør evt. om stjernetegn. Bruke kassasystemer som minner om legitimasjonskontroll ved registrering av alkoholholdig drikk. Ved mistanke om at kjøpet skjer på vegne av en mindreårig, medfølgende venn, nektes salg. Salg til mindreårige er fast tema på alle personalsamtaler/møter.',
    responsibleText: 'Hvem enn som er i kassen ved gitt tidspunkt',
  },
  {
    id: 2,
    law: 'AL § 1-5',
    dots: 8,
    title: 'Salg eller utlevering til person som er under 18 år',
    challenges:
      'Mindreårige kunder bruker lånt/falskt ID-kort. Kassapersonalet sjekker ikke legitimasjon eller vet ikke hvordan man foretar legitimasjonskontroll. Det er vanskelig å fastslå alder. Ansatte synes det er ubehagelig å spørre om legitimasjon, det er enklere å la være. Mindreårige handler sammen med eldre venner og de eldre vennene kjøper/betaler.',
    measures:
      'Instruks om å sjekke legitimasjon på alle som ser ut som de er under 25 år. Oppslag i kassen om at alle som er yngre enn 25 år blir bedt om å vise legitimasjon. Heng opp en oversikt som viser de ansatte hvilket årstall man må være født i for å være over 18 år. Er personalet i tvil om alder, har den ansatte rett plikt til å spørre om (men ikke kreve) legitimasjon. Hvis personen ikke viser legitimasjon, skal salg nektes. Steg-for-steg rutiner for hva man ser etter ved legitimasjonskontroll: Hold legitimasjonen i hånden. Sjekk om det er helt, og kjennes ekte ut. Ved å stryke tommelen diagonalt over ID-kortet kjenner man om det er i flukt eller forhøyet på riktige steder. Bruk god tid. Ligner bildet? Stemmer alderen? Spør evt. om stjernetegn. Bruke kassasystemer som minner om legitimasjonskontroll ved registrering av alkoholholdig drikk. Ved mistanke om at kjøpet skjer på vegne av en mindreårig, medfølgende venn, nektes salg. Salg til mindreårige er fast tema på alle personalsamtaler/møter.',
    responsibleText: 'Alle',
  },
]

const mappingPoints = ref<MappingPoint[]>([])
const hiddenPoints = ref<number[]>([])
const isLoading = ref(true)
const error = ref<string | null>(null)

const visiblePoints = computed(() =>
  mappingPoints.value.filter(p => !hiddenPoints.value.includes(p.id))
)

const hiddenPointsList = computed(() =>
  mappingPoints.value.filter(p => hiddenPoints.value.includes(p.id))
)

function hidePoint(id: number) {
  if (!hiddenPoints.value.includes(id)) {
    hiddenPoints.value.push(id)
  }
}

function showPoint(id: number) {
  hiddenPoints.value = hiddenPoints.value.filter(h => h !== id)
}

async function fetchMappingPoints() {
  try {
    const response = await api.get<MappingPoint[]>('/mapping-points')
    mappingPoints.value = response.data
  } catch (err) {
    mappingPoints.value = mockData
    error.value = 'Kunne ikke hente data fra server. Viser eksempeldata.'
    console.error(err)
  } finally {
    isLoading.value = false
  }
}

onMounted(fetchMappingPoints)
</script>

<template>
  <section class="page-layout">
    <SectionHeading
      title="Kartlegging & tiltak"
      icon="alert"
    />

    <div
      v-if="isLoading"
      class="loading"
    >
      Laster...
    </div>
    <div
      v-else-if="error"
      class="error-banner"
    >
      <CircleAlert :size="20" />
      <p>{{ error }}</p>
    </div>

    <div v-if="isLoading" />
    <div
      v-else-if="mappingPoints.length === 0"
      class="empty"
    >
      <p>Ingen kartleggingspunkter funnet.</p>
    </div>

    <template v-else>
      <div
        v-if="visiblePoints.length > 0"
        class="mapping-list"
      >
        <article
          v-for="point in visiblePoints"
          :key="point.id"
          class="surface-card mapping-card"
        >
          <header class="mapping-card__header">
            <div class="mapping-card__badges">
              <span class="badge badge--navy">{{ point.law }}</span>
              <span class="badge badge--cherry">
                <CircleAlert :size="14" />
                {{ point.dots }} prikker
              </span>
              <button
                class="hide-button"
                type="button"
                :aria-label="`Skjul ${point.title}`"
                @click="hidePoint(point.id)"
              >
                <EyeOff :size="16" />
              </button>
            </div>
            <h2 class="mapping-card__title">
              {{ point.title }}
            </h2>
          </header>

          <section
            v-if="point.challenges"
            class="mapping-card__section"
          >
            <h3>Utfordringer</h3>
            <p>{{ point.challenges }}</p>
          </section>

          <section
            v-if="point.measures"
            class="mapping-card__section"
          >
            <h3>Tiltak / Rutiner</h3>
            <p>{{ point.measures }}</p>
          </section>

          <section
            v-if="point.responsibleText"
            class="mapping-card__section"
          >
            <h3>Ansvarlig</h3>
            <p>{{ point.responsibleText }}</p>
          </section>
        </article>
      </div>

      <div
        v-if="hiddenPointsList.length > 0"
        class="hidden-list"
      >
        <div class="hidden-list__header">
          <h3>Skjulte punkter ({{ hiddenPointsList.length }})</h3>
        </div>
        <article
          v-for="point in hiddenPointsList"
          :key="point.id"
          class="hidden-item"
        >
          <span class="hidden-item__title">{{ point.title }}</span>
          <button
            class="restore-button"
            type="button"
            :aria-label="`Vis ${point.title}`"
            @click="showPoint(point.id)"
          >
            <Eye :size="16" />
            Vis
          </button>
        </article>
      </div>
    </template>
  </section>
</template>

<style scoped>
.loading {
  text-align: center;
  padding: 48px 16px;
  color: var(--color-ink-400);
  font-size: 15px;
}

.error-banner {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 20px;
  background: linear-gradient(135deg, rgba(220, 38, 38, 0.08), rgba(255, 255, 255, 0.95));
  border: 1px solid rgba(220, 38, 38, 0.2);
  border-radius: var(--radius-sm);
  color: var(--color-danger);
}

.error-banner :deep(svg) {
  flex-shrink: 0;
  margin-top: 2px;
}

.error-banner p {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
}

.empty {
  text-align: center;
  padding: 48px 16px;
  color: var(--color-ink-400);
  font-size: 15px;
}

.mapping-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.mapping-card {
  padding: 20px;
}

.mapping-card__header {
  margin-bottom: 16px;
}

.mapping-card__badges {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.hide-button {
  margin-left: auto;
  padding: 6px;
  background: transparent;
  border: none;
  color: var(--color-ink-400);
  cursor: pointer;
  border-radius: var(--radius-xs);
  transition: color 0.2s, background 0.2s;
}

.hide-button:hover {
  color: var(--color-ink-700);
  background: var(--color-ink-100);
}

.badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: var(--radius-xs);
  font-size: 12px;
  font-weight: 600;
}

.badge--navy {
  background: var(--color-brand-100);
  color: var(--color-brand-700);
}

.badge--cherry {
  background: var(--color-danger-soft);
  color: var(--color-danger);
}

.mapping-card__title {
  font-size: 16px;
  font-weight: 600;
  color: var(--color-ink-950);
  margin: 0;
  line-height: 1.4;
}

.mapping-card__section {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--color-border);
}

.mapping-card__section h3 {
  font-size: 13px;
  font-weight: 600;
  color: var(--color-brand-700);
  margin: 0 0 8px;
}

.mapping-card__section p {
  font-size: 14px;
  color: var(--color-ink-700);
  margin: 0;
  line-height: 1.6;
}

.hidden-list {
  margin-top: 8px;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-sm);
  overflow: hidden;
}

.hidden-list__header {
  padding: 12px 16px;
  background: var(--color-ink-50);
  border-bottom: 1px solid var(--color-border);
}

.hidden-list__header h3 {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--color-ink-600);
}

.hidden-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: var(--color-surface);
  border-bottom: 1px solid var(--color-border);
}

.hidden-item:last-child {
  border-bottom: none;
}

.hidden-item__title {
  font-size: 14px;
  font-weight: 500;
  color: var(--color-ink-700);
}

.restore-button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: var(--color-brand-50);
  border: 1px solid var(--color-brand-200);
  border-radius: var(--radius-xs);
  font-size: 13px;
  font-weight: 500;
  color: var(--color-brand-700);
  cursor: pointer;
  transition: background 0.2s, border-color 0.2s;
}

.restore-button:hover {
  background: var(--color-brand-100);
  border-color: var(--color-brand-300);
}
</style>