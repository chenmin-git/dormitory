<template>
  <el-card>
    <div class="toolbar">
      <span class="tip">FAQ 知识库会作为上下文注入到 AI 智能问答，维护这里可提升问答准确度。</span>
      <el-button type="success" @click="openDialog()">新增FAQ</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column type="index" label="#" width="55" />
      <el-table-column prop="question" label="问题" />
      <el-table-column prop="answer" label="答案" />
      <el-table-column prop="category" label="分类" width="120" />
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑FAQ' : '新增FAQ'" width="520px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="问题">
          <el-input v-model="form.question" />
        </el-form-item>
        <el-form-item label="答案">
          <el-input v-model="form.answer" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" />
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
import { faqApi } from '@/api'

const loading = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const form = reactive({ id: null, question: '', answer: '', category: '' })

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await faqApi.list()
    list.value = data
  } finally {
    loading.value = false
  }
}
const openDialog = (row) => {
  if (row) Object.assign(form, row)
  else Object.assign(form, { id: null, question: '', answer: '', category: '' })
  dialogVisible.value = true
}
const save = async () => {
  if (form.id) await faqApi.edit(form)
  else await faqApi.add(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}
const remove = (row) => {
  ElMessageBox.confirm('确定删除该FAQ？', '提示', { type: 'warning' }).then(async () => {
    await faqApi.remove(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}

onMounted(loadData)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.tip {
  color: #909399;
  font-size: 13px;
}
</style>
