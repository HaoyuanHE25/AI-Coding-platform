import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/LoginUser'
import ACCESS_ENUM from '@/access/accessEnum'

const LOGIN_REDIRECT = '/user/login'

/**
 * 认证相关 composable
 * 提供 isLoggedIn、isAdmin、currentUserId、goLogin 等公共逻辑
 */
export const useAuth = () => {
  const loginUserStore = useLoginUserStore()
  const router = useRouter()

  const isLoggedIn = computed(() => {
    const { id, userRole } = loginUserStore.loginUser
    return !!id && !!userRole && userRole !== ACCESS_ENUM.NO_LOGIN
  })

  const isAdmin = computed(() => loginUserStore.loginUser.userRole === ACCESS_ENUM.ADMIN)

  const currentUserId = computed(() => loginUserStore.loginUser.id)

  const goLogin = (redirect = '/', extraQuery: Record<string, string> = {}) => {
    message.warning('未登录无法使用对话等业务功能，请先登录')
    router.push({
      path: LOGIN_REDIRECT,
      query: { redirect, ...extraQuery },
    })
  }

  return { isLoggedIn, isAdmin, currentUserId, goLogin, loginUserStore }
}