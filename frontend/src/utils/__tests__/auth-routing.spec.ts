import { describe, expect, it } from 'vitest'

import { getPostAuthRoute } from '../auth-routing'

describe('getPostAuthRoute', () => {
  it('sends users without a token to /auth', () => {
    expect(getPostAuthRoute(null, [])).toBe('/auth')
  })

  it('sends users without organizations to the no-org page', () => {
    expect(
      getPostAuthRoute(
        {
          userId: 1,
          orgId: null,
          role: null,
          legalName: 'Alice Example',
          email: 'alice@example.com',
        },
        [],
      ),
    ).toBe('/desktop/no-org')
  })

  it('sends workers to mobile', () => {
    expect(
      getPostAuthRoute(
        {
          userId: 1,
          orgId: 20,
          role: 'WORKER',
          legalName: 'Alice Example',
          email: 'alice@example.com',
        },
        [
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
      ),
    ).toBe('/mobile/rutiner')
  })

  it('sends owners and managers to desktop', () => {
    expect(
      getPostAuthRoute(
        {
          userId: 1,
          orgId: 10,
          role: 'OWNER',
          legalName: 'Alice Example',
          email: 'alice@example.com',
        },
        [
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
      ),
    ).toBe('/desktop/users/me')
  })
})
