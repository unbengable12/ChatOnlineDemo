import axios from "axios";
import {ElMessage} from "element-plus";
import router from "@/router";

const authItemName = "access_token"

const defaultFailure = (message, code, url) => {
    console.warn(`请求地址：${url}，状态码：${code}，错误信息：${message}`)
    ElMessage.warning(message)
    if (code === 404) {
        router.push("/not-found")
    } else if (code === 500) {
        router.push("/error")
    }
}

const defaultError = (err) => {
    console.error(err)
    ElMessage.warning("发生了一些错误，请联系管理员")
}

function accessHeader() {
    const token = takeAccessToken();

    return token ? {
        'Authorization': `Bearer ${takeAccessToken()}`
    } : {}
}

function takeAccessToken() {
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName)
    if (!str)
        return null
    const authObj = JSON.parse(str)
    if (authObj.expire <= new Date()) {
        deleteAccessToken()
        ElMessage.warning("登陆状态已过期，请重新登陆")
        return null
    }
    return authObj.token
}

function storeAccessToken(token, remember, expire) {
    const authObj = {token: token, expire: expire}
    const str = JSON.stringify(authObj)
    if (remember)
        localStorage.setItem(authItemName, str)
    else
        sessionStorage.setItem(authItemName, str)
}

function deleteAccessToken() {
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
}

function internalPost(url, data, header, success, failure = defaultFailure, error = defaultError) {
    axios.post(url, data, {headers: header}).then(({data}) => {
        if (data.code === 200) {
            success(data.data)
        } else {
            failure(data.message, data.code, url);
        }
    }).catch(err => error(err))
}

function internGet(url, header, success, failure = defaultFailure, error = defaultError) {
    axios.get(url, {headers: header}).then(({data}) => {
        if (data.code === 200) {
            success(data.data)
        } else {
            failure(data.message, data.code, url);
        }
    }).catch(err => error(err))
}

function login(username, password, remember, success, failure, error = defaultError) {
    internalPost('/api/auth/login', {
        username: username,
        password: password,
    }, {
        'Content-Type': 'application/x-www-form-urlencoded'
    }, (data) => {
        storeAccessToken(data.token, remember, data.expire)
        ElMessage.success(`登陆成功，欢迎${data.username}来到我们系统`)
        success(data)
    }, failure)
}

function get(url, success, failure = defaultFailure) {
    internGet(url, accessHeader(), success, failure)
}

function post(url, data, success, failure = defaultFailure) {
    internalPost(url, data, accessHeader(), success, failure)
}

function logout(success, failure = defaultFailure) {
    get('api/auth/logout', () => {
        deleteAccessToken()
        ElMessage.success("退出登陆成功，欢迎您再次使用")
        success()
    }, failure)
}

function unauthorized() {
    return takeAccessToken() == null;
}

export {login, logout, get, post, unauthorized, takeAccessToken}