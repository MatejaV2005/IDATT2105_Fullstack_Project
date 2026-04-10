<script setup lang="ts">
import DesktopButton from '@/components/desktop/shared/DesktopButton.vue'
import type { DesktopProductCategory, NewCriticalControlPoint } from '@/interfaces/api-interfaces'
import { Plus, Save, X } from '@lucide/vue'
import { computed, ref, watch } from 'vue'
import CorrectiveMeasureCard from './CorrectiveMeasureCard.vue'
import ProductCatgorySelector from './ProductCatgorySelector.vue'

type CorrectiveMeasurePayload = NewCriticalControlPoint['ccpCorrectiveMeasure'][number]

type EditableCorrectiveMeasure = {
  localId: number
  id: number | null
  productCategoryId: number
  productCategoryName: string
  measureDescription: string
}

const props = withDefaults(
  defineProps<{
    measures: CorrectiveMeasurePayload[]
    setMeasures: (measures: CorrectiveMeasurePayload[]) => void
    categories?: DesktopProductCategory[]
    disabled?: boolean
  }>(),
  {
    disabled: false,
  },
)

const localMeasures = ref<EditableCorrectiveMeasure[]>([])
const isAdding = ref(false)
const newCategoryId = ref<number | null>(null)
const newMeasureDescription = ref('')

const selectedCategoryIds = computed(() => {
  return localMeasures.value.map((measure) => measure.productCategoryId)
})

function getCategoryNameById(categoryId: number) {
  const category = props.categories?.find((entry) => entry.id === categoryId)
  return category?.name ?? `Produktkategori #${categoryId}`
}

const canAdd = computed(() => {
  if (newCategoryId.value === null) {
    return false
  }

  return (
    newMeasureDescription.value.trim().length > 0 &&
    !selectedCategoryIds.value.includes(newCategoryId.value)
  )
})

watch(
  () => props.measures,
  (measures) => {
    localMeasures.value = measures.map((measure) => ({
      localId: measure.id ?? randomId(),
      id: measure.id ?? null,
      productCategoryId: measure.productCategoryId,
      productCategoryName:
        measure.productName?.trim() || getCategoryNameById(measure.productCategoryId),
      measureDescription: measure.measureDescription,
    }))
  },
  { immediate: true, deep: true },
)

function randomId() {
  return Math.floor(Math.random() * 1000000)
}

function syncMeasuresToParent() {
  props.setMeasures(
    localMeasures.value.map((measure) => ({
      id: measure.id,
      productCategoryId: measure.productCategoryId,
      measureDescription: measure.measureDescription,
      productName: measure.productCategoryName,
    })),
  )
}

function startAdding() {
  if (props.disabled) {
    return
  }

  isAdding.value = true
}

function setNewCategoryId(categoryId: number | null) {
  newCategoryId.value = categoryId
}

function cancelAdding() {
  isAdding.value = false
  newCategoryId.value = null
  newMeasureDescription.value = ''
}

function addMeasure() {
  if (!canAdd.value) {
    return
  }

  if (newCategoryId.value === null) {
    return
  }

  localMeasures.value = [
    ...localMeasures.value,
    {
      localId: randomId(),
      id: null,
      productCategoryId: newCategoryId.value,
      productCategoryName: getCategoryNameById(newCategoryId.value),
      measureDescription: newMeasureDescription.value.trim(),
    },
  ]
  syncMeasuresToParent()
  cancelAdding()
}

function updateMeasure(payload: { id: number; measureDescription: string }) {
  localMeasures.value = localMeasures.value.map((measure) => {
    if (measure.localId !== payload.id) {
      return measure
    }

    return {
      ...measure,
      measureDescription: payload.measureDescription,
    }
  })

  syncMeasuresToParent()
}

function deleteMeasure(payload: { id: number }) {
  localMeasures.value = localMeasures.value.filter((measure) => measure.localId !== payload.id)
  syncMeasuresToParent()
}
</script>

<template>
  <div class="corrective-measures">
    <div class="corrective-measures-header">
      <span class="navy-subtitle">Korrigerende tiltak</span>
      <DesktopButton
        v-if="!isAdding"
        :icon="Plus"
        content="Nytt tiltak"
        :on-click="startAdding"
        :disabled="disabled"
      />
    </div>

    <div v-if="isAdding" class="create-measure-card">
      <label class="form-field">
        <span class="navy-subtitle">Produktkategori</span>
        <ProductCatgorySelector
          :categories="categories"
          :selected-category-id="newCategoryId"
          :set-selected-category-id="setNewCategoryId"
          :excluded-category-ids="selectedCategoryIds"
          :disabled="disabled"
        />
      </label>

      <label class="form-field">
        <span class="navy-subtitle">Korrigerende tiltak</span>
        <textarea
          v-model="newMeasureDescription"
          class="simple-text-input"
          rows="3"
          :disabled="disabled"
          placeholder="Beskriv tiltak for valgt kategori"
        />
      </label>

      <div class="create-actions">
        <DesktopButton
          :icon="Save"
          content="Lagre tiltak"
          :on-click="addMeasure"
          :disabled="!canAdd"
        />
        <DesktopButton
          :icon="X"
          content="Avbryt"
          button-color="boring-ghost"
          :on-click="cancelAdding"
        />
      </div>
    </div>

    <p v-if="localMeasures.length === 0" class="empty-state">
      Ingen korrigerende tiltak er lagt til ennå.
    </p>

    <CorrectiveMeasureCard
      v-for="measure in localMeasures"
      :id="measure.localId"
      :key="measure.localId"
      :product-category-id="measure.productCategoryId"
      :product-category-name="measure.productCategoryName"
      :measure-description="measure.measureDescription"
      :disabled="disabled"
      @update="updateMeasure"
      @delete="deleteMeasure"
    />
  </div>
</template>

<style scoped>
.corrective-measures {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.corrective-measures-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 0.75rem;
}

.create-measure-card {
  border: 1px solid var(--blue-navy-40);
  border-radius: 0.5rem;
  background-color: var(--white-greek);
  padding: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.form-field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

textarea.simple-text-input {
  width: 100%;
  resize: vertical;
  box-sizing: border-box;
  text-indent: 0;
  padding: 0.5rem;
}

.create-actions {
  display: flex;
  gap: 0.5rem;
}

.empty-state {
  margin: 0;
  color: var(--blue-navy-80);
}

@media (max-width: 768px) {
  .corrective-measures-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .create-actions {
    width: 100%;
    flex-direction: column;
  }
}
</style>
