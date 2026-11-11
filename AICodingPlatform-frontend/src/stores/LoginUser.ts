import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUser } from '@/api/userController.ts'

export const useLoginUserStore = defineStore('loginUser', () => {
  // 默认值
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  // 是否已拉取过登录态，避免路由守卫与页头重复请求
  let hasFetched = false

  // 获取登录用户信息
  // @param force 登录/注册成功后强制刷新
  async function fetchLoginUser(force = false) {
    if (hasFetched && !force) return
    try {
      const res = await getLoginUser()
      if (res.data.code === 0 && res.data.data) {
        loginUser.value = res.data.data
      }
    } catch {
      // 未登录或请求失败，保持默认值
    } finally {
      hasFetched = true
    }
  }
  // 更新登录用户信息
  function setLoginUser(newLoginUser: API.LoginUserVO) {
    loginUser.value = newLoginUser
  }

  return { loginUser, setLoginUser, fetchLoginUser }
})
