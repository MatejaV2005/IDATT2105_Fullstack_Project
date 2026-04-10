import type { OrgAccessLevel } from '@/interfaces/util-interfaces'

export interface DirectoryUser {
  id: number
  legalName: string
  email: string
  orgName: string
}

export const mockAllUsers: DirectoryUser[] = [
  { id: 11, legalName: 'Mona Jul', email: 'mona.jul@example.com', orgName: 'Arctic Foods AS' },
  { id: 12, legalName: 'Jagland', email: 'jagland@example.com', orgName: 'Nordic Kiosk Drift' },
  { id: 13, legalName: 'Ane Brevik', email: 'ane.brevik@example.com', orgName: 'Everest Sushi' },
  {
    id: 14,
    legalName: 'Ola Svenneby',
    email: 'ola.svenneby@example.com',
    orgName: 'Vestby Matstasjon',
  },
  { id: 15, legalName: 'Vedum', email: 'vedum@example.com', orgName: 'Everest Sushi' },
  {
    id: 16,
    legalName: 'Kari Nessa Nordtun',
    email: 'kari.northun@example.com',
    orgName: 'Byfjord Catering',
  },
  {
    id: 17,
    legalName: 'Thomas Solberg',
    email: 'thomas.solberg@example.com',
    orgName: 'Arctic Foods AS',
  },
]

export interface NewOrganizationUserPayload {
  userId: number
  orgRole: OrgAccessLevel
}
