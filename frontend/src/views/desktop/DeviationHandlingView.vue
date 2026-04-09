<script setup lang="ts">
import SidebarPageContainer from '@/components/desktop/sidebar/SidebarPageContainer.vue'
import Badge from '@/components/desktop/shared/Badge.vue'
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import { AlertTriangle, Check, X, Eye } from '@lucide/vue'
import { deviations, type Deviation, reviewStatuses, deviationCategories } from '@/data/deviations'
import api from '@/api/api'
import { ref, computed, onMounted } from 'vue'

const deviationsList = ref<Deviation[]>([])
const loading = ref(true)
const error = ref<string | null>(null)
const patchError = ref<string | null>(null)
const useMockData = ref(false)

const openDeviations = computed(() => 
  deviationsList.value.filter(d => d.reviewStatus === 'OPEN')
)
const closedDeviations = computed(() => 
  deviationsList.value.filter(d => d.reviewStatus === 'CLOSED')
)

const selectedDeviation = ref<Deviation | null>(null)
const showResolveModal = ref(false)
const resolveMeasure = ref('')

function getCategoryLabel(category: string): string {
  return deviationCategories.find(c => c.value === category)?.label || category
}

function formatDate(dateString: string): string {
  const date = new Date(dateString)
  return date.toLocaleDateString('nb-NO', { day: 'numeric', month: 'long', year: 'numeric' })
}

async function fetchDeviations() {
  try {
    const response = await api.get('/deviations')
    deviationsList.value = response.data
    error.value = null
  } catch (err) {
    console.warn('Using fallback mock data due to API error:', err)
    deviationsList.value = [...deviations]
    error.value = 'Kunne ikke hente avvik fra server. Viser test-data.'
    useMockData.value = true
  } finally {
    loading.value = false
  }
}

function openResolveModal(deviation: Deviation) {
  selectedDeviation.value = deviation
  resolveMeasure.value = ''
  showResolveModal.value = true
  patchError.value = null
}

function closeResolveModal() {
  showResolveModal.value = false
  selectedDeviation.value = null
  resolveMeasure.value = ''
  patchError.value = null
}

async function submitResolve() {
  if (!selectedDeviation.value || !resolveMeasure.value.trim()) return

  if (useMockData.value) {
    const index = deviationsList.value.findIndex(d => d.id === selectedDeviation.value!.id)
    if (index !== -1) {
      const dev = deviationsList.value[index]
      if (dev){
        dev.reviewStatus = 'CLOSED'
        dev.preventativeMeasureActuallyTaken = resolveMeasure.value.trim()
      }
    }
    closeResolveModal()
    return
  }

  try {
    const response = await api.patch(`/deviations/${selectedDeviation.value.id}/resolve`, {
      preventativeMeasureActuallyTaken: resolveMeasure.value.trim()
    })

    const updatedDeviation = response.data
    
    const index = deviationsList.value.findIndex(d => d.id === updatedDeviation.id)
    if (index !== -1) {
      deviationsList.value[index] = updatedDeviation
    }
    
    closeResolveModal()
    patchError.value = null
  } catch (err) {
    console.error('Error resolving deviation:', err)
    patchError.value = 'Kunne ikke løse avvik. Vennligst prøv igjen.'
  }
}

onMounted(() => {
  fetchDeviations()
})
</script>

