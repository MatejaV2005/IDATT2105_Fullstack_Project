<script setup lang="ts">
    defineProps({
        currentBusinessName: String
    })
    function showOtherOrgs() {
        alert('Not yet implemented');
    }
</script>
<template>
    <button class="user-badge">

    </button>
</template>
<style scoped>
    .user-badge {
        background-color: var(--blue-navy-20);
        border: 1px solid var(--blue-navy-40);
        border-radius: 1rem;
        padding: .25rem 1rem;
        display: flex;
        gap: .5rem;
    }
</style>