<template>
  <!-- 智能宿舍 Logo：楼栋 + AI 火花 -->
  <svg :width="size" :height="size" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
    <!-- 楼体 -->
    <path d="M7 28V12.2L16 7l9 5.2V28" :stroke="color" stroke-width="2" stroke-linejoin="round" stroke-linecap="round"/>
    <!-- 大门 -->
    <path d="M13.5 28v-4.5a2.5 2.5 0 0 1 5 0V28" :stroke="color" stroke-width="1.7" stroke-linejoin="round"/>
    <!-- 窗户 -->
    <rect x="10.5" y="14.5" width="3" height="3" rx="0.6" :fill="color"/>
    <rect x="18.5" y="14.5" width="3" height="3" rx="0.6" :fill="color"/>
    <rect x="10.5" y="19.5" width="3" height="3" rx="0.6" :fill="color" opacity="0.55"/>
    <rect x="18.5" y="19.5" width="3" height="3" rx="0.6" :fill="color" opacity="0.55"/>
    <!-- AI 火花 -->
    <path d="M24.5 4.5l.9 2.1 2.1.9-2.1.9-.9 2.1-.9-2.1-2.1-.9 2.1-.9z" :fill="spark"/>
  </svg>
</template>

<script setup>
defineProps({
  size: { type: [Number, String], default: 26 },
  color: { type: String, default: '#ffffff' },
  spark: { type: String, default: '#f0c27b' }
})
</script>
