<template>
  <el-card>
    <div class="toolbar">
      <el-button type="success" @click="openDialog()">新增楼栋</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column type="index" label="#" width="55" />
      <el-table-column prop="name" label="楼栋名称" />
      <el-table-column prop="gender_type" label="住宿性别" width="100" />
      <el-table-column prop="managerName" label="宿管员" />
      <el-table-column prop="floor_count" label="楼层数" width="90" />
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑楼栋' : '新增楼栋'" width="460px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="楼栋名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="住宿性别">
          <el-radio-group v-model="form.genderType">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="宿管员">
          <el-select v-model="form.managerId" placeholder="选择宿管" style="width: 100%" clearable>
            <el-option v-for="m in managers" :key="m.id" :label="m.realName" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="楼层数">
          <el-input-number v-model="form.floorCount" :min="1" :max="30" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { buildingApi, userApi } from '@/api'

const loading = ref(false)
const list = ref([])
const managers = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', genderType: '男', managerId: null, floorCount: 6, remark: '' })

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await buildingApi.list()
    list.value = data
  } finally {
    loading.value = false
  }
}

const loadManagers = async () => {
  const { data } = await userApi.managers()
  managers.value = data
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, { id: row.id, name: row.name, genderType: row.gender_type, managerId: row.manager_id, floorCount: row.floor_count, remark: row.remark })
  } else {
    Object.assign(form, { id: null, name: '', genderType: '男', managerId: null, floorCount: 6, remark: '' })
  }
  dialogVisible.value = true
}

const save = async () => {
  if (form.id) {
    await buildingApi.edit(form)
  } else {
    await buildingApi.add(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const remove = (row) => {
  ElMessageBox.confirm(`确定删除楼栋 ${row.name}？`, '提示', { type: 'warning' })
    .then(async () => {
      await buildingApi.remove(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
}

onMounted(() => {
  loadData()
  loadManagers()
})
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
</style>
