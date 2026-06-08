import request from '@/utils/request'

// ============ 认证 ============
export const authApi = {
  login: (data) => request.post('/auth/login', data),
  info: () => request.get('/auth/info'),
  logout: () => request.post('/auth/logout')
}

// ============ 用户管理 ============
export const userApi = {
  page: (params) => request.get('/user/page', { params }),
  add: (data) => request.post('/user', data),
  edit: (data) => request.put('/user', data),
  remove: (id) => request.delete(`/user/${id}`),
  reset: (id) => request.put(`/user/reset/${id}`),
  changePassword: (data) => request.put('/user/password', data),
  managers: () => request.get('/user/managers')
}

// ============ 楼栋 ============
export const buildingApi = {
  list: () => request.get('/building/list'),
  add: (data) => request.post('/building', data),
  edit: (data) => request.put('/building', data),
  remove: (id) => request.delete(`/building/${id}`)
}

// ============ 房间 ============
export const roomApi = {
  page: (params) => request.get('/room/page', { params }),
  listByBuilding: (buildingId) => request.get(`/room/listByBuilding/${buildingId}`),
  add: (data) => request.post('/room', data),
  edit: (data) => request.put('/room', data),
  remove: (id) => request.delete(`/room/${id}`)
}

// ============ 床位 + 入住分配 ============
export const bedApi = {
  listByRoom: (roomId) => request.get(`/bed/listByRoom/${roomId}`),
  free: (params) => request.get('/bed/free', { params }),
  add: (data) => request.post('/bed', data),
  remove: (id) => request.delete(`/bed/${id}`),
  allocate: (data) => request.post('/bed/allocate', data),
  deallocate: (studentId) => request.post(`/bed/deallocate/${studentId}`)
}

// ============ 学生 ============
export const studentApi = {
  page: (params) => request.get('/student/page', { params }),
  add: (data) => request.post('/student', data),
  edit: (data) => request.put('/student', data),
  remove: (id) => request.delete(`/student/${id}`),
  me: () => request.get('/student/me')
}

// ============ 报修 ============
export const repairApi = {
  submit: (data) => request.post('/repair', data),
  page: (params) => request.get('/repair/page', { params }),
  assign: (data) => request.put('/repair/assign', data),
  finish: (data) => request.put('/repair/finish', data),
  remove: (id) => request.delete(`/repair/${id}`)
}

// ============ 请假/晚归 ============
export const leaveApi = {
  submit: (data) => request.post('/leave', data),
  page: (params) => request.get('/leave/page', { params }),
  approve: (data) => request.put('/leave/approve', data),
  remove: (id) => request.delete(`/leave/${id}`)
}

// ============ 公告 ============
export const announcementApi = {
  page: (params) => request.get('/announcement/page', { params }),
  add: (data) => request.post('/announcement', data),
  edit: (data) => request.put('/announcement', data),
  remove: (id) => request.delete(`/announcement/${id}`)
}

// ============ 卫生检查 ============
export const hygieneApi = {
  page: (params) => request.get('/hygiene/page', { params }),
  add: (data) => request.post('/hygiene', data),
  edit: (data) => request.put('/hygiene', data),
  remove: (id) => request.delete(`/hygiene/${id}`)
}

// ============ FAQ ============
export const faqApi = {
  list: () => request.get('/faq/list'),
  add: (data) => request.post('/faq', data),
  edit: (data) => request.put('/faq', data),
  remove: (id) => request.delete(`/faq/${id}`)
}

// ============ 统计 ============
export const statApi = {
  overview: () => request.get('/stat/overview')
}

// ============ AI ============
export const aiApi = {
  chat: (data) => request.post('/ai/chat', data),
  chatHistory: (params) => request.get('/ai/chat/history', { params }),
  report: () => request.get('/ai/report'),
  reportHistory: (params) => request.get('/ai/report/history', { params }),
  deleteReport: (id) => request.delete(`/ai/report/${id}`)
}
