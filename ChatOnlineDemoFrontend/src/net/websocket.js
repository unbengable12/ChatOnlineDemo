import {onBeforeUnmount, ref} from 'vue'
import {get, unauthorized} from "@/net/index";
import {useUserInfoStore} from "@/store/UserInfo";
import {useOnlineUserStore} from "@/store/OnlineUser";
import {useNewMessageStore} from "@/store/NewMessage";
import {ElNotification} from "element-plus";

const userInfoStore = useUserInfoStore()
const onlineUserStore = useOnlineUserStore()
const newMessageStore = useNewMessageStore()
const url = 'ws://localhost:8888/ws?token=';

export const useWebSocket = (config) => {

    const socket = ref(null)
    const isConnected = ref(false)

    const reconnectInterval = config.reconnectInterval || 3000 // 重连间隔
    const maxReconnectAttempts = config.maxReconnectAttempts || 5 // 最大尝试次数
    const heartBeatInterval = config.heartBeatInterval || 5000 // 心跳间隔

    let reconnectAttempts = 0 // 重连次数

    const heartBeat = ref(null)
    const connect = () => {
        if (!unauthorized())
            socket.value = new WebSocket(url + config.token)

        socket.value.addEventListener('open', () => {
            console.log('WebSocket connected')
            isConnected.value = true
            reconnectAttempts = 0
            startHeartBeat()
        })

        socket.value.addEventListener('message', (event) => {
            console.log('Received message:', event.data)
            const message = JSON.parse(event.data)
            console.log(message)
            if (message.messageType === 'NEW_LOGIN') {
                getOnlineUsers()
                open2(message.data)
            } else if (message.messageType === 'NEW_LOGOUT') {
                // message.data 是退出的用户 id 可以用来更新在线用户列表
                getOnlineUsers()
            } else if (message.messageType === 'NEW_MESSAGE') {
                // message.data 是新的消息，可以用来直接更新
                getOnlineUsers()
                newMessageStore.setNewMessage(message.data)
                console.log(newMessageStore.newMessage)
            }
        })

        socket.value.addEventListener('close', (event) => {
            console.log('WebSocket closed')
            isConnected.value = false

            if (!unauthorized() && (maxReconnectAttempts === null || reconnectAttempts < maxReconnectAttempts)) {
                setTimeout(() => {
                    console.log('Reconnecting...')
                    reconnectAttempts++
                    connect()
                }, reconnectInterval);
            }
        })

        socket.value.addEventListener('error', (event) => {
            console.log('websocket error:', event)
        })
    }
    const open2 = (username) => {
        const title = `${username} 已上线`
        ElNotification({
            title: title,
            position: 'bottom-right',
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

    const sendMessage = (message) => {
        if (socket.value && socket.value.readyState === WebSocket.OPEN) {
            socket.value.send(message)
        }
    }

    const startHeartBeat = () => {
        heartBeat.value = setInterval(() => {
            sendMessage("heart beat")
        }, heartBeatInterval)
    }

    const start = async () => {
        await connect()
    }

    const stop = () => {
        if (socket) {
            socket.value.close()
        }
        if (heartBeat) {
            clearInterval(heartBeat.value)
        }
    }

    onBeforeUnmount(() => {
        stop()
    })
    return {
        socket,
        isConnected,
        sendMessage,
        start,
        stop
    }
}