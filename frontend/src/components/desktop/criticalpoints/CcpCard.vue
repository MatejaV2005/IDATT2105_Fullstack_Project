<script setup lang="ts">
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import UserBadge from '@/components/desktop/shared/UserBadge.vue'
import type { CriticalControlPoint } from '@/interfaces/api-interfaces'
import { Edit2 } from '@lucide/vue'
import { ref } from 'vue'

defineProps<{
  ccp: CriticalControlPoint
}>()

const expandedMeasures = ref<Record<number, boolean>>({})

function toggleMeasure(measureIndex: number) {
  expandedMeasures.value[measureIndex] = !expandedMeasures.value[measureIndex]
}

function getMeasureDescription(measureIndex: number, text: string) {
  if (expandedMeasures.value[measureIndex] || text.length <= 220) {
    return text
  }

  return `${text.slice(0, 220)}...`
}
</script>

<template>
  <div class="ccp">
    <div class="ccp-header">
      <div>
        <h2 class="no-margin">
          {{ ccp.name }}
        </h2>
      </div>
      <DesktopButton
        :icon="Edit2"
        content="Rediger"
        disabled
      />
    </div>

    <div class="ccp-grid">
      <div class="info-card">
        <h3 class="no-margin">
          Hvordan overvåkes det
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
        Hva overvåkes
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
        <span class="navy-subtitle">Utførere</span>
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
          <span>{{ getMeasureDescription(measureIndex, measure.measureDescription) }}</span>
          <button
            v-if="measure.measureDescription.length > 220"
            class="show-more-button"
            type="button"
            @click="toggleMeasure(measureIndex)"
          >
            {{ expandedMeasures[measureIndex] ? 'Vis mindre' : 'Vis mer' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
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
}

.measure-container > div {
  max-width: 60%;
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

@media (max-width: 1200px) {
  .ccp-grid,
  .people-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .ccp-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .measure-container {
    flex-direction: column;
  }
}
</style>
