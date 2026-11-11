<template>
  <a-modal
    :open="open"
    title="部署成功"
    :footer="null"
    width="480px"
    @cancel="$emit('update:open', false)"
  >
    <div class="deploy-success">
      <div class="deploy-success__icon">
        <CheckCircleFilled />
      </div>
      <div class="deploy-success__title">应用已成功部署！</div>
      <div class="deploy-success__url">
        <span class="deploy-success__url-label">访问地址：</span>
        <a :href="deployUrl" target="_blank" rel="noopener noreferrer">{{ deployUrl }}</a>
      </div>
      <div class="deploy-success__actions">
        <a-button type="primary" size="large" @click="$emit('openSite')">
          立即访问
        </a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { CheckCircleFilled } from '@ant-design/icons-vue'

defineProps<{
  open: boolean
  deployUrl: string
}>()

defineEmits<{
  'update:open': [value: boolean]
  openSite: []
}>()
</script>

<style scoped>
.deploy-success {
  text-align: center;
  padding: 16px 0;
}

.deploy-success__icon {
  font-size: 56px;
  color: #52c41a;
  margin-bottom: 12px;
}

.deploy-success__title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 16px;
}

.deploy-success__url {
  background: #f5f5f5;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 20px;
  word-break: break-all;
}

.deploy-success__url-label {
  font-weight: 500;
  color: #666;
}

.deploy-success__url a {
  color: #1677ff;
}

.deploy-success__actions {
  display: flex;
  justify-content: center;
}
</style>