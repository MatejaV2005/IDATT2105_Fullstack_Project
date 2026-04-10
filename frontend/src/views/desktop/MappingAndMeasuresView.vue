<script setup lang="ts">
import MappingPointCard from '@/components/desktop/alcohol/MappingPointCard.vue'
import MappingPointCreateCard from '@/components/desktop/alcohol/MappingPointCreateCard.vue'
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import type { MappingPoint, MappingPointAllInfo } from '@/interfaces/api-interfaces'
import { delay } from '@/utils'
import { Plus } from '@lucide/vue'
import { onMounted, ref } from 'vue'

const mockData: MappingPointAllInfo = [
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
    responsibleText: 'Hvem enn som er i kassen ved gitt tidspunkt',
  },
  {
    id: 3,
    law: 'AL § 1-5',
    dots: 8,
    title: 'Salg eller utlevering til person som er under 18 år',
    challenges:
      'Mindreårige kunder bruker lånt/falskt ID-kort. Kassapersonalet sjekker ikke legitimasjon eller vet ikke hvordan man foretar legitimasjonskontroll. Det er vanskelig å fastslå alder. Ansatte synes det er ubehagelig å spørre om legitimasjon, det er enklere å la være. Mindreårige handler sammen med eldre venner og de eldre vennene kjøper/betaler.',
    measures:
      'Instruks om å sjekke legitimasjon på alle som ser ut som de er under 25 år. Oppslag i kassen om at alle som er yngre enn 25 år blir bedt om å vise legitimasjon. Heng opp en oversikt som viser de ansatte hvilket årstall man må være født i for å være over 18 år. Er personalet i tvil om alder, har den ansatte rett plikt til å spørre om (men ikke kreve) legitimasjon. Hvis personen ikke viser legitimasjon, skal salg nektes. Steg-for-steg rutiner for hva man ser etter ved legitimasjonskontroll: Hold legitimasjonen i hånden. Sjekk om det er helt, og kjennes ekte ut. Ved å stryke tommelen diagonalt over ID-kortet kjenner man om det er i flukt eller forhøyet på riktige steder. Bruk god tid. Ligner bildet? Stemmer alderen? Spør evt. om stjernetegn. Bruke kassasystemer som minner om legitimasjonskontroll ved registrering av alkoholholdig drikk. Ved mistanke om at kjøpet skjer på vegne av en mindreårig, medfølgende venn, nektes salg. Salg til mindreårige er fast tema på alle personalsamtaler/møter.',
    responsibleText: 'Hvem enn som er i kassen ved gitt tidspunkt',
  },
]

const resource = ref<MappingPointAllInfo>([])
const loading = ref(true)
const error = ref<boolean | null>(null)

const isCreating = ref(false)
const isCreatingPoint = ref(false)
const createError = ref(false)

const savingPointId = ref<number | null>(null)
const deletingPointId = ref<number | null>(null)
const saveErrorPointId = ref<number | null>(null)
const deleteErrorPointId = ref<number | null>(null)

