import DesktopLayout from '@/layouts/DesktopLayout.vue'
import MobileLayout from '@/layouts/MobileLayout.vue'
import CreateOrgView from '@/views/desktop/CreateOrgView.vue'
import CriticalControlPointsView from '@/views/desktop/CriticalControlPointsView.vue'
import DangerAnalysisView from '@/views/desktop/DangerAnalysisView.vue'
import HaccpView from '@/views/desktop/HaccpView.vue'
import LearningView from '@/views/desktop/LearningView.vue'
import MappingAndMeasuresView from '@/views/desktop/MappingAndMeasuresView.vue'
import MeView from '@/views/desktop/MeView.vue'
import PrerequisitesView from '@/views/desktop/PrerequisitesView.vue'
import SignInView from '@/views/desktop/SignInView.vue'
import SignUpView from '@/views/desktop/SignUpView.vue'
import TeamView from '@/views/desktop/TeamView.vue'
import DeviationsView from '@/views/mobile/DeviationsView.vue'
import LoggingView from '@/views/mobile/LoggingView.vue'
import LoginView from '@/views/mobile/LoginView.vue'
import RoutinesView from '@/views/mobile/RoutinesView.vue'
import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/desktop',
      component: DesktopLayout,
      children: [
        {
          path: 'create-org',
          component: CreateOrgView
        },
        {
          path: 'bedrift-teamsammensetning',
          component: TeamView
        },
        {
          path: 'sign-in',
          component: SignInView
        },
        {
          path: 'sign-up',
          component: SignUpView
        },
        {
          path: 'users',
          children: [
            {
              path: 'me',
              component: MeView
            },

          ]
        },
        {
          path: 'haccp',
          children: [
            {
              path: '',
              component: HaccpView
            },
            {
              path: 'prerequisites',
              component: PrerequisitesView
            },
            {
              path: 'danger-analysis',
              component: DangerAnalysisView
            },
            {
              path: 'ccps',
              component: CriticalControlPointsView
            },

          ]
        },
        {
          path: 'ik-alkohol-kartlegging-og-tiltak',
          children: [
            {
              path: '',
              component: MappingAndMeasuresView
            },
          ]
        },
        {
          path: 'bedrift-opplaering',
          component: LearningView
        }
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
          path: 'avvik',
          name: 'avvik',
          component: DeviationsView,
        },
        {
          path: 'login',
          name: 'login',
          component: LoginView,
        },
      ],
    },
    {
      path: '/',
      redirect: '/mobile',
    },
  ],
})

export default router
