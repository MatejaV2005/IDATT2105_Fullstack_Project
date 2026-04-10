// @vitest-environment jsdom

import { mount } from '@vue/test-utils'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const { ensureOrgSessionLoaded, switchOrganization, pushMock } = vi.hoisted(() => ({
  ensureOrgSessionLoaded: vi.fn(() => Promise.resolve()),
  switchOrganization: vi.fn(() => Promise.resolve()),
  pushMock: vi.fn(() => Promise.resolve()),
}))

vi.mock('@/composables/useOrgSession', async () => {
  const { computed, ref } = await vi.importActual<typeof import('vue')>('vue')
  const organizations = ref([
    {
      id: 10,
      orgName: 'Org A',
      orgAddress: 'Gate 1',
      orgNumber: 1,
      alcoholEnabled: true,
      foodEnabled: true,
      orgRole: 'OWNER',
      isCurrent: true,
    },
    {
      id: 20,
      orgName: 'Org B',
      orgAddress: 'Gate 2',
      orgNumber: 2,
      alcoholEnabled: false,
      foodEnabled: true,
      orgRole: 'WORKER',
      isCurrent: false,
    },
  ])

  return {
    useOrgSession: () => ({
      organizations,
      currentOrganization: computed(() => organizations.value.find((organization) => organization.isCurrent) ?? null),
      isAuthenticated: ref(true),
      isLoadingOrganizations: ref(false),
      isSwitchingOrganization: ref(false),
      orgErrorMessage: ref(''),
      ensureOrgSessionLoaded,
      switchOrganization,
    }),
  }
})

vi.mock('vue-router', async () => {
  const actual = await vi.importActual<typeof import('vue-router')>('vue-router')
  return {
    ...actual,
    useRouter: () => ({
      push: pushMock,
    }),
  }
})

import OrganizationSwitcher from '../OrganizationSwitcher.vue'

describe('OrganizationSwitcher', () => {
  beforeEach(() => {
    ensureOrgSessionLoaded.mockClear()
    switchOrganization.mockClear()
    pushMock.mockClear()
  })

  it('renders the current organization as disabled and active in the menu', async () => {
    const wrapper = mount(OrganizationSwitcher, {
      props: {
        redirectTo: '/desktop/users/me',
        variant: 'desktop',
      },
    })

    await wrapper.find('.org-switcher-button').trigger('click')

    const currentOrgButton = wrapper
      .findAll('.org-switcher-menu__item')
      .find((button) => button.text().includes('Org A'))

    expect(currentOrgButton?.attributes('disabled')).toBeDefined()
    expect(currentOrgButton?.text()).toContain('Aktiv')
  })
})
