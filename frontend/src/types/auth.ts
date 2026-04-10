export interface AuthJwtClaims {
  userId: number
  orgId: number | null
  role: string | null
  legalName: string
  email: string
}

export interface OrganizationMembership {
  id: number
  orgName: string
  orgAddress: string
  orgNumber: number
  alcoholEnabled: boolean
  foodEnabled: boolean
  orgRole: string
  isCurrent: boolean
}
