<template>
  <div class="app-card-list">
    <div v-if="loading && !list.length" class="app-card-list__skeleton">
      <a-skeleton v-for="n in 6" :key="n" active :paragraph="{ rows: 3 }" />
    </div>
    <template v-else>
      <div v-if="!list.length" class="app-card-list__empty">
        <a-empty :description="emptyText" />
      </div>
      <div v-else class="app-card-list__grid">
        <AppCard
          v-for="item in list"
          :key="item.id"
          :app="item"
          :variant="variant"
          :show-actions="showActions"
          :show-chat="showChatForItem ? showChatForItem(item) : true"
          @click="(app) => emit('click', app)"
          @edit="(app) => emit('edit', app)"
          @delete="(app) => emit('delete', app)"
          @detail="(app) => emit('detail', app)"
          @chat="(app) => emit('chat', app)"
          @view-work="(app) => emit('viewWork', app)"
        />
      </div>
      <div v-if="total > 0" class="app-card-list__pagination">
        <a-pagination
          :current="pageNum"
          :page-size="pageSize"
          :total="total"
          :show-size-changer="false"
          @change="handlePageChange"
        />
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import AppCard from './AppCard.vue'

withDefaults(
  defineProps<{
    list: API.AppVO[]
    total: number
    pageNum: number
    pageSize: number
    loading?: boolean
    emptyText?: string
    variant?: 'mine' | 'featured'
    showActions?: boolean
    showChatForItem?: (app: API.AppVO) => boolean
  }>(),
  {
    loading: false,
    emptyText: '暂无数据',
    variant: 'featured',
    showActions: false,
  },
)

const emit = defineEmits<{
  (e: 'click', app: API.AppVO): void
  (e: 'page-change', page: number): void
  (e: 'edit', app: API.AppVO): void
  (e: 'delete', app: API.AppVO): void
  (e: 'detail', app: API.AppVO): void
  (e: 'chat', app: API.AppVO): void
  (e: 'viewWork', app: API.AppVO): void
}>()

const handlePageChange = (page: number) => {
  emit('page-change', page)
}
</script>

<style scoped>
.app-card-list {
  width: 100%;
}
.app-card-list__grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}
.app-card-list__empty {
  padding: 60px 0;
}
.app-card-list__skeleton {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}
.app-card-list__pagination {
  margin-top: 24px;
  display: flex;
  justify-content: center;
}
</style>
