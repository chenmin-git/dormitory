<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="状态筛选" clearable style="width: 140px"
                 @change="() => { query.pageNum = 1; loadData() }">
        <el-option label="待审批" value="待审批" />
        <el-option label="通过" value="通过" />
        <el-option label="驳回" value="驳回" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button v-if="isStudent" type="success" @click="submitDialog = true">发起申请</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading" @row-dblclick="openDetail">
      <el-table-column type="index" label="#" width="55" />
      <el-table-column v-if="!isStudent" prop="studentName" label="学生" width="100" />
      <el-table-column prop="type" label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.type === '请假' ? 'primary' : 'warning'">{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="reason" label="事由" />
      <el-table-column prop="start_time" label="开始时间" width="160" />
      <el-table-column prop="end_time" label="结束时间" width="160" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="详情" width="80" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="openDetail(row)">查看</el-button>
        </template>
      </el-table-column>
      <el-table-column v-if="!isStudent" label="操作" width="170">
        <template #default="{ row }">
          <template v-if="row.status === '待审批'">
            <el-button size="small" type="success" @click="approve(row, true)">通过</el-button>
            <el-button size="small" type="danger" @click="approve(row, false)">驳回</el-button>
          </template>
          <span v-else>{{ row.approverName ? '审批人:' + row.approverName : '-' }}</span>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pager" background layout="total, prev, pager, next"
                   :total="total" :page-size="query.pageSize" :current-page="query.pageNum"
                   @current-change="(p) => { query.pageNum = p; loadData() }" />

    <el-dialog v-model="submitDialog" title="发起请假/晚归申请" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio value="请假">请假</el-radio>
            <el-radio value="晚归">晚归</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="事由">
          <el-input v-model="form.reason" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" style="width: 100%"
                          value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" style="width: 100%"
                          value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="submitDialog = false">取消</el-button>
        <el-button type="primary" @click="submit">提交</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialog" title="申请审批详情" width="640px">
      <div v-if="detailRow" class="detail-panel">
        <div class="detail-hero">
          <div>
            <div class="detail-kicker">{{ detailRow.type }}申请 #{{ detailRow.id }}</div>
            <h3>{{ detailRow.reason || '个人事由' }}</h3>
            <p>{{ detailRow.start_time }} 至 {{ detailRow.end_time }}</p>
          </div>
          <el-tag :type="statusType(detailRow.status)" size="large">{{ detailRow.status }}</el-tag>
        </div>

        <el-steps :active="leaveStep(detailRow.status)" :process-status="detailRow.status === '驳回' ? 'error' : 'process'"
                  finish-status="success" align-center>
          <el-step title="提交申请" :description="detailRow.create_time || '-'" />
          <el-step title="宿管审批" :description="detailRow.approverName || '待审批'" />
          <el-step :title="detailRow.status === '驳回' ? '审批驳回' : '审批通过'"
                   :description="detailRow.status === '待审批' ? '等待结果' : detailRow.status" />
        </el-steps>

        <el-descriptions :column="2" border class="detail-desc">
          <el-descriptions-item label="学生">{{ detailRow.studentName || '本人' }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ detailRow.studentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ detailRow.type }}</el-descriptions-item>
          <el-descriptions-item label="审批人">{{ detailRow.approverName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="开始时间">{{ detailRow.start_time }}</el-descriptions-item>
          <el-descriptions-item label="结束时间">{{ detailRow.end_time }}</el-descriptions-item>
          <el-descriptions-item label="审批意见" :span="2">
            {{ detailRow.approve_remark || '暂无审批意见' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { leaveApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isStudent = computed(() => userStore.role === 'STUDENT')

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: '' })

const submitDialog = ref(false)
const form = reactive({ type: '请假', reason: '', startTime: '', endTime: '' })
const detailDialog = ref(false)
const detailRow = ref(null)

const statusType = (s) => ({ 待审批: 'info', 通过: 'success', 驳回: 'danger' }[s])
const leaveStep = (s) => ({ 待审批: 1, 通过: 3, 驳回: 3 }[s] || 1)

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await leaveApi.page(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const submit = async () => {
  if (!form.reason || !form.startTime || !form.endTime) {
    ElMessage.warning('请填写完整')
    return
  }
  await leaveApi.submit(form)
  ElMessage.success('提交成功')
  submitDialog.value = false
  Object.assign(form, { type: '请假', reason: '', startTime: '', endTime: '' })
  loadData()
}

const approve = (row, pass) => {
  const title = pass ? '通过申请' : '驳回申请'
  ElMessageBox.prompt('审批意见（可选）', title, { inputType: 'textarea' })
    .then(async ({ value }) => {
      await leaveApi.approve({ id: row.id, pass, remark: value })
      ElMessage.success('操作成功')
      loadData()
    })
    .catch(() => {})
}

const openDetail = (row) => {
  detailRow.value = row
  detailDialog.value = true
}

onMounted(loadData)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.detail-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}
.detail-hero {
  display: flex;
  justify-content: space-between;
  gap: 18px;
  padding: 18px;
  border-radius: 12px;
  background: linear-gradient(135deg, var(--primary-050), #fff7ef);
  border: 1px solid var(--line);
}
.detail-kicker {
  color: var(--primary);
  font-size: 12px;
  font-weight: 700;
}
.detail-hero h3 {
  margin: 6px 0 8px;
  color: var(--ink);
  font-size: 20px;
}
.detail-hero p {
  margin: 0;
  color: var(--ink-soft);
}
.detail-desc {
  margin-top: 2px;
}
</style>
