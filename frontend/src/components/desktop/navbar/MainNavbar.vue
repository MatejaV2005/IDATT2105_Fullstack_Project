<script setup lang="ts">
import { RouterLink } from 'vue-router';
import OrgSelector from './OrgSelector.vue';
import ProfileNavbarButton from './ProfileNavbarButton.vue';

</script>
<template>
    <div id="navbar">
        <RouterLink to="/desktop" class="logo-button">
            <img src="@/assets/logo/logo-medium.png" alt="Logo" />
        </RouterLink>
        <OrgSelector currentBusinessName="Everest Sushi"/>
        <ProfileNavbarButton username="Jonas Ghar støre" />
    </div>
</template>
<style scoped>
    #navbar {
        display: flex;
        height: 6rem;
        background-color: var(--white-greek);
        border-bottom: 1px solid var(--blue-navy-40);
        align-items: center;
        padding-left: 2rem;
        padding-right: 2rem;
        justify-content: space-between;
        img {
            height: 2.25rem;
        }
    }
</style>