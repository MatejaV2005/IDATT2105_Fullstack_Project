<script setup lang="ts">
import Badge from '@/components/desktop/shared/Badge.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import { CircleAlert, Edit2, User } from '@lucide/vue'

const mappingPoints = [
  {
    law: 'AL § 1-5',
    dots: 8,
    title: 'Salg eller utlevering til person som er under 18 år',
    challenges:
      'Mindreårige kunder bruker lånt/falskt ID-kort. Kassapersonalet sjekker ikke legitimasjon eller vet ikke hvordan man foretar legitimasjonskontroll. Det er vanskelig å fastslå alder. Ansatte synes det er ubehagelig å spørre om legitimasjon, det er enklere å la være. Mindreårige handler sammen med eldre venner og de eldre vennene kjøper/betaler.',
    measures:
      'Instruks om å sjekke legitimasjon på alle som ser ut som de er under 25 år. Oppslag i kassen om at alle som er yngre enn 25 år blir bedt om å vise legitimasjon. Heng opp en oversikt som viser de ansatte hvilket årstall man må være født i for å være over 18 år. Er personalet i tvil om alder, har den ansatte rett plikt til å spørre om (men ikke kreve) legitimasjon. Hvis personen ikke viser legitimasjon, skal salg nektes. Steg-for-steg rutiner for hva man ser etter ved legitimasjonskontroll: Hold legitimasjonen i hånden. Sjekk om det er helt, og kjennes ekte ut. Ved å stryke tommelen diagonalt over ID-kortet kjenner man om det er i flukt eller forhøyet på riktige steder. Bruk god tid. Ligner bildet? Stemmer alderen? Spør evt. om stjernetegn. Bruke kassasystemer som minner om legitimasjonskontroll ved registrering av alkoholholdig drikk. Ved mistanke om at kjøpet skjer på vegne av en mindreårig, medfølgende venn, nektes salg. Salg til mindreårige er fast tema på alle personalsamtaler/møter.',
    responsible: ['Mímir Kristjánsson', 'Ane Brevik'],
    responsibleText: 'Hvem enn som er i kassen ved gitt tidspunkt',
  },
  {
    law: 'AL § 1-5',
    dots: 8,
    title: 'Salg eller utlevering til person som er under 18 år',
    challenges:
      'Mindreårige kunder bruker lånt/falskt ID-kort. Kassapersonalet sjekker ikke legitimasjon eller vet ikke hvordan man foretar legitimasjonskontroll. Det er vanskelig å fastslå alder. Ansatte synes det er ubehagelig å spørre om legitimasjon, det er enklere å la være. Mindreårige handler sammen med eldre venner og de eldre vennene kjøper/betaler.',
    measures:
      'Instruks om å sjekke legitimasjon på alle som ser ut som de er under 25 år. Oppslag i kassen om at alle som er yngre enn 25 år blir bedt om å vise legitimasjon. Heng opp en oversikt som viser de ansatte hvilket årstall man må være født i for å være over 18 år. Er personalet i tvil om alder, har den ansatte rett plikt til å spørre om (men ikke kreve) legitimasjon. Hvis personen ikke viser legitimasjon, skal salg nektes. Steg-for-steg rutiner for hva man ser etter ved legitimasjonskontroll: Hold legitimasjonen i hånden. Sjekk om det er helt, og kjennes ekte ut. Ved å stryke tommelen diagonalt over ID-kortet kjenner man om det er i flukt eller forhøyet på riktige steder. Bruk god tid. Ligner bildet? Stemmer alderen? Spør evt. om stjernetegn. Bruke kassasystemer som minner om legitimasjonskontroll ved registrering av alkoholholdig drikk. Ved mistanke om at kjøpet skjer på vegne av en mindreårig, medfølgende venn, nektes salg. Salg til mindreårige er fast tema på alle personalsamtaler/møter.',
    responsible: ['Mímir Kristjánsson', 'Mona Jul'],
    responsibleText: 'Hvem enn som er i kassen ved gitt tidspunkt',
  },
  {
    law: 'AL § 1-5',
    dots: 8,
    title: 'Salg eller utlevering til person som er under 18 år',
    challenges:
      'Mindreårige kunder bruker lånt/falskt ID-kort. Kassapersonalet sjekker ikke legitimasjon eller vet ikke hvordan man foretar legitimasjonskontroll. Det er vanskelig å fastslå alder. Ansatte synes det er ubehagelig å spørre om legitimasjon, det er enklere å la være. Mindreårige handler sammen med eldre venner og de eldre vennene kjøper/betaler.',
    measures:
      'Instruks om å sjekke legitimasjon på alle som ser ut som de er under 25 år. Oppslag i kassen om at alle som er yngre enn 25 år blir bedt om å vise legitimasjon. Heng opp en oversikt som viser de ansatte hvilket årstall man må være født i for å være over 18 år. Er personalet i tvil om alder, har den ansatte rett plikt til å spørre om (men ikke kreve) legitimasjon. Hvis personen ikke viser legitimasjon, skal salg nektes. Steg-for-steg rutiner for hva man ser etter ved legitimasjonskontroll: Hold legitimasjonen i hånden. Sjekk om det er helt, og kjennes ekte ut. Ved å stryke tommelen diagonalt over ID-kortet kjenner man om det er i flukt eller forhøyet på riktige steder. Bruk god tid. Ligner bildet? Stemmer alderen? Spør evt. om stjernetegn. Bruke kassasystemer som minner om legitimasjonskontroll ved registrering av alkoholholdig drikk. Ved mistanke om at kjøpet skjer på vegne av en mindreårig, medfølgende venn, nektes salg. Salg til mindreårige er fast tema på alle personalsamtaler/møter.',
    responsible: [
      'Ola Svenneby',
      'Bondevik',
      'Ole Borten Moe',
      'Mona Jul',
      'Guri Melby',
      'Mímir Kristjánsson',
      'Kirsti Bergstø',
      'Jagland',
      'Siv Jensen',
      'Listhaug',
      'Ane Brevik',
      'Tangen',
      'Vedum',
    ],
    responsibleText: 'Hvem enn som er i kassen ved gitt tidspunkt',
  },
]
</script>

