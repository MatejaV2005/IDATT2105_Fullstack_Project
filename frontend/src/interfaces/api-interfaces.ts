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
  deviationRecievers: PrerequisiteUser[]
  performers: PrerequisiteUser[]
  deputy: PrerequisiteUser[]
  routineId: number
}

interface StandardPrerequisitePoint {
  title: string
  type: 'standard'
  description: string
  resources: {
    id: number
    name: string
    type: 'link' | 'file'
  }[]
  standardId: number
}

export interface PrerequisiteCategory {
  categoryName: string
  points: (RoutinePrerequisitePoint | StandardPrerequisitePoint)[]
  id: number
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
  productCategoryId: number
  id: number
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
  intervalStart?: number | null
  intervalRepeatTime?: number | null
  repeatText?: string | null
  id: number
}

export type CriticalControlPointAllInfo = CriticalControlPoint[]

export interface NewCriticalControlPoint {
  name: string
  how: string
  equipment: string
  instructionsAndCalibration: string
  immediateCorrectiveAction: string
  criticalMin: number
  criticalMax: number
  unit: string
  monitoredDescription: string
  intervalStart?: number | null
  intervalRepeatTime?: number | null
  verifiers: number[] // list of user id's
  deviationRecievers: number[] // list of user id's
  performers: number[] // list of user id's
  deputy: number[] // list of user id's
  ccpCorrectiveMeasure: {
    id?: number | null
    productCategoryId: number
    measureDescription: string
    productName?: string
  }[]
}
// #endregion

export interface DesktopProductCategory {
  id: number
  name: string
  description: string
}

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
  responsibleText: string
  id: number
}

export type MappingPointAllInfo = MappingPoint[]
// #endregion

// #region LearningView
export interface LearningResource {
  name: string
  type: 'link' | 'file'
  id: number
}

export interface LearningResponsibleUser {
  userId: number
  legalName: string
}

export interface LearningCourse {
  id: number
  title: string
  courseDescription: string
  resources: LearningResource[]
  responsibleUsers: LearningResponsibleUser[]
}

export interface LearningUserCourseProgress {
  title: string
  completed: boolean
  courseId: number
  hasProgressRecord?: boolean
}

export interface LearningUserProgress {
  id: number
  legalName: string
  email: string
  courses: LearningUserCourseProgress[]
}

export interface LearningOverviewResponse {
  allCourses: LearningCourse[]
  userProgress: {
    userId: number
    legalName: string
    courses: {
      courseId: number
      title: string
      completed: boolean
    }[]
  }[]
}

export type LearningAllInfo = LearningOverviewResponse

export interface LearningOrganizationUser {
  id: number
  legalName: string
  email: string
  accessLevel: string
}

export interface CreateLearningCoursePayload {
  title: string
  description: string
  links: string[]
  resources: File[]
}
// #endregion

// #region HaccpView
export interface DangerAnalysisCollaborator {
  userId: number
  legalName: string
}

export type DangerAnalysisCollaboratorsAllInfo = DangerAnalysisCollaborator[]
// #endregion

// #region TasksView
export interface TasksOverviewInfo {
  remainingCcpVerifications: number
  remainingDeviationReviews: number
}
// #endregion

// #region CcpLogsView
export type VerificationStatus = 'SKIPPED' | 'VERIFIED' | 'REJECTED' | 'WAITING'

export interface CcpLogRecord {
  id: number
  value: number
  min: number
  max: number
  unit: string
  comment: string
  performedBy: {
    id: number
    legalName: string
  }
}

export interface CcpLogsGroup {
  id: number
  name: string
  records: CcpLogRecord[]
}

export type CcpLogsAllInfo = CcpLogsGroup[]
// #endregion

// #region MeView
export interface MeInfo {
  id: number
  legalName: string
  email: string
  createdAt: string
}
// #endregion
