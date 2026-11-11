<template>
  <div class="app-edit-page">
    <div v-if="loading" class="app-edit-page__loading">
      <a-spin size="large" />
    </div>
    <template v-else-if="appInfo">
      <a-page-header
        class="app-edit-page__header"
        :title="`编辑应用 - ${appInfo.appName}`"
        @back="router.back()"
      >
        <template #extra>
          <a-space>
            <a-button @click="handleCancel">取消</a-button>
            <a-button type="primary" :loading="saving" @click="handleSave">保存</a-button>
          </a-space>
        </template>
      </a-page-header>

      <a-form
        class="app-edit-page__form"
        :model="formState"
        :label-col="{ span: 6 }"
        :wrapper-col="{ span: 12 }"
      >
        <a-form-item label="应用名称" :rules="[{ required: true, message: '请输入应用名称' }]">
          <a-input
            v-model:value="formState.appName"
            placeholder="请输入应用名称"
            :maxlength="50"
            show-count
          />
        </a-form-item>

        <template v-if="isAdmin">
          <a-form-item label="应用封面">
            <a-input v-model:value="formState.cover" placeholder="请输入封面图片 URL" />
            <div v-if="formState.cover" style="margin-top: 12px">
              <a-image :src="formState.cover" :width="200" />
            </div>
          </a-form-item>
          <a-form-item label="优先级">
            <a-input-number
              v-model:value="formState.priority"
              :min="0"
              :max="999"
              style="width: 120px"
            />
            <span style="margin-left: 12px; color: rgba(0, 0, 0, 0.45)">
              优先级 >= 99 即为精选应用
            </span>
          </a-form-item>
        </template>

        <a-form-item label="初始提示词">
          <a-textarea :value="appInfo.initPrompt" :rows="4" disabled />
          <div style="margin-top: 4px; font-size: 12px; color: rgba(0, 0, 0, 0.35)">
            初始提示词创建后不可修改
          </div>
        </a-form-item>

        <a-form-item label="创建时间">
          <span>
            {{ appInfo.createTime ? formatDateTime(appInfo.createTime) : '' }}
          </span>
        </a-form-item>
      </a-form>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { getAppVoById, updateApp, updateAppByAdmin } from '@/api/appController'
import { useAuth } from '@/composables/useAuth'
import { useAppId } from '@/composables/useAppId'
import { formatDateTime } from '@/utils/date'

const router = useRouter()
const { isAdmin, loginUserStore } = useAuth()
const { appId, isValid: isAppIdValid } = useAppId()

const appInfo = ref<API.AppVO | null>(null)
const loading = ref(true)
const saving = ref(false)

const formState = reactive({
  appName: '',
  cover: '',
  priority: 0,
})

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
      const data = res.data.data
      if (!isAdmin.value && data.userId !== loginUserStore.loginUser.id) {
        message.error('无权编辑该应用')
        router.replace('/noAuth')
        return
      }
      appInfo.value = data
      formState.appName = data.appName ?? ''
      formState.cover = data.cover ?? ''
      formState.priority = data.priority ?? 0
    } else {
      message.error('应用不存在')
      router.replace('/')
    }
  } catch {
    message.error('获取应用信息失败')
    router.replace('/')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  if (!formState.appName.trim()) {
    message.warning('请输入应用名称')
    return
  }
  saving.value = true
  try {
    // 雪花 ID 超出 JS 安全整数范围，必须保持字符串原样传回后端，避免精度丢失
    const appIdValue = appId.value
    let res: Awaited<ReturnType<typeof updateApp>>
    if (isAdmin.value) {
      res = await updateAppByAdmin({
        id: appIdValue,
        appName: formState.appName.trim(),
        cover: formState.cover || undefined,
        priority: formState.priority,
      })
    } else {
      res = await updateApp({
        id: appIdValue,
        appName: formState.appName.trim(),
      })
    }
    if (res.data.code === 0) {
      message.success('保存成功')
      router.back()
    } else {
      message.error('保存失败：' + (res.data.message ?? ''))
    }
  } catch {
    message.error('保存失败，请稍后重试')
  } finally {
    saving.value = false
  }
}

const handleCancel = () => {
  router.back()
}

onMounted(() => {
  loadApp()
})
</script>

<style scoped>
.app-edit-page {
  padding: 0;
  max-width: 960px;
  margin: 0 auto;
}
.app-edit-page__loading {
  display: flex;
  justify-content: center;
  padding: 80px;
}
.app-edit-page__header {
  padding: 0;
  margin-bottom: 24px;
}
.app-edit-page__form {
  background: #fff;
  padding: 32px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
</style>
