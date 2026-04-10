import type { AuthJwtClaims, OrganizationMembership } from '@/types/auth'

export function getPostAuthRoute(
  claims: AuthJwtClaims | null,
  organizations: OrganizationMembership[],
): string {
  if (!claims) {
    return '/auth'
  }

  if (claims.orgId === null || organizations.length === 0) {
    return '/desktop/no-org'
  }

  const currentOrganization =
    organizations.find((organization) => organization.isCurrent) ??
    organizations.find((organization) => organization.id === claims.orgId) ??
    null

  const effectiveRole = currentOrganization?.orgRole ?? claims.role

  if (effectiveRole === 'WORKER') {
    return '/mobile/rutiner'
  }

  return '/desktop/users/me'
}
