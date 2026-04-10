import type { OrganizationMembership } from '@/types/auth'

export type DeviationCategory = 'IK_MAT' | 'IK_ALKOHOL' | 'OTHER'
export type ReviewStatus = 'OPEN' | 'CLOSED'

export interface Deviation {
  id: number
  organizationId: number
  reportedById: number
  reportedByName: string
  category: DeviationCategory
  reviewStatus: ReviewStatus
  createdAt: string
  reviewedById: number | null
  reviewedByName: string | null
  reviewedAt: string | null
  whatWentWrong: string
  immediateActionTaken: string
  potentialCause: string
  potentialPreventativeMeasure: string
  preventativeMeasureActuallyTaken: string
}

export const deviationCategories: { value: DeviationCategory; label: string }[] = [
  { value: 'IK_MAT', label: 'IK Mat' },
  { value: 'IK_ALKOHOL', label: 'IK Alkohol' },
  { value: 'OTHER', label: 'Annet' },
]

type DeviationCategoryScope = Pick<OrganizationMembership, 'foodEnabled' | 'alcoholEnabled'> | null

export function getAvailableDeviationCategories(
  scope: DeviationCategoryScope,
): { value: DeviationCategory; label: string }[] {
  return deviationCategories.filter((category) => {
    if (category.value === 'IK_MAT') {
      return Boolean(scope?.foodEnabled)
    }

    if (category.value === 'IK_ALKOHOL') {
      return Boolean(scope?.alcoholEnabled)
    }

    return true
  })
}

export function getDefaultDeviationCategory(scope: DeviationCategoryScope): DeviationCategory {
  if (scope?.foodEnabled) {
    return 'IK_MAT'
  }

  if (scope?.alcoholEnabled) {
    return 'IK_ALKOHOL'
  }

  return 'OTHER'
}

export const reviewStatuses: { value: ReviewStatus; label: string }[] = [
  { value: 'OPEN', label: 'Åpen' },
  { value: 'CLOSED', label: 'Lukket' },
]

export const deviations: Deviation[] = [
  {
    id: 1,
    organizationId: 1,
    reportedById: 2,
    reportedByName: 'Jonas Ghar Støre',
    category: 'IK_MAT',
    reviewStatus: 'OPEN',
    createdAt: '2024-03-15T08:30:00',
    reviewedById: null,
    reviewedByName: null,
    reviewedAt: null,
    whatWentWrong: 'Temperatur i kjøleskap 1 var 8°C istedenfor maks 4°C ved morgenkontroll.',
    immediateActionTaken: 'Varslet leverandør og satt varer i karantene. Kjølemaskin reparert.',
    potentialCause: 'Kjølemaskin defekt. Termometer kan ha vært unøyaktig.',
    potentialPreventativeMeasure: 'Regelmessig kalibrering av termometer og daglig temperaturkontroll.',
    preventativeMeasureActuallyTaken: '',
  },
  {
    id: 2,
    organizationId: 1,
    reportedById: 3,
    reportedByName: 'Kari Normann',
    category: 'OTHER',
    reviewStatus: 'CLOSED',
    createdAt: '2024-03-08T10:15:00',
    reviewedById: 2,
    reviewedByName: 'Jonas Ghar Støre',
    reviewedAt: '2024-03-10T14:00:00',
    whatWentWrong: 'Fant museekskrementer i lagerrommet bak fryseren.',
    immediateActionTaken: 'Varslet skadedyrbekjemper og ryddet området.',
    potentialCause: 'Mulig inntrenging via ventilasjonskanal.',
    potentialPreventativeMeasure: 'Tett ventilasjonsåpninger og regelmessig kontroll.',
    preventativeMeasureActuallyTaken: 'Satt opp musefeller og tetting av ventilasjon. Skadedyrbekjemper bekreftet at området er fritt.',
  },
  {
    id: 3,
    organizationId: 1,
    reportedById: 2,
    reportedByName: 'Jonas Ghar Støre',
    category: 'IK_ALKOHOL',
    reviewStatus: 'OPEN',
    createdAt: '2024-03-20T13:45:00',
    reviewedById: null,
    reviewedByName: null,
    reviewedAt: null,
    whatWentWrong: 'Alkohol levering mottatt uten gyldig kwitansjon fra leverandør.',
    immediateActionTaken: 'Satt varer i karantene og kontaktet leverandør.',
    potentialCause: 'Leverandørfeil eller manglende dokumentasjon.',
    potentialPreventativeMeasure: 'Kontroll av dokumentasjon ved mottak.',
    preventativeMeasureActuallyTaken: '',
  },
]
