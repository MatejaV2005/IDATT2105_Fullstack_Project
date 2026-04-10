<script setup lang="ts">
import MainNavbar from '@/components/desktop/navbar/MainNavbar.vue'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const name = ref('')
const address = ref('')
const orgNumber = ref('')
const isGoingToServeAlcohol = ref(false)
const isGoingToServeFood = ref(false)
const isLoading = ref(false)
const errorMessage = ref('')

const router = useRouter()

async function handleSubmit() {
  if (!name.value || !address.value || !orgNumber.value || isLoading.value) {
    return
  }

  errorMessage.value = ''
  isLoading.value = true

  const payload = {
    name: name.value,
    address: address.value,
    orgNumber: orgNumber.value,
    isGoingToServeAlcohol: isGoingToServeAlcohol.value,
    isGoingToServeFood: isGoingToServeFood.value,
  }

  try {
    const response = await fetch('/api/organizations', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(payload),
    })

    if (!response.ok) {
      errorMessage.value = 'Klarte ikke å lage organisasjonen. Prøv igjen.'
      return
    }

    name.value = ''
    address.value = ''
    orgNumber.value = ''
    isGoingToServeAlcohol.value = false
    isGoingToServeFood.value = false

    await router.push('/desktop')
  } catch {
    errorMessage.value = 'Det oppstod en feil. Prøv igjen.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <main>
    <form @submit.prevent="handleSubmit">
      <h1 class="instrument-serif-regular no-margin">
        Legg til organisasjon
      </h1>
      <hr class="no-margin">
      <label>
        *Navn<br>
        <input
          v-model="name"
          required
          class="simple-text-input"
          placeholder="Per & Pål bolleklubb"
          :disabled="isLoading"
        >
      </label>
      <label>
        *Addresse<br>
        <input
          v-model="address"
          required
          class="simple-text-input"
          placeholder="Bolleveien 7, 0912 Oslo Norge"
          :disabled="isLoading"
        >
      </label>
      <label>
        *Org nummer<br>
        <input
          v-model="orgNumber"
          required
          class="simple-text-input"
          placeholder="123456789"
          type="number"
          :disabled="isLoading"
        >
      </label>
      <label>
        <input
          v-model="isGoingToServeAlcohol"
          type="checkbox"
          :disabled="isLoading"
        >
        Vi kommer til å servere alkohol
      </label>
      <label>
        <input
          v-model="isGoingToServeFood"
          type="checkbox"
          :disabled="isLoading"
        >
        Vi kommer til å servere mat
      </label>
      <p
        v-if="errorMessage"
        class="error-message"
      >
        {{ errorMessage }}
      </p>
      <button
        class="transition"
        type="submit"
      >
        <span class="no-margin">
          {{ isLoading ? 'Lager organisasjon...' : 'Lag organisasjon' }}
        </span>
        <svg
          v-if="!isLoading"
          xmlns="http://www.w3.org/2000/svg"
          width="16"
          height="16"
          viewBox="0 0 24 24"
          fill="none"
          stroke-width="2"
          stroke-linecap="round"
          stroke-linejoin="round"
          class="lucide lucide-send-horizontal-icon lucide-send-horizontal"
        >
          <path
            d="M3.714 3.048a.498.498 0 0 0-.683.627l2.843 7.627a2 2 0 0 1 0 1.396l-2.842 7.627a.498.498 0 0 0 .682.627l18-8.5a.5.5 0 0 0 0-.904z"
          />
          <path d="M6 12h16" />
        </svg>
        <span
          v-else
          class="loading-spinner"
        />
      </button>
    </form>
  </main>
</template>
<style scoped>
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
  form {
    width: calc(3 / 5 * 100%);
    background-color: var(--white-greek);
    border-radius: 1rem;
    padding: 1rem;
    border: 1px solid var(--blue-navy-40);
    display: flex;
    flex-direction: column;
    gap: 1rem;

    hr {
      border-color: var(--blue-navy-40);
      border-width: 1px;
    }
    input {
      accent-color: var(--blue-navy);
    }

    /* 
      Noe greier jeg fant på nett. Skal bytte til standard component senere
    */
    input[type='number']::-webkit-inner-spin-button,
    input[type='number']::-webkit-outer-spin-button {
      -webkit-appearance: none;
      margin: 0;
    }

    input[type='number'] {
      -moz-appearance: textfield;
    }
    .simple-text-input {
      width: 100%;
    }
    button {
      background-color: var(--blue-navy);
      border: 1px solid var(--blue-navy);
      border-radius: 0.5rem;
      color: var(--white-greek);
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 0.5rem;
      padding-top: 0.25rem;
      padding-bottom: 0.25rem;
      stroke: var(--white-greek);
      /* font-size: x-large; */
    }
    button:hover,
    button:focus {
      background-color: var(--blue-navy-20);
      color: var(--blue-navy);
      svg {
        stroke: var(--blue-navy);
      }
    }
    button:hover .loading-spinner,
    button:focus .loading-spinner {
      border-color: var(--blue-navy);
      border-bottom-color: transparent;
    }
    .loading-spinner {
      width: 1rem;
      height: 1rem;
      border: 2px solid var(--white-greek);
      border-bottom-color: transparent;
      border-radius: 50%;
      animation: spin 0.7s linear infinite;
    }
    .error-message {
      color: #b42318;
      margin: 0;
    }
  }
}
@media (max-width: 768px) {
  main > form {
    width: 100%;
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
