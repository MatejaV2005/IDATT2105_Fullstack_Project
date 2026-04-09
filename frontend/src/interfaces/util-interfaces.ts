export type OrgAccessLevel = 'OWNER' | 'MANAGER' | 'WORKER'

export type FileAccessLevel = 'OWNER' | 'MANAGER' | 'WORKER' | 'ANYONE_IN_ORG' | 'PUBLIC'

export interface BasicUserWithAccessLevel {
  id: number
  email: string
  legalName: string
  accessLevel: OrgAccessLevel
}
