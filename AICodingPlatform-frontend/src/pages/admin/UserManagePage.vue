<template>
  <div id="userManagePage">
    <!-- 搜索表单 -->
    <a-form layout="inline" :model="searchParams" @finish="doSearch">
      <a-form-item label="账号">
        <a-input v-model:value="searchParams.userAccount" placeholder="输入账号" />
      </a-form-item>
      <a-form-item label="用户名">
        <a-input v-model:value="searchParams.userName" placeholder="输入用户名" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <a-divider />
    <!-- 表格 -->
    <a-table
      :columns="columns"
      :data-source="data"
      :pagination="pagination"
      @change="doTableChange"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-input
            v-if="editableData[record.id]"
            :value="editableData[record.id]?.userAvatar"
            placeholder="头像 URL"
            @update:value="(val: string) => setEditableField(record.id, 'userAvatar', val)"
          />
          <a-image v-else :src="record.userAvatar" :width="120" />
        </template>
        <template v-else-if="column.dataIndex === 'userName'">
          <a-input
            v-if="editableData[record.id]"
            :value="editableData[record.id]?.userName"
            @update:value="(val: string) => setEditableField(record.id, 'userName', val)"
          />
          <span v-else>{{ record.userName }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'userProfile'">
          <a-input
            v-if="editableData[record.id]"
            :value="editableData[record.id]?.userProfile"
            @update:value="(val: string) => setEditableField(record.id, 'userProfile', val)"
          />
          <span v-else>{{ record.userProfile }}</span>
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <a-select
            v-if="editableData[record.id]"
            :value="editableData[record.id]?.userRole"
            style="width: 120px"
            :options="roleOptions"
            @update:value="(val: string) => setEditableField(record.id, 'userRole', val)"
          />
          <template v-else>
            <div v-if="record.userRole === 'admin'">
              <a-tag color="green">管理员</a-tag>
            </div>
            <div v-else>
              <a-tag color="blue">普通用户</a-tag>
            </div>
          </template>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ formatDateTime(record.createTime) }}
        </template>
        <template v-else-if="column.key === 'action'">
          <div v-if="editableData[record.id]" class="editable-row-actions">
            <a-button type="link" @click="save(record.id)">保存</a-button>
            <a-button type="link" @click="cancel(record.id)">取消</a-button>
          </div>
          <a-space v-else>
            <a-button type="link" @click="edit(record.id)">编辑</a-button>
            <a-button danger @click="doDelete(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { listUserVoByPage, deleteUser, updateUser } from '@/api/userController'
import { message } from 'ant-design-vue'
import { ref, onMounted, reactive, computed } from 'vue'
import { formatDateTime } from '@/utils/date'

const roleOptions = [
  { label: '管理员', value: 'admin' },
  { label: '普通用户', value: 'user' },
]

const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]

// 数据
const data = ref<API.UserVO[]>([])
const total = ref(0)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 正在编辑的行数据，按记录 id 索引
const editableData = reactive<Record<number, API.UserUpdateRequest>>({})

// 更新指定行的编辑字段（索引结果可能不存在，需做空值保护）
const setEditableField = (
  id: number,
  field: 'userAvatar' | 'userName' | 'userProfile' | 'userRole',
  value: string,
) => {
  const target = editableData[id]
  if (target) {
    target[field] = value
  }
}

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPage({
    ...searchParams,
  })
  if (res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.totalRow ?? 0
  } else {
    message.error('获取数据失败，' + res.data.message)
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})

// 分页参数
const pagination = computed(() => {
  return {
    current: searchParams.pageNum ?? 1,
    pageSize: searchParams.pageSize ?? 10,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条`,
  }
})

// 表格变化处理
const doTableChange = (page: { current?: number; pageSize?: number }) => {
  searchParams.pageNum = page.current
  searchParams.pageSize = page.pageSize
  fetchData()
}

// 搜索处理
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  fetchData()
}

// 删除数据
const doDelete = async (id: number) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id })
  if (res.data.code === 0) {
    message.success('删除成功')
    // 刷新数据
    fetchData()
  } else {
    message.error('删除失败')
  }
}

// 进入编辑：将当前行数据拷贝一份到 editableData
const edit = (id: number) => {
  const target = data.value.find((item) => item.id === id)
  if (!target) {
    return
  }
  editableData[id] = {
    id,
    userName: target.userName,
    userAvatar: target.userAvatar,
    userProfile: target.userProfile,
    userRole: target.userRole,
  }
}

// 保存编辑
const save = async (id: number) => {
  const updateData = editableData[id]
  if (!updateData) {
    return
  }
  const res = await updateUser(updateData)
  if (res.data.code === 0) {
    message.success('更新成功')
    delete editableData[id]
    fetchData()
  } else {
    message.error('更新失败：' + res.data.message)
  }
}

// 取消编辑
const cancel = (id: number) => {
  delete editableData[id]
}
</script>

<style scoped>
.editable-row-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
