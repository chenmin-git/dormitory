<template>
  <div>
    <!-- 数字卡片 -->
    <el-row :gutter="16">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-icon" :style="{ background: card.soft, color: card.color }">
            <el-icon :size="24"><component :is="card.icon" /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value display-font">{{ card.value }}</div>
            <div class="stat-label">{{ card.label }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <div class="ai-metrics">
      <div v-for="metric in aiMetrics" :key="metric.label" class="ai-metric">
        <div class="ai-metric-icon" :style="{ color: metric.color, background: metric.soft }">
          <el-icon :size="22"><component :is="metric.icon" /></el-icon>
        </div>
        <div>
          <div class="ai-metric-value display-font">{{ metric.value }}</div>
          <div class="ai-metric-label">{{ metric.label }}</div>
        </div>
        <span>{{ metric.note }}</span>
      </div>
    </div>

    <!-- AI 数据分析报告 -->
    <el-card class="ai-report" shadow="hover">
      <template #header>
        <div class="report-header">
          <span class="report-title">
            <el-icon><MagicStick /></el-icon>
            AI 数据分析报告
            <em>智能生成</em>
          </span>
          <div class="report-actions">
            <el-button size="small" :icon="Clock" @click="openHistory">历史记录</el-button>
            <el-button size="small" :icon="DocumentCopy" :disabled="!report" @click="copyReport">复制</el-button>
            <el-button size="small" :icon="Download" :disabled="!report" @click="downloadReport">导出</el-button>
            <el-button type="primary" size="small" :loading="reportLoading" @click="loadReport">
              {{ report ? '重新生成' : '生成报告' }}
            </el-button>
          </div>
        </div>
      </template>
      <div v-if="reportLoading" class="report-loading">
        <el-icon class="is-loading"><Loading /></el-icon> AI 正在分析数据，请稍候...
      </div>
      <div v-else-if="report" class="markdown-body" v-html="renderedReport"></div>
      <el-empty v-else description="点击右上角按钮，让 AI 分析当前宿舍数据" :image-size="80" />
    </el-card>

    <!-- 历史报告抽屉 -->
    <el-drawer v-model="historyVisible" title="历史分析报告" size="560px">
      <div v-if="historyList.length === 0" class="history-empty">
        <el-empty description="暂无历史记录" :image-size="80" />
      </div>
      <div v-for="item in historyList" :key="item.id" class="history-item">
        <div class="history-meta">
          <span><el-icon><Calendar /></el-icon> {{ item.create_time }}</span>
          <span class="history-creator">{{ item.creatorName || '—' }}</span>
          <el-button link type="danger" size="small" @click="removeHistory(item.id)">删除</el-button>
        </div>
        <div class="markdown-body history-content" v-html="renderMd(item.content)"></div>
      </div>
    </el-drawer>

    <!-- 图表 -->
    <el-row :gutter="16" class="charts">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>报修分类占比</template>
          <div ref="pieRef" style="height: 320px"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>报修状态分布</template>
          <div ref="barRef" style="height: 320px"></div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 各楼栋入住情况 -->
    <el-card shadow="hover" class="charts">
      <template #header>各楼栋入住情况</template>
      <div ref="buildingRef" style="height: 300px"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { marked } from 'marked'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MagicStick, Clock, Calendar, DocumentCopy, Download } from '@element-plus/icons-vue'
import { statApi, aiApi } from '@/api'

// 主题配色（与全局 theme.css 呼应）
const PALETTE = ['#156760', '#c2683a', '#a8791f', '#4a6e84', '#5b8c7e', '#9b6a86']

marked.setOptions({ breaks: true })
const renderMd = (text) => marked.parse(text || '')

const cards = ref([])
const aiMetrics = ref([])
const report = ref('')
const reportLoading = ref(false)
const pieRef = ref()
const barRef = ref()
const buildingRef = ref()

const renderedReport = computed(() => renderMd(report.value))

// 历史报告
const historyVisible = ref(false)
const historyList = ref([])

