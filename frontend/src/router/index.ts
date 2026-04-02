import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      redirect: '/rutiner',
    },
    {
      path: '/rutiner',
      name: 'rutiner',
      component: () => import('../views/RoutinesView.vue'),
    },
    {
      path: '/logging',
      name: 'logging',
      component: () => import('../views/LoggingView.vue'),
    },
    {
      path: '/avvik',
      name: 'avvik',
      component: () => import('../views/DeviationsView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue'),
    },
  ],
})

export default router
