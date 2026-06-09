<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="query.status" placeholder="状态筛选" clearable style="width: 140px"
                 @change="() => { query.pageNum = 1; loadData() }">
        <el-option label="待派单" value="待派单" />
        <el-option label="维修中" value="维修中" />
        <el-option label="已完成" value="已完成" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button v-if="isStudent" type="success" @click="submitDialog = true">我要报修</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading" @row-dblclick="openDetail">
      <el-table-column type="index" label="#" width="55" />
      <el-table-column prop="title" label="标题" />
      <el-table-column v-if="!isStudent" prop="studentName" label="报修人" width="100" />
      <el-table-column prop="roomNo" label="房间" width="80" />
      <el-table-column label="AI分类" width="90">
        <template #default="{ row }">
          <el-tag type="primary">{{ row.ai_category || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="AI优先级" width="100">
        <template #default="{ row }">
          <el-tag :type="priorityType(row.ai_priority)">{{ row.ai_priority || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="!isStudent" prop="handlerName" label="处理人" width="100" />
      <el-table-column prop="create_time" label="提交时间" width="160" />
      <el-table-column label="详情" width="80" fixed="right">
        <template #default="{ row }">
          <el-button size="small" link type="primary" @click="openDetail(row)">查看</el-button>
        </template>
      </el-table-column>
      <el-table-column v-if="!isStudent" label="操作" width="200">
        <template #default="{ row }">
          <el-button v-if="row.status === '待派单'" size="small" type="primary" @click="openAssign(row)">派单</el-button>
          <el-button v-if="row.status === '维修中'" size="small" type="success" @click="openFinish(row)">完成</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pager" background layout="total, prev, pager, next"
                   :total="total" :page-size="query.pageSize" :current-page="query.pageNum"
                   @current-change="(p) => { query.pageNum = p; loadData() }" />

    <!-- 学生报修 -->
    <el-dialog v-model="submitDialog" title="提交报修（AI 将自动分类）" width="480px">
      <el-form :model="repairForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="repairForm.title" placeholder="如：宿舍水龙头漏水" />
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input v-model="repairForm.description" type="textarea" :rows="4"
                    placeholder="请详细描述故障现象，AI 会据此判断类别与紧急程度" />
        </el-form-item>
        <el-alert type="info" :closable="false" show-icon
                  title="提交后系统将自动关联你所住房间，并由 AI 智能判定故障类别与紧急程度" />
      </el-form>
      <template #footer>
        <el-button @click="submitDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitRepair">提交</el-button>
      </template>
    </el-dialog>

    <!-- 工单详情 -->
    <el-dialog v-model="detailDialog" title="报修处理详情" width="660px">
      <div v-if="detailRow" class="detail-panel">
        <div class="detail-hero">
          <div>
            <div class="detail-kicker">工单 #{{ detailRow.id }}</div>
            <h3>{{ detailRow.title }}</h3>
            <p>{{ detailRow.description || '暂无描述' }}</p>
          </div>
          <el-tag :type="statusType(detailRow.status)" size="large">{{ detailRow.status }}</el-tag>
        </div>

        <div class="ai-proof">
          <div>
            <span>AI 分类</span>
            <strong>{{ detailRow.ai_category || '-' }}</strong>
          </div>
          <div>
            <span>AI 优先级</span>
            <strong>{{ detailRow.ai_priority || '-' }}</strong>
          </div>
          <div>
            <span>房间</span>
            <strong>{{ detailRow.roomNo || '-' }}</strong>
          </div>
        </div>

        <el-steps :active="repairStep(detailRow.status)" finish-status="success" align-center>
          <el-step title="提交报修" :description="detailRow.create_time || '-'" />
          <el-step title="宿管派单" :description="detailRow.handlerName || '待派单'" />
          <el-step title="维修完成" :description="detailRow.finish_time || '待完成'" />
        </el-steps>

        <el-descriptions :column="2" border class="detail-desc">
          <el-descriptions-item label="报修人">{{ detailRow.studentName || '本人' }}</el-descriptions-item>
          <el-descriptions-item label="学号">{{ detailRow.studentNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理人">{{ detailRow.handlerName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成时间">{{ detailRow.finish_time || '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理备注" :span="2">
            {{ detailRow.handle_remark || '暂无处理备注' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>

    <!-- 派单 -->
    <el-dialog v-model="assignDialog" title="派单" width="380px">
      <el-form label-width="80px">
        <el-form-item label="处理人">
          <el-select v-model="handlerId" placeholder="选择宿管" style="width: 100%">
            <el-option v-for="m in managers" :key="m.id" :label="m.realName" :value="m.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialog = false">取消</el-button>
        <el-button type="primary" @click="doAssign">确定</el-button>
      </template>
    </el-dialog>

    <!-- 完成 -->
    <el-dialog v-model="finishDialog" title="完成维修" width="380px">
      <el-form label-width="80px">
        <el-form-item label="处理备注">
          <el-input v-model="finishRemark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="finishDialog = false">取消</el-button>
        <el-button type="primary" @click="doFinish">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { repairApi, userApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const isStudent = computed(() => userStore.role === 'STUDENT')

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, status: '' })
const managers = ref([])

const submitDialog = ref(false)
const submitting = ref(false)
const repairForm = reactive({ title: '', description: '' })

const assignDialog = ref(false)
const handlerId = ref(null)
const currentRow = ref(null)

const finishDialog = ref(false)
const finishRemark = ref('')
const detailDialog = ref(false)
const detailRow = ref(null)

const priorityType = (p) => ({ 紧急: 'danger', 一般: 'warning', 低: 'info' }[p] || 'info')
const statusType = (s) => ({ 待派单: 'info', 维修中: 'warning', 已完成: 'success' }[s])
const repairStep = (s) => ({ 待派单: 1, 维修中: 2, 已完成: 3 }[s] || 1)

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await repairApi.page(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const submitRepair = async () => {
  if (!repairForm.title || !repairForm.description) {
    ElMessage.warning('请填写标题和描述')
    return
  }
  submitting.value = true
  try {
    await repairApi.submit(repairForm)
    ElMessage.success('提交成功，AI 已自动分类')
    submitDialog.value = false
    Object.assign(repairForm, { title: '', description: '' })
    loadData()
  } finally {
    submitting.value = false
  }
}

const openAssign = async (row) => {
  currentRow.value = row
  handlerId.value = null
  if (managers.value.length === 0) {
    const { data } = await userApi.managers()
    managers.value = data
  }
  assignDialog.value = true
}
const doAssign = async () => {
  if (!handlerId.value) {
    ElMessage.warning('请选择处理人')
    return
  }
  await repairApi.assign({ repairId: currentRow.value.id, handlerId: handlerId.value })
  ElMessage.success('派单成功')
  assignDialog.value = false
  loadData()
}

const openFinish = (row) => {
  currentRow.value = row
  finishRemark.value = ''
  finishDialog.value = true
}
const doFinish = async () => {
  await repairApi.finish({ repairId: currentRow.value.id, remark: finishRemark.value })
  ElMessage.success('已完成')
  finishDialog.value = false
  loadData()
}

const remove = (row) => {
  ElMessageBox.confirm('确定删除该报修单？', '提示', { type: 'warning' }).then(async () => {
    await repairApi.remove(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
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
  line-height: 1.7;
}
.ai-proof {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}
.ai-proof div {
  padding: 12px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: var(--bg-soft);
}
.ai-proof span {
  display: block;
  color: var(--muted);
  font-size: 12px;
}
.ai-proof strong {
  display: block;
  margin-top: 5px;
  color: var(--ink);
  font-size: 16px;
}
.detail-desc {
  margin-top: 2px;
}
</style>
