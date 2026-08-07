<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { siteConfig, menuItems } from '@/config'
import type { MenuItem, MenuItemMeta } from '@/config'
import { userLogout } from '@/api/userController'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/LoginUser'
import checkAccess from '@/access/checkAccess'
import ACCESS_ENUM from '@/access/accessEnum'
import type { MenuProps } from 'ant-design-vue'

const loginUserStore = useLoginUserStore()

const route = useRoute()
const router = useRouter()

/** 移动端断点（px），小于该宽度切换为抽屉菜单 */
const MOBILE_BREAKPOINT = 768
const isMobile = ref(false)
const drawerVisible = ref(false)

const updateResponsive = () => {
  isMobile.value = window.innerWidth < MOBILE_BREAKPOINT
}

onMounted(async () => {
  updateResponsive()
  window.addEventListener('resize', updateResponsive)

  // 自动登录：如果之前没登录过，尝试获取登录用户信息
  // 加 await 是为了等用户登录成功之后，再执行后续的代码
  const loginUser = loginUserStore.loginUser
  if (!loginUser || !loginUser.userRole) {
    await loginUserStore.fetchLoginUser()
  }
})

onUnmounted(() => {
  window.removeEventListener('resize', updateResponsive)
})

/** 扩展 antd 菜单项，带上路由元信息（key 即 path） */
interface AntdMenuItemWithMeta {
  key: string
  label: string
  meta?: MenuItemMeta
}

//  menu -> 路由 item（读取 menu 上的 meta）
const menuToRouteItem = (menu: MenuItem) => {
  return {
    path: menu.path,
    meta: menu.meta ?? {},
  }
}

//  将菜单配置转换为 a-menu 的 items 格式（同时携带 meta，过滤时用）
const menuItemsForAntd = computed<AntdMenuItemWithMeta[]>(() =>
  menuItems.map((item) => ({
    key: item.path,
    label: item.label,
    meta: item.meta,
  })),
)

//  过滤菜单项
const filterMenus = (menus: AntdMenuItemWithMeta[]) => {
  return menus.filter((menu) => {
    //  todo 需要自己实现 menu 到路由 item 的转化
    const source = menuItems.find((m) => m.path === menu.key)
    if (!source) return false
    const item = menuToRouteItem(source)
    //  根据权限过滤菜单，有权限则返回 true，则保留该菜单
    const needAccess = (item.meta?.access as string) ?? ACCESS_ENUM.NO_LOGIN
    return checkAccess(loginUserStore.loginUser, needAccess)
  }) as unknown as MenuProps['items']
}

//  使用 originItems 并过滤
const filteredMenuItems = computed<MenuProps['items']>(() => filterMenus(menuItemsForAntd.value))

//  当前选中的菜单项
const selectedKeys = computed(() => [route.path])

const handleMenuClick = ({ key }: { key: string }) => {
  router.push(key)
  if (isMobile.value) {
    drawerVisible.value = false
  }
}

const handleBrandClick = () => {
  router.push('/')
}

//  退出登录
const handleLogout = async () => {
  try {
    const res = await userLogout()
    if (res.data.code === 0) {
      loginUserStore.setLoginUser({ userName: '未登录' })
      message.success('退出登录成功')
      await router.push({
        path: '/user/login',
        replace: true,
      })
    } else {
      message.error('退出登录失败：' + res.data.message)
    }
  } catch {
    message.error('退出登录失败，请稍后重试')
  }
}

//  登录方法
const handleLogin = () => {
  router.push('/user/login')
}

//  进入个人中心
const handleUserCenter = () => {
  router.push('/user/userCenter')
}

//  当前用户信息（计算属性简化模板）
const currentUser = computed(() => loginUserStore.loginUser)
</script>

<template>
  <a-layout-header class="global-header">
    <div class="global-header__brand" @click="handleBrandClick">
      <img :src="siteConfig.logo" alt="logo" class="global-header__logo" />
      <span class="global-header__title">{{ siteConfig.name }}</span>
    </div>

    <!-- 桌面端：水平菜单 -->
    <div v-if="!isMobile" class="global-header__menu">
      <a-menu
        mode="horizontal"
        :selected-keys="selectedKeys"
        :items="filteredMenuItems"
        @click="handleMenuClick"
      />
    </div>

    <!-- 桌面端：用户操作 -->
    <div v-if="!isMobile" class="global-header__actions">
      <!--  已登录状态 -->
      <div v-if="currentUser.id">
        <a-dropdown>
          <a-space>
            <a-avatar :src="currentUser.userAvatar" size="small" />
            {{ currentUser.userName ?? '无名' }}
          </a-space>
          <template #overlay>
            <a-menu>
              <a-menu-item key="center" @click="handleUserCenter">个人中心</a-menu-item>
              <a-menu-divider />
              <!--  使用 a-menu-item 包裹 -->
              <a-menu-item key="logout" @click="handleLogout">
                <LogoutOutlined /> 退出登录
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>

      <!--  未登录状态 -->
      <div v-else>
        <a-button type="primary" @click="handleLogin">登录</a-button>
      </div>
    </div>

    <!-- 移动端：汉堡按钮 -->
    <div v-else class="global-header__actions">
      <a-button type="text" class="global-header__trigger" @click="drawerVisible = true">
        <MenuOutlined />
      </a-button>
    </div>

    <!-- 移动端抽屉菜单 -->
    <a-drawer
      v-model:open="drawerVisible"
      placement="right"
      :width="240"
      :body-style="{ padding: 0 }"
      :title="siteConfig.name"
    >
      <a-menu
        mode="inline"
        :selected-keys="selectedKeys"
        :items="filteredMenuItems"
        @click="handleMenuClick"
      />
      <div class="global-header__drawer-actions">
        <a-button type="primary" block @click="handleLogin">登录</a-button>
      </div>
    </a-drawer>
  </a-layout-header>
</template>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  padding: 0 24px;
  background-color: #ffffff;
  border-bottom: 1px solid #f0f0f0;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
  height: 64px;
}

.global-header__brand {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
  cursor: pointer;
}

.global-header__logo {
  height: 32px;
  width: auto;
  object-fit: contain;
}

.global-header__title {
  font-size: 18px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.88);
  white-space: nowrap;
}

.global-header__menu {
  flex: 1;
  min-width: 0;
  margin-left: 24px;
}

/* 水平菜单与 64px 头部对齐，并去掉默认下边框 */
.global-header__menu :deep(.ant-menu) {
  background-color: transparent;
  border-bottom: none;
  line-height: 62px;
}

.global-header__actions {
  flex-shrink: 0;
  margin-left: auto;
  display: flex;
  align-items: center;
}

.global-header__trigger {
  font-size: 20px;
}

.global-header__drawer-actions {
  padding: 16px;
}
</style>
