<template>
 <div style="text-align: center">
  <div style="margin-top: 30px">
   <el-steps :active="active" finish-status="success" align-center>
    <el-step title="验证电子邮件"></el-step>
    <el-step title="重新设立密码"></el-step>
   </el-steps>
  </div>
  <div style="margin-top: 80px">
   <div style="margin: 0 20px" v-if="active === 0">
    <h2>重置密码</h2>
    <div style="font-size: 14px; color: grey">请输入要重置用户的邮箱地址</div>
    <div style="margin-top: 50px">
     <el-form :model="form" :rules="rule" ref="formRef">
      <el-form-item prop="email">
       <el-input v-model="form.email" type="text" maxlength="20" placeholder="请输入电子邮箱地址">
        <template #prefix>
         <el-icon><Message/></el-icon>
        </template>
       </el-input>
      </el-form-item>
      <el-form-item prop="code">
       <el-row :gutter="10" style="width: 100%">
        <el-col :span="17">
         <el-input v-model="form.code" maxlength="6" type="text" placeholder="验证码">
          <template #prefix>
           <el-icon><EditPen/></el-icon>
          </template>
         </el-input>
        </el-col>
        <el-col :span="5">
         <el-button :disabled="!isEmailValid || coldTime !== 0" @click="askCode" type="success" plain>
          {{ coldTime > 0 ? `请稍后 ${coldTime} 秒` : `获取验证码` }}
         </el-button>
        </el-col>
       </el-row>
      </el-form-item>
     </el-form>
    </div>
    <div style="margin-top: 80px">
     <el-button @click="confirmReset" style="width: 270px" type="warning" plain>开始重置密码</el-button>
    </div>
   </div>
   <div style="margin: 0 20px" v-if="active === 1">
    <h2>重置密码</h2>
    <div style="font-size: 14px; color: grey">请填写你的新密码，务必牢记防止丢失</div>
    <div style="margin-top: 50px">
     <el-form :model="form" :rules="rule" ref="formRef">
      <el-form-item prop="password">
       <el-input v-model="form.password" maxlength="20" type="password" placeholder="密码">
        <template #prefix>
         <el-icon><Lock/></el-icon>
        </template>
       </el-input>
      </el-form-item>
      <el-form-item prop="password_repeat">
       <el-input v-model="form.password_repeat" maxlength="20" type="password" placeholder="重复密码">
        <template #prefix>
         <el-icon><Lock/></el-icon>
        </template>
       </el-input>
      </el-form-item>
     </el-form>
    </div>
    <div style="margin-top: 80px">
     <el-button @click="doReset" style="width: 270px" type="danger" plain>立即重置密码</el-button>
    </div>
   </div>
  </div>
 </div>
</template>

<script setup>
import {ref, reactive, computed} from "vue";
import {EditPen, Lock, Message} from "@element-plus/icons-vue";
import {ElMessage} from "element-plus";
import {get, post} from "@/net";
import router from "@/router";

const active = ref(0)

const formRef = ref()
const form = reactive({
  email: '',
  code: '',
  password: '',
  password_repeat: ''
})

const validatePassword = (rule, value, callback) => {
  if (value === '') {
    callback(new Error("请再次输入密码"))
  } else if (value !== form.password) {
    callback(new Error("两次输入的密码不一致"))
  } else {
    callback()
  }
}

const rule = {
  email: [
    { required: true, message: "请输入邮箱地址", trigger: 'blur'},
    { type: "email", message: "请输入合法的邮箱地址", trigger: ['blur', 'change'] }
  ],
  code: [
    { required: true, message: "请输入验证码", trigger: 'blur'}
  ],
  password: [
    { required: true, message: "请输入密码", trigger: 'blur'},
    { min: 6, max: 20, message: "密码的长度必须在6-20个字符之间", trigger: ['blur', 'change']}
  ],
  password_repeat: [
    { validator: validatePassword, trigger: ['blur', 'change']},
  ]
}

const isEmailValid = computed(()=>{
  return /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(form.email)
})
const coldTime = ref(0)
let timer = null
function askCode() {
  if (!isEmailValid) {
    ElMessage.warning("请输入正确的邮箱地址")
    return
  }
  coldTime.value = 60
  get(`/api/auth/ask-code?email=${form.email}&type=register`, () => {
    ElMessage.success("验证码已发送到邮箱，请注意查收")
    timer = setInterval(() => {
      coldTime.value --
      if (coldTime.value <= 0) {
        clearInterval(timer)
      }
    }, 1000, () => {
      coldTime.value = 0
    })
  }, () => {
    coldTime.value = 0
  })
}
function confirmReset() {
  formRef.value.validate((valid) => {
    if (valid) {
      post('/api/auth/resetConfirm', {
        email: form.email,
        code: form.code
      }, () => {
        active.value ++
      })
    }
  })
}

function doReset() {
  formRef.value.validate((valid) => {
    if (valid) {
      post('/api/auth/reset-password', {
        email: form.email,
        code: form.code,
        password: form.password
      }, () => {
        active.value ++
        ElMessage.success("密码重置成功，请重新登陆")
        router.push('/')
      })
    }
  })
}

</script>

<style scoped>

</style>

