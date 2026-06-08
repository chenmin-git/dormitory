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

    <el-table :data="list" border stripe v-loading="loading">
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

const statusType = (s) => ({ 待审批: 'info', 通过: 'success', 驳回: 'danger' }[s])

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
</style>
