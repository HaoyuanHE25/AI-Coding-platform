<script setup lang="ts">

</script>

<template>
  <main>
    <div class="title">未授权页面</div>
    <div class="desc">您没有权限访问该页面</div>
    <div class="tips">请联系管理员获取权限</div>
  </main>
</template>

<style scoped>
main {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 50vh;
}

div.title {
  font-size: 80px;
  font-weight: bold;
  margin-bottom: 20px;
}

div.desc {
  font-size: 18px;
  margin-bottom: 20px;
}

div.tips {
  font-size: 18px;
}
</style>