<template>
  <SidebarPageContainer active-page="/desktop/ik-alkohol-kartlegging-og-tiltak">
    <div class="mapping-area-container">
      <h1 class="instrument-serif-regular no-margin">
        Kartlegging & tiltak
      </h1>
      <div
        v-for="point in mappingPoints"
        class="mapping-point"
      >
        <div class="point-header">
          <div>
            <Badge badge-color="navy">
              {{ point.law }}
            </Badge>
            <Badge
              badge-color="cherry"
              :icon="CircleAlert"
            >
              {{ point.dots }} Prikker
            </Badge>
            <h2 class="no-margin">
              {{ point.title }}
            </h2>
          </div>
          <DesktopButton
            content="Rediger"
            :icon="Edit2"
          />
        </div>
        <div class="">
          <h3 class="navy-subtitle no-margin">
            Utfordringer
          </h3>
          {{ point.challenges }}
        </div>
        <div class="">
          <h3 class="navy-subtitle no-margin">
            Tiltak / Rutiner
          </h3>
          {{ point.measures }}
        </div>
        <div>
          <h3 class="navy-subtitle no-margin">
            Ansvarlige
          </h3>
          <div class="responsible-parent">
            <Badge
              v-for="user in point.responsible"
              badge-color="navy"
              :icon="User"
            >
              {{ user }}
            </Badge>
          </div>
        </div>
      </div>
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
.mapping-point {
  background-color: var(--white-greek);
  border-radius: 1rem;
  border: 1px solid var(--blue-navy-40);
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  overflow-wrap: anywhere;
  .point-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 0.75rem;
    padding-bottom: 1rem;
    border-bottom: 1px solid var(--blue-navy-40);
    /* background-color: red; */
  }
  .point-header > :first-child {
    display: flex;
    gap: 0.5rem;
    flex-wrap: wrap;
    min-width: 0;
  }
}
.responsible-parent {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
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

  .mapping-point {
    padding: 0.75rem;

    .point-header {
      flex-direction: column;
      align-items: flex-start;
    }

    .point-header > :first-child {
      width: 100%;
    }
  }
}
</style>
