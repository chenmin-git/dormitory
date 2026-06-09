<template>
  <el-card class="chat-card" shadow="never">
    <div class="chat-head">
      <div class="chat-head-title">
        <el-icon :size="20"><MagicStick /></el-icon>
        <div>
          <div class="ht-name">宿舍智能助手</div>
          <div class="ht-sub">可解答规章制度，还能帮你办理报修、请假等业务</div>
        </div>
      </div>
      <el-button :icon="Delete" size="small" @click="clearChat">清空对话</el-button>
    </div>

    <div class="chat-body" ref="bodyRef">
      <div v-for="(msg, idx) in messages" :key="idx" :class="['row', msg.role]">
        <div class="avatar" :class="msg.role">
          <el-icon v-if="msg.role === 'assistant'"><MagicStick /></el-icon>
          <el-icon v-else><User /></el-icon>
        </div>
        <div v-if="msg.role === 'assistant'" class="assistant-stack">
          <div v-if="actionInfo(msg.content)" class="action-card">
            <div class="action-top">
              <div>
                <span class="action-kicker">{{ actionLabel(actionInfo(msg.content).TYPE) }}</span>
                <div class="action-title">{{ actionInfo(msg.content).TITLE || 'AI 已识别业务意图' }}</div>
              </div>
              <el-tag :type="statusTag(actionInfo(msg.content).STATUS)" effect="plain">
                {{ actionInfo(msg.content).STATUS || '处理中' }}
              </el-tag>
            </div>
            <div class="action-summary">{{ actionInfo(msg.content).SUMMARY }}</div>
            <div class="action-grid">
              <div v-for="item in actionItems(actionInfo(msg.content))" :key="item.label" class="action-item">
                <span>{{ item.label }}</span>
                <strong>{{ item.value }}</strong>
              </div>
            </div>
            <div v-if="actionInfo(msg.content).NEXT" class="action-next">
              下一步：{{ actionInfo(msg.content).NEXT }}
            </div>
          </div>
          <div class="bubble" v-html="render(stripAction(msg.content))"></div>
        </div>
        <div class="bubble" v-else>{{ msg.content }}</div>
      </div>
      <div v-if="loading" class="row assistant">
        <div class="avatar assistant"><el-icon><MagicStick /></el-icon></div>
        <div class="bubble"><span class="typing">思考中...</span></div>
      </div>
    </div>

    <!-- 推荐问题 -->
    <div class="suggests" v-if="messages.length <= 1">
      <span class="suggests-label">试试问：</span>
      <button v-for="q in suggests" :key="q" class="suggest" @click="quickAsk(q)">{{ q }}</button>
    </div>

    <div class="chat-input">
      <el-input v-model="input" type="textarea" :rows="2" resize="none"
                placeholder="输入你的问题，Enter 发送（Shift+Enter 换行）"
                @keydown.enter="onEnter" :disabled="loading" />
      <el-button type="primary" :icon="Promotion" :loading="loading" @click="send">发送</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { marked } from 'marked'
