<template>
  <el-row :gutter="16">
    <el-col :span="10">
      <el-card header="个人信息">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="账号">{{ userStore.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ userStore.realName }}</el-descriptions-item>
          <el-descriptions-item label="角色">
            <el-tag :type="roleType">{{ roleLabel }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </el-col>
    <el-col :span="14">
      <el-card header="修改密码">
        <el-form :model="form" label-width="90px" style="max-width: 420px">
          <el-form-item label="原密码">
            <el-input v-model="form.oldPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="form.newPassword" type="password" show-password />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="form.confirm" type="password" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="changePwd">提交修改</el-button>
          </el-form-item>
        </el-form>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '@/api'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const roleLabel = computed(() => ({ ADMIN: '超级管理员', MANAGER: '宿管员', STUDENT: '学生' }[userStore.role]))
const roleType = computed(() => ({ ADMIN: 'danger', MANAGER: 'warning', STUDENT: 'success' }[userStore.role]))

const form = reactive({ oldPassword: '', newPassword: '', confirm: '' })

const changePwd = async () => {
  if (!form.oldPassword || !form.newPassword) {
    ElMessage.warning('请填写密码')
    return
  }
  if (form.newPassword !== form.confirm) {
    ElMessage.warning('两次新密码不一致')
    return
  }
  await userApi.changePassword({ oldPassword: form.oldPassword, newPassword: form.newPassword })
  ElMessage.success('密码修改成功')
  form.oldPassword = form.newPassword = form.confirm = ''
}
</script>
