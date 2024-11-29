import {createApp} from 'vue'
import App from './App.vue'
import router from "@/router";
import axios from "axios";
import 'element-plus/dist/index.css';
import { createPinia } from 'pinia'

axios.defaults.baseURL = 'http://localhost:8080'

const app = createApp(App)
const pinia = createPinia()

app.use(router)
app.use(pinia)
app.mount("#app")
