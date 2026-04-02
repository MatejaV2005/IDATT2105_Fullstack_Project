<script setup lang="ts">
import type { Component } from 'vue';

    defineProps<{
        isLoading?: boolean,
        loadingText?: string,
        content: string,
        icon: Component,
        onClick?: () => any
    }>()
</script>

<template>
    <button 
    @click="onClick" 
    class="transition navy-button" 
    v-bind="$attrs"
    >
        <component class="icon" v-if="!isLoading" :is="icon" />
        <span v-else class="loading-spinner"></span>
        <span class="no-margin">
            {{ isLoading ? (loadingText || "Loading...") : content }}
        </span>
    </button>
</template>
<style scoped>
    .icon {
        width: 1rem;
        height: 1rem;
    }
    .navy-button {
        background-color: var(--blue-navy);
        border: 1px solid var(--blue-navy);
        border-radius: 0.5rem;
        color: var(--white-greek);
        display: flex;
        justify-content: center;
        align-items: center;
        gap: 0.5rem;
        padding-top: 0.25rem;
        padding-bottom: 0.25rem;
        padding-left: 1rem;
        padding-right: 1rem;
        stroke: var(--white-greek);
        /* font-size: x-large; */
    }
    .navy-button:hover,
    .navy-button:focus {
        background-color: var(--blue-navy-20);
        color: var(--blue-navy);
        svg {
            stroke: var(--blue-navy);
        }
    }
    .navy-button:hover .loading-spinner,
    .navy-button:focus .loading-spinner {
        border-color: var(--blue-navy);
        border-bottom-color: transparent;
    }
    .loading-spinner {
        width: 1rem;
        height: 1rem;
        border: 2px solid var(--white-greek);
        border-bottom-color: transparent;
        border-radius: 50%;
        animation: spin 0.7s linear infinite;
    }
    @keyframes spin {
        to {
            transform: rotate(360deg);
        }
    }
</style>
