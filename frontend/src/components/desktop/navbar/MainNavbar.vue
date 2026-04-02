<script setup lang="ts">
import { RouterLink } from 'vue-router';
import OrgSelector from './OrgSelector.vue';
import ProfileNavbarButton from './ProfileNavbarButton.vue';
import { ref } from 'vue';

const username = ref("Jonas Ghar støre"); // We should fetch this from the server
const currentBusinessName = ref("Everest Sushi"); // We should fetch this from the server

</script>
<template>
    <div id="navbar">
        <RouterLink to="/desktop" class="logo-button">
            <img src="@/assets/logo/logo-medium.png" alt="Logo" />
        </RouterLink>
        <OrgSelector :currentBusinessName="currentBusinessName"/>
        <ProfileNavbarButton :username="username" />
    </div>
</template>
<style scoped>
    #navbar {
        display: flex;
        height: 6rem !important;
        max-height: 6rem;
        min-height: 6rem;
        background-color: var(--white-greek);
        border-bottom: 1px solid var(--blue-navy-40);
        align-items: center;
        justify-content: space-between;
        img {
            height: 2.25rem;
        }
    }
    @media (min-width: 1200px) {
        #navbar {
            padding-left: 2rem;
            padding-right: 2rem;
        }
    }
    @media (max-width: 1200px) {
        #navbar {
            padding-left: .5rem;
            padding-right: .5rem;
        }
    }
</style>