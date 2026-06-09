<template>
  <el-card class="guide-card">
    <div class="guide-head">
      <div>
        <div class="guide-kicker">FAQ Knowledge Base</div>
        <h2>宿舍生活指南</h2>
        <p>这里的内容会同步作为 AI 智能问答的知识来源，常见制度和办理说明可以直接查询。</p>
      </div>
      <el-button v-if="isAdmin" type="success" @click="openDialog()">新增FAQ</el-button>
    </div>

    <div class="guide-tools">
      <el-input v-model="keyword" placeholder="搜索门禁、用电、报修、请假..." clearable :prefix-icon="Search" />
      <el-radio-group v-model="category" size="large">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button v-for="item in categories" :key="item" :label="item">{{ item }}</el-radio-button>
      </el-radio-group>
    </div>

    <div class="guide-stats">
      <div>
        <strong class="display-font">{{ list.length }}</strong>
        <span>知识条目</span>
      </div>
      <div>
        <strong class="display-font">{{ categories.length }}</strong>
        <span>覆盖分类</span>
      </div>
      <div>
        <strong class="display-font">AI</strong>
        <span>问答上下文</span>
      </div>
    </div>

    <el-table v-if="isAdmin" :data="filteredList" border stripe v-loading="loading">
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

    <div v-else v-loading="loading" class="guide-grid">
      <el-empty v-if="filteredList.length === 0" description="没有找到相关问题" :image-size="90" />
      <article v-for="item in filteredList" :key="item.id" class="guide-item">
        <div class="guide-item-top">
          <el-tag effect="plain">{{ item.category || '通用' }}</el-tag>
          <span>#{{ item.id }}</span>
        </div>
        <h3>{{ item.question }}</h3>
        <p>{{ item.answer }}</p>
      </article>
    </div>

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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { faqApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const list = ref([])
const keyword = ref('')
const category = ref('')
const dialogVisible = ref(false)
const form = reactive({ id: null, question: '', answer: '', category: '' })
const isAdmin = computed(() => userStore.role === 'ADMIN')

const categories = computed(() => {
  return [...new Set(list.value.map((i) => i.category || '通用'))]
})

const filteredList = computed(() => {
  const key = keyword.value.trim()
  return list.value.filter((item) => {
    const itemCategory = item.category || '通用'
    const hitCategory = !category.value || itemCategory === category.value
    const text = `${item.question || ''}${item.answer || ''}${itemCategory}`
    const hitKeyword = !key || text.includes(key)
    return hitCategory && hitKeyword
  })
})

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
.guide-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.guide-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 24px;
  padding: 6px 2px 0;
}
.guide-kicker {
  color: var(--primary);
  font-size: 12px;
  font-weight: 700;
}
.guide-head h2 {
  margin: 4px 0 8px;
  color: var(--ink);
  font-size: 24px;
}
.guide-head p {
  margin: 0;
  color: var(--muted);
  line-height: 1.7;
}
.guide-tools {
  display: grid;
  grid-template-columns: minmax(260px, 1fr) auto;
  gap: 12px;
  align-items: center;
}
.guide-stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}
.guide-stats div {
  padding: 14px;
  border: 1px solid var(--line);
  border-radius: 10px;
  background: var(--bg-soft);
}
.guide-stats strong {
  display: block;
  color: var(--ink);
  font-size: 26px;
  line-height: 1;
}
.guide-stats span {
  display: block;
  margin-top: 6px;
  color: var(--muted);
  font-size: 13px;
}
.guide-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 14px;
  min-height: 220px;
}
.guide-item {
  padding: 16px;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: #fff;
  box-shadow: var(--shadow-sm);
}
.guide-item-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: var(--muted);
  font-size: 12px;
}
.guide-item h3 {
  margin: 13px 0 8px;
  color: var(--ink);
  font-size: 17px;
}
.guide-item p {
  margin: 0;
  color: var(--ink-soft);
  line-height: 1.8;
}
</style>
