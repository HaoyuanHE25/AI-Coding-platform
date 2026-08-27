<script setup lang="ts">
import { ref, onMounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { ArrowUpOutlined, PaperClipOutlined } from '@ant-design/icons-vue'
import { addApp, deleteApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { useAuth } from '@/composables/useAuth'
import { siteConfig } from '@/config'
import { getDeployUrl } from '@/config/env'
import { openNewWindow } from '@/utils/open'
import AppCardList from '@/components/AppCardList.vue'

const router = useRouter()
const route = useRoute()
const { isLoggedIn, isAdmin, goLogin, loginUserStore } = useAuth()

const PAGE_SIZE = 8
const HOT_PROMPTS = [
  '请帮我生成一个响应式个人博客网站，包含首页文章列表、文章详情页、关于我页面、标签分类和搜索功能，整体采用简洁的极简设计风格，配色以白色和深蓝色为主',
  '创建一个在线电商商城，包含商品展示首页、商品分类导航、商品详情页、购物车和结算功能，支持商品筛选和搜索，界面采用现代简约风格，突出商品图片展示',
  '帮我做一个企业官网，包含首页公司介绍、产品服务展示、成功案例、新闻动态和联系我们页面，设计风格需要专业大气，适合科技公司形象',
  '生成一个任务管理看板应用，支持创建待办事项、拖拽排序、设置优先级和截止日期，包含列表视图和看板视图切换，界面简洁高效适合团队协作',
]

const promptInput = ref('')
const createLoading = ref(false)

const myApps = ref<API.AppVO[]>([])
const myAppsTotal = ref(0)
const myAppsPage = ref(1)
const myAppsLoading = ref(false)
const myAppName = ref('')

const goodApps = ref<API.AppVO[]>([])
const goodAppsTotal = ref(0)
const goodAppsPage = ref(1)
const goodAppsLoading = ref(false)
const goodAppName = ref('')

const mySearchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: PAGE_SIZE,
  sortField: 'createTime',
  sortOrder: 'desc',
})

const goodSearchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: PAGE_SIZE,
  sortField: 'createTime',
  sortOrder: 'desc',
})

const fetchMyApps = async () => {
  if (!isLoggedIn.value) return
  myAppsLoading.value = true
  try {
    mySearchParams.pageNum = myAppsPage.value
    mySearchParams.appName = myAppName.value.trim() || undefined
    const res = await listMyAppVoByPage({ ...mySearchParams })
    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records ?? []
      myAppsTotal.value = res.data.data.totalRow ?? 0
    } else if (res.data.code === 40100) {
      message.warning('未登录无法查看我的作品，请先登录')
    } else {
      message.error('获取我的应用失败：' + (res.data.message ?? ''))
    }
  } catch {
    message.error('获取我的应用失败')
  } finally {
    myAppsLoading.value = false
  }
}

const fetchGoodApps = async () => {
  goodAppsLoading.value = true
  try {
    goodSearchParams.pageNum = goodAppsPage.value
    goodSearchParams.appName = goodAppName.value.trim() || undefined
    const res = await listGoodAppVoByPage({ ...goodSearchParams })
    if (res.data.code === 0 && res.data.data) {
      goodApps.value = res.data.data.records ?? []
      goodAppsTotal.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取精选应用失败：' + (res.data.message ?? ''))
    }
  } catch {
    message.error('获取精选应用失败')
  } finally {
    goodAppsLoading.value = false
  }
}

