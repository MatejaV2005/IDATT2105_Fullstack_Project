<script setup lang="ts">
import { delay } from '@/utils'
import { computed, onMounted, ref, watch } from 'vue'

interface ProductCategory {
  id: number
  name: string
  description: string
}

const props = withDefaults(
  defineProps<{
    selectedCategoryId: number | null
    setSelectedCategoryId: (categoryId: number | null) => void
    excludedCategoryIds?: number[]
    disabled?: boolean
  }>(),
  {
    excludedCategoryIds: () => [],
    disabled: false,
  },
)

const categories = ref<ProductCategory[]>([])
const loading = ref(true)
const error = ref('')
const isOpen = ref(false)
const searchQuery = ref('')

const selectedCategory = computed(() => {
  return categories.value.find((category) => category.id === props.selectedCategoryId) || null
})

const filteredCategories = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()
  return categories.value.filter((category) => {
    const isExcluded =
      props.excludedCategoryIds.includes(category.id) && category.id !== props.selectedCategoryId

    if (isExcluded) {
      return false
    }

    if (query.length === 0) {
      return true
    }

    return (
      category.name.toLowerCase().includes(query) ||
      category.description.toLowerCase().includes(query) ||
      category.id.toString().includes(query)
    )
  })
})

watch(
  selectedCategory,
  (category) => {
    searchQuery.value = category ? category.name : ''
  },
  { immediate: true },
)

onMounted(async () => {
  try {
    // const response = await fetch('/api/product-categories')
    // if (!response.ok) {
    //   throw new Error(`Failed to fetch product categories (${response.status})`)
    // }
    // const data: ProductCategory[] = await response.json()
    await delay(400)
    const data: ProductCategory[] = [
      { id: 10, name: 'Fisk', description: 'Fisk og sjomat' },
      { id: 11, name: 'Kjott', description: 'Kjott og farseprodukter' },
      { id: 12, name: 'Kylling', description: 'Fjaerfe og egg' },
      { id: 13, name: 'Meieri', description: 'Melk, ost og yoghurt' },
      { id: 14, name: 'Saus', description: 'Varme og kalde sauser' },
    ]

    categories.value = data
    error.value = ''
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    error.value = 'Klarte ikke a hente produktkategorier.'
  } finally {
    loading.value = false
  }
})

function selectCategory(category: ProductCategory) {
  if (props.disabled) {
    return
  }

  props.setSelectedCategoryId(category.id)
  searchQuery.value = category.name
  isOpen.value = false
}

function clearSelection() {
  if (props.disabled) {
    return
  }

  props.setSelectedCategoryId(null)
  searchQuery.value = ''
  isOpen.value = true
}

function onInput() {
  if (props.disabled) {
    return
  }

  isOpen.value = true
  if (selectedCategory.value && searchQuery.value !== selectedCategory.value.name) {
    props.setSelectedCategoryId(null)
  }
}
</script>

<template>
  <div class="selector-root">
    <div class="input-row">
      <input
        v-model="searchQuery"
        class="simple-text-input selector-input"
        type="text"
        :disabled="disabled || loading"
        placeholder="Velg produktkategori"
        @focus="isOpen = true"
        @input="onInput"
      >
      <button
        v-if="selectedCategoryId !== null"
        class="clear-button"
        type="button"
        :disabled="disabled"
        @click="clearSelection"
      >
        Fjern
      </button>
    </div>

    <p
      v-if="error"
      class="error-message"
    >
      {{ error }}
    </p>

    <div
      v-if="isOpen && !loading"
      class="dropdown-container"
    >
      <button
        v-for="category in filteredCategories"
        :key="category.id"
        type="button"
        class="dropdown-option"
        :disabled="disabled"
        @click="selectCategory(category)"
      >
        <span>{{ category.name }}</span>
        <span class="option-meta">#{{ category.id }}</span>
      </button>

      <div
        v-if="filteredCategories.length === 0"
        class="empty-state"
      >
        Ingen treff.
      </div>
    </div>
  </div>
</template>

<style scoped>
.selector-root {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.input-row {
  display: flex;
  gap: 0.5rem;
}

.selector-input {
  width: 100%;
}

.clear-button {
  border: 1px solid var(--blue-navy-40);
  background-color: var(--white-greek);
  border-radius: 0.5rem;
  color: var(--blue-navy);
  padding: 0 0.75rem;
}

.dropdown-container {
  border: 1px solid var(--blue-navy-40);
  border-radius: 0.5rem;
  max-height: 12rem;
  overflow-y: auto;
  background-color: var(--white-greek);
}

.dropdown-option {
  width: 100%;
  border: 0;
  border-bottom: 1px solid var(--blue-navy-20);
  background: transparent;
  padding: 0.6rem 0.75rem;
  display: flex;
  justify-content: space-between;
  text-align: left;
}

.dropdown-option:hover,
.dropdown-option:focus {
  background-color: var(--blue-decor-10);
}

.option-meta {
  color: var(--blue-navy-80);
}

.empty-state {
  padding: 0.75rem;
  color: var(--blue-navy-80);
}

.error-message {
  color: #b42318;
  margin: 0;
}
</style>
