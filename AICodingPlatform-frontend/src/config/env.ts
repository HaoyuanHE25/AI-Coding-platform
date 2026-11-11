/**
 * 环境变量配置
 */
import { CodeGenTypeEnum } from '@/constants/codeGenType'

// 应用部署域名
export const DEPLOY_DOMAIN = import.meta.env.VITE_DEPLOY_DOMAIN || 'http://localhost'

// API 基础地址
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8123/api'

// 静态资源地址
// 必须与主页面同源（默认相对路径 /api/static，由 Vite 代理 / 反向代理转发到后端），
// 否则预览 iframe 与主页面跨域时，无法访问 iframe.contentDocument 注入可视化编辑脚本。
// 可通过 VITE_STATIC_BASE_URL 覆盖（例如生产环境前端与 API 不同域时配置绝对地址）。
export const STATIC_BASE_URL = import.meta.env.VITE_STATIC_BASE_URL || '/api/static'

// 获取部署应用的完整URL
export const getDeployUrl = (deployKey: string) => {
  return `${DEPLOY_DOMAIN}/${deployKey}`
}

// 获取静态资源预览URL
export const getStaticPreviewUrl = (codeGenType: string, appId: string | number) => {
  const baseUrl = `${STATIC_BASE_URL}/${codeGenType}_${appId}/`
  // 如果是 Vue 项目，浏览地址需要添加 dist 后缀
  if (codeGenType === CodeGenTypeEnum.VUE_PROJECT) {
    return `${baseUrl}dist/index.html`
  }
  return baseUrl
}
