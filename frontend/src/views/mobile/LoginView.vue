<script setup lang="ts">
import { reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'

import PrimaryActionButton from '@/components/PrimaryActionButton.vue'
import { setAuthToken } from '@/utils/auth'

const loginForm = reactive({
  email: '',
  password: '',
})

const isLoading = ref(false)
const errorMessage = ref('')

async function tryLogin(endpoint: string) {
  return fetch(endpoint, {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      email: loginForm.email.trim(),
      password: loginForm.password,
    }),
  })
}

async function handleSubmit() {
  if (!loginForm.email.trim() || !loginForm.password || isLoading.value) {
    return
  }

  isLoading.value = true
  errorMessage.value = ''

  try {
    const response = await tryLogin('/api/auth/login')

    if (!response.ok) {
      const responseText = (await response.text()).trim()
      errorMessage.value = responseText
        ? `Kunne ikke logge inn (${response.status}). ${responseText}`
        : `Kunne ikke logge inn (${response.status}). Sjekk e-post og passord.`
      return
    }

    const token = (await response.text()).trim()
    if (!token) {
      errorMessage.value = 'Innlogging lyktes ikke: tomt token-svar fra server.'
      return
    }

    setAuthToken(token)
    window.location.assign('/mobile/rutiner')
  } catch {
    errorMessage.value = 'Det oppstod en feil under innlogging.'
  } finally {
    isLoading.value = false
  }
}
</script>

<template>
  <section class="login-page">
    <article class="login-card">
      <p class="eyebrow">
        Logg inn
      </p>
      <h1 class="login-card__title">
        Logg inn for å åpne rutiner og logging
      </h1>
      <p class="card-copy">
        Bruk e-postadressen og passordet som ligger i databasen for å logge inn.
      </p>

      <form
        class="login-form"
        @submit.prevent="handleSubmit"
      >
        <label
          class="form-label"
          for="email"
        >E-post</label>
        <input
          id="email"
          v-model="loginForm.email"
          class="field-shell__input login-form__input"
          type="email"
          autocomplete="username"
        >

        <label
          class="form-label"
          for="password"
        >Passord</label>
        <input
          id="password"
          v-model="loginForm.password"
          class="field-shell__input login-form__input"
          type="password"
          autocomplete="current-password"
        >

        <PrimaryActionButton
          :label="isLoading ? 'Logger inn...' : 'Logg inn'"
          type="submit"
        />
      </form>

      <p
        v-if="errorMessage"
        class="caption-note login-card__error"
      >
        {{ errorMessage }}
      </p>

      <RouterLink
        class="login-card__back"
        to="/mobile/rutiner"
      >
        Tilbake til rutiner
      </RouterLink>
    </article>
  </section>
</template>
