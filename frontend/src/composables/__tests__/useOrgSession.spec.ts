// @vitest-environment jsdom

import { beforeEach, describe, expect, it, vi } from 'vitest'

const { getMock, postMock } = vi.hoisted(() => ({
  getMock: vi.fn(),
  postMock: vi.fn(),
}))

vi.mock('@/api/api', () => ({
  default: {
    get: getMock,
    post: postMock,
  },
}))

import { ensureOrgSessionLoaded, resetOrgSessionForTests, switchOrganization, useOrgSession } from '../useOrgSession'

function toBase64Url(payload: object) {
  return btoa(JSON.stringify(payload)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '')
}

function createToken(payload: {
  sub: string
  orgId: number | null
  role: string | null
  legalName: string
  email: string
}) {
  return `header.${toBase64Url(payload)}.signature`
}

describe('useOrgSession', () => {
  beforeEach(() => {
    window.localStorage.clear()
    getMock.mockReset()
    postMock.mockReset()
    resetOrgSessionForTests()
  })

  it('loads organizations and exposes the current user session data', async () => {
    const token = createToken({
      sub: '7',
      orgId: 10,
      role: 'OWNER',
      legalName: 'Alice Example',
      email: 'alice@example.com',
    })

    window.localStorage.setItem('auth_token', token)
    getMock.mockResolvedValue({
      data: [
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
      ],
    })

    const session = useOrgSession()
    await ensureOrgSessionLoaded(true)

    expect(session.currentUserName.value).toBe('Alice Example')
    expect(session.currentUserInitials.value).toBe('AE')
    expect(session.currentOrganization.value?.orgName).toBe('Org A')
    expect(getMock).toHaveBeenCalledWith('/me/organizations')
  })

  it('switches organization and replaces the active JWT/session state', async () => {
    const startingToken = createToken({
      sub: '7',
      orgId: 10,
      role: 'OWNER',
      legalName: 'Alice Example',
      email: 'alice@example.com',
    })
    const switchedToken = createToken({
      sub: '7',
      orgId: 20,
      role: 'WORKER',
      legalName: 'Alice Example',
      email: 'alice@example.com',
    })

    window.localStorage.setItem('auth_token', startingToken)
    postMock.mockResolvedValue({ data: switchedToken })
    getMock.mockResolvedValue({
      data: [
        {
          id: 10,
          orgName: 'Org A',
          orgAddress: 'Gate 1',
          orgNumber: 1,
          alcoholEnabled: true,
          foodEnabled: true,
          orgRole: 'OWNER',
          isCurrent: false,
        },
        {
          id: 20,
          orgName: 'Org B',
          orgAddress: 'Gate 2',
          orgNumber: 2,
          alcoholEnabled: false,
          foodEnabled: true,
          orgRole: 'WORKER',
          isCurrent: true,
        },
      ],
    })

    const session = useOrgSession()
    await switchOrganization(20)

    expect(postMock).toHaveBeenCalledWith('/auth/switch-organization', { organizationId: 20 })
    expect(window.localStorage.getItem('auth_token')).toBe(switchedToken)
    expect(session.claims.value?.orgId).toBe(20)
    expect(session.currentOrganization.value?.orgName).toBe('Org B')
    expect(session.currentUserRole.value).toBe('WORKER')
  })

  it('keeps the old token when switching organization fails', async () => {
    const token = createToken({
      sub: '7',
      orgId: 10,
      role: 'OWNER',
      legalName: 'Alice Example',
      email: 'alice@example.com',
    })

    window.localStorage.setItem('auth_token', token)
    postMock.mockRejectedValue(new Error('Kunne ikke bytte organisasjon.'))

    const session = useOrgSession()

    await expect(switchOrganization(99)).rejects.toThrow('Kunne ikke bytte organisasjon.')
    expect(window.localStorage.getItem('auth_token')).toBe(token)
    expect(session.claims.value?.orgId).toBe(10)
    expect(session.orgErrorMessage.value).toBe('Kunne ikke bytte organisasjon.')
  })
})
