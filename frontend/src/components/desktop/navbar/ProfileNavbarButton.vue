<script setup lang="ts">
withDefaults(
  defineProps<{
    username?: string
    userId?: string
    initials?: string
    to?: string
  }>(),
  {
    username: '',
    userId: '',
    initials: '?',
    to: '/desktop/users/me',
  },
)
</script>
<template>
  <RouterLink
    class="me-button transition"
    :to="to"
  >
    <span class="me-button__avatar">{{ initials }}</span>
    <div>
      {{ username }}
    </div>
  </RouterLink>
</template>
<style scoped>
.me-button {
  text-decoration: none;
  color: var(--red-cherry);
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  background-color: var(--red-cherry-20);
  border-radius: 1rem;
  border: 1px solid var(--red-cherry-40);
}

.me-button__avatar {
  width: 2rem;
  height: 2rem;
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: var(--red-cherry);
  color: var(--white-greek);
  font-weight: 700;
  font-size: 0.75rem;
}
.me-button:hover {
  background-color: var(--red-cherry-40);
}
@media (min-width: 768px) {
  .me-button {
    padding: 0.5rem 2rem;
  }
}
@media (max-width: 768px) {
  .me-button {
    padding: 0.25rem;
    width: min-content;
    > div {
      display: none;
    }
  }
}
</style>
r
