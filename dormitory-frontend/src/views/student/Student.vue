<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.keyword" placeholder="姓名/学号" clearable style="width: 200px"
                @keyup.enter="loadData" />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button type="success" @click="openDialog()">新增学生</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column type="index" label="#" width="55" />
      <el-table-column prop="student_no" label="学号" />
      <el-table-column prop="real_name" label="姓名" />
      <el-table-column prop="gender" label="性别" width="70" />
      <el-table-column prop="class_name" label="班级" />
      <el-table-column prop="major" label="专业" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column label="住宿位置">
        <template #default="{ row }">
          <el-tag v-if="row.buildingName" type="success">
            {{ row.buildingName }} {{ row.roomNo }} {{ row.bedNo }}
          </el-tag>
          <el-tag v-else type="info">未入住</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pager" background layout="total, prev, pager, next"
                   :total="total" :page-size="query.pageSize" :current-page="query.pageNum"
                   @current-change="(p) => { query.pageNum = p; loadData() }" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑学生' : '新增学生'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="学号">
          <el-input v-model="form.studentNo" :disabled="!!form.id" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio value="男">男</el-radio>
            <el-radio value="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="班级">
          <el-input v-model="form.className" />
        </el-form-item>
        <el-form-item label="专业">
          <el-input v-model="form.major" />
        </el-form-item>
        <el-form-item label="学院">
          <el-input v-model="form.college" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
      </el-form>
      <div v-if="!form.id" class="tip">新增学生将以「学号」作为登录账号，默认密码 123456</div>
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
import { studentApi } from '@/api'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10, keyword: '' })

const dialogVisible = ref(false)
const form = reactive({ id: null, studentNo: '', realName: '', gender: '男', className: '', major: '', college: '', phone: '' })

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await studentApi.page(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  if (row) {
    Object.assign(form, {
      id: row.id, studentNo: row.student_no, realName: row.real_name, gender: row.gender,
      className: row.class_name, major: row.major, college: row.college, phone: row.phone
    })
  } else {
    Object.assign(form, { id: null, studentNo: '', realName: '', gender: '男', className: '', major: '', college: '', phone: '' })
  }
  dialogVisible.value = true
}

const save = async () => {
  if (form.id) await studentApi.edit(form)
  else await studentApi.add(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}

const remove = (row) => {
  ElMessageBox.confirm(`确定删除学生 ${row.real_name}？将同时删除其登录账号。`, '提示', { type: 'warning' })
    .then(async () => {
      await studentApi.remove(row.id)
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
