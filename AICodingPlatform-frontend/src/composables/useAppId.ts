import { computed } from 'vue'
import { useRoute } from 'vue-router'

/**
 * 从路由参数中提取并验证 appId
 * @returns 验证后的 appId 字符串，无效时返回空字符串
 */
export const useAppId = () => {
  const route = useRoute()
  const appId = computed(() => String(route.params.appId))
  const isValid = computed(() => !!appId.value)
  return { appId, isValid }
}