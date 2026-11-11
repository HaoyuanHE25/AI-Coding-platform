/**
 * 在新窗口打开链接，带安全属性
 */
export const openNewWindow = (url: string): void => {
  window.open(url, '_blank', 'noopener,noreferrer')
}