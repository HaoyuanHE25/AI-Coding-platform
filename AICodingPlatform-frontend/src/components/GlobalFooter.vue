<script setup lang="ts">
import { siteConfig } from '@/config'

const currentYear = new Date().getFullYear()
</script>

<template>
  <a-layout-footer class="global-footer">
    © {{ currentYear }} {{ siteConfig.name }}. 保留所有权利。
  </a-layout-footer>
</template>

<style scoped>
.global-footer {
  /* 不参与伸缩，保持自身高度，始终位于布局最底部 */
  flex: 0 0 auto;
  text-align: center;
  background-color: #ffffff51;
  color: rgba(0, 0, 0, 0.65);
}
</style>
