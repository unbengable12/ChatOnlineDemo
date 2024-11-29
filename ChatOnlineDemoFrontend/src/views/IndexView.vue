<template>
  <div id="index">
    <el-container class="container">
      <el-header class="header">
        <Navbar :stop="stop"/>
      </el-header>
      <el-main class="content">
        <el-scrollbar height="calc(100vh - 120px)">
          <router-view/>
        </el-scrollbar>
      </el-main>
      <el-footer class="footer">
        <Footer/>
      </el-footer>
    </el-container>
  </div>
</template>

<script setup>
import Navbar from "@/views/index/Navbar.vue";
import Footer from "@/views/index/Footer.vue";
import {useWebSocket} from "@/net/websocket"
import {takeAccessToken} from "@/net";

const {socket, isConnected, sendMessage, start, stop} = useWebSocket({
  token: `Bearer ${takeAccessToken()}`
});
start()
</script>

<style scoped>
#index .container {
  width: 100%;
  height: 100%;
  padding: 0;
  margin: 0;
}

#index .header {
  width: 100%;
  height: 60px;
  padding: 0;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;
}

#index .content {
  width: 100%;
  height: 100%;
  padding: 60px 0;
}

#index .footer {
  width: 100%;
  height: 60px;
  padding: 0;
  position: fixed;
  bottom: 0;
  left: 0;
  z-index: 1000;
}


</style>

