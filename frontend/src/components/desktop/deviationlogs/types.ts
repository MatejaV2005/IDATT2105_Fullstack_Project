export type DeviationCategory = 'IK_MAT' | 'IK_ALKOHOL' | 'OTHER'
export type DeviationReviewStatus = 'OPEN' | 'CLOSED'
export type OrgAccessLevel = 'OWNER' | 'MANAGER' | 'WORKER'

export type DeviationUser = {
  id: number
  legalName: string
  accessLevel: OrgAccessLevel
}

export type DeviationLog = {
  id: number
  ccpRecordId: number | null
  routineRecordId: number | null
  category: DeviationCategory
  reportedBy: DeviationUser
  reviewStatus: DeviationReviewStatus
  reviewedBy: DeviationUser | null
  reviewedAt: string | null
  whatWentWrong: string
  immediateActionTaken: string
  potentialCause: string
  potentialPreventativeMeasure: string
  preventativeMeasureActuallyTaken: string
  deviationReceivers: DeviationUser[]
  createdAt: string
}

export type UpdateDeviationPayload = {
  preventativeMeasureActuallyTaken: string
}