<template>
  <SidebarPageContainer active-page="/desktop/deviations">
    <div class="deviations-page">
      <h1 class="instrument-serif-regular no-margin">
        Avvikshåndtering
      </h1>

      <div
        v-if="loading"
        class="loading-state"
      >
        <p>Laster avvik...</p>
      </div>

      <div
        v-else-if="error"
        class="error-banner"
      >
        <div class="error-content">
          <AlertTriangle class="error-icon" />
          <p>{{ error }}</p>
        </div>
      </div>

      <div
        v-if="openDeviations.length > 0"
        class="section"
      >
        <h2 class="section-title">
          Åpne avvik ({{ openDeviations.length }})
        </h2>
        <div class="deviation-list">
          <article 
            v-for="deviation in openDeviations" 
            :key="deviation.id" 
            class="deviation-card"
          >
            <div class="deviation-header">
              <Badge :badge-color="deviation.category === 'IK_MAT' ? 'navy' : deviation.category === 'IK_ALKOHOL' ? 'cherry' : 'grey'">
                {{ getCategoryLabel(deviation.category) }}
              </Badge>
              <span class="deviation-id">#{{ deviation.id }}</span>
            </div>
            <div class="deviation-meta">
              <span><strong>Rapportert av:</strong> {{ deviation.reportedByName }}</span>
              <span><strong>Dato opprettet:</strong> {{ formatDate(deviation.createdAt) }}</span>
            </div>
            <div class="deviation-field">
              <strong>Hva gikk galt:</strong>
              <p>{{ deviation.whatWentWrong }}</p>
            </div>
            <div class="deviation-field">
              <strong>Umiddelbar handling:</strong>
              <p>{{ deviation.immediateActionTaken }}</p>
            </div>
            <div class="deviation-field">
              <strong>Potensiell årsak:</strong>
              <p>{{ deviation.potentialCause }}</p>
            </div>
            <div class="deviation-field">
              <strong>Potensielt tiltak:</strong>
              <p>{{ deviation.potentialPreventativeMeasure }}</p>
            </div>
            <div class="deviation-actions">
              <DesktopButton
                content="Løs avvik"
                :icon="Check"
                button-color="navy"
                @click="openResolveModal(deviation)"
              />
            </div>
          </article>
        </div>
      </div>

      <div
        v-if="closedDeviations.length > 0"
        class="section"
      >
        <h2 class="section-title">
          Lukkede avvik ({{ closedDeviations.length }})
        </h2>
        <div class="deviation-list">
          <article 
            v-for="deviation in closedDeviations" 
            :key="deviation.id" 
            class="deviation-card deviation-card--closed"
          >
            <div class="deviation-header">
              <Badge :badge-color="deviation.category === 'IK_MAT' ? 'navy' : deviation.category === 'IK_ALKOHOL' ? 'cherry' : 'grey'">
                {{ getCategoryLabel(deviation.category) }}
              </Badge>
              <span class="deviation-id">#{{ deviation.id }}</span>
            </div>
            <div class="deviation-meta">
              <span><strong>Rapportert av:</strong> {{ deviation.reportedByName }}</span>
              <span><strong>Dato opprettet:</strong> {{ formatDate(deviation.createdAt) }}</span>
            </div>
            <div class="deviation-field">
              <strong>Hva gikk galt:</strong>
              <p>{{ deviation.whatWentWrong }}</p>
            </div>
            <div class="deviation-field">
              <strong>Umiddelbar handling:</strong>
              <p>{{ deviation.immediateActionTaken }}</p>
            </div>
            <div class="deviation-field">
              <strong>Potensiell årsak:</strong>
              <p>{{ deviation.potentialCause }}</p>
            </div>
            <div class="deviation-field">
              <strong>Potensielt tiltak:</strong>
              <p>{{ deviation.potentialPreventativeMeasure }}</p>
            </div>
            <div class="deviation-field deviation-field--resolved">
              <strong>Tiltak utført:</strong>
              <p>{{ deviation.preventativeMeasureActuallyTaken }}</p>
              <p class="resolved-meta">
                Løst av {{ deviation.reviewedByName }} {{ formatDate(deviation.reviewedAt!) }}
              </p>
            </div>
          </article>
        </div>
      </div>

      <div
        v-if="deviationsList.length === 0 && !loading"
        class="empty-state"
      >
        <p>Ingen avvik funnet.</p>
      </div>

      <Teleport to="body">
        <div
          v-if="showResolveModal"
          class="modal-overlay"
          @click="closeResolveModal"
        >
          <div
            class="modal-content"
            @click.stop
          >
            <div class="modal-header">
              <h2>Løs avvik #{{ selectedDeviation?.id }}</h2>
              <button
                class="modal-close"
                @click="closeResolveModal"
              >
                <X />
              </button>
            </div>
            <div class="modal-body">
              <p class="modal-description">
                Beskriv hvilke tiltak som ble utført for å løse dette avviket:
              </p>
              <textarea
                v-model="resolveMeasure"
                class="resolve-textarea"
                placeholder="Beskriv tiltakene som ble utført..."
                rows="6"
              />
            </div>
            <div
              v-if="patchError"
              class="error-message"
            >
              <div class="error-content">
                <AlertTriangle class="error-icon" />
                <p>{{ patchError }}</p>
              </div>
            </div>
            <div class="modal-footer">
              <DesktopButton
                content="Avbryt"
                :icon="X"
                button-color="grey"
                @click="closeResolveModal"
              />
              <DesktopButton
                content="Løs avvik"
                :icon="Check"
                button-color="navy"
                :disabled="!resolveMeasure.trim()"
                @click="submitResolve"
              />
            </div>
          </div>
        </div>
      </Teleport>
    </div>
  </SidebarPageContainer>
