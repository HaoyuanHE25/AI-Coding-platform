<template>
  <div class="app-detail-page">
    <div v-if="loading" class="app-detail-page__loading">
      <a-spin size="large" />
    </div>
    <template v-else-if="appInfo">
      <a-page-header class="app-detail-page__header" :title="appInfo.appName" @back="goBack">
        <template #extra>
          <a-space>
            <a-button v-if="canChat" type="primary" @click="goChat">
              <template #icon><MessageOutlined /></template>
              进入对话
            </a-button>
          </a-space>
        </template>
      </a-page-header>

      <div class="app-detail-page__body">
        <!-- 封面 -->
        <div v-if="appInfo.cover" class="app-detail-page__cover-wrap">
          <img :src="appInfo.cover" :alt="appInfo.appName" class="app-detail-page__cover" />
        </div>

        <a-descriptions :column="2" bordered size="middle">
          <a-descriptions-item label="应用 ID">{{ appInfo.id ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="应用名称">{{ appInfo.appName ?? '-' }}</a-descriptions-item>
          <a-descriptions-item label="创建人">
            <template v-if="appInfo.user">
              <a-avatar :src="appInfo.user.userAvatar" :size="24">
                {{ (appInfo.user.userName || '?').charAt(0) }}
              </a-avatar>
              <span style="margin-left: 8px">{{ appInfo.user.userName }}</span>
            </template>
            <span v-else>-</span>
          </a-descriptions-item>
          <a-descriptions-item label="生成类型">
            <a-tag v-if="appInfo.codeGenType" color="blue">{{ codeGenLabel }}</a-tag>
            <span v-else>-</span>
          </a-descriptions-item>
          <a-descriptions-item label="优先级">
            <a-tag v-if="isFeatured(appInfo.priority)" color="gold">精选</a-tag>
            <span v-else>{{ appInfo.priority ?? 0 }}</span>
          </a-descriptions-item>
          <a-descriptions-item label="状态">
            <a-tag v-if="appInfo.deployKey" color="green">已部署</a-tag>
            <a-tag v-else color="default">未部署</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ appInfo.createTime ? formatDateTime(appInfo.createTime) : '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="部署时间">
            {{ appInfo.deployedTime ? formatDateTime(appInfo.deployedTime) : '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="初始提示词" :span="2">
            <div class="app-detail-page__prompt">{{ appInfo.initPrompt || '-' }}</div>
          </a-descriptions-item>
        </a-descriptions>

        <div v-if="previewUrl" class="app-detail-page__preview">
          <div class="app-detail-page__preview-head">
            <h3 class="app-detail-page__preview-title">应用预览</h3>
            <a
              class="app-detail-page__preview-link"
              href="#"
              @click.prevent="openNewWindow(previewUrl)"
            >
              在新窗口打开 →
            </a>
          </div>
          <iframe :src="previewUrl" class="app-detail-page__preview-frame" frameborder="0" />
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppVoById } from '@/api/appController'
import { useAuth } from '@/composables/useAuth'
import { useAppId } from '@/composables/useAppId'
import { formatDateTime } from '@/utils/date'
import { openNewWindow } from '@/utils/open'
import { MessageOutlined } from '@ant-design/icons-vue'
import { getCodeGenTypeLabel } from '@/constants/codeGenType'
import { isFeatured } from '@/constants/app'
import { getStaticPreviewUrl } from '@/config/env'

const router = useRouter()
const { isLoggedIn, currentUserId, goLogin } = useAuth()
const { appId, isValid: isAppIdValid } = useAppId()

const appInfo = ref<API.AppVO | null>(null)
const loading = ref(true)

const isOwner = computed(() => !!appInfo.value && appInfo.value.userId === currentUserId.value)
const canChat = computed(() => isLoggedIn.value && isOwner.value)

const codeGenLabel = computed(() => getCodeGenTypeLabel(appInfo.value?.codeGenType))

const previewUrl = computed(() => {
  if (!appInfo.value?.codeGenType || !appInfo.value.id) return ''
  return getStaticPreviewUrl(appInfo.value.codeGenType, appInfo.value.id)
})

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.replace('/')
  }
}

const loadApp = async () => {
  if (!isAppIdValid.value) {
    message.error('无效的应用 ID')
    router.replace('/')
    return
  }
  loading.value = true
  try {
    const res = await getAppVoById({ id: appId.value })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data
    } else if (res.data.code === 40400 || !res.data.data) {
      message.error('应用不存在')
      router.replace('/')
    } else {
      message.error('获取应用失败：' + (res.data.message ?? ''))
      router.replace('/')
    }
  } catch {
    message.error('获取应用信息失败，请稍后重试')
    router.replace('/')
  } finally {
    loading.value = false
  }
}

const goChat = () => {
  if (!isLoggedIn.value) {
    goLogin(`/app/chat/${appId.value}`)
    return
  }
  if (!isOwner.value) {
    message.warning('仅应用创建者可进入对话生成')
    return
  }
  router.push(`/app/chat/${appId.value}`)
}

onMounted(() => {
  loadApp()
})
</script>

<style scoped>
.app-detail-page {
  padding: 0;
  max-width: 1100px;
  margin: 0 auto;
}
.app-detail-page__loading {
  display: flex;
  justify-content: center;
  padding: 80px;
}
.app-detail-page__header {
  padding: 0;
  margin-bottom: 24px;
}
.app-detail-page__body {
  display: flex;
  flex-direction: column;
  gap: 24px;
}
.app-detail-page__cover-wrap {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.app-detail-page__cover {
  width: 100%;
  max-height: 360px;
  object-fit: cover;
  display: block;
}
.app-detail-page__prompt {
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 260px;
  overflow: auto;
  padding: 4px 2px;
  line-height: 1.7;
  color: rgba(0, 0, 0, 0.85);
}
.app-detail-page__preview {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.app-detail-page__preview-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.app-detail-page__preview-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}
.app-detail-page__preview-link {
  font-size: 13px;
}
.app-detail-page__preview-frame {
  width: 100%;
  height: 500px;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  background: #fff;
}
</style>
