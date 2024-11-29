<template>
  <el-menu
      style="padding: 0 16px"
      :default-active="activeIndex"
      class="el-menu-demo"
      mode="horizontal"
      :ellipsis="false"
      @select="handleSelect"
  >
    <el-menu-item index="0">
      <img
          style="width: 100px"
          src="@/assets/img.png"
          alt="Element logo"
      />
    </el-menu-item>
    <el-menu-item index="1" @click="router.push('/index')">首页</el-menu-item>
    <el-menu-item index="2" @click="router.push('/about')">关于</el-menu-item>
    <el-menu-item index="3" @click="showClick">
      <el-dropdown ref="dropdown1" trigger="contextmenu">
        <div style="display: flex; justify-content: center; align-items: center; ">
          <el-avatar>user</el-avatar>
          <span style="margin-left: 8px;">{{ userInfo.userInfo ? userInfo.userInfo.username : '未登录' }}</span>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>查看</el-dropdown-item>
            <el-dropdown-item @click="userLogout">退出</el-dropdown-item>
            <el-dropdown-item>Action 3</el-dropdown-item>
            <el-dropdown-item disabled>Action 4</el-dropdown-item>
            <el-dropdown-item divided>Action 5</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </el-menu-item>
  </el-menu>
</template>

<script setup>
import {ref, nextTick} from 'vue'
import router from "@/router";
import {logout} from "@/net";
import {useUserInfoStore} from "@/store/UserInfo";

const props = defineProps({
  stop: {
    type: Function,
    required: true
  }
})

const userInfo = useUserInfoStore();

function userLogout() {
  logout(() => {
    stop()
    nextTick(() => {
      router.push('/')
    })

  })
}

const activeIndex = ref('1')
const handleSelect = (key, keyPath) => {
  activeIndex.value = key
  console.log(key, keyPath)
}

const dropdown1 = ref()

function showClick() {
  if (!dropdown1.value) return
  dropdown1.value.handleOpen()
}
</script>

<style>
.el-menu-demo :hover {
  background-color: white !important;
}

.el-menu--horizontal > .el-menu-item:nth-child(3) {
  margin-right: auto;
}
</style>