const loadOverview = async () => {
  const { data } = await statApi.overview()
  cards.value = [
    { label: '学生总数', value: data.studentCount, icon: 'Avatar', color: '#156760', soft: '#e3eeec' },
    { label: '入住率', value: data.occupancyRate + '%', icon: 'House', color: '#c2683a', soft: '#f6e7dd' },
    { label: '待处理报修', value: data.pendingRepair, icon: 'Tools', color: '#a8791f', soft: '#f3ead2' },
    { label: '待审批请假', value: data.pendingLeave, icon: 'Calendar', color: '#4a6e84', soft: '#e2eaf0' }
  ]
  aiMetrics.value = [
    { label: 'AI 对话次数', value: data.aiChatCount || 0, note: '问答与业务办理留痕', icon: 'ChatDotRound', color: '#156760', soft: '#e3eeec' },
    { label: 'AI 分类工单', value: data.aiRepairCount || 0, note: '报修自动判类', icon: 'MagicStick', color: '#c2683a', soft: '#f6e7dd' },
    { label: '紧急工单识别', value: data.urgentRepairCount || 0, note: '优先处理安全隐患', icon: 'Warning', color: '#b74935', soft: '#fae5df' },
    { label: '智能请假办理', value: data.aiLeaveCount || 0, note: '自然语言生成申请', icon: 'Tickets', color: '#4a6e84', soft: '#e2eaf0' }
  ]
  await nextTick()
  renderPie(data.repairByCategory || [])
  renderBar(data.repairStatus || {})
  renderBuilding(data.buildingOccupancy || [])
}

const renderBuilding = (list) => {
  const chart = echarts.init(buildingRef.value)
  chart.setOption({
    color: ['#156760', '#dcc9a8'],
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['已住', '空床'], top: 0, textStyle: { color: '#574f3f' } },
    grid: { left: 50, right: 24, top: 40, bottom: 30 },
    xAxis: { type: 'category', data: list.map((i) => i.name), axisLine: { lineStyle: { color: '#cfc6b4' } }, axisLabel: { color: '#574f3f' } },
    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#efe9dc' } }, axisLabel: { color: '#574f3f' } },
    series: [
      { name: '已住', type: 'bar', stack: 'b', barWidth: '36%', data: list.map((i) => Number(i.used)), itemStyle: { borderRadius: [0, 0, 0, 0] } },
      { name: '空床', type: 'bar', stack: 'b', barWidth: '36%', data: list.map((i) => Number(i.total) - Number(i.used)), itemStyle: { borderRadius: [6, 6, 0, 0] } }
    ]
  })
}

const renderPie = (list) => {
  const chart = echarts.init(pieRef.value)
  chart.setOption({
    color: PALETTE,
    tooltip: { trigger: 'item' },
    legend: { bottom: 0, textStyle: { color: '#574f3f' } },
    series: [{
      type: 'pie',
      radius: ['45%', '68%'],
      itemStyle: { borderColor: '#fff', borderWidth: 3, borderRadius: 6 },
      data: list.map((i) => ({ name: i.name || '未分类', value: i.value })),
      label: { formatter: '{b}: {c}', color: '#574f3f' }
    }]
  })
}

const renderBar = (statusMap) => {
  const chart = echarts.init(barRef.value)
  const keys = Object.keys(statusMap)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: keys, axisLine: { lineStyle: { color: '#cfc6b4' } }, axisLabel: { color: '#574f3f' } },
    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#efe9dc' } }, axisLabel: { color: '#574f3f' } },
    series: [{
      type: 'bar',
      data: keys.map((k, i) => ({ value: statusMap[k], itemStyle: { color: PALETTE[i % PALETTE.length] } })),
      barWidth: '42%',
      itemStyle: { borderRadius: [8, 8, 0, 0] }
    }]
  })
}

const loadReport = async () => {
  reportLoading.value = true
  report.value = ''
  try {
    const res = await aiApi.report()
    report.value = res.data
  } catch (e) {
    report.value = ''
  } finally {
    reportLoading.value = false
  }
}

