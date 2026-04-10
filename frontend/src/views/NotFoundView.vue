<template>
  <main class="not-found-page">
    <section class="not-found-card">
      <p class="not-found-card__eyebrow">
        Grimni
      </p>
      <h1 class="instrument-serif-regular no-margin not-found-card__title">
        404
      </h1>
      <p class="not-found-card__copy">
        Siden du prøvde å åpne finnes ikke.
      </p>
      <RouterLink
        class="not-found-card__button transition"
        to="/auth"
      >
        Gå til innlogging
      </RouterLink>
    </section>
  </main>
</template>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 2rem;
  background: var(--blue-background);
}

.not-found-card {
  width: min(100%, 28rem);
  display: flex;
  flex-direction: column;
  gap: 1rem;
  background: var(--white-greek);
  border: 1px solid var(--blue-navy-20);
  border-radius: 1rem;
  padding: 2rem;
  text-align: center;
}

.not-found-card__eyebrow {
  margin: 0;
  color: var(--blue-navy-80);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 0.8rem;
  font-weight: 700;
}

.not-found-card__title {
  font-size: clamp(3rem, 8vw, 4.5rem);
  line-height: 1;
}

.not-found-card__copy {
  margin: 0;
  color: var(--blue-navy-80);
}

.not-found-card__button {
  align-self: center;
  text-decoration: none;
  color: var(--white-greek);
  background: var(--blue-navy);
  border: 1px solid var(--blue-navy);
  border-radius: 999px;
  padding: 0.7rem 1.1rem;
  font-weight: 600;
}

.not-found-card__button:hover,
.not-found-card__button:focus {
  color: var(--blue-navy);
  background: var(--blue-light-20);
}
</style>
