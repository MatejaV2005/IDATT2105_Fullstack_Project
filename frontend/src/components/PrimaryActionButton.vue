<script setup lang="ts">
withDefaults(
  defineProps<{
    label: string
    type?: 'button' | 'submit'
    disabled?: boolean
  }>(),
  {
    type: 'button',
    disabled: false,
  },
)
</script>

<template>
  <button
    class="primary-action"
    :disabled="disabled"
    :type="type"
  >
    {{ label }}
  </button>
</template>
