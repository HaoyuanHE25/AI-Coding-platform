<template>
  <div class="app-manage-page">
    <a-form layout="inline" :model="searchForm" @finish="doSearch">
      <a-form-item label="应用名称">
        <a-input v-model:value="searchForm.appName" placeholder="输入应用名称" allow-clear />
      </a-form-item>
      <a-form-item label="封面">
        <a-input v-model:value="searchForm.cover" placeholder="封面关键词" allow-clear />
      </a-form-item>
      <a-form-item label="生成类型">
        <a-select
          v-model:value="searchForm.codeGenType"
          placeholder="全部"
          allow-clear
          style="width: 140px"
          :options="codeGenTypeOptions"
        />
      </a-form-item>
      <a-form-item label="优先级">
        <a-input-number
          v-model:value="searchForm.priority"
          :min="0"
          :max="999"
          :controls="false"
          placeholder="优先级"
          style="width: 100px"
        />
      </a-form-item>
      <a-form-item label="用户 ID">
        <a-input-number
          v-model:value="searchForm.userId"
          :min="1"
          :controls="false"
          placeholder="用户 ID"
          style="width: 120px"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
      <a-form-item>
        <a-button @click="doReset">重置</a-button>
      </a-form-item>
    </a-form>
    <a-divider />

    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      :loading="loading"
      :row-key="getRowKey"
      :scroll="{ x: 1200 }"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'cover'">
          <a-image
            v-if="record.cover"
            :src="record.cover"
            :width="80"
            :height="45"
            style="object-fit: cover"
          />
          <span v-else>-</span>
        </template>
        <template v-else-if="column.dataIndex === 'appName'">
          <a @click.prevent="goDetail(record)">{{ record.appName }}</a>
        </template>
        <template v-else-if="column.dataIndex === 'codeGenType'">
          {{ getCodeGenTypeLabel(record.codeGenType) }}
        </template>
        <template v-else-if="column.dataIndex === 'user'">
          <template v-if="record.user">
            <a-avatar :src="record.user.userAvatar" :size="24">
              {{ (record.user.userName || '?').charAt(0) }}
            </a-avatar>
            <span style="margin-left: 8px">{{ record.user.userName }}</span>
          </template>
          <span v-else>-</span>
        </template>
        <template v-else-if="column.dataIndex === 'priority'">
          <a-tag v-if="isFeatured(record.priority)" color="gold">精选</a-tag>
          <span v-else>{{ record.priority ?? 0 }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'deployKey'">
          <a-tag v-if="record.deployKey" color="green">已部署</a-tag>
          <span v-else>-</span>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ record.createTime ? formatDateTime(record.createTime) : '' }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space>
            <a-button type="link" size="small" @click="goEdit(record)">
              <template #icon><EditOutlined /></template>
              编辑
            </a-button>
            <a-button
              v-if="isFeatured(record.priority)"
              type="link"
              size="small"
              @click="doUnfeature(record)"
            >
              <template #icon><StarFilled /></template>
              取消精选
            </a-button>
            <a-button v-else type="link" size="small" @click="doFeature(record)">
              <template #icon><StarOutlined /></template>
              精选
            </a-button>
            <a-button type="link" size="small" danger @click="doDelete(record)">
              <template #icon><DeleteOutlined /></template>
              删除
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message, Modal } from 'ant-design-vue'
import { listAppVoByPageByAdmin, updateAppByAdmin, deleteAppByAdmin } from '@/api/appController'
import { EditOutlined, StarOutlined, StarFilled, DeleteOutlined } from '@ant-design/icons-vue'
import { CODE_GEN_TYPE_OPTIONS, getCodeGenTypeLabel } from '@/constants/codeGenType'
import { isFeatured, FEATURED_PRIORITY } from '@/constants/app'
import { formatDateTime } from '@/utils/date'

const router = useRouter()

const PAGE_SIZE = 10

const codeGenTypeOptions = CODE_GEN_TYPE_OPTIONS

