<template>
  <el-card>
    <div class="toolbar">
      <el-select v-model="query.buildingId" placeholder="按楼栋筛选" clearable style="width: 180px"
                 @change="() => { query.pageNum = 1; loadData() }">
        <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
      </el-select>
      <el-button type="success" @click="openRoomDialog()">新增房间</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column type="index" label="#" width="55" />
      <el-table-column prop="buildingName" label="楼栋" />
      <el-table-column prop="room_no" label="房间号" />
      <el-table-column prop="floor" label="楼层" width="80" />
      <el-table-column label="入住情况" width="140">
        <template #default="{ row }">
          <el-tag :type="row.occupied >= row.capacity ? 'danger' : 'success'">
            {{ row.occupied }} / {{ row.capacity }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openBedDrawer(row)">床位/入住</el-button>
          <el-button size="small" @click="openRoomDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="removeRoom(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pager" background layout="total, prev, pager, next"
                   :total="total" :page-size="query.pageSize" :current-page="query.pageNum"
                   @current-change="(p) => { query.pageNum = p; loadData() }" />

    <!-- 房间表单 -->
    <el-dialog v-model="roomDialog" :title="roomForm.id ? '编辑房间' : '新增房间'" width="420px">
      <el-form :model="roomForm" label-width="80px">
        <el-form-item label="楼栋">
          <el-select v-model="roomForm.buildingId" style="width: 100%">
            <el-option v-for="b in buildings" :key="b.id" :label="b.name" :value="b.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="房间号">
          <el-input v-model="roomForm.roomNo" />
        </el-form-item>
        <el-form-item label="楼层">
          <el-input-number v-model="roomForm.floor" :min="1" />
        </el-form-item>
        <el-form-item label="可住人数">
          <el-input-number v-model="roomForm.capacity" :min="1" :max="8" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roomDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRoom">确定</el-button>
      </template>
    </el-dialog>

    <!-- 床位 + 入住分配抽屉 -->
    <el-drawer v-model="bedDrawer" :title="`房间 ${currentRoom?.room_no} - 床位管理`" size="500px">
      <div class="bed-toolbar">
        <el-button type="success" size="small" @click="addBed">新增床位</el-button>
      </div>
      <el-table :data="beds" border>
        <el-table-column prop="bed_no" label="床位号" width="90" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'danger' : 'success'">
              {{ row.status === 1 ? '已住' : '空闲' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="入住学生">
          <template #default="{ row }">
            {{ row.studentName ? `${row.studentName}(${row.studentNo})` : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" size="small" type="primary" @click="openAllocate(row)">分配</el-button>
            <el-button v-else size="small" type="warning" @click="deallocate(row)">退宿</el-button>
            <el-button v-if="row.status === 0" size="small" type="danger" @click="removeBed(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-drawer>

    <!-- 分配学生 -->
    <el-dialog v-model="allocateDialog" title="分配学生入住" width="400px">
      <el-form label-width="80px">
        <el-form-item label="床位">
          <el-input :value="currentBed?.bed_no" disabled />
        </el-form-item>
        <el-form-item label="学生">
          <el-select v-model="allocateStudentId" placeholder="选择未入住学生" filterable style="width: 100%">
            <el-option v-for="s in freeStudents" :key="s.id"
                       :label="`${s.realName} (${s.studentNo})`" :value="s.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="allocateDialog = false">取消</el-button>
        <el-button type="primary" @click="doAllocate">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { roomApi, buildingApi, bedApi, studentApi } from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const buildings = ref([])
const query = reactive({ pageNum: 1, pageSize: 10, buildingId: null })

// 房间表单
const roomDialog = ref(false)
const roomForm = reactive({ id: null, buildingId: null, roomNo: '', floor: 1, capacity: 4 })

// 床位抽屉
const bedDrawer = ref(false)
const currentRoom = ref(null)
const beds = ref([])

// 分配
const allocateDialog = ref(false)
const currentBed = ref(null)
const allocateStudentId = ref(null)
const freeStudents = ref([])

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await roomApi.page(query)
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

// ---- 房间 ----
const openRoomDialog = (row) => {
  if (row) {
    Object.assign(roomForm, { id: row.id, buildingId: row.building_id, roomNo: row.room_no, floor: row.floor, capacity: row.capacity })
  } else {
    Object.assign(roomForm, { id: null, buildingId: query.buildingId || (buildings.value[0]?.id ?? null), roomNo: '', floor: 1, capacity: 4 })
  }
  roomDialog.value = true
}
const saveRoom = async () => {
  if (roomForm.id) await roomApi.edit(roomForm)
  else await roomApi.add(roomForm)
  ElMessage.success('保存成功')
  roomDialog.value = false
  loadData()
}
const removeRoom = (row) => {
  ElMessageBox.confirm(`确定删除房间 ${row.room_no}？`, '提示', { type: 'warning' }).then(async () => {
    await roomApi.remove(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

// ---- 床位 ----
const openBedDrawer = async (room) => {
  currentRoom.value = room
  bedDrawer.value = true
  await loadBeds()
}
const loadBeds = async () => {
  const { data } = await bedApi.listByRoom(currentRoom.value.id)
  beds.value = data
}
const addBed = async () => {
  const { value } = await ElMessageBox.prompt('请输入床位号', '新增床位', { inputValue: (beds.value.length + 1) + '床' })
  await bedApi.add({ roomId: currentRoom.value.id, bedNo: value })
  ElMessage.success('已新增')
  loadBeds()
  loadData()
}
const removeBed = (row) => {
  ElMessageBox.confirm(`确定删除 ${row.bed_no}？`, '提示', { type: 'warning' }).then(async () => {
    await bedApi.remove(row.id)
    ElMessage.success('已删除')
    loadBeds()
  })
}

// ---- 分配 ----
const openAllocate = async (bed) => {
  currentBed.value = bed
  allocateStudentId.value = null
  // 拉取未入住学生（前端过滤 bedId 为空）
  const { data } = await studentApi.page({ pageNum: 1, pageSize: 1000 })
  freeStudents.value = data.records.filter((s) => !s.bed_id)
  allocateDialog.value = true
}
const doAllocate = async () => {
  if (!allocateStudentId.value) {
    ElMessage.warning('请选择学生')
    return
  }
  await bedApi.allocate({ studentId: allocateStudentId.value, bedId: currentBed.value.id })
  ElMessage.success('分配成功')
  allocateDialog.value = false
  loadBeds()
  loadData()
}
const deallocate = (bed) => {
  ElMessageBox.confirm(`确定让 ${bed.studentName} 退宿？`, '提示', { type: 'warning' }).then(async () => {
    await bedApi.deallocate(bed.studentId)
    ElMessage.success('已退宿')
    loadBeds()
    loadData()
  })
}

onMounted(() => {
  loadBuildings()
  loadData()
})
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
.bed-toolbar {
  margin-bottom: 12px;
}
</style>
