<template>
  <div class="userCenterPage">
    <h2 class="title">个人中心</h2>
    <div class="desc">编辑你的个人信息</div>
    <a-form
      :model="formState"
      name="basic"
      layout="vertical"
      autocomplete="off"
      @finish="handleSubmit"
    >
      <a-form-item label="账号">
        <a-input :value="formState.userAccount" disabled />
      </a-form-item>
      <a-form-item
        label="用户名"
        name="userName"
        :rules="[{ required: true, message: '请输入用户名!' }]"
      >
        <a-input v-model:value="formState.userName" placeholder="请输入用户名" />
      </a-form-item>
      <a-form-item label="头像" name="userAvatar">
        <a-input v-model:value="formState.userAvatar" placeholder="请输入头像 URL" />
        <a-image
          v-if="formState.userAvatar"
          :src="formState.userAvatar"
          :width="80"
          style="margin-top: 8px"
        />
      </a-form-item>
      <a-form-item label="简介" name="userProfile">
        <a-textarea v-model:value="formState.userProfile" placeholder="请输入简介" :rows="4" />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" block>保存</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { updateMyUser } from '@/api/userController'
import { useLoginUserStore } from '@/stores/LoginUser'
import { useRouter } from 'vue-router'

const loginUserStore = useLoginUserStore()
const router = useRouter()

const formState = reactive<{
  id?: number
  userAccount?: string
  userName?: string
  userAvatar?: string
  userProfile?: string
}>({
  userName: '',
  userAvatar: '',
  userProfile: '',
})

onMounted(async () => {
  // 确保已获取登录用户信息
  if (!loginUserStore.loginUser.id) {
    await loginUserStore.fetchLoginUser()
  }
  const loginUser = loginUserStore.loginUser
  if (!loginUser.id) {
    message.warning('请先登录')
    router.replace('/user/login')
    return
  }
  formState.id = loginUser.id
  formState.userAccount = loginUser.userAccount
  formState.userName = loginUser.userName
  formState.userAvatar = loginUser.userAvatar
  formState.userProfile = loginUser.userProfile
})

/**
 * 保存个人信息
 */
const handleSubmit = async () => {
  // 仅提交个人可编辑字段，不携带 userRole，避免越权改角色
  const res = await updateMyUser({
    id: formState.id,
    userName: formState.userName,
    userAvatar: formState.userAvatar,
    userProfile: formState.userProfile,
  })
  if (res.data.code === 0) {
    message.success('保存成功')
    // 刷新登录用户信息，使顶部头像/昵称同步更新
    await loginUserStore.fetchLoginUser()
    router.push('/')
  } else {
    message.error('保存失败：' + res.data.message)
  }
}
</script>

<style scoped>
.userCenterPage {
  max-width: 480px;
  margin: 0 auto;
}

.title {
  text-align: center;
  margin-bottom: 16px;
}

.desc {
  text-align: center;
  color: #bbb;
  margin-bottom: 16px;
}
</style>
