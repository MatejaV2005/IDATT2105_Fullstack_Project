<script setup lang="ts">
import { reactive } from 'vue'
import { RouterLink } from 'vue-router'

import PrimaryActionButton from '@/components/PrimaryActionButton.vue'

const loginForm = reactive({
  identifier: '',
  password: '',
})
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
        Brukernavn eller e-post og passord kan senere kobles direkte til backend-autentisering.
      </p>

      <form
        class="login-form"
        @submit.prevent
      >
        <label
          class="form-label"
          for="identifier"
        >Brukernavn / e-post</label>
        <input
          id="identifier"
          v-model="loginForm.identifier"
          class="field-shell__input login-form__input"
          type="text"
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
          label="Logg inn"
          type="submit"
        />
      </form>

      <RouterLink
        class="login-card__back"
        to="/rutiner"
      >
        Tilbake til demoen
      </RouterLink>
    </article>
  </section>
</template>
