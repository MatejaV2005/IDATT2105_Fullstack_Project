<script setup lang="ts">
withDefaults(
  defineProps<{
    label: string
    type?: 'button' | 'submit'
  }>(),
  {
    type: 'button',
  },
)
</script>

<template>
  <button class="primary-action" :type="type">
    {{ label }}
  </button>
</template>