</template>

<style scoped>
.deviations-page {
  padding: 1rem 0;
}

.section {
  margin-top: 2rem;
}

.section-title {
  font-size: 1.25rem;
  margin-bottom: 1rem;
  color: var(--blue-navy);
}

.deviation-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.deviation-card {
  background-color: var(--white-greek);
  border: 1px solid var(--blue-navy-40);
  border-radius: 0.5rem;
  padding: 1rem;
}

.deviation-card--closed {
  opacity: 0.7;
}

.deviation-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.deviation-id {
  color: var(--blue-navy-60);
  font-size: 0.875rem;
}

.deviation-meta {
  display: flex;
  gap: 1.5rem;
  margin-bottom: 0.75rem;
  font-size: 0.875rem;
  color: var(--blue-navy-80);
}

.deviation-field {
  margin-bottom: 0.75rem;
}

.deviation-field strong {
  display: block;
  font-size: 0.75rem;
  text-transform: uppercase;
  color: var(--blue-navy-60);
  margin-bottom: 0.25rem;
}

.deviation-field p {
  margin: 0;
}

.deviation-field--resolved {
  background-color: var(--blue-navy-10);
  padding: 0.75rem;
  border-radius: 0.5rem;
  margin-top: 0.5rem;
}

.resolved-meta {
  font-size: 0.75rem;
  color: var(--blue-navy-60);
  margin-top: 0.5rem !important;
}

.deviation-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 1rem;
}

.empty-state {
  text-align: center;
  padding: 3rem;
  color: var(--blue-navy-60);
}

.loading-state {
  text-align: center;
  padding: 3rem;
  color: var(--blue-navy-60);
}

.error-banner {
  background-color: #fee2e2;
  border: 1px solid #ef4444;
  border-radius: 0.5rem;
  padding: 1rem;
  margin-bottom: 1rem;
}

.error-content {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  color: #dc2626;
}

.error-icon {
  flex-shrink: 0;
}

.error-message {
  background-color: #fee2e2;
  border: 1px solid #ef4444;
  border-radius: 0.5rem;
  padding: 0.75rem;
  margin-bottom: 1rem;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background-color: var(--white-greek);
  border-radius: 0.5rem;
  width: 90%;
  max-width: 500px;
  padding: 1.5rem;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.25rem;
}

.modal-close {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-description {
  margin-bottom: 1rem;
  color: var(--blue-navy-80);
}

.resolve-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--blue-navy-40);
  border-radius: 0.5rem;
  font-family: inherit;
  font-size: 1rem;
  resize: vertical;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
  margin-top: 1.5rem;
}
</style>
