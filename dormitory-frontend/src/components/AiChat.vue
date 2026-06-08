<template>
  <!-- 悬浮按钮 -->
  <div class="ai-fab" @click="visible = true" v-show="!visible">
    <el-icon :size="24"><ChatDotRound /></el-icon>
    <span class="fab-dot"></span>
  </div>

  <!-- 聊天窗口 -->
  <transition name="slide">
    <div class="ai-chat" v-show="visible">
      <div class="ai-header">
        <div class="ai-title">
          <el-icon><MagicStick /></el-icon>
          <span>宿舍智能助手</span>
        </div>
        <el-icon class="close-btn" @click="visible = false"><Close /></el-icon>
      </div>

      <div class="ai-body" ref="bodyRef">
        <div v-for="(msg, idx) in messages" :key="idx" :class="['msg', msg.role]">
          <div class="bubble">{{ msg.content }}</div>
        </div>
        <div v-if="loading" class="msg assistant">
          <div class="bubble">思考中<span class="dot">...</span></div>
        </div>
      </div>

      <div class="ai-footer">
        <el-input v-model="input" placeholder="请输入问题，回车发送" @keyup.enter="send"
                  :disabled="loading" />
        <el-button type="primary" :loading="loading" @click="send">发送</el-button>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import { ChatDotRound, Close, MagicStick } from '@element-plus/icons-vue'
import { aiApi } from '@/api'

const visible = ref(false)
const input = ref('')
const loading = ref(false)
const bodyRef = ref()
const messages = ref([
  { role: 'assistant', content: '你好！我是宿舍智能助手，可以解答宿舍规章、报修、请假等问题，有什么可以帮你？' }
])

const scrollToBottom = () => {
  nextTick(() => {
    if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
  })
}

const send = async () => {
  const question = input.value.trim()
  if (!question || loading.value) return
  messages.value.push({ role: 'user', content: question })
  input.value = ''
  scrollToBottom()
  loading.value = true
  try {
    const res = await aiApi.chat({ question })
    messages.value.push({ role: 'assistant', content: res.data })
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，AI 服务暂时不可用，请稍后再试。' })
  } finally {
    loading.value = false
    scrollToBottom()
  }
}
</script>

<style scoped>
.ai-fab {
  position: fixed;
  right: 30px;
  bottom: 30px;
  width: 56px;
  height: 56px;
  border-radius: 18px;
  background: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 10px 24px rgba(21, 103, 96, 0.4);
  z-index: 2000;
  transition: transform 0.2s, box-shadow 0.2s;
}
.ai-fab:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 30px rgba(21, 103, 96, 0.5);
}
.fab-dot {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 0 3px var(--primary);
}
.ai-chat {
  position: fixed;
  right: 30px;
  bottom: 30px;
  width: 360px;
  height: 500px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 6px 24px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
  z-index: 2001;
  overflow: hidden;
}
.ai-header {
  height: 52px;
  background: var(--sidebar);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  font-weight: 600;
}
.ai-title {
  display: flex;
  align-items: center;
  gap: 8px;
}
.ai-title .el-icon {
  color: var(--accent);
}
.close-btn {
  cursor: pointer;
  color: var(--sidebar-muted);
}
.close-btn:hover {
  color: #fff;
}
.ai-body {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
  background: #f7f8fa;
}
.msg {
  display: flex;
  margin-bottom: 10px;
}
.msg.user {
  justify-content: flex-end;
}
.bubble {
  max-width: 75%;
  padding: 8px 12px;
  border-radius: 10px;
  font-size: 14px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}
.msg.assistant .bubble {
  background: #fff;
  color: #303133;
  border: 1px solid #ebeef5;
}
.msg.user .bubble {
  background: var(--primary);
  color: #fff;
}
.ai-footer {
  padding: 10px;
  display: flex;
  gap: 8px;
  border-top: 1px solid #ebeef5;
}
.slide-enter-active, .slide-leave-active {
  transition: all 0.25s ease;
}
.slide-enter-from, .slide-leave-to {
  opacity: 0;
  transform: translateY(20px);
}
.dot {
  animation: blink 1s infinite;
}
@keyframes blink {
  50% { opacity: 0.2; }
}
</style>
