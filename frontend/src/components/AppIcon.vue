<script setup lang="ts">
defineProps<{
  name:
    | 'temperature'
    | 'clock'
    | 'humidity'
    | 'camera'
    | 'checklist'
    | 'alert'
    | 'info'
    | 'upload'
    | 'chevron'
    | 'spark'
}>()
</script>

<template>
  <svg
    v-if="name === 'temperature'"
    width="20"
    height="20"
    viewBox="0 0 20 20"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <path
      d="M8.25 3.5a1.75 1.75 0 1 1 3.5 0v6.17a3.75 3.75 0 1 1-3.5 0V3.5Z"
      stroke="currentColor"
      stroke-width="1.7"
      stroke-linecap="round"
      stroke-linejoin="round"
    />
    <path d="M10 12.25v2.25" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
    <path d="M13.75 5.5H16.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
    <path d="M13.75 9H15.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
  </svg>

  <svg
    v-else-if="name === 'clock'"
    width="20"
    height="20"
    viewBox="0 0 20 20"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <circle cx="10" cy="10" r="7" stroke="currentColor" stroke-width="1.7" />
    <path d="M10 6.5v3.9l2.8 1.6" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" />
  </svg>

  <svg
    v-else-if="name === 'humidity'"
    width="18"
    height="22"
    viewBox="0 0 18 22"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <path
      d="M9 1.75C6.1 5.6 3 9.18 3 13a6 6 0 0 0 12 0c0-3.82-3.1-7.4-6-11.25Z"
      stroke="currentColor"
      stroke-width="1.7"
      stroke-linejoin="round"
    />
    <path d="M6.4 13.5c.5 1.4 1.7 2.2 3.1 2.4" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
  </svg>

  <svg
    v-else-if="name === 'camera'"
    width="20"
    height="18"
    viewBox="0 0 20 18"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <path
      d="M2.5 5.5h2.45l1.2-2h3.7l1.2 2H17.5a1.5 1.5 0 0 1 1.5 1.5v7A1.5 1.5 0 0 1 17.5 15.5h-15A1.5 1.5 0 0 1 1 14V7A1.5 1.5 0 0 1 2.5 5.5Z"
      stroke="currentColor"
      stroke-width="1.7"
      stroke-linejoin="round"
    />
    <circle cx="10" cy="10.5" r="3.25" stroke="currentColor" stroke-width="1.7" />
  </svg>

  <svg
    v-else-if="name === 'checklist'"
    width="20"
    height="20"
    viewBox="0 0 20 20"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <path d="M7.5 3.5h8v13h-11v-8" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round" />
    <path d="m3.5 6.5 1.5 1.5 3-3" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" />
    <path d="m3.5 11 1.5 1.5 3-3" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" />
  </svg>

  <svg
    v-else-if="name === 'alert'"
    width="20"
    height="20"
    viewBox="0 0 20 20"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <path
      d="M10 2.5c4.14 0 7.5 3.36 7.5 7.5s-3.36 7.5-7.5 7.5S2.5 14.14 2.5 10 5.86 2.5 10 2.5Z"
      stroke="currentColor"
      stroke-width="1.7"
    />
    <path d="M10 6.3v4.45" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
    <circle cx="10" cy="13.35" r="0.9" fill="currentColor" />
  </svg>

  <svg
    v-else-if="name === 'info'"
    width="18"
    height="18"
    viewBox="0 0 18 18"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <circle cx="9" cy="9" r="7.15" stroke="currentColor" stroke-width="1.7" />
    <path d="M9 7.45v4.1" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
    <circle cx="9" cy="5.2" r="0.85" fill="currentColor" />
  </svg>

  <svg
    v-else-if="name === 'upload'"
    width="20"
    height="20"
    viewBox="0 0 20 20"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <path d="M10 13.75v-8" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
    <path d="m6.75 8.5 3.25-3.25L13.25 8.5" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" />
    <path d="M4 15.5h12" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" />
  </svg>

  <svg
    v-else-if="name === 'spark'"
    width="20"
    height="20"
    viewBox="0 0 20 20"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <path d="M10 2.75 11.8 7.2 16.25 9l-4.45 1.8L10 15.25l-1.8-4.45L3.75 9 8.2 7.2 10 2.75Z" stroke="currentColor" stroke-width="1.7" stroke-linejoin="round" />
  </svg>

  <svg
    v-else
    width="16"
    height="16"
    viewBox="0 0 16 16"
    fill="none"
    xmlns="http://www.w3.org/2000/svg"
    aria-hidden="true"
  >
    <path d="m4 6 4 4 4-4" stroke="currentColor" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round" />
  </svg>
</template>
