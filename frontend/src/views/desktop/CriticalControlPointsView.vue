<script setup lang="ts">
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import Paginator from '@/components/desktop/shared/Paginator.vue'
import UserBadge from '@/components/desktop/shared/UserBadge.vue'
import { Edit2, Plus } from '@lucide/vue'
import { ref } from 'vue'

const expandedMeasures = ref<Record<string, boolean>>({})

function getMeasureKey(ccpIndex: number, measureIndex: number) {
  return `${ccpIndex}-${measureIndex}`
}

function toggleMeasure(ccpIndex: number, measureIndex: number) {
  const key = getMeasureKey(ccpIndex, measureIndex)
  expandedMeasures.value[key] = !expandedMeasures.value[key]
}

function getMeasureDescription(ccpIndex: number, measureIndex: number, text: string) {
  const key = getMeasureKey(ccpIndex, measureIndex)
  if (expandedMeasures.value[key] || text.length <= 220) {
    return text
  }

  return `${text.slice(0, 220)}...`
}

const ccps = [
  {
    name: 'Renhold av lokaler og utstyr',
    how: 'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea  commodo consequat. Duis aute irure dolor. Lerum in reprehenderit in voluptate  velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint  occaecat cupidatat non proident, sunt in culpa qui officia deserunt  mollit anim id est laborum',
    equipment:
      'Lorem ipsum esse dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore esse magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation esse ullamco laboris nisi ut aliquip ex ea  commodo consequat. Duis aute irure dolor. Lerum in reprehenderit in voluptate  velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint  occaecat cupidatat non proident, sunt in culpa qui officia deserunt  mollit anim id est laborum',
    instructionsAndCalibration:
      'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea  commodo consequat. Duis aute irure dolor. Lerum in reprehenderit in voluptate  velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint  occaecat cupidatat non proident, sunt in culpa qui officia deserunt  mollit anim id est laborum esse',
    immediateCorrectiveAction:
      'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea  commodo consequat. Duis aute irure dolor. Lerum in reprehenderit in voluptate  velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint  occaecat cupidatat non proident, sunt in culpa qui officia deserunt  mollit anim id est laborum. Duis aute irure dolor. Lerum in reprehenderit in voluptate  velit esse cillum.',
    criticalMin: 2.1,
    criticalMax: 4.2,
    unit: 'C',
    monitoredDescription: '',
    verifiers: [
      {
        userId: 1234,
        userName: 'Kari Næss Northun',
      },
      {
        userId: 5643,
        userName: 'Stoltenberg',
      },
    ],
    deviationRecievers: [
      {
        userId: 1234,
        userName: 'Simen Velle',
      },
      {
        userId: 5643,
        userName: 'Ola Svenneby',
      },
    ],
    performers: [
      {
        userId: 1234,
        userName: 'Jonas Ghar Støre',
      },
      {
        userId: 5643,
        userName: 'Jens Stoltenberg',
      },
    ],
    deputy: [
      {
        userId: 1234,
        userName: 'Kårw Willoch',
      },
      {
        userId: 5643,
        userName: 'Gro Harlem Brundtland',
      },
    ],
    ccpCorrectiveMeasure: [
      {
        productName: 'Burger',
        measureDescription:
          'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation',
      },
      {
        productName: 'Fish',
        measureDescription:
          'Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod  tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim  veniam, quis nostrud exercitation',
      },
    ],
  },
  {
    name: 'Oppbevaring av ferske råvarer',
    how: 'Kontroll utføres ved åpning, etter varelevering og før stengetid.',
    equipment: 'Kalibrert termometer og loggskjema.',
    instructionsAndCalibration: 'Termometer kontrolleres ukentlig mot referansepunkt.',
    immediateCorrectiveAction: 'Flytt produkter til reservekjøl og varsle ansvarlig leder.',
    criticalMin: 0,
    criticalMax: 4,
    unit: 'C',
    monitoredDescription: 'Kjølerom for fisk, kjøtt og meieri.',
    verifiers: [
      {
        userId: 3344,
        userName: 'Mona Jul',
      },
    ],
    deviationRecievers: [
      {
        userId: 4499,
        userName: 'Ane Brevik',
      },
    ],
    performers: [
      {
        userId: 9301,
        userName: 'Jagland',
      },
    ],
    deputy: [
      {
        userId: 1199,
        userName: 'Bondevik',
      },
    ],
    ccpCorrectiveMeasure: [
      {
        productName: 'Kylling',
        measureDescription: 'Kasser produkt hvis temperaturgrense er brutt over tid.',
      },
    ],
  },
  {
    name: 'Varmholding ved servering',
    how: 'Måles hver 30. minutt under service.',
    equipment: 'Digitalt stikktermometer.',
    instructionsAndCalibration: 'Termometer desinfiseres mellom hver måling.',
    immediateCorrectiveAction: 'Øk varme umiddelbart, eller ta produkt ut av salg.',
    criticalMin: 60,
    criticalMax: 90,
    unit: 'C',
    monitoredDescription: 'Supper, sauser og varme retter i buffet.',
    verifiers: [
      {
        userId: 2222,
        userName: 'Kari Næss Northun',
      },
    ],
    deviationRecievers: [
      {
        userId: 7777,
        userName: 'Simen Velle',
      },
    ],
    performers: [
      {
        userId: 9090,
        userName: 'Jens Stoltenberg',
      },
    ],
    deputy: [
      {
        userId: 8080,
        userName: 'Gro Harlem Brundtland',
      },
    ],
    ccpCorrectiveMeasure: [
      {
        productName: 'Suppe',
        measureDescription: 'Fortsett oppvarming til kritisk grense er oppnådd før servering.',
      },
      {
        productName: 'Saus',
        measureDescription: 'Skift beholder og dokumenter avvik i logg.',
      },
    ],
  },
]

