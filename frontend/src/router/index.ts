import DesktopLayout from '@/layouts/DesktopLayout.vue'
import MobileLayout from '@/layouts/MobileLayout.vue'
import CreateOrgView from '@/views/desktop/CreateOrgView.vue'
import HaccpView from '@/views/desktop/HaccpView.vue'
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
          path: 'haccp',
          children: [
            {
              path: '',
              component: HaccpView
            }
          ]
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