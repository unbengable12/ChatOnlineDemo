<script setup>
import {computed, nextTick, ref, watch} from 'vue'
import {Avatar, Message} from '@element-plus/icons-vue'
import {useUserInfoStore} from "@/store/UserInfo";
import {useOnlineUserStore} from "@/store/OnlineUser";
import {useNewMessageStore} from "@/store/NewMessage";
import {get, post} from "@/net";

const userInfoStore = useUserInfoStore()
const onlineUserStore = useOnlineUserStore()
const newMessageStore = useNewMessageStore()

const user = ref({
  id: "",
  username: ""
})
const nowChatUser = ref({
  id: "0",
  username: "ALL"
})

init()

async function init() {
  await nextTick()
  user.value.id = userInfoStore.userInfo.userId
  user.value.username = userInfoStore.userInfo.username
  setScrollToBottom()
  console.log(user.value)
}

const item = {
  date: '2016-05-02',
  name: 'Tom',
  address: 'No. 189, Grove St, Los Angeles',
}
const scrollbarRef = ref()
const innerRef = ref()
const scroll = ({scrollTop}) => {
  // console.log(scrollTop)
}

// 设置滚动条到底部
function setScrollToBottom() {
  scrollbarRef.value.setScrollTop(innerRef.value.scrollHeight);
}

const messageList = ref([])

function updateNowChatUser(item) {
  nowChatUser.value.id = item.id
  nowChatUser.value.username = item.username
  getHistoryMessage().then(() => {
   setTimeout(() => {
     setScrollToBottom()
   }, 50)
  })
  console.log(messageList.value)
  updateStatus()
}

async function getHistoryMessage() {
  await get(`/api/message/getHistoryMessage/${user.value.id}/${nowChatUser.value.id}`, (data) => {
    messageList.value = JSON.parse(data)
  })
}

const usernamesMap = computed(() => {
  const map = {
    "0": "ALL"
  }
  for (let i = 0; i < onlineUserStore.commonUserList.length; i++) {
    map[onlineUserStore.commonUserList[i].id] = onlineUserStore.commonUserList[i].username
  }
  for (let i = 0; i < onlineUserStore.hasNewUserList.length; i++) {
    map[onlineUserStore.hasNewUserList[i]] = onlineUserStore.hasNewUserList[i]
  }
  return map;
})

const inputContent = ref("")

async function sendMessage() {
  await post("/api/message/send", {
    content: inputContent.value,
    rid: nowChatUser.value.id,
    sid: user.value.id
  }, (data) => {
    messageList.value.push({
      content: inputContent.value,
      sid: user.value.id,
      rid: nowChatUser.value.id,
      sendTime: formatDate(new Date())
    })
    setTimeout(() => {
      setScrollToBottom()
    }, 50)
    inputContent.value = ""
  })
}