onMounted(async () => {
  try {
    // const response = await fetch('/api/mapping-points')
    // if (!response.ok) {
    //   throw new Error(`Failed to complete request... (${response.status})`)
    // }
    // const data = await response.json()
    await delay(2000)
    const data = mockData
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

function startCreating() {
  if (isCreatingPoint.value || savingPointId.value !== null || deletingPointId.value !== null) {
    return
  }
  isCreating.value = true
  createError.value = false
}

function cancelCreating() {
  if (isCreatingPoint.value) {
    return
  }
  isCreating.value = false
  createError.value = false
}

async function createPoint(payload: Omit<MappingPoint, 'id'>) {
  if (isCreatingPoint.value) {
    return
  }

  isCreatingPoint.value = true
  createError.value = false

  try {
    const pointToBeAdded: any = { // Omit<MappingPoint, 'id'>
      ...payload,
    }
    // const response = await fetch('/api/mapping-points', {
    //   method: 'POST',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify(pointToBeAdded),
    // })
    // if (!response.ok) {
    //   throw new Error(`Failed to complete request... (${response.status})`)
    // }
    // const newPoint: MappingPoint = await response.json()
    await delay(2000)
    // resource.value = [...resource.value, newPoint]
    resource.value = [...resource.value, pointToBeAdded]
    isCreating.value = false
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    createError.value = true
  } finally {
    isCreatingPoint.value = false
  }
}

async function savePoint(payload: MappingPoint) {
  if (savingPointId.value !== null || deletingPointId.value !== null) {
    return
  }

  savingPointId.value = payload.id
  saveErrorPointId.value = null
  deleteErrorPointId.value = null

  try {
    // const response = await fetch('/api/mapping-points', {
    //   method: 'PUT',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify(payload),
    // })
    // if (!response.ok) {
    //   throw new Error(`Failed to complete request... (${response.status})`)
    // }
    await delay(2000)
    resource.value = resource.value.map((point) => (point.id === payload.id ? payload : point))
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    saveErrorPointId.value = payload.id
  } finally {
    savingPointId.value = null
  }
}

async function deletePoint(payload: MappingPoint) {
  if (savingPointId.value !== null || deletingPointId.value !== null) {
    return
  }

  deletingPointId.value = payload.id
  deleteErrorPointId.value = null
  saveErrorPointId.value = null

  try {
    // const response = await fetch('/api/mapping-points', {
    //   method: 'DELETE',
    //   headers: { 'Content-Type': 'application/json' },
    //   body: JSON.stringify({ mappingPointId: payload.id }),
    // })
    // if (!response.ok) {
    //   throw new Error(`Failed to complete request... (${response.status})`)
    // }
    await delay(2000)
    resource.value = resource.value.filter((point) => point.id !== payload.id)
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    deleteErrorPointId.value = payload.id
  } finally {
    deletingPointId.value = null
  }
}
</script>

<template>
  <SidebarPageContainer active-page="/desktop/ik-alkohol-kartlegging-og-tiltak">
    <div class="mapping-area-container">
      <div class="header-row">
        <h1 class="instrument-serif-regular no-margin">
          Kartlegging & tiltak
        </h1>
        <DesktopButton
          v-if="!isCreating"
          content="Nytt punkt"
          :icon="Plus"
          :on-click="startCreating"
        />
      </div>

      <Loading v-if="loading" />
      <p
        v-else-if="error"
        class="error-message"
      >
        Klarte ikke å hente kartleggingspunkter.
      </p>

      <template v-else>
        <MappingPointCreateCard
          v-if="isCreating"
          :is-creating="isCreatingPoint"
          :create-error="createError"
          @cancel="cancelCreating"
          @create="createPoint"
        />

        <MappingPointCard
          v-for="point in resource"
          :key="point.id"
          :point="point"
          :is-saving="savingPointId === point.id"
          :is-deleting="deletingPointId === point.id"
          :save-error="saveErrorPointId === point.id"
          :delete-error="deleteErrorPointId === point.id"
          @save="savePoint"
          @delete-point="deletePoint"
        />
      </template>
    </div>
  </SidebarPageContainer>
</template>

<style scoped>
.mapping-area-container {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  border-radius: 1rem;
  padding: 1rem;
  margin-top: 2rem;
}

.header-row {
  display: flex;
  justify-content: space-between;
  gap: 1rem;
  align-items: center;
}

.error-message {
  color: #b42318;
  margin: 0;
}

@media (max-width: 1200px) {
  .mapping-area-container {
    margin-top: 1rem;
    padding: 0.5rem;
  }
}

@media (max-width: 768px) {
  .mapping-area-container {
    padding: 0;
  }

  .header-row {
    align-items: stretch;
    flex-direction: column;
  }
}
</style>
