// #region PrerequisitesView
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
// #endregion

// #region CriticalControlPointsView
interface CcpUser {
  userId: number
  userName: string
}

interface CcpCorrectiveMeasure {
  productName: string
  measureDescription: string
}

export interface CriticalControlPoint {
  name: string
  how: string
  equipment: string
  instructionsAndCalibration: string
  immediateCorrectiveAction: string
  criticalMin: number
  criticalMax: number
  unit: string
  monitoredDescription: string
  verifiers: CcpUser[]
  deviationRecievers: CcpUser[]
  performers: CcpUser[]
  deputy: CcpUser[]
  ccpCorrectiveMeasure: CcpCorrectiveMeasure[]
}

export type CriticalControlPointAllInfo = CriticalControlPoint[]
// #endregion

// #region TeamView
interface TeamAssignments {
  verifier: number
  deviationReceiver: number
  performer: number
  deputy: number
}

interface TeamCourseProgress {
  completed: number
  total: number
}

export interface TeamUser {
  userId: number
  legalName: string
  orgRole: string
  ccpAssignments: TeamAssignments
  routineAssignments: TeamAssignments
  mappingPointResponsibilities: number
  openReviewedCcpDeviations: number
  openReviewedRoutineDeviations: number
  courseProgress: TeamCourseProgress
}

export type TeamAllInfo = TeamUser[]
// #endregion

// #region MappingAndMeasuresView
export interface MappingPoint {
  law: string
  dots: number
  title: string
  challenges: string
  measures: string
  responsible: string[]
  responsibleText: string
}

export type MappingPointAllInfo = MappingPoint[]
// #endregion

// #region LearningView
interface LearningResource {
  name: string
  type: 'link' | 'file'
}

export interface LearningCourse {
  name: string
  description: string
  resources: LearningResource[]
  responsible: string[]
  uniqueId: number
}

interface LearningUserCourseProgress {
  name: string
  completed: boolean
  uniqueId: number
}

export interface LearningUserProgress {
  name: string
  courses: LearningUserCourseProgress[]
}

export interface LearningAllInfo {
  allCourses: LearningCourse[]
  userProgress: LearningUserProgress[]
}
// #endregion

// #region HaccpView
export interface DangerAnalysisCollaborator {
  userId: number
  legalName: string
}

export type DangerAnalysisCollaboratorsAllInfo = DangerAnalysisCollaborator[]
// #endregion
