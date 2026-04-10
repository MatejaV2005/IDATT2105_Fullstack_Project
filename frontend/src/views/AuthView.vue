<script setup lang="ts">
import { ensureOrgSessionLoaded, syncOrgSessionFromStorage, useOrgSession } from '@/composables/useOrgSession'
import { setAuthToken } from '@/utils/auth'
import { getPostAuthRoute } from '@/utils/auth-routing'
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

type AuthMode = 'login' | 'register'

const router = useRouter()
const { claims, isAuthenticated, organizations } = useOrgSession()

const mode = ref<AuthMode>('login')
const form = reactive({
  email: '',
  password: '',
  legalName: '',
})

const isSubmitting = ref(false)
const errorMessage = ref('')
const successMessage = ref('')

const isRegisterMode = computed(() => mode.value === 'register')

function clearMessages() {
  errorMessage.value = ''
  successMessage.value = ''
}

function setMode(nextMode: AuthMode) {
  mode.value = nextMode
  form.password = ''
  clearMessages()
}

async function redirectAuthenticatedUser() {
  try {
    await ensureOrgSessionLoaded(true)
    const nextRoute = getPostAuthRoute(claims.value, organizations.value)

    if (router.currentRoute.value.path !== nextRoute) {
      await router.replace(nextRoute)
    }
  } catch {
    errorMessage.value = 'Kunne ikke hente organisasjonene dine akkurat nå. Prøv igjen.'
  }
}

onMounted(() => {
  if (isAuthenticated.value) {
    void redirectAuthenticatedUser()
  }
})

async function submitLogin() {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      email: form.email.trim(),
      password: form.password,
    }),
  })

  if (!response.ok) {
    const responseText = (await response.text()).trim()
    throw new Error(
      responseText
        ? `Kunne ikke logge inn (${response.status}). ${responseText}`
        : `Kunne ikke logge inn (${response.status}). Sjekk e-post og passord.`,
    )
  }

  const token = (await response.text()).trim()
  if (!token) {
    throw new Error('Innlogging lyktes ikke: tomt token-svar fra server.')
  }

  setAuthToken(token)
  syncOrgSessionFromStorage()
  await redirectAuthenticatedUser()
}

async function submitRegister() {
  const response = await fetch('/api/auth/register', {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      email: form.email.trim(),
      password: form.password,
      legalName: form.legalName.trim(),
    }),
  })

  if (!response.ok) {
    const responseText = (await response.text()).trim()
    throw new Error(
      responseText
        ? `Kunne ikke opprette bruker (${response.status}). ${responseText}`
        : `Kunne ikke opprette bruker (${response.status}). Prøv igjen.`,
    )
  }

  const registeredEmail = form.email.trim()
  form.password = ''
  form.legalName = ''
  successMessage.value = 'Brukeren er opprettet. Du kan logge inn nå.'
  errorMessage.value = ''
  mode.value = 'login'
  form.email = registeredEmail
}

async function handleSubmit() {
  const email = form.email.trim()
  const password = form.password
  const legalName = form.legalName.trim()

  if (!email || !password || isSubmitting.value) {
    return
  }

  if (isRegisterMode.value && !legalName) {
    return
  }

  clearMessages()
  isSubmitting.value = true

  try {
    if (isRegisterMode.value) {
      await submitRegister()
    } else {
      await submitLogin()
    }
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : 'Det oppstod en feil. Prøv igjen.'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <main class="auth-page">
    <section class="auth-card">
      <p class="auth-card__eyebrow">
        Grimni
      </p>
      <h1 class="instrument-serif-regular no-margin auth-card__title">
        {{ isRegisterMode ? 'Opprett bruker' : 'Logg inn' }}
      </h1>
      <p class="auth-card__copy">
        {{
          isRegisterMode
            ? 'Opprett en bruker med navn, e-post og passord. Du kan bli lagt til i organisasjon etterpå.'
            : 'Logg inn med e-post og passord. Du blir sendt til riktig løsning basert på rolle og organisasjon.'
        }}
      </p>

      <form
        class="auth-form"
        @submit.prevent="handleSubmit"
      >
        <label
          v-if="isRegisterMode"
          class="auth-form__field"
        >
          <span>Fullt navn</span>
          <input
            v-model="form.legalName"
            class="simple-text-input"
            type="text"
            placeholder="Ada Lovelace"
            :disabled="isSubmitting"
          >
        </label>

        <label class="auth-form__field">
          <span>E-post</span>
          <input
            v-model="form.email"
            class="simple-text-input"
            type="email"
            placeholder="ada@lovelace.uk"
            :disabled="isSubmitting"
          >
        </label>

        <label class="auth-form__field">
          <span>Passord</span>
          <input
            v-model="form.password"
            class="simple-text-input"
            type="password"
            placeholder="Skriv passord"
            :disabled="isSubmitting"
          >
        </label>

        <button
          class="auth-form__submit transition"
          type="submit"
          :disabled="isSubmitting"
        >
          {{ isSubmitting ? (isRegisterMode ? 'Oppretter bruker...' : 'Logger inn...') : isRegisterMode ? 'Opprett bruker' : 'Logg inn' }}
        </button>
      </form>

      <p
        v-if="errorMessage"
        class="auth-card__message auth-card__message--error"
      >
        {{ errorMessage }}
      </p>

      <p
        v-if="successMessage"
        class="auth-card__message auth-card__message--success"
      >
        {{ successMessage }}
      </p>

      <p class="auth-card__toggle">
        <template v-if="isRegisterMode">
          Har du allerede en bruker?
          <button
            class="auth-card__link"
            type="button"
            :disabled="isSubmitting"
            @click="setMode('login')"
          >
            Logg inn
          </button>
        </template>
        <template v-else>
          Har du ikke bruker ennå?
          <button
            class="auth-card__link"
            type="button"
            :disabled="isSubmitting"
            @click="setMode('register')"
          >
            Opprett bruker
          </button>
        </template>
      </p>

      <RouterLink
        class="auth-card__back"
        to="/"
      >
        Tilbake
      </RouterLink>
    </section>
  </main>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background:
    radial-gradient(circle at top, rgba(181, 216, 255, 0.5), transparent 45%),
    var(--blue-decor-10);
}

.auth-card {
  width: min(100%, 32rem);
  background-color: var(--white-greek);
  border: 1px solid var(--blue-navy-40);
  border-radius: 1rem;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.auth-card__eyebrow {
  margin: 0;
  color: var(--blue-navy-80);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 0.85rem;
}

.auth-card__title,
.auth-card__copy,
.auth-card__message,
.auth-card__toggle {
  margin: 0;
}

.auth-card__copy {
  color: var(--blue-navy-80);
}

.auth-form {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}

.auth-form__field {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.auth-form__submit {
  width: 100%;
  border: 1px solid var(--blue-navy);
  border-radius: 0.5rem;
  background-color: var(--blue-navy);
  color: var(--white-greek);
  padding: 0.65rem 1rem;
}

.auth-form__submit:hover,
.auth-form__submit:focus {
  background-color: var(--blue-navy-20);
  color: var(--blue-navy);
}

.auth-card__message--error {
  color: #b42318;
}

.auth-card__message--success {
  color: #027a48;
}

.auth-card__link {
  border: 0;
  background: transparent;
  color: var(--blue-navy);
  padding: 0;
  text-decoration: underline;
}

.auth-card__back {
  color: var(--blue-navy);
  text-decoration: underline;
  width: fit-content;
}

@media (max-width: 768px) {
  .auth-page {
    padding: 1rem;
  }

  .auth-card {
    padding: 1rem;
  }
}
</style>
