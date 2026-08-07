import router from '@/router'
import { useLoginUserStore } from '@/stores/LoginUser'
import { message } from 'ant-design-vue'
import ACCESS_ENUM from '@/access/accessEnum'
import checkAccess from '@/access/checkAccess'

// 是否为首次获取登录用户（页面刷新后需等后端返回用户信息再校验权限）
let firstFetchLoginUser = true

/**
 * 全局权限校验
 *
 * 路由通过 meta.access 声明所需权限：
 * - ACCESS_ENUM.NO_LOGIN（默认）：无需登录
 * - ACCESS_ENUM.USER：需要登录
 * - ACCESS_ENUM.ADMIN：需要管理员
 *
 * 未登录访问需登录页 → 跳转登录页（携带 redirect）
 * 已登录但权限不足 → 跳转无权限页
 */
router.beforeEach(async (to, from, next) => {
  const loginUserStore = useLoginUserStore()

  // 首次进入（页面刷新）时，等待后端返回登录用户信息后再做校验
  if (firstFetchLoginUser) {
    await loginUserStore.fetchLoginUser()
    firstFetchLoginUser = false
  }

  const loginUser = loginUserStore.loginUser
  const needAccess = (to.meta?.access as string) ?? ACCESS_ENUM.NO_LOGIN

  // 无需登录即可访问
  if (needAccess === ACCESS_ENUM.NO_LOGIN) {
    next()
    return
  }

  // 权限不足
  if (!checkAccess(loginUser, needAccess)) {
    const isLogin = loginUser?.userRole && loginUser.userRole !== ACCESS_ENUM.NO_LOGIN;
    if (!isLogin) {
      message.error('请先登录')
      next(`/user/login?redirect=${to.fullPath}`)
    } else {
      message.error('没有权限')
      next('/noAuth')
    }
    return
  }

  next()
})
