import {defineStore} from 'pinia'
import {ref} from 'vue'

export const useNewMessageStore = defineStore('newMessage', () => {
    const newMessage = ref({
        "content": "",
        "id": -1,
        "rid": -1,
        "sendTime": "",
        "sid": -1,
        "status": 0,
    })
    const isReset =  ref(true)
    function setNewMessage(message) {
        newMessage.value.content = message.content
        newMessage.value.id = message.id
        newMessage.value.rid = message.rid
        newMessage.value.sendTime = message.sendTime
        newMessage.value.sid = message.sid
        newMessage.value.status = message.status
        isReset.value = false
    }

    function reset() {
        newMessage.value = {
            "content": "",
            "id": -1,
            "rid": -1,
            "sendTime": "",
            "sid": -1,
            "status": 0
        }
        isReset.value = true
    }

    return {newMessage, isReset, setNewMessage, reset}
})