function sayHello() {
  alert('Hello')
}
</script>

<template>
  <div class="con">
    <main>
      <div class="main-no-sidebar-container">
        <Paginator />
        <h1 class="instrument-serif-regular no-margin">
          Kritiske punkter
        </h1>
        <hr class="navy-hr">

        <div
          v-for="(ccp, index) in ccps"
          :key="`${ccp.name}-${index}`"
          class="ccp"
        >
          <div class="ccp-header">
            <div>
              <h2 class="no-margin">
                {{ ccp.name }}
              </h2>
            </div>
            <DesktopButton
              :icon="Edit2"
              content="Rediger"
              :on-click="sayHello"
            />
          </div>

          <div class="ccp-grid">
            <div class="info-card">
              <h3 class="no-margin">
                Hvordan overvakes det
              </h3>
              <span>{{ ccp.how }}</span>
            </div>
            <div class="info-card">
              <h3 class="no-margin">
                Utstyr
              </h3>
              <span>{{ ccp.equipment }}</span>
            </div>
            <div class="info-card">
              <h3 class="no-margin">
                Instruks og kalibrering
              </h3>
              <span>{{ ccp.instructionsAndCalibration }}</span>
            </div>
            <div class="info-card">
              <h3 class="no-margin">
                Umiddelbart avvikstiltak
              </h3>
              <span>{{ ccp.immediateCorrectiveAction }}</span>
            </div>
          </div>

          <div class="thresholds">
            <span class="navy-subtitle">Kritisk grense:</span>
            <span>{{ ccp.criticalMin }} - {{ ccp.criticalMax }} {{ ccp.unit }}</span>
          </div>

          <div
            v-if="ccp.monitoredDescription"
            class="info-card"
          >
            <h3 class="no-margin">
              Hva overvakes
            </h3>
            <span>{{ ccp.monitoredDescription }}</span>
          </div>

          <div class="people-grid">
            <div>
              <span class="navy-subtitle">Godkjennere</span>
              <div class="user-parent">
                <UserBadge
                  v-for="verifier in ccp.verifiers"
                  :key="verifier.userId"
                  :name="verifier.userName"
                  :user-id="verifier.userId"
                />
              </div>
            </div>
            <div>
              <span class="navy-subtitle">Avviksmottakere</span>
              <div class="user-parent">
                <UserBadge
                  v-for="deviationReciever in ccp.deviationRecievers"
                  :key="deviationReciever.userId"
                  :name="deviationReciever.userName"
                  :user-id="deviationReciever.userId"
                />
              </div>
            </div>
            <div>
              <span class="navy-subtitle">Utforere</span>
              <div class="user-parent">
                <UserBadge
                  v-for="performer in ccp.performers"
                  :key="performer.userId"
                  :name="performer.userName"
                  :user-id="performer.userId"
                />
              </div>
            </div>
            <div>
              <span class="navy-subtitle">Vikarleder</span>
              <div class="user-parent">
                <UserBadge
                  v-for="deputy in ccp.deputy"
                  :key="deputy.userId"
                  :name="deputy.userName"
                  :user-id="deputy.userId"
                />
              </div>
            </div>
          </div>

          <div>
            <span class="navy-subtitle">Korrigerende tiltak</span>
            <div class="measure-container">
              <div
                v-for="(measure, measureIndex) in ccp.ccpCorrectiveMeasure"
                :key="`${measure.productName}-${measureIndex}`"
                class="measure-card"
              >
                <h3 class="no-margin">
                  {{ measure.productName }}
                </h3>
                <span>{{
                  getMeasureDescription(index, measureIndex, measure.measureDescription)
                }}</span>
                <button
                  v-if="measure.measureDescription.length > 220"
                  class="show-more-button"
                  type="button"
                  @click="toggleMeasure(index, measureIndex)"
                >
                  {{
                    expandedMeasures[getMeasureKey(index, measureIndex)] ? 'Vis mindre' : 'Vis mer'
                  }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <DesktopButton
          :icon="Plus"
          content="Legg til CCP"
          :on-click="sayHello"
        />
      </div>
    </main>
  </div>
</template>

<style scoped>
.con {
  overflow: scroll;
}

.user-parent {
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.ccp {
  border-radius: 1rem;
  padding: 1rem;
  display: flex;
  gap: 1rem;
  flex-direction: column;
  border: 1px solid var(--blue-decor);
  background-color: var(--blue-decor-10);
}

.ccp-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
}

.ccp-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.info-card {
  padding: 1rem;
  border-radius: 0.5rem;
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.thresholds {
  padding: 1rem;
  border-radius: 0.5rem;
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
  display: flex;
  gap: 0.5rem;
  flex-wrap: wrap;
}

.people-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.measure-container {
  display: flex;
  gap: 1rem;
  > div {
    max-width: 60%;
  }
}

.measure-card {
  padding: 1rem;
  border-radius: 0.5rem;
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.show-more-button {
  border: 0;
  background: transparent;
  color: var(--blue-decor);
  padding: 0;
  width: fit-content;
  cursor: pointer;
}

.show-more-button:hover,
.show-more-button:focus {
  text-decoration: underline;
}

main {
  display: flex;
  margin-top: 5rem;
  padding-bottom: 5rem;
  padding-left: 2rem;
  padding-right: 2rem;
  display: flex;
  justify-content: center;
  align-items: center;
  flex-direction: column;
  width: 100%;
  .main-no-sidebar-container {
    width: calc(5 / 5 * 100%);
    display: flex;
    flex-direction: column;
    gap: 1rem;
  }
}

hr {
  border-color: var(--blue-navy-40);
  border-width: 1px;
}

@media (max-width: 1200px) {
  .ccp-grid,
  .people-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  main {
    padding-left: 1rem;
    padding-right: 1rem;
    .main-no-sidebar-container {
      width: 100%;
    }
  }

  .ccp-header {
    flex-direction: column;
    align-items: flex-start;
  }
  .measure-container {
    flex-direction: column;
  }
}
</style>
