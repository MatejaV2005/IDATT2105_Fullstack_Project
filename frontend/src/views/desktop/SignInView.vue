<script setup lang="ts">
import MainNavbar from '@/components/desktop/navbar/MainNavbar.vue'
import Loading from '@/components/desktop/shared/Loading.vue'
import { delay } from '@/utils'
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const email = ref('')
const password = ref('')
const isLoading = ref(false)
const errorMessage = ref('')

const router = useRouter()

async function handleSubmit() {
  if (!email.value || !password.value || isLoading.value) {
    return
  }

  errorMessage.value = ''
  isLoading.value = true

  const payload = {
    email: email.value,
    password: password.value,
  }

  try {
    // const response = await fetch('/api/auth/login', {
    //   method: 'POST',
    //   headers: {
    //     'Content-Type': 'application/json',
    //   },
    //   body: JSON.stringify(payload),
    // })
    await delay(2000)
    const response = { ok: true }

    if (!response.ok) {
      errorMessage.value = 'Klarte ikke å logge inn. Prøv igjen.'
      return
    }

    email.value = ''
    password.value = ''

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
        Logg inn
      </h1>
      <hr class="no-margin">
      <label>
        *Email<br>
        <input
          v-model="email"
          type="email"
          required
          class="simple-text-input"
          placeholder="ada@lovelace.uk"
          :disabled="isLoading"
        >
      </label>
      <label>
        *Passord<br>
        <input
          v-model="password"
          required
          class="simple-text-input"
          placeholder="Skriv passord"
          type="password"
          :disabled="isLoading"
        >
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
        <span v-if="!isLoading"> Logg inn </span>
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
        <Loading v-else />
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
