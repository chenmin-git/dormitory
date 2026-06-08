<template>
  <div class="login">
    <!-- 左侧品牌面板 -->
    <div class="brand">
      <div class="brand-inner">
        <div class="mark"><Logo :size="38" /></div>
        <h1 class="brand-title display-font">智能宿舍<br />管理系统</h1>
        <p class="brand-sub">让宿舍管理更高效、更智能</p>
        <ul class="brand-points">
          <li><span></span>楼栋床位 · 入住分配 · 一站可视</li>
          <li><span></span>报修 · 请假 · 卫生 · 流程闭环</li>
          <li><span></span>AI 智能助手，对话即可办理业务</li>
        </ul>
      </div>
      <div class="brand-foot">SpringBoot · Vue3 · AI 智能助手</div>
      <div class="brand-glow"></div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="panel">
      <div class="form-box">
        <p class="welcome">欢迎回来 👋</p>
        <h2 class="form-title display-font">账号登录</h2>
        <el-form ref="formRef" :model="form" :rules="rules" @keyup.enter="handleLogin" size="large">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="请输入账号" :prefix-icon="User" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码"
                      :prefix-icon="Lock" show-password />
          </el-form-item>
          <el-button type="primary" size="large" class="submit" :loading="loading" @click="handleLogin">
            登 录
          </el-button>
        </el-form>

        <div class="tips">
          <span class="tips-title">测试账号</span>
          <div class="chips">
            <button class="chip" @click="fill('admin')">超管 admin</button>
            <button class="chip" @click="fill('manager1')">宿管 manager1</button>
            <button class="chip" @click="fill('stu001')">学生 stu001</button>
          </div>
          <span class="tips-note">密码均为 123456，点击可快速填充</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import Logo from '@/components/Logo.vue'
import { authApi } from '@/api'
import { useUserStore } from '@/store/user'
import { firstAccessiblePath } from '@/router'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({ username: 'admin', password: '123456' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const fill = (name) => {
  form.username = name
  form.password = '123456'
}

const handleLogin = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await authApi.login(form)
      userStore.setUser(res.data)
      ElMessage.success('登录成功')
      router.push(firstAccessiblePath(res.data.role))
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login {
  height: 100vh;
  display: flex;
  background: var(--bg);
}

/* ---- 左侧品牌 ---- */
.brand {
  position: relative;
  flex: 0 0 46%;
  background:
    radial-gradient(120% 80% at 80% 10%, #2c3a34 0%, transparent 55%),
    linear-gradient(160deg, #1c2420 0%, #16201c 100%);
  color: #eef2ee;
  padding: 64px 56px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  overflow: hidden;
}
.brand-inner {
  position: relative;
  z-index: 2;
}
.mark {
  width: 60px;
  height: 60px;
  border-radius: 16px;
  background: var(--primary);
  color: #fff;
  font-family: 'Fraunces', 'Songti SC', serif;
  font-size: 30px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 8px 24px rgba(21, 103, 96, 0.45);
  margin-bottom: 36px;
}
.brand-title {
  font-size: 46px;
  line-height: 1.15;
  font-weight: 600;
  letter-spacing: 1px;
  margin: 0 0 18px;
}
.brand-sub {
  color: #aab8b1;
  font-size: 16px;
  margin: 0 0 40px;
}
.brand-points {
  list-style: none;
  padding: 0;
  margin: 0;
}
.brand-points li {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #cdd6d0;
  font-size: 15px;
  margin-bottom: 16px;
}
.brand-points li span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--accent);
  flex: none;
}
.brand-foot {
  position: relative;
  z-index: 2;
  color: #75827b;
  font-size: 13px;
  letter-spacing: 0.5px;
}
.brand-glow {
  position: absolute;
  right: -120px;
  bottom: -120px;
  width: 360px;
  height: 360px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(194, 104, 58, 0.28) 0%, transparent 70%);
  z-index: 1;
}

/* ---- 右侧表单 ---- */
.panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}
.form-box {
  width: 100%;
  max-width: 380px;
}
.welcome {
  color: var(--muted);
  font-size: 15px;
  margin: 0 0 6px;
}
.form-title {
  font-size: 32px;
  font-weight: 600;
  color: var(--ink);
  margin: 0 0 32px;
}
.submit {
  width: 100%;
  margin-top: 6px;
  letter-spacing: 4px;
  height: 46px;
}
.tips {
  margin-top: 36px;
  padding-top: 24px;
  border-top: 1px dashed var(--line);
}
.tips-title {
  display: block;
  font-size: 13px;
  color: var(--ink-soft);
  font-weight: 600;
  margin-bottom: 12px;
}
.chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.chip {
  border: 1px solid var(--line);
  background: var(--bg-soft);
  color: var(--ink-soft);
  border-radius: 20px;
  padding: 6px 14px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.chip:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--primary-050);
}
.tips-note {
  display: block;
  margin-top: 12px;
  font-size: 12px;
  color: var(--muted);
}

@media (max-width: 860px) {
  .brand {
    display: none;
  }
}
</style>