const createAndGo = async (prompt: string) => {
  if (!prompt.trim()) {
    message.warning('请输入应用描述')
    return
  }
  if (!isLoggedIn.value) {
    goLogin('/', { prompt: prompt.trim() })
    return
  }
  createLoading.value = true
  try {
    const res = await addApp({ initPrompt: prompt.trim() })
    if (res.data.code === 0 && res.data.data != null) {
      // 兼容后端返回 { data: { id: "123" } }、{ data: { id: 123 } } 或 { data: "123" }
      const rawId =
        typeof res.data.data === 'object'
          ? ((res.data.data as any).id ?? (res.data.data as any).appId)
          : res.data.data
      const appId = String(rawId)
      if (!appId) {
        message.error('创建失败：无效的应用 ID')
        return
      }
      message.success('创建成功')
      sessionStorage.setItem('initPrompt', prompt.trim())
      router.push(`/app/chat/${appId}?init=1`)
    } else if (res.data.code === 40100) {
      goLogin('/', { prompt: prompt.trim() })
    } else {
      message.error('创建失败：' + (res.data.message ?? ''))
    }
  } catch {
    message.error('创建失败，请稍后重试')
  } finally {
    createLoading.value = false
  }
}

const handleCreate = () => {
  createAndGo(promptInput.value)
}

const handleHotPrompt = (p: string) => {
  promptInput.value = p
}

const handlePromptKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleCreate()
  }
}

const handleMyAppClick = (app: API.AppVO) => {
  if (!isLoggedIn.value) {
    goLogin(`/app/chat/${app.id}`)
    return
  }
  router.push(`/app/chat/${app.id}?view=1`)
}

/** 精选案例：进入详情（他人应用无法直接对话） */
const handleGoodAppClick = (app: API.AppVO) => {
  router.push(`/app/detail/${app.id}`)
}

const handleChat = (app: API.AppVO) => {
  router.push(`/app/chat/${app.id}?view=1`)
}

const handleViewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    openNewWindow(getDeployUrl(app.deployKey))
  }
}

const showChatForFeatured = (app: API.AppVO) => {
  return isAdmin.value || app.userId === loginUserStore.loginUser.id
}

const handleEdit = (app: API.AppVO) => {
  router.push(`/app/edit/${app.id}`)
}

const handleDetail = (app: API.AppVO) => {
  router.push(`/app/detail/${app.id}`)
}

