export interface AssignedRoutine {
  routineId: number
  title: string
  categoryName: string | null
  description: string
  immediateCorrectiveAction: string
  dueAt: string | null
  completedForCurrentInterval: boolean
  completedAt: string | null
  lastCompletedAt: string | null
}
