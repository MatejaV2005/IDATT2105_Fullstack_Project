<script setup lang="ts">
    defineProps({
        username: String,
        userId: String
    })
</script>
<template>
    <RouterLink class="me-button transition" to="/desktop/users/me">
        <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" class="lucide lucide-circle-user-icon lucide-circle-user"><circle cx="12" cy="12" r="10"/><circle cx="12" cy="10" r="3"/><path d="M7 20.662V19a2 2 0 0 1 2-2h6a2 2 0 0 1 2 2v1.662"/></svg>
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
        padding: 0.5rem 2rem;
        background-color: var(--red-cherry-20);
        border-radius: 1rem;
    }
    .me-button:hover {
        background-color: var(--red-cherry-40);
    }
</style>r