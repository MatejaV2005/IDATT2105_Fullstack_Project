<script setup lang="ts">
defineProps({
  currentBusinessName: String,
})
function showOtherOrgs() {
  alert('Not yet implemented')
}
</script>
<template>
  <button
    v-if="!!currentBusinessName"
    class="org-selector-button"
    @click="showOtherOrgs"
  >
    <svg
      xmlns="http://www.w3.org/2000/svg"
      width="24"
      height="24"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      stroke-width="2"
      stroke-linecap="round"
      stroke-linejoin="round"
      class="lucide lucide-building2-icon lucide-building-2"
    >
      <path d="M10 12h4" />
      <path d="M10 8h4" />
      <path d="M14 21v-3a2 2 0 0 0-4 0v3" />
      <path d="M6 10H4a2 2 0 0 0-2 2v7a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2V9a2 2 0 0 0-2-2h-2" />
      <path d="M6 21V5a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v16" />
    </svg>
    <div>
      {{ currentBusinessName }}
    </div>
  </button>
</template>
<style scoped>
.org-selector-button {
  color: black;
  background-color: var(--white-greek);
  cursor: pointer;
  border: 0;
  text-decoration: none;
  gap: 0.5rem;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem 2rem;
  border-radius: 1rem;
}
.org-selector-button:hover {
  box-shadow: 0rem 0.25rem 0.25rem rgba(0, 0, 0, 0.2);
}
</style>