function formatDate(date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0'); // 月份从 0 开始
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

const newMessage = computed(() => {
  return newMessageStore.newMessage
})

async function updateStatus() {
  await get(`/api/message/updateStatus/${user.value.id}/${nowChatUser.value.id}`, () => {
    getOnlineUsers()
  })
}

const getOnlineUsers = async () => {
  const userId = userInfoStore.userInfo.userId
  await get(`/api/message/getOnlineUser/${userId}`, (data) => {
    console.log(data)
    const onlineUsers = JSON.parse(data)
    let commonUserList = []
    let hasNewUserList = []
    for (let i = 0; i < onlineUsers.length; i++) {
      console.log(onlineUsers[i])
      if (onlineUsers[i].id === 0) {
        onlineUserStore.allUser = onlineUsers[i]
        continue
      }
      if (onlineUsers[i].unreadCount > 0) {
        hasNewUserList.push(onlineUsers[i])
      } else {
        commonUserList.push(onlineUsers[i])
      }
    }
    onlineUserStore.hasNewUserList = hasNewUserList
    onlineUserStore.commonUserList = commonUserList
    console.log(onlineUserStore.allUser)
    console.log(onlineUserStore.hasNewUserList)
    console.log(onlineUserStore.commonUserList)
  })
}

watch(newMessage, (value, oldValue, onCleanup) => {
  console.log("newMessage")
  console.log(user.value)
  console.log(nowChatUser.value)
  if (newMessage.value.rid == 0 && nowChatUser.value.id == 0 && newMessage.value.sid != user.value.id) {
    console.log("ALL")
    messageList.value.push({
      content: newMessage.value.content,
      sid: newMessage.value.sid,
      rid: newMessage.value.rid,
      sendTime: formatDate(new Date())
    })
  }
  if (newMessage.value.rid == user.value.id && newMessage.value.sid == nowChatUser.value.id) {
    console.log("Single")
    messageList.value.push({
      content: newMessage.value.content,
      sid: newMessage.value.sid,
      rid: newMessage.value.rid,
      sendTime: formatDate(new Date())
    })
  }
  setTimeout(() => {
    setScrollToBottom()
  }, 50)
}, {deep: true})


</script>

<template>
  <div style="height: calc(100vh - 120px); overflow: hidden; background-color: #f5f5f5;">
    <el-container class="layout-container-demo" style="height: 100%;">
      <el-aside width="200px" style="background-color: white">
        <el-scrollbar>
          <el-menu :default-openeds="['1']">
            <el-sub-menu index="1">
              <template #title>
                <el-icon>
                  <message/>
                </el-icon>
                在线用户
              </template>
              <el-menu-item @click="updateNowChatUser(onlineUserStore.allUser)">
                <el-icon>
                  <Avatar/>
                </el-icon>
                {{ onlineUserStore.allUser.username }}
                <el-badge style="left: 10px; bottom: 16px" :value="onlineUserStore.allUser.unreadCount" :max="99"/>
              </el-menu-item>
              <el-menu-item
                  @click="updateNowChatUser(item)"
                  v-for="item in onlineUserStore.hasNewUserList">
                <el-icon>
                  <Avatar/>
                </el-icon>
                {{ item.username }}
                <el-badge style="left: 10px; bottom: 16px" :value="item.unreadCount" :max="99" v-if="item.unreadCount > 0"/>
              </el-menu-item>
              <el-menu-item
                  @click="updateNowChatUser(item)"
                  v-for="item in onlineUserStore.commonUserList">
                <el-icon>
                  <Avatar/>
                </el-icon>
                {{ item.username }}
                <el-badge style="left: 10px; bottom: 16px" :value="item.unreadCount" :max="99" v-if="item.unreadCount > 0"/>
              </el-menu-item>
            </el-sub-menu>
          </el-menu>
        </el-scrollbar>
      </el-aside>
      <el-container>
        <el-header style="text-align: center; font-size: 24px; background-color: #f0f2f5;">
          <div class="toolbar">
            <span>{{ `Chat with ${nowChatUser.username}` }}</span>
          </div>
        </el-header>
        <el-main>
          <el-scrollbar ref="scrollbarRef" always @scroll="scroll">
            <div ref="innerRef">
              <div class="message-list" v-for="item in messageList">
                <div class="message message-sent" v-if="''+item.sid === user.id">
                  <div style="font-size: 12px; color: #888; margin-bottom: 5px;">
                    you
                  </div>
                  <div class="message-bubble">
                    <div>{{ item.content }}</div>
                  </div>
                  <div class="message-info">
                    <span>{{ item.sendTime }}</span>
                  </div>
                </div>
                <div class="message message-received" v-else>
                  <div style="font-size: 12px; color: #888; margin-bottom: 5px;">
                    {{ usernamesMap[item.sid] }}
                  </div>
                  <div class="message-bubble">
                    <div>{{ item.content }}</div>
                  </div>
                  <div class="message-info">
                    <span>{{ item.sendTime }}</span>
                  </div>
                </div>
              </div>
            </div>

          </el-scrollbar>
        </el-main>
        <el-footer style="align-content: center; background-color: #f0f2f5;">
          <el-row :gutter="10">
            <el-col :span="22">
              <el-input type="text" v-model="inputContent" placeholder="请输入消息内容"/>
            </el-col>
            <el-col :span="2">
              <el-button type="success" @click="sendMessage">发送</el-button>
            </el-col>
          </el-row>
        </el-footer>
      </el-container>
    </el-container>
  </div>
</template>

<style scoped>
.layout-container-demo .el-header {
  position: relative;
  background-color: var(--el-color-primary-light-7);
  color: var(--el-text-color-primary);
}

.layout-container-demo .el-aside {
  color: var(--el-text-color-primary);
  background: var(--el-color-primary-light-8);
}

.layout-container-demo .el-menu {
  border-right: none;
}

.layout-container-demo .el-main {
  padding: 0;
}

.layout-container-demo .toolbar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.message-list {
  flex-grow: 1;
  overflow-y: auto;
  padding: 10px;
  display: flex;
  flex-direction: column;
}

.message {
  max-width: 70%;
  margin-bottom: 15px;
  clear: both;
}

.message-sent {
  align-self: flex-end;
  text-align: right;
}

.message-received {
  align-self: flex-start;
  text-align: left;
}

.message-bubble {
  border-radius: 10px;
  padding: 10px;
  max-width: 100%;
  word-wrap: break-word;
  position: relative;
}

.message-sent .message-bubble {
  background-color: #a1e65d;
  color: black;
  margin-left: auto;
}

.message-received .message-bubble {
  background-color: white;
  color: black;
  margin-right: auto;
}

.message-sent .message-bubble::after {
  content: '';
  position: absolute;
  right: -10px;
  top: 10px;
  border: 5px solid transparent;
  border-left-color: #a1e65d;
}

.message-received .message-bubble::before {
  content: '';
  position: absolute;
  left: -10px;
  top: 10px;
  border: 5px solid transparent;
  border-right-color: white;
}

.message-info {
  font-size: 12px;
  color: #888;
  margin-top: 5px;
}
</style>