const handleDelete = (app: API.AppVO) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除应用「${app.appName}」吗？删除后不可恢复。`,
    okType: 'danger',
    onOk: async () => {
      const res = await deleteApp({ id: app.id })
      if (res.data.code === 0) {
        message.success('删除成功')
        fetchMyApps()
      } else {
        message.error('删除失败：' + (res.data.message ?? ''))
      }
    },
  })
}

const handleMySearch = () => {
  myAppsPage.value = 1
  fetchMyApps()
}

const handleGoodSearch = () => {
  goodAppsPage.value = 1
  fetchGoodApps()
}

const handleMyPageChange = (page: number) => {
  if (!isLoggedIn.value) return
  myAppsPage.value = page
  fetchMyApps()
}

const handleGoodPageChange = (page: number) => {
  goodAppsPage.value = page
  fetchGoodApps()
}

const handleTipClick = (tip: string) => {
  message.info(`${tip}功能开发中`)
}

watch(isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    fetchMyApps()
  } else {
    myApps.value = []
    myAppsTotal.value = 0
    myAppsPage.value = 1
  }
})

onMounted(() => {
  const promptFromQuery = route.query.prompt
  if (typeof promptFromQuery === 'string' && promptFromQuery.trim()) {
    promptInput.value = promptFromQuery
  }
  if (isLoggedIn.value) {
    fetchMyApps()
  }
  fetchGoodApps()
})
</script>

<template>
  <div class="home-page">
    <section class="home-hero">
      <div class="home-hero__content">
        <h1 class="home-hero__title">
          <img :src="siteConfig.logo" alt="logo" class="home-hero__logo" />
          <span>AI 应用生成平台</span>
        </h1>
        <p class="home-hero__subtitle">一句话轻松创建网站应用</p>

        <a-alert
          v-if="!isLoggedIn"
          class="home-hero__login-tip"
          type="info"
          show-icon
          message="未登录无法使用 AI 对话创建、我的作品等业务功能，请先登录"
        >
          <template #action>
            <a-button size="small" type="primary" @click="goLogin()">去登录</a-button>
          </template>
        </a-alert>

        <div class="home-hero__input">
          <a-textarea
            v-model:value="promptInput"
            :auto-size="{ minRows: 2, maxRows: 6 }"
            placeholder="帮我创建个人博客网站"
            :maxlength="500"
            @keydown="handlePromptKeydown"
          />
          <div class="home-hero__actions">
            <div class="home-hero__tools">
              <a-button type="text" size="small" @click="handleTipClick('上传')">
                <template #icon><PaperClipOutlined /></template>
                上传
              </a-button>
            </div>
            <a-button
              type="primary"
              shape="circle"
              size="large"
              class="home-hero__send"
              :loading="createLoading"
              @click="handleCreate"
            >
              <template #icon><ArrowUpOutlined /></template>
            </a-button>
          </div>
        </div>

        <div class="home-hero__chips">
          <button
            v-for="t in HOT_PROMPTS"
            :key="t"
            type="button"
            class="home-hero__chip"
            :title="t"
            @click="handleHotPrompt(t)"
          >
            {{ t.slice(0, 30) }}…
          </button>
        </div>
      </div>
    </section>

    <div class="home-page__inner">
      <section class="home-section">
        <div class="home-section__header">
          <h2 class="home-section__title">我的作品</h2>
          <a-input-search
            v-if="isLoggedIn"
            v-model:value="myAppName"
            placeholder="按名称搜索"
            allow-clear
            style="width: 220px"
            @search="handleMySearch"
          />
        </div>
        <AppCardList
          v-if="isLoggedIn"
          :list="myApps"
          :total="myAppsTotal"
          :page-num="myAppsPage"
          :page-size="PAGE_SIZE"
          :loading="myAppsLoading"
          variant="mine"
          show-actions
          empty-text="暂无作品，试着创建一个吧"
          @click="handleMyAppClick"
          @page-change="handleMyPageChange"
          @edit="handleEdit"
          @delete="handleDelete"
          @detail="handleDetail"
          @chat="handleChat"
          @view-work="handleViewWork"
        />
        <div v-else class="home-login-prompt">
          <a-empty description="未登录无法查看作品，登录后可使用对话创建等功能">
            <a-button type="primary" @click="goLogin()">立即登录</a-button>
          </a-empty>
        </div>
      </section>

      <section class="home-section">
        <div class="home-section__header">
          <h2 class="home-section__title">精选案例</h2>
          <a-input-search
            v-model:value="goodAppName"
            placeholder="按名称搜索"
            allow-clear
            style="width: 220px"
            @search="handleGoodSearch"
          />
        </div>
        <AppCardList
          :list="goodApps"
          :total="goodAppsTotal"
          :page-num="goodAppsPage"
          :page-size="PAGE_SIZE"
          :loading="goodAppsLoading"
          variant="featured"
          empty-text="暂无精选应用"
          :show-chat-for-item="showChatForFeatured"
          @click="handleGoodAppClick"
          @page-change="handleGoodPageChange"
          @chat="handleChat"
          @view-work="handleViewWork"
        />
      </section>
    </div>
  </div>
</template>

<style scoped>
.home-page {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0e27 0%, #1a1040 30%, #0d1b3e 60%, #0a1628 100%);
  position: relative;
  overflow: hidden;
}
.home-page::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse at 20% 50%, rgba(56, 189, 248, 0.08) 0%, transparent 60%),
    radial-gradient(ellipse at 80% 20%, rgba(99, 102, 241, 0.1) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 80%, rgba(16, 185, 129, 0.06) 0%, transparent 50%);
  pointer-events: none;
}

/* ---- inner content container ---- */
.home-page__inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px 48px;
  position: relative;
  z-index: 1;
}

.home-hero {
  position: relative;
  padding: 60px 16px 28px;
  text-align: center;
  background: transparent;
  border-radius: 0;
  margin-bottom: 0;
  z-index: 1;
}
.home-hero__content {
  max-width: 800px;
  margin: 0 auto;
}
.home-hero__title {
  font-size: 48px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 16px;
  line-height: 1.2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  text-shadow: 0 0 40px rgba(99, 102, 241, 0.3);
}
.home-hero__logo {
  width: 56px;
  height: 56px;
  object-fit: contain;
  border-radius: 50%;
  filter: drop-shadow(0 0 12px rgba(99, 102, 241, 0.4));
}
.home-hero__subtitle {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0 0 28px;
  letter-spacing: 1px;
}
.home-hero__login-tip {
  margin-bottom: 20px;
  text-align: left;
  background: rgba(255, 255, 255, 0.05) !important;
  border-color: rgba(255, 255, 255, 0.1) !important;
}
.home-hero__login-tip :deep(.ant-alert-message) {
  color: rgba(255, 255, 255, 0.85) !important;
}
.home-hero__input {
  background: rgba(255, 255, 255, 0.06);
  border-radius: 16px;
  padding: 18px 20px 14px;
  text-align: left;
  border: 1px solid rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(12px);
  transition: border-color 0.2s;
}
.home-hero__input:focus-within {
  border-color: rgba(99, 102, 241, 0.4);
  box-shadow: 0 0 20px rgba(99, 102, 241, 0.1);
}
.home-hero__input :deep(.ant-input) {
  border: none;
  box-shadow: none;
  padding: 4px 0;
  font-size: 15px;
  line-height: 1.8;
  resize: none;
  background: transparent;
  color: #fff;
}
.home-hero__input :deep(.ant-input::placeholder) {
  color: rgba(255, 255, 255, 0.3);
}
.home-hero__actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
  gap: 16px;
}
.home-hero__tools {
  display: flex;
  gap: 4px;
}
.home-hero__tools :deep(.ant-btn-text) {
  color: rgba(255, 255, 255, 0.5);
}
.home-hero__tools :deep(.ant-btn-text:hover) {
  color: rgba(255, 255, 255, 0.85);
}
.home-hero__send {
  background: linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%) !important;
  border-color: transparent !important;
  box-shadow: 0 4px 16px rgba(99, 102, 241, 0.4);
}
.home-hero__send:hover {
  background: linear-gradient(135deg, #818cf8 0%, #a78bfa 100%) !important;
  box-shadow: 0 6px 24px rgba(99, 102, 241, 0.55);
}
.home-hero__chips {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 10px;
  margin-top: 20px;
  max-width: 800px;
  margin-left: auto;
  margin-right: auto;
}
.home-hero__chip {
  border: 1px solid rgba(255, 255, 255, 0.1);
  background: rgba(255, 255, 255, 0.05);
  color: rgba(255, 255, 255, 0.65);
  padding: 10px 18px;
  border-radius: 10px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  backdrop-filter: blur(8px);
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.home-hero__chip:hover {
  color: #a78bfa;
  border-color: rgba(139, 92, 246, 0.4);
  background: rgba(139, 92, 246, 0.1);
  box-shadow: 0 4px 16px rgba(139, 92, 246, 0.15);
}

.home-section {
  margin-top: 48px;
}
.home-section__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
  gap: 16px;
}
.home-section__title {
  font-size: 22px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin: 0;
}
.home-section :deep(.ant-input-search .ant-input) {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.1);
  color: #000;
}
.home-section :deep(.ant-input-search .ant-input::placeholder) {
  color: rgba(0, 0, 0, 0.45);
}

.home-login-prompt {
  padding: 40px 0;
}

@media (max-width: 640px) {
  .home-hero__title {
    font-size: 30px;
  }
  .home-hero__logo {
    width: 40px;
    height: 40px;
  }
  .home-section__header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
