import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userId: Number(localStorage.getItem('userId')) || null,
    username: localStorage.getItem('username') || '',
    realName: localStorage.getItem('realName') || '',
    role: localStorage.getItem('role') || ''
  }),
  actions: {
    setUser(data) {
      this.token = data.token
      this.userId = data.userId
      this.username = data.username
      this.realName = data.realName
      this.role = data.role
      localStorage.setItem('token', data.token)
      localStorage.setItem('userId', data.userId)
      localStorage.setItem('username', data.username)
      localStorage.setItem('realName', data.realName)
      localStorage.setItem('role', data.role)
    },
    logout() {
      this.token = ''
      this.userId = null
      this.username = ''
      this.realName = ''
      this.role = ''
      localStorage.clear()
    }
  }
})
