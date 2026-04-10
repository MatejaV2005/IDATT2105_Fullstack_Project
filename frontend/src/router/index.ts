import DesktopLayout from '@/layouts/DesktopLayout.vue'
import MobileLayout from '@/layouts/MobileLayout.vue'
import AuthView from '@/views/AuthView.vue'
import NotFoundView from '@/views/NotFoundView.vue'
import CcpLogsView from '@/views/desktop/CcpLogsView.vue'
import CreateOrgView from '@/views/desktop/CreateOrgView.vue'
import CriticalControlPointsView from '@/views/desktop/CriticalControlPointsView.vue'
import DeviationHandlingView from '@/views/desktop/DeviationHandlingView.vue'
import DangerAnalysisView from '@/views/desktop/DangerAnalysisView.vue'
import AnalView from '@/views/desktop/AnalView.vue'
import DeviationLogsView from '@/views/desktop/DeviationLogsView.vue'
import HaccpView from '@/views/desktop/HaccpView.vue'
import LearningView from '@/views/desktop/LearningView.vue'
import MappingAndMeasuresView from '@/views/desktop/MappingAndMeasuresView.vue'
import MeView from '@/views/desktop/MeView.vue'
import NoOrganizationView from '@/views/desktop/NoOrganizationView.vue'
import PrerequisitesView from '@/views/desktop/PrerequisitesView.vue'
import TasksView from '@/views/desktop/TasksView.vue'
import TeamView from '@/views/desktop/TeamView.vue'
import DeviationsView from '@/views/mobile/DeviationsView.vue'
import LoggingView from '@/views/mobile/LoggingView.vue'
import MappingPointsView from '@/views/mobile/MappingPointsView.vue'
import RoutinesView from '@/views/mobile/RoutinesView.vue'
import { ensureOrgSessionLoaded, useOrgSession } from '@/composables/useOrgSession'
import { getPostAuthRoute } from '@/utils/auth-routing'
import { getAuthToken } from '@/utils/auth'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/auth',
      component: AuthView,
    },
    {
      path: '/desktop',
      component: DesktopLayout,
      children: [
        {
          path: '',
          redirect: '/desktop/bedrift-analyse',
        },
        {
          path: 'create-org',
          component: CreateOrgView,
        },
        {
          path: 'no-org',
          component: NoOrganizationView,
        },
        {
          path: 'bedrift-teamsammensetning',
          component: TeamView,
        },
        {
          path: 'bedrift-analyse',
          component: AnalView,
        },
        {
          path: 'sign-in',
          redirect: '/auth',
        },
        {
          path: 'sign-up',
          redirect: '/auth',
        },
        {
          path: 'users',
          children: [
            {
              path: 'me',
              component: MeView,
            },
          ],
        },
        {
          path: 'oppgaver-oversikt',
          children: [
            {
              path: '',
              component: TasksView,
            },
            {
              path: 'kontrollpunkt-logger',
              component: CcpLogsView,
            },
            {
              path: 'avvik',
              component: DeviationLogsView,
            },
          ],
        },
        {
          path: 'haccp',
          children: [
            {
              path: '',
              component: HaccpView,
            },
            {
              path: 'prerequisites',
              component: PrerequisitesView,
            },
            {
              path: 'danger-analysis',
              component: DangerAnalysisView,
            },
            {
              path: 'ccps',
              component: CriticalControlPointsView,
            },
          ],
        },
        {
          path: 'ik-alkohol-kartlegging-og-tiltak',
          children: [
            {
              path: '',
              component: MappingAndMeasuresView,
            },
          ],
        },
        {
          path: 'bedrift-opplaering',
          component: LearningView,
        },
      ],
    },
    {
      path: '/mobile',
      component: MobileLayout,
      children: [
        {
          path: '',
          redirect: '/mobile/rutiner',
        },
        {
          path: 'rutiner',
          name: 'rutiner',
          component: RoutinesView,
        },
        {
          path: 'logging',
          name: 'logging',
          component: LoggingView,
        },
        {
          path: 'kartlegging-og-tiltak',
          name: 'kartlegging-og-tiltak',
          component: MappingPointsView,
        },
        {
          path: 'avvik',
          name: 'avvik',
          component: DeviationsView,
        },
        {
          path: 'login',
          name: 'login',
          redirect: '/auth',
        },
      ],
    },
    {
      path: '/',
      redirect: '/auth',
    },
    {
      path: '/:pathMatch(.*)*',
      component: NotFoundView,
    },
  ],
})

router.beforeEach(async (to) => {
  const { claims, organizations } = useOrgSession()
  const hasStoredToken = Boolean(getAuthToken()?.trim())

  if (to.path === '/auth') {
    if (!hasStoredToken || !claims.value) {
      return true
    }

    await ensureOrgSessionLoaded()
    return getPostAuthRoute(claims.value, organizations.value)
  }

  const isDesktopRoute = to.path.startsWith('/desktop')
  const isMobileRoute = to.path.startsWith('/mobile')

  if (!isDesktopRoute && !isMobileRoute) {
    return true
  }

  if (!hasStoredToken || !claims.value) {
    return '/auth'
  }

  await ensureOrgSessionLoaded()

  const postAuthRoute = getPostAuthRoute(claims.value, organizations.value)
  const hasOrganization = postAuthRoute !== '/desktop/no-org'
  const isNoOrgFlowRoute = to.path === '/desktop/no-org' || to.path === '/desktop/create-org'
  const currentOrganization =
    organizations.value.find((organization) => organization.isCurrent) ??
    organizations.value.find((organization) => organization.id === claims.value?.orgId) ??
    null
  const effectiveRole = currentOrganization?.orgRole ?? claims.value?.role ?? null
  const workerDesktopAllowedRoutes = new Set([
    '/desktop/users/me',
    '/desktop/oppgaver-oversikt',
    '/desktop/oppgaver-oversikt/kontrollpunkt-logger',
    '/desktop/oppgaver-oversikt/avvik',
  ])

  if (!hasOrganization && !isNoOrgFlowRoute) {
    return '/desktop/no-org'
  }

  if (isDesktopRoute && hasOrganization && isNoOrgFlowRoute) {
    return postAuthRoute
  }

  if (
    isDesktopRoute &&
    hasOrganization &&
    effectiveRole === 'WORKER' &&
    !workerDesktopAllowedRoutes.has(to.path)
  ) {
    return '/desktop/oppgaver-oversikt'
  }

  return true
})

export default router
