<template>
 <div style="width: 100vw; height: 100vh; overflow:hidden; display: flex">
  <div style="flex: 1; background-color: black">
   <el-image style="width: 100%; height: 100%" fit="cover"
     src="https://img-baofun.zhhainiao.com/fs/cfbf7c21639698479b755e7fba4565a1.jpg"></el-image>
   <div class="welcome-title">
    <h3>欢迎来到我们的在线聊天室</h3>
   </div>
  </div>

  <div class="right-card">
   <router-view v-slot="{ Component }">
    <transition name="el-fade-in-linear" mode="out-in">
     <component :is="Component"/>
    </transition>
   </router-view>
  </div>
 </div>
</template>

<script setup>

</script>

<style scoped>
.right-card {
    width: 400px;
    z-index: 1;
    background-color: var(--el-bg-color);
}
.welcome-title {
    position: absolute;
    bottom: 30px;
    left: 30px;
}
</style>

