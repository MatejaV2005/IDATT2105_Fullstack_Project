import type { BasicUserWithAccessLevel } from '@/interfaces/util-interfaces'

export const mockUsers: BasicUserWithAccessLevel[] = [
  { id: 11, legalName: 'Mona Jul', email: 'mona.jul@example.com', accessLevel: 'OWNER' },
  { id: 12, legalName: 'Jagland', email: 'jagland@example.com', accessLevel: 'MANAGER' },
  { id: 13, legalName: 'Ane Brevik', email: 'ane.brevik@example.com', accessLevel: 'WORKER' },
  {
    id: 14,
    legalName: 'Ola Svenneby',
    email: 'ola.svenneby@example.com',
    accessLevel: 'WORKER',
  },
  { id: 15, legalName: 'Vedum', email: 'vedum@example.com', accessLevel: 'WORKER' },
  {
    id: 16,
    legalName: 'Kari Nessa Nordtun',
    email: 'kari.northun@example.com',
    accessLevel: 'MANAGER',
  },
]

export function getMockUserNameById(userId: number): string {
  return mockUsers.find((user) => user.id === userId)?.legalName || `Bruker ${userId}`
}
