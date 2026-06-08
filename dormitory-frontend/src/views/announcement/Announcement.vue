<template>
  <el-card>
    <div class="toolbar">
      <el-button v-if="canManage" type="success" @click="openDialog()">发布公告</el-button>
    </div>

    <el-table :data="list" border stripe v-loading="loading">
      <el-table-column type="index" label="#" width="55" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="target" label="范围" width="140" />
      <el-table-column prop="publisherName" label="发布人" width="100" />
      <el-table-column prop="create_time" label="发布时间" width="170" />
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="view(row)">查看</el-button>
          <template v-if="canManage">
            <el-button size="small" type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
          </template>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination class="pager" background layout="total, prev, pager, next"
                   :total="total" :page-size="query.pageSize" :current-page="query.pageNum"
                   @current-change="(p) => { query.pageNum = p; loadData() }" />

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑公告' : '发布公告'" width="560px">
      <el-form :model="form" label-width="70px">
        <el-form-item label="标题">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="范围">
          <el-input v-model="form.target" placeholder="全体 或 具体楼栋名" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="6" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="viewVisible" :title="current?.title" width="560px">
      <p class="meta">{{ current?.target }} | {{ current?.publisherName }} | {{ current?.create_time }}</p>
      <div class="content">{{ current?.content }}</div>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { announcementApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const canManage = computed(() => ['ADMIN', 'MANAGER'].includes(userStore.role))

const loading = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

const dialogVisible = ref(false)
const form = reactive({ id: null, title: '', target: '全体', content: '' })

const viewVisible = ref(false)
const current = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const { data } = await announcementApi.page(query)
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

const openDialog = (row) => {
  if (row) Object.assign(form, { id: row.id, title: row.title, target: row.target, content: row.content })
  else Object.assign(form, { id: null, title: '', target: '全体', content: '' })
  dialogVisible.value = true
}
const save = async () => {
  if (form.id) await announcementApi.edit(form)
  else await announcementApi.add(form)
  ElMessage.success('保存成功')
  dialogVisible.value = false
  loadData()
}
const remove = (row) => {
  ElMessageBox.confirm('确定删除该公告？', '提示', { type: 'warning' }).then(async () => {
    await announcementApi.remove(row.id)
    ElMessage.success('删除成功')
    loadData()
  })
}
const view = (row) => {
  current.value = row
  viewVisible.value = true
}

onMounted(loadData)
</script>

<style scoped>
.toolbar {
  margin-bottom: 16px;
}
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
.meta {
  color: #909399;
  font-size: 13px;
}
.content {
  line-height: 1.8;
  white-space: pre-wrap;
}
</style>
