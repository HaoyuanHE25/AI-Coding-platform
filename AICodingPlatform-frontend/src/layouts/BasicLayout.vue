<template>
  <a-layout class="basic-layout">
    <GlobalHeader />

    <a-layout-content
      class="main-content"
      :class="{ 'main-content--full': isFullContent }"
    >
      <router-view />
    </a-layout-content>

    <GlobalFooter v-if="!hideFooter" />
  </a-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'

const route = useRoute()

const isFullContent = computed(() => !!route.meta.fullContent)
const hideFooter = computed(() => !!route.meta.hideFooter)
</script>

<style scoped>
.basic-layout {
  background: #f0f2f5;
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  width: 100%;
  padding: 24px;
  margin: 16px auto;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.main-content--full {
  padding: 0;
  margin: 0;
  max-width: none;
  background: transparent;
  border-radius: 0;
  box-shadow: none;
}
</style>
