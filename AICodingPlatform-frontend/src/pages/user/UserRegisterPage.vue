<template>
  <div class="UserRegisterPage">
    <h2 class="title">AI应用生成平台-用户注册</h2>
    <div class="desc">注册账号以开始使用</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入用户账号!' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入用户账号" />
      </a-form-item>

      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入用户密码!' },
          { min: 8, message: '密码长度不能小于8位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入用户密码" />
      </a-form-item>

      <a-form-item
        name="checkPassword"
        :rules="[
          { required: true, message: '请输入确认密码!' },
          { min: 8, message: '密码长度不能小于8位' },
          { validator: validateCheckPassword },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请输入确认密码" />
      </a-form-item>

      <div class="tips">已有账号？<RouterLink to="/user/login">去登录</RouterLink></div>

      <a-form-item>
        <a-button type="primary" html-type="submit" block>注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { message } from 'ant-design-vue'
import { register } from '@/api/userController'
import { useRouter } from 'vue-router'

const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const router = useRouter()

/**
 * 校验确认密码是否与用户密码一致
 */
const validateCheckPassword = async (_rule: unknown, value: string) => {
  if (value !== formState.userPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

/**
 * 用户注册提交
 * @param values 注册表单数据
 */
const handleSubmit = async (values: API.UserRegisterRequest) => {
  const res = await register(values)
  // 注册成功
  if (res.data.code === 0 && res.data.data) {
    message.success('注册成功')
    router.push({
      path: '/user/login',
      replace: true,
    })
  } else {
    message.error('注册失败：' + res.data.message)
  }
}
</script>

<style>
.UserRegisterPage {
  max-width: 360px;
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

.tips {
  margin-bottom: 16px;
  color: #bbb;
  font-size: 13px;
  text-align: right;
}
</style>
