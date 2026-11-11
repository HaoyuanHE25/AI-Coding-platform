<template>
  <a-modal
    :open="open"
    title="应用详情"
    :footer="null"
    width="560px"
    @cancel="$emit('update:open', false)"
  >
    <a-spin :spinning="!app">
      <div v-if="app" class="app-detail">
        <div class="app-detail__cover">
          <img v-if="app.cover" :src="app.cover" alt="封面" />
          <div v-else class="app-detail__cover-placeholder">
            <FileImageOutlined />
          </div>
        </div>

        <a-descriptions :column="1" size="small" bordered>
          <a-descriptions-item label="应用名称">
            {{ app.appName || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="代码生成类型">
            <a-tag v-if="app.codeGenType" color="blue">
              {{ getCodeGenTypeLabel(app.codeGenType) }}
            </a-tag>
            <span v-else>未知类型</span>
          </a-descriptions-item>
          <a-descriptions-item label="部署状态">
            <a-tag v-if="app.deployKey" color="green">已部署</a-tag>
            <a-tag v-else color="default">未部署</a-tag>
          </a-descriptions-item>
          <a-descriptions-item v-if="app.deployedTime" label="部署时间">
            {{ app.deployedTime }}
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ app.createTime || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            {{ app.updateTime || '-' }}
          </a-descriptions-item>
        </a-descriptions>

        <div v-if="app.initPrompt" class="app-detail__prompt">
          <div class="app-detail__prompt-label">初始提示词</div>
          <div class="app-detail__prompt-content">{{ app.initPrompt }}</div>
        </div>

        <div v-if="showActions" class="app-detail__actions">
          <a-button type="primary" @click="$emit('edit')">编辑</a-button>
          <a-popconfirm
            title="确认删除该应用？"
            ok-text="确认"
            cancel-text="取消"
            @confirm="$emit('delete')"
          >
            <a-button danger>删除</a-button>
          </a-popconfirm>
        </div>
      </div>
    </a-spin>
  </a-modal>
</template>

<script setup lang="ts">
import { FileImageOutlined } from '@ant-design/icons-vue'
import { getCodeGenTypeLabel } from '@/constants/codeGenType'

defineProps<{
  open: boolean
  // 详情页的 appInfo 初始值为 undefined，故此处同时接受 null / undefined
  app: API.AppVO | null | undefined
  showActions: boolean
}>()

defineEmits<{
  'update:open': [value: boolean]
  edit: []
  delete: []
}>()
</script>

<style scoped>
.app-detail__cover {
  width: 100%;
  height: 200px;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 16px;
  background: #f5f5f5;
}

.app-detail__cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-detail__cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: #ccc;
}

.app-detail__prompt {
  margin-top: 16px;
}

.app-detail__prompt-label {
  font-weight: 500;
  margin-bottom: 8px;
  color: #333;
}

.app-detail__prompt-content {
  background: #f9f9f9;
  border-radius: 8px;
  padding: 12px;
  max-height: 150px;
  overflow-y: auto;
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}

.app-detail__actions {
  margin-top: 16px;
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}
</style>
