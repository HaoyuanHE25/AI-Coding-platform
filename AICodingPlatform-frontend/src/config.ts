import logo from '@/assets/logo.png'
import ACCESS_ENUM from '@/access/accessEnum'

export interface SiteConfig {
  /** 网站名称 */
  name: string
  /** 网站 logo */
  logo: string
}

export interface MenuItemMeta {
  /** 访问所需权限 */
  access?: string
}

export interface MenuItem {
  /** 菜单项唯一标识 */
  key: string
  /** 菜单项展示文案 */
  label: string
  /** 点击后跳转的路由路径 */
  path: string
  /** 菜单项元信息（权限、隐藏等） */
  meta?: MenuItemMeta
}

/** 站点全局配置 */
export const siteConfig: SiteConfig = {
  name: 'AI 应用生成平台',
  logo,
}

/**
 * 顶部导航菜单配置
 * 通过修改此处的配置即可调整顶部导航的菜单项，无需改动组件代码。
 * meta.access 使用 ACCESS_ENUM 中的常量：
 * - NO_LOGIN：无需登录（默认）
 * - USER：需要登录
 * - ADMIN：需要管理员
 */
export const menuItems: MenuItem[] = [
  { key: 'home', label: '首页', path: '/' },
  { key: 'about', label: '关于', path: '/about' },
  {
    key: 'userManage',
    label: '用户管理',
    path: '/admin/userManage',
    meta: { access: ACCESS_ENUM.ADMIN },
  },
]
