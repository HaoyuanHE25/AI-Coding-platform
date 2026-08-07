import { createRouter, createWebHistory } from 'vue-router'
import ACCESS_ENUM from '@/access/accessEnum.ts'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: '首页',
      component: () => import('@/pages/HomePage.vue'),
    },

    {
      path: '/user/login',
      name: '用户登录',
      component: () => import('@/pages/user/UserLoginPage.vue'),
    },
    {
      path: '/user/register',
      name: '用户注册',
      component: () => import('@/pages/user/UserRegisterPage.vue'),
    },

    {
      path: '/user/userCenter',
      name: '个人中心',
      component: () => import('@/pages/user/UserCenterPage.vue'),
      meta: {
        access: ACCESS_ENUM.USER,
      },
    },

    {
      path: '/admin/userManage',
      name: '管理员管理',
      component: () => import('@/pages/admin/UserManagePage.vue'),
      meta: {
        access: ACCESS_ENUM.ADMIN,
      },
    },
    {
      path: '/noAuth',
      name: '未授权页面',
      component: () => import('@/pages/NoAuth.vue'),
    },
  ],
})

export default router
