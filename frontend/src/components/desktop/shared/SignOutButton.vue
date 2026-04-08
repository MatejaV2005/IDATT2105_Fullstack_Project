<script setup lang="ts">
import Loading from '@/components/desktop/shared/Loading.vue'
import { delay } from '@/utils'
import { LogOut } from '@lucide/vue'
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import DesktopButton from './DesktopButton.vue'

const isSigningOut = ref(false)
const signOutError = ref(false)
const router = useRouter()

async function signOut() {
  if (isSigningOut.value) {
    return
  }

  signOutError.value = false
  isSigningOut.value = true

  try {
    // const response = await fetch('/api/users/me/signout', {
    //   method: 'POST',
    // })
    // if (!response.ok) {
    //   throw new Error(`Failed to sign out (${response.status})`)
    // }
    await delay(2000)
    await router.push('/desktop/sign-in')
  } catch (err) {
    if (err instanceof Error) {
      console.error(err.message)
    } else {
      console.error('Unknown error occurred')
    }
    signOutError.value = true
  } finally {
    isSigningOut.value = false
  }
}
</script>

<template>
  <div class="signout-container">
    <Loading v-if="isSigningOut" />
    <DesktopButton
      v-else
      :icon="LogOut"
      content="Logg ut"
      button-color="cherry"
      :on-click="signOut"
    />
    <p v-if="signOutError" class="error-message">
      Kunne ikke logge ut.
    </p>
  </div>
</template>

<style scoped>
.signout-container {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.error-message {
  color: #b42318;
  margin: 0;
}
</style>
