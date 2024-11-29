import {defineStore} from 'pinia'
import {ref} from 'vue'

export const useOnlineUserStore = defineStore('onlineUser', () => {

    const allUser = ref({
        id: '',
        username: '',
        unreadCount: 0
    })
    const commonUserList = ref([])
    const hasNewUserList = ref([])

    return {allUser, commonUserList, hasNewUserList}
})