const columns = [
  { title: 'ID', dataIndex: 'id', width: 80 },
  { title: '封面', dataIndex: 'cover', width: 100 },
  { title: '应用名称', dataIndex: 'appName', width: 180 },
  { title: '生成类型', dataIndex: 'codeGenType', width: 110 },
  { title: '创建人', dataIndex: 'user', width: 140 },
  { title: '优先级', dataIndex: 'priority', width: 90 },
  { title: '状态', dataIndex: 'deployKey', width: 90 },
  { title: '创建时间', dataIndex: 'createTime', width: 180 },
  { title: '操作', key: 'action', fixed: 'right', width: 220 },
]

const data = ref<API.AppVO[]>([])
const total = ref(0)
const loading = ref(false)

// 表格行唯一标识
const getRowKey = (record: API.AppVO) => record.id ?? ''

const searchParams = reactive<API.AppQueryRequest>({
  pageNum: 1,
  pageSize: PAGE_SIZE,
  sortField: 'createTime',
  sortOrder: 'desc',
})

const searchForm = reactive<{
  appName: string
  cover: string
  codeGenType?: string
  priority?: number
  userId?: number
}>({
  appName: '',
  cover: '',
  codeGenType: undefined,
  priority: undefined,
  userId: undefined,
})

const fetchData = async () => {
  loading.value = true
  try {
    const res = await listAppVoByPageByAdmin({
      ...searchParams,
      appName: searchForm.appName.trim() || undefined,
      cover: searchForm.cover.trim() || undefined,
      codeGenType: searchForm.codeGenType || undefined,
      priority: searchForm.priority,
      userId: searchForm.userId,
    })
    if (res.data.code === 0 && res.data.data) {
      data.value = res.data.data.records ?? []
      total.value = res.data.data.totalRow ?? 0
    } else {
      message.error('获取数据失败：' + (res.data.message ?? ''))
    }
  } catch {
    message.error('获取数据失败')
  } finally {
    loading.value = false
  }
}

const pagination = computed(() => ({
  current: searchParams.pageNum ?? 1,
  pageSize: searchParams.pageSize ?? PAGE_SIZE,
  total: total.value,
  showSizeChanger: true,
  showTotal: (t: number) => `共 ${t} 条`,
}))

const doTableChange = (page: { current?: number; pageSize?: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

const doSearch = () => {
  searchParams.pageNum = 1
  fetchData()
}

const doReset = () => {
  searchForm.appName = ''
  searchForm.cover = ''
  searchForm.codeGenType = undefined
  searchForm.priority = undefined
  searchForm.userId = undefined
  searchParams.pageNum = 1
  fetchData()
}

const goEdit = (record: API.AppVO) => {
  // 新开页面跳转编辑
  const url = router.resolve(`/app/edit/${record.id}`).href
  window.open(url, '_blank')
}

const goDetail = (record: API.AppVO) => {
  router.push(`/app/detail/${record.id}`)
}

const doDelete = (record: API.AppVO) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除应用「${record.appName}」吗？`,
    okType: 'danger',
    onOk: async () => {
      const res = await deleteAppByAdmin({ id: record.id })
      if (res.data.code === 0) {
        message.success('删除成功')
        fetchData()
      } else {
        message.error('删除失败：' + (res.data.message ?? ''))
      }
    },
  })
}

const doFeature = async (record: API.AppVO) => {
  try {
    const res = await updateAppByAdmin({
      id: record.id,
      priority: FEATURED_PRIORITY,
    })
    if (res.data.code === 0) {
      message.success('已设为精选')
      fetchData()
    } else {
      message.error('设置失败：' + (res.data.message ?? ''))
    }
  } catch {
    message.error('设置失败')
  }
}

const doUnfeature = async (record: API.AppVO) => {
  try {
    const res = await updateAppByAdmin({
      id: record.id,
      priority: 0,
    })
    if (res.data.code === 0) {
      message.success('已取消精选')
      fetchData()
    } else {
      message.error('取消失败：' + (res.data.message ?? ''))
    }
  } catch {
    message.error('取消失败')
  }
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.app-manage-page {
  padding: 0;
  max-width: 1400px;
  margin: 0 auto;
}
.app-manage-page :deep(.ant-form) {
  margin-bottom: 0;
}
</style>
