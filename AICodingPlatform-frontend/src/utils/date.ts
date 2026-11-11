import dayjs from 'dayjs'

/**
 * 格式化日期时间：YYYY-MM-DD HH:mm:ss
 */
export const formatDateTime = (t?: string): string => {
  if (!t) return ''
  return dayjs(t).format('YYYY-MM-DD HH:mm:ss')
}

/**
 * 格式化聊天消息时间：HH:MM
 */
export const formatChatTime = (): string => {
  const d = new Date()
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}