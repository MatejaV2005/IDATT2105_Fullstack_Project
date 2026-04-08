interface PrerequisiteUser {
  userId: number
  legalName: string
}

interface RoutinePrerequisitePoint {
  title: string
  type: 'routine'
  measures: string
  repeatText: string
  verifiers: PrerequisiteUser[]
  deviationRecievers: PrerequisiteUser[]
  performers: PrerequisiteUser[]
  deputy: PrerequisiteUser[]
}

interface StandardPrerequisitePoint {
  title: string
  type: 'standard'
  description: string
}

export interface PrerequisiteCategory {
  categoryName: string
  points: (RoutinePrerequisitePoint | StandardPrerequisitePoint)[]
}

export type PrerequisiteCategoryAllInfo = PrerequisiteCategory[]
