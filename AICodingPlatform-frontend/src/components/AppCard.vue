<template>
  <a-card class="app-card" hoverable :body-style="{ padding: 0 }" @click="handleClick">
    <div class="app-card__cover">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" class="app-card__cover-img" />
      <div v-else class="app-card__cover-placeholder">
        <RobotOutlined />
      </div>
      <div v-if="isFeaturedApp" class="app-card__badge">
        <a-tag color="gold">精选</a-tag>
      </div>
      <div class="app-card__overlay" @click.stop>
        <a-button v-if="showChat" type="primary" size="small" @click="emit('chat', props.app)">
          <template #icon><EyeOutlined /></template>
          查看对话
        </a-button>
        <a-button
          v-if="hasDeployKey"
          size="small"
          class="app-card__overlay-work"
          @click="emit('viewWork', props.app)"
        >
          <template #icon><GlobalOutlined /></template>
          查看作品
        </a-button>
      </div>
    </div>

    <!-- 我的作品：标题 + 创建时间 -->
    <div v-if="variant === 'mine'" class="app-card__body app-card__body--mine">
      <h3 class="app-card__title" :title="app.appName">{{ app.appName }}</h3>
      <p class="app-card__time-text">创建于 {{ formatRelativeTime(app.createTime) }}</p>
    </div>

    <!-- 精选案例：头像 + 标题/作者 + 标签 -->
    <div v-else class="app-card__body app-card__body--featured">
      <div class="app-card__featured-row">
        <a-avatar :src="app.user?.userAvatar" :size="36" class="app-card__featured-avatar">
          {{ (app.user?.userName || '?').charAt(0) }}
        </a-avatar>
        <div class="app-card__featured-info">
          <h3 class="app-card__title" :title="app.appName">{{ app.appName }}</h3>
          <p class="app-card__author-name">{{ app.user?.userName || '未知用户' }}</p>
        </div>
        <div class="app-card__tags">
          <a-tag color="purple">用户应用</a-tag>
          <a-tag v-if="app.codeGenType" color="blue">{{ codeGenLabel }}</a-tag>
        </div>
      </div>
    </div>
  </a-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import dayjs from 'dayjs'
import { RobotOutlined, EyeOutlined, GlobalOutlined } from '@ant-design/icons-vue'
import { getCodeGenTypeLabel } from '@/constants/codeGenType'
import { isFeatured } from '@/constants/app'

const props = withDefaults(
  defineProps<{
    app: API.AppVO
    /** mine：我的作品；featured：精选案例 */
    variant?: 'mine' | 'featured'
    showActions?: boolean
    showChat?: boolean
  }>(),
  {
    variant: 'featured',
    showActions: false,
    showChat: true,
  },
)

const emit = defineEmits<{
  (e: 'click', app: API.AppVO): void
  (e: 'edit', app: API.AppVO): void
  (e: 'delete', app: API.AppVO): void
  (e: 'detail', app: API.AppVO): void
  (e: 'chat', app: API.AppVO): void
  (e: 'viewWork', app: API.AppVO): void
}>()

const isFeaturedApp = computed(() => isFeatured(props.app.priority))

const hasDeployKey = computed(() => !!props.app.deployKey)

const codeGenLabel = computed(() => getCodeGenTypeLabel(props.app.codeGenType))

const handleClick = () => {
  emit('click', props.app)
}

const formatRelativeTime = (t?: string) => {
  if (!t) return '未知时间'
  const d = dayjs(t)
  const now = dayjs()
  const minutes = now.diff(d, 'minute')
  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes} 分钟前`
  const hours = now.diff(d, 'hour')
  if (hours < 24) return `${hours} 小时前`
  const days = now.diff(d, 'day')
  if (days < 7) return `${days} 天前`
  return d.format('YYYY-MM-DD')
}
</script>

<style scoped>
.app-card {
  height: 100%;
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease;
}
.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}
.app-card__cover {
  position: relative;
  width: 100%;
  aspect-ratio: 16 / 9;
  background: linear-gradient(135deg, #e0eafc 0%, #cfdef3 100%);
  overflow: hidden;
}
.app-card__cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.app-card__cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: #8c8c8c;
}
.app-card__badge {
  position: absolute;
  top: 12px;
  left: 12px;
}
.app-card__actions {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 6px;
}
.app-card__more {
  color: rgba(0, 0, 0, 0.65);
}
.app-card__body {
  padding: 14px 16px 16px;
}
.app-card__title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.88);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.app-card__time-text {
  margin: 8px 0 0;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
}
.app-card__featured-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}
.app-card__featured-avatar {
  flex-shrink: 0;
}
.app-card__featured-info {
  flex: 1;
  min-width: 0;
}
.app-card__author-name {
  margin: 4px 0 0;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.45);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.app-card__tags {
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.app-card__overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: rgba(0, 0, 0, 0.45);
  opacity: 0;
  transition: opacity 0.2s ease;
}
.app-card:hover .app-card__overlay {
  opacity: 1;
}
.app-card__overlay-work {
  background: #fff;
  border-color: #fff;
  color: rgba(0, 0, 0, 0.85);
}
.app-card__overlay-work:hover {
  background: #f0f0f0 !important;
  border-color: #f0f0f0 !important;
}
</style>
