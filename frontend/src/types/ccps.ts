export interface AssignedCcp {
  ccpId: number
  name: string
  monitoredDescription: string | null
  criticalMin: number
  criticalMax: number
  unit: string | null
  repeatText: string | null
  dueAt: string | null
  completedForCurrentInterval: boolean
  completedAt: string | null
  lastCompletedAt: string | null
  lastMeasuredValue: number | null
  immediateCorrectiveAction: string
}

export interface SubmittedCcpRecordResponse {
  ccp: AssignedCcp
  recordId: number
  outsideCriticalRange: boolean
}