import { MagicStick, User, Promotion, Delete } from '@element-plus/icons-vue'
import { aiApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
marked.setOptions({ breaks: true })
const render = (t) => marked.parse(t || '')
const ACTION_RE = /@@AI_ACTION\n([\s\S]*?)@@END_AI_ACTION\n?/m

const bodyRef = ref()
const input = ref('')
const loading = ref(false)
const welcome = { role: 'assistant', content: '你好！我是宿舍智能助手 👋\n\n我不仅能解答宿舍规章，还能直接帮你**办理业务**：\n- 报修：直接描述故障，如「宿舍水龙头漏水」\n- 请假/晚归：如「我要请假，6月10日8点到18点回家」\n- 查询：「我的报修」「我住在哪」「我的请假记录」' }
const messages = ref([{ ...welcome }])

// 学生可办理业务，给出业务化推荐问；其它角色给咨询类
const suggests = userStore.role === 'STUDENT'
  ? ['宿舍水龙头漏水了', '我要请假明天一天', '查询我的报修记录', '我住在哪个房间？']
  : ['宿舍门禁时间是几点？', '怎么申请报修？', '可以使用哪些电器？', '卫生检查标准是什么？']

const scrollBottom = () => {
  nextTick(() => {
    if (bodyRef.value) bodyRef.value.scrollTop = bodyRef.value.scrollHeight
  })
}

const actionInfo = (content) => {
  const match = (content || '').match(ACTION_RE)
  if (!match) return null
  const data = {}
  match[1].split('\n').forEach((line) => {
    const idx = line.indexOf('=')
    if (idx > 0) data[line.slice(0, idx)] = line.slice(idx + 1)
  })
  return data
}

const stripAction = (content) => (content || '').replace(ACTION_RE, '').trim()

const actionLabel = (type) => ({
  REPAIR_SUBMITTED: 'AI 报修 Agent',
  LEAVE_SUBMITTED: 'AI 请假 Agent',
  NEED_MORE_INFO: 'AI 信息校验'
}[type] || 'AI 办理结果')

const statusTag = (status) => ({
  待派单: 'warning',
  待审批: 'warning',
  等待补充: 'info',
  已完成: 'success',
  通过: 'success',
  驳回: 'danger'
}[status] || 'primary')

const actionItems = (data) => {
  if (!data) return []
  const labels = {
    CATEGORY: 'AI 分类',
    PRIORITY: 'AI 优先级',
    START: '开始时间',
    END: '结束时间'
  }
  return ['CATEGORY', 'PRIORITY', 'START', 'END']
    .filter((key) => data[key])
    .map((key) => ({ label: labels[key], value: data[key] }))
}

const onEnter = (e) => {
  if (e.shiftKey) return
  e.preventDefault()
  send()
}

const quickAsk = (q) => {
  input.value = q
  send()
}

const send = async () => {
  const question = input.value.trim()
  if (!question || loading.value) return
  messages.value.push({ role: 'user', content: question })
  input.value = ''
  scrollBottom()
  loading.value = true
  try {
    const res = await aiApi.chat({ question })
    messages.value.push({ role: 'assistant', content: res.data })
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，AI 服务暂时不可用，请稍后再试。' })
  } finally {
    loading.value = false
    scrollBottom()
  }
}

const clearChat = () => {
  messages.value = [{ ...welcome }]
}

// 进入页面加载历史对话
const loadHistory = async () => {
  try {
    const { data } = await aiApi.chatHistory({ limit: 20 })
    if (data && data.length) {
      const history = []
      data.forEach((log) => {
        history.push({ role: 'user', content: log.question })
        history.push({ role: 'assistant', content: log.answer })
      })
      messages.value = [{ ...welcome }, ...history]
      scrollBottom()
    }
  } catch (e) {
    // 忽略历史加载失败
  }
}

onMounted(loadHistory)
</script>

<style scoped>
.chat-card {
  height: calc(100vh - 108px);
  display: flex;
  flex-direction: column;
}
.chat-card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 0;
  min-height: 0;
}
.chat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid var(--line);
}
.chat-head-title {
  display: flex;
  align-items: center;
  gap: 12px;
}
.chat-head-title .el-icon {
  color: var(--primary);
}
.ht-name {
  font-weight: 600;
  color: var(--ink);
  font-size: 16px;
}
.ht-sub {
  color: var(--muted);
  font-size: 12px;
  margin-top: 2px;
}
.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 22px;
  background:
    radial-gradient(60% 50% at 50% 0%, rgba(21, 103, 96, 0.03), transparent),
    var(--bg-soft);
}
.row {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
  max-width: 860px;
}
.row.user {
  flex-direction: row-reverse;
  margin-left: auto;
}
.avatar {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex: none;
  color: #fff;
}
.avatar.assistant {
  background: var(--primary);
}
.avatar.user {
  background: var(--accent);
}
.bubble {
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 14.5px;
  line-height: 1.8;
  max-width: 75%;
  white-space: pre-wrap;
  word-break: break-word;
}
.assistant-stack {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-width: 75%;
}
.assistant-stack .bubble {
  max-width: 100%;
}
.action-card {
  width: min(560px, 100%);
  padding: 14px;
  border: 1px solid var(--primary-100);
  background:
    linear-gradient(135deg, rgba(21, 103, 96, 0.08), rgba(194, 104, 58, 0.06)),
    #fff;
  border-radius: 12px;
  box-shadow: 0 10px 24px rgba(34, 45, 39, 0.06);
}
.action-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.action-kicker {
  color: var(--primary);
  font-size: 12px;
  font-weight: 600;
}
.action-title {
  margin-top: 3px;
  color: var(--ink);
  font-weight: 700;
  font-size: 16px;
}
.action-summary {
  margin-top: 10px;
  color: var(--ink-soft);
  font-size: 14px;
}
.action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
  margin-top: 12px;
}
.action-item {
  padding: 9px 10px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(21, 103, 96, 0.12);
}
.action-item span {
  display: block;
  color: var(--muted);
  font-size: 12px;
}
.action-item strong {
  display: block;
  margin-top: 3px;
  color: var(--ink);
  font-size: 14px;
}
.action-next {
  margin-top: 12px;
  padding: 9px 10px;
  border-radius: 8px;
  color: var(--primary);
  background: var(--primary-050);
  font-size: 13px;
}
.row.assistant .bubble {
  background: #fff;
  border: 1px solid var(--line);
  color: var(--ink-soft);
}
.row.user .bubble {
  background: var(--primary);
  color: #fff;
}
/* markdown in bubble */
.bubble :deep(p) { margin: 6px 0; }
.bubble :deep(ul), .bubble :deep(ol) { padding-left: 20px; margin: 6px 0; }
.bubble :deep(strong) { color: var(--accent); }
.bubble :deep(h3), .bubble :deep(h4) { margin: 8px 0 4px; color: var(--ink); }
.typing {
  color: var(--muted);
  animation: blink 1.2s infinite;
}
@keyframes blink { 50% { opacity: 0.4; } }

.suggests {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 12px 20px 0;
}
.suggests-label {
  color: var(--muted);
  font-size: 13px;
}
.suggest {
  border: 1px solid var(--line);
  background: var(--surface);
  color: var(--ink-soft);
  border-radius: 20px;
  padding: 6px 14px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}
.suggest:hover {
  border-color: var(--primary);
  color: var(--primary);
  background: var(--primary-050);
}
.chat-input {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  padding: 16px 20px;
  border-top: 1px solid var(--line);
}
.chat-input .el-button {
  height: 56px;
  padding: 0 22px;
}
</style>