const openHistory = async () => {
  historyVisible.value = true
  const { data } = await aiApi.reportHistory({ pageNum: 1, pageSize: 50 })
  historyList.value = data.records
}

const removeHistory = (id) => {
  ElMessageBox.confirm('确定删除该历史报告？', '提示', { type: 'warning' }).then(async () => {
    await aiApi.deleteReport(id)
    ElMessage.success('已删除')
    historyList.value = historyList.value.filter((i) => i.id !== id)
  })
}

const copyReport = async () => {
  if (!report.value) return
  try {
    await navigator.clipboard.writeText(report.value)
    ElMessage.success('报告内容已复制')
  } catch (e) {
    ElMessage.warning('浏览器暂不支持自动复制')
  }
}

const downloadReport = () => {
  if (!report.value) return
  const blob = new Blob([report.value], { type: 'text/markdown;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '宿舍管理AI数据分析报告.md'
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(loadOverview)
</script>

<style scoped>
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stat-value {
  font-size: 30px;
  font-weight: 600;
  color: var(--ink);
  line-height: 1.1;
}
.stat-label {
  color: var(--muted);
  font-size: 13px;
  margin-top: 2px;
}
.ai-metrics {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}
.ai-metric {
  position: relative;
  min-height: 104px;
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.94), rgba(250, 248, 242, 0.94));
  box-shadow: var(--shadow-sm);
  display: grid;
  grid-template-columns: 44px 1fr;
  gap: 12px;
  align-items: start;
}
.ai-metric-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ai-metric-value {
  color: var(--ink);
  font-size: 28px;
  font-weight: 600;
  line-height: 1;
}
.ai-metric-label {
  margin-top: 5px;
  color: var(--ink-soft);
  font-size: 13px;
  font-weight: 600;
}
.ai-metric span {
  grid-column: 1 / -1;
  color: var(--muted);
  font-size: 12px;
}
.ai-report {
  margin-top: 16px;
  position: relative;
  overflow: hidden;
}
.ai-report::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  width: 4px;
  height: 100%;
  background: linear-gradient(var(--primary), var(--accent));
}
.report-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.report-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--ink);
}
.report-title .el-icon {
  color: var(--accent);
}
.report-title em {
  font-style: normal;
  font-size: 12px;
  color: var(--primary);
  background: var(--primary-050);
  border: 1px solid var(--primary-100);
  padding: 1px 8px;
  border-radius: 20px;
}
.report-actions {
  display: flex;
  gap: 8px;
}
.report-loading {
  color: var(--muted);
  padding: 10px 0;
}
.charts {
  margin-top: 16px;
}

/* Markdown 渲染样式 */
.markdown-body {
  color: var(--ink-soft);
  font-size: 14.5px;
  line-height: 1.85;
}
.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4) {
  color: var(--ink);
  font-weight: 600;
  margin: 18px 0 10px;
  line-height: 1.4;
}
.markdown-body :deep(h3) {
  font-size: 17px;
  padding-left: 10px;
  border-left: 3px solid var(--primary);
}
.markdown-body :deep(h4) {
  font-size: 15px;
  color: var(--primary-600);
}
.markdown-body :deep(p) {
  margin: 8px 0;
}
.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 22px;
  margin: 8px 0;
}
.markdown-body :deep(li) {
  margin: 5px 0;
}
.markdown-body :deep(strong) {
  color: var(--accent);
  font-weight: 600;
}
.markdown-body :deep(h1:first-child),
.markdown-body :deep(h2:first-child),
.markdown-body :deep(h3:first-child) {
  margin-top: 0;
}

/* 历史报告 */
.history-item {
  border: 1px solid var(--line);
  border-radius: 12px;
  padding: 14px 16px;
  margin-bottom: 16px;
  background: var(--bg-soft);
}
.history-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: var(--muted);
  margin-bottom: 8px;
  padding-bottom: 8px;
  border-bottom: 1px dashed var(--line);
}
.history-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}
.history-creator {
  color: var(--primary);
  font-weight: 500;
}
.history-meta .el-button {
  margin-left: auto;
}
.history-content {
  font-size: 13.5px;
}
</style>
