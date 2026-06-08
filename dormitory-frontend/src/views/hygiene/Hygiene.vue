<template>
  <el-card>
    <div class="toolbar">
      <el-button v-if="canManage" type="success" @click="openDialog()">新增检查记录</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column type="index" label="#" width="55" />
      <el-table-column prop="buildingName" label="楼栋" />
      <el-table-column prop="roomNo" label="房间" width="90" />
      <el-table-column label="评分" width="120">
        <template #default="{ row }">
          <el-tag :type="scoreType(row.score)">{{ row.score }} 分</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="comment" label="评语" />
      <el-table-column prop="checkerName" label="检查人" width="100" />
      <el-table-column prop="check_date" label="检查日期" width="130" />
      <el-table-column v-if="canManage" label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pager" background layout="total, prev, pager, next"
                   :total="total" :page-size="query.pageSize" :current-page="query.pageNum"
                   @current-change="(p) => { query.pageNum = p; loadData() }" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑检查' : '新增检查'" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="楼栋">
          <el-select v-model="selectedBuilding" placeholder="选择楼栋" style="width: 100%" @change="loadRooms">
            <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间">
          <el-select v-model="form.roomId" placeholder="选择房间" style="width: 100%">
            <el-option v-for="r in rooms" :key="r.id" :label="r.roomNo" :value="r.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="评分">
          <el-input-number v-model="form.score" :min="0" :max="100" />
        </el-form-item>
        <el-form-item label="评语">
          <el-input v-model="form.comment" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="检查日期">
          <el-date-picker v-model="form.checkDate" type="date" style="width: 100%"
                          value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { hygieneApi, buildingApi, roomApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const canManage = computed(() => ['ADMIN', 'MANAGER'].includes(userStore.role))

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

const buildings = ref([])
const rooms = ref([])
const selectedBuilding = ref(null)

const dialogVisible = ref(false)
const form = reactive({ id: null, roomId: null, score: 90, comment: '', checkDate: '' })

const scoreType = (s) => (s >= 90 ? 'success' : s >= 60 ? 'warning' : 'danger')

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await hygieneApi.page(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const loadBuildings = async () => {
  const { data } = await buildingApi.list()
  buildings.value = data
}
const loadRooms = async () => {
  form.roomId = null
  const { data } = await roomApi.listByBuilding(selectedBuilding.value)
  rooms.value = data
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, { id: row.id, roomId: row.room_id, score: row.score, comment: row.comment, checkDate: row.check_date })
  } else {
    Object.assign(form, { id: null, roomId: null, score: 90, comment: '', checkDate: new Date().toISOString().slice(0, 10) })
    selectedBuilding.value = null
    rooms.value = []
  }
  dialogVisible.value = true
}
const save = async () => {
  if (!form.roomId) {
    ElMessage.warning('请选择房间')
    return
  }
  if (form.id) await hygieneApi.edit(form)
  else await hygieneApi.add(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}
const remove = (row) => {
  ElMessageBox.confirm('确定删除该记录？', '提示', { type: 'warning' }).then(async () => {
    await hygieneApi.remove(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

onMounted(() => {
  loadData()
  loadBuildings()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
