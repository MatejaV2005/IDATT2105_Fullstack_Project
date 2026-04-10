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
  display: inline-flex;
  justify-content: center;
  align-items: center;
  gap: 0.5rem;
  background-color: var(--red-cherry-20);
  border-radius: 1rem;
  border: 1px solid var(--red-cherry-40);
  min-width: 0;
  max-width: min(260px, 100%);
}

.me-button__avatar {
  width: 2.1rem;
  height: 2.1rem;
  border-radius: 999px;
  display: grid;
  place-items: center;
  background: var(--red-cherry);
  color: var(--white-greek);
  font-weight: 700;
  font-size: 0.8rem;
  flex: 0 0 auto;
}

.me-button > div {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 600;
  font-size: 0.9rem;
}
.me-button:hover {
  background-color: var(--red-cherry-20);
}
@media (min-width: 768px) {
  .me-button {
    padding: 0.45rem 0.9rem;
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
