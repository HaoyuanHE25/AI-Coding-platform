import axios from 'axios'
import { message } from 'ant-design-vue'
import { API_BASE_URL } from '@/config/env'

// 公开页面路径（不需要登录即可访问的页面）
const PUBLIC_PATHS = ['/', '/user/login', '/user/register', '/app/detail']

// 创建 Axios 实例
const myAxios = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000,
  withCredentials: true,
})

// 全局请求拦截器
myAxios.interceptors.request.use(
  function (config) {
    // Do something before request is sent
    return config
  },
  function (error) {
    // Do something with request error
    return Promise.reject(error)
  },
)

// 全局响应拦截器
myAxios.interceptors.response.use(
  function (response) {
    const { data } = response
    // 未登录
    if (data.code === 40100) {
      const currentPath = window.location.pathname
      // 检查当前是否在公开页面
      const isPublicPage = PUBLIC_PATHS.some(
        (p) => currentPath === p || currentPath.startsWith(p + '/'),
      )
      // 获取请求配置中是否标记了跳过鉴权重定向
      const skipRedirect = (response.config as { skipAuthRedirect?: boolean })?.skipAuthRedirect

      if (
        !response.request?.responseURL?.includes('user/get/login') &&
        !isPublicPage &&
        !skipRedirect
      ) {
        message.warning('请先登录')
        window.location.href = `/user/login?redirect=${window.location.href}`
      }
    }
    return response
  },
  function (error) {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error
    return Promise.reject(error)
  },
)

export default myAxios
