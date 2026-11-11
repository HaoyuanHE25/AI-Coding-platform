/**
 * 应用相关常量
 */

/** 精选应用的优先级阈值 */
export const FEATURED_PRIORITY = 99

/** 判断是否精选应用 */
export const isFeatured = (priority?: number | null): boolean => {
  return (priority ?? 0) >= FEATURED_PRIORITY
}