<template>
  <div class="markdown-body" v-html="renderedHtml" />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { renderMarkdown } from '@/utils/markdown'

const props = defineProps<{
  content: string
}>()

const renderedHtml = computed(() => renderMarkdown(props.content))
</script>

<style>
.markdown-body {
  line-height: 1.6;
  word-break: break-word;
  font-size: 14px;
  color: #1f1f1f;
}

.markdown-body pre {
  background: #0d1117;
  border-radius: 8px;
  padding: 16px;
  overflow-x: auto;
  margin: 12px 0;
}

.markdown-body pre code {
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #e6edf3;
  background: transparent;
  padding: 0;
}

.markdown-body p code {
  background: #f0f0f0;
  color: #d63384;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 12px;
}

.markdown-body p {
  margin: 0 0 8px;
}

.markdown-body p:last-child {
  margin-bottom: 0;
}

.markdown-body ul,
.markdown-body ol {
  padding-left: 20px;
  margin: 8px 0;
}

.markdown-body blockquote {
  border-left: 3px solid #d0d7de;
  padding-left: 12px;
  margin: 8px 0;
  color: #656d76;
}

.markdown-body table {
  border-collapse: collapse;
  margin: 8px 0;
  width: 100%;
}

.markdown-body th,
.markdown-body td {
  border: 1px solid #d0d7de;
  padding: 8px 12px;
  text-align: left;
}

.markdown-body th {
  background: #f6f8fa;
  font-weight: 600;
}

.markdown-body a {
  color: #1677ff;
  text-decoration: none;
}

.markdown-body a:hover {
  text-decoration: underline;
}

.markdown-body h1,
.markdown-body h2,
.markdown-body h3,
.markdown-body h4 {
  margin: 16px 0 8px;
  font-weight: 600;
  color: #1f1f1f;
}

.markdown-body h1 { font-size: 1.4em; }
.markdown-body h2 { font-size: 1.25em; }
.markdown-body h3 { font-size: 1.1em; }

.markdown-body hr {
  border: none;
  border-top: 1px solid #d0d7de;
  margin: 16px 0;
}
</style>