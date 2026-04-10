import api from '@/api/api'
import type { AuthJwtClaims, OrganizationMembership } from '@/types/auth'
import { clearAuthToken, getAuthToken, setAuthToken } from '@/utils/auth'
import { computed, ref } from 'vue'

const claims = ref<AuthJwtClaims | null>(null)
const organizations = ref<OrganizationMembership[]>([])
const isLoadingOrganizations = ref(false)
const isSwitchingOrganization = ref(false)
const orgErrorMessage = ref('')

let loadPromise: Promise<void> | null = null

function decodeBase64Url(value: string): string {
  const normalized = value.replace(/-/g, '+').replace(/_/g, '/')
  const padding = normalized.length % 4 === 0 ? '' : '='.repeat(4 - (normalized.length % 4))
  const base64 = `${normalized}${padding}`

  if (typeof globalThis.atob === 'function') {
    return decodeURIComponent(
      Array.from(globalThis.atob(base64))
        .map((char) => `%${char.charCodeAt(0).toString(16).padStart(2, '0')}`)
        .join(''),
    )
  }

  throw new Error('Base64 decoding is not available in this environment.')
}

export function decodeJwtClaims(token: string | null): AuthJwtClaims | null {
  if (!token?.trim()) {
    return null
  }

  const parts = token.trim().split('.')
  if (parts.length < 2) {
    return null
  }

  const payloadPart = parts[1]
  if (!payloadPart) {
    return null
  }

  try {
    const payload = JSON.parse(decodeBase64Url(payloadPart)) as {
      sub?: string
      orgId?: number | null
      role?: string | null
      legalName?: string
      email?: string
    }

    const userId = Number(payload.sub)
    if (!Number.isFinite(userId) || !payload.legalName || !payload.email) {
      return null
    }

    return {
      userId,
      orgId: typeof payload.orgId === 'number' ? payload.orgId : null,
      role: typeof payload.role === 'string' ? payload.role : null,
      legalName: payload.legalName,
      email: payload.email,
    }
  } catch {
    return null
  }
}

export function getInitials(name: string): string {
  const trimmedName = name.trim()
  if (!trimmedName) {
    return '?'
  }

  const parts = trimmedName.split(/\s+/).slice(0, 2)
  return parts.map((part) => part[0]?.toUpperCase() ?? '').join('') || '?'
}

function syncClaimsFromStorage() {
  claims.value = decodeJwtClaims(getAuthToken())
}

async function loadOrganizations() {
  syncClaimsFromStorage()

  if (!claims.value) {
    organizations.value = []
    orgErrorMessage.value = ''
    return
  }

  isLoadingOrganizations.value = true
  orgErrorMessage.value = ''

  try {
    const response = await api.get<OrganizationMembership[]>('/me/organizations')
    organizations.value = Array.isArray(response.data) ? response.data : []
  } catch {
    organizations.value = []
    orgErrorMessage.value = 'Kunne ikke hente organisasjoner.'
    throw new Error('Kunne ikke hente organisasjoner')
  } finally {
    isLoadingOrganizations.value = false
  }
}

export async function ensureOrgSessionLoaded(force = false) {
  if (force) {
    loadPromise = null
  }

  if (!loadPromise) {
    loadPromise = loadOrganizations().finally(() => {
      loadPromise = null
    })
  }

  return loadPromise
}

export async function switchOrganization(organizationId: number) {
  isSwitchingOrganization.value = true
  orgErrorMessage.value = ''

  try {
    const response = await api.post('/auth/switch-organization', {
      organizationId,
    })

    const nextToken = typeof response.data === 'string' ? response.data.trim() : ''
    if (!nextToken) {
      throw new Error('Serveren returnerte ikke et gyldig token.')
    }

    setAuthToken(nextToken)
    claims.value = decodeJwtClaims(nextToken)
    await ensureOrgSessionLoaded(true)
  } catch (error) {
    orgErrorMessage.value = error instanceof Error ? error.message : 'Kunne ikke bytte organisasjon.'
    throw error
  } finally {
    isSwitchingOrganization.value = false
  }
}

export function clearOrgSession() {
  clearAuthToken()
  claims.value = null
  organizations.value = []
  orgErrorMessage.value = ''
  loadPromise = null
}

export function useOrgSession() {
  syncClaimsFromStorage()

  const isAuthenticated = computed(() => claims.value !== null)
  const currentOrganization = computed(
    () =>
      organizations.value.find((organization) => organization.isCurrent) ??
      organizations.value.find((organization) => organization.id === claims.value?.orgId) ??
      null,
  )
  const currentUserName = computed(() => claims.value?.legalName ?? '')
  const currentUserInitials = computed(() => getInitials(claims.value?.legalName ?? ''))
  const currentUserRole = computed(() => currentOrganization.value?.orgRole ?? claims.value?.role ?? null)

  return {
    claims,
    organizations,
    isAuthenticated,
    currentOrganization,
    currentUserName,
    currentUserInitials,
    currentUserRole,
    isLoadingOrganizations,
    isSwitchingOrganization,
    orgErrorMessage,
    ensureOrgSessionLoaded,
    switchOrganization,
    clearOrgSession,
  }
}

export function resetOrgSessionForTests() {
  claims.value = null
  organizations.value = []
  isLoadingOrganizations.value = false
  isSwitchingOrganization.value = false
  orgErrorMessage.value = ''
  loadPromise = null
}
