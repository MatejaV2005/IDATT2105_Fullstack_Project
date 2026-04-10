import DesktopLayout from '@/layouts/DesktopLayout.vue'
import MobileLayout from '@/layouts/MobileLayout.vue'
import AuthView from '@/views/AuthView.vue'
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
  ],
})

router.beforeEach(async (to) => {
  if (!to.path.startsWith('/desktop')) {
    return true
  }

  const { claims, organizations } = useOrgSession()

  if (!claims.value) {
    return true
  }

  await ensureOrgSessionLoaded()

  const postAuthRoute = getPostAuthRoute(claims.value, organizations.value)
  const hasOrganization = postAuthRoute !== '/desktop/no-org'
  const isNoOrgFlowRoute = to.path === '/desktop/no-org' || to.path === '/desktop/create-org'

  if (!hasOrganization && !isNoOrgFlowRoute) {
    return '/desktop/no-org'
  }

  if (hasOrganization && isNoOrgFlowRoute) {
    return postAuthRoute
  }

  return true
})

export default router
