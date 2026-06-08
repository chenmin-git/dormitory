<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="账号/姓名" clearable style="width: 200px"
                @keyup.enter="loadData" />
      <el-select v-model="query.role" placeholder="角色" clearable style="width: 140px" @change="loadData">
        <el-option label="超级管理员" value="ADMIN" />
        <el-option label="宿管员" value="MANAGER" />
        <el-option label="学生" value="STUDENT" />
      </el-select>
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button type="success" @click="openDialog()">新增用户</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column type="index" label="#" width="55" />
      <el-table-column prop="username" label="账号" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="role" label="角色">
        <template #default="{ row }">
          <el-tag :type="roleType(row.role)">{{ roleLabel(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="gender" label="性别" width="70" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="warning" @click="resetPwd(row)">重置密码</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pager" background layout="total, prev, pager, next"
                   :total="total" :page-size="query.pageSize" :current-page="query.pageNum"
                   @current-change="(p) => { query.pageNum = p; loadData() }" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新增用户'" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="账号" v-if="!form.id">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="超级管理员" value="ADMIN" />
            <el-option label="宿管员" value="MANAGER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <div v-if="!form.id" class="tip">新增用户默认密码为 123456</div>
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
import { userApi } from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', role: '' })

const dialogVisible = ref(false)
const form = reactive({ id: null, username: '', realName: '', role: 'STUDENT', phone: '', gender: '男', status: 1 })

const roleLabel = (r) => ({ ADMIN: '超级管理员', MANAGER: '宿管员', STUDENT: '学生' }[r])
const roleType = (r) => ({ ADMIN: 'danger', MANAGER: 'warning', STUDENT: 'success' }[r])

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await userApi.page(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: null, username: '', realName: '', role: 'STUDENT', phone: '', gender: '男', status: 1 })
  }
  dialogVisible.value = true
}

const save = async () => {
  if (form.id) {
    await userApi.edit(form)
  } else {
    await userApi.add(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const resetPwd = (row) => {
  ElMessageBox.confirm(`确定将 ${row.realName} 的密码重置为 123456？`, '提示', { type: 'warning' })
    .then(async () => {
      await userApi.reset(row.id)
      ElMessage.success('已重置')
    })
}

const remove = (row) => {
  ElMessageBox.confirm(`确定删除用户 ${row.realName}？`, '提示', { type: 'warning' })
    .then(async () => {
      await userApi.remove(row.id)
      ElMessage.success('删除成功')
      loadData()
    })
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
.tip {
  color: #e6a23c;
  font-size: 12px;
}
</style>
