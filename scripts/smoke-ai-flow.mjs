#!/usr/bin/env node

const baseUrl = process.env.API_BASE || 'http://localhost:8088/api'
const username = process.env.DEMO_STUDENT_USER || 'stu001'
const password = process.env.DEMO_STUDENT_PASSWORD || '123456'
const currentYear = new Date().getFullYear()

const requiredCases = [
  {
    name: '自动报修：窗户关不上',
    question: '宿舍窗户关不上了',
    expects: ['已为你提交报修', '门窗', '紧急']
  },
  {
    name: '完整请假：解析时间并提交',
    question: '我要请假，6月20日8点到20点回家',
    expects: ['已为你提交请假申请', `${currentYear}-06-20 08:00:00`, `${currentYear}-06-20 20:00:00`]
  },
  {
    name: '信息不全：只追问不提交',
    question: '我想请假',
    expects: ['开始时间', '结束时间']
  },
  {
    name: '查询报修',
    question: '查我的报修',
    expects: ['报修记录']
  },
  {
    name: '查询宿舍',
    question: '我住哪',
    expects: ['宿舍信息', '楼栋', '房间']
  },
  {
    name: '查询请假记录',
    question: '我的请假记录',
    expects: ['请假/晚归记录']
  },
  {
    name: 'FAQ问答：电磁炉',
    question: '宿舍能用电磁炉吗',
    expects: ['电磁炉']
  }
]

async function request(path, options = {}) {
  const res = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...(options.token ? { Authorization: `Bearer ${options.token}` } : {}),
      ...(options.headers || {})
    }
  })
  const text = await res.text()
  let json
  try {
    json = JSON.parse(text)
  } catch {
    throw new Error(`Non-JSON response from ${path}: ${text.slice(0, 180)}`)
  }
  if (!res.ok || json.code !== 200) {
    throw new Error(`${path} failed: HTTP ${res.status}, code ${json.code}, msg ${json.msg}`)
  }
  return json.data
}

async function login() {
  const data = await request('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password })
  })
  return data.token
}

async function pageTotal(token, path) {
  const data = await request(`${path}?pageNum=1&pageSize=1`, { token })
  return Number(data.total || 0)
}

async function ask(token, question) {
  return await request('/ai/chat', {
    method: 'POST',
    token,
    body: JSON.stringify({ question })
  })
}

function assertIncludes(name, answer, expected) {
  if (!answer.includes(expected)) {
    throw new Error(`${name} expected "${expected}" in answer, got:\n${answer}`)
  }
}

const token = await login()
const leaveBeforeIncomplete = await pageTotal(token, '/leave/page')
const incompleteAnswer = await ask(token, '我想请假')
for (const expected of ['开始时间', '结束时间']) {
  assertIncludes('信息不全：只追问不提交', incompleteAnswer, expected)
}
const leaveAfterIncomplete = await pageTotal(token, '/leave/page')
if (leaveAfterIncomplete !== leaveBeforeIncomplete) {
  throw new Error(`Incomplete leave request changed leave count: ${leaveBeforeIncomplete} -> ${leaveAfterIncomplete}`)
}

const repairBefore = await pageTotal(token, '/repair/page')
const leaveBefore = await pageTotal(token, '/leave/page')
const results = []

for (const item of requiredCases.filter((c) => c.question !== '我想请假')) {
  const answer = await ask(token, item.question)
  for (const expected of item.expects) {
    assertIncludes(item.name, answer, expected)
  }
  results.push({ name: item.name, answer })
}

const repairAfter = await pageTotal(token, '/repair/page')
const leaveAfter = await pageTotal(token, '/leave/page')
if (repairAfter !== repairBefore + 1) {
  throw new Error(`Repair count should increase by 1: ${repairBefore} -> ${repairAfter}`)
}
if (leaveAfter !== leaveBefore + 1) {
  throw new Error(`Leave count should increase by 1: ${leaveBefore} -> ${leaveAfter}`)
}

console.log('AI smoke flow passed')
console.log(`- incomplete leave kept total at ${leaveBeforeIncomplete}`)
console.log(`- repair total: ${repairBefore} -> ${repairAfter}`)
console.log(`- leave total: ${leaveBefore} -> ${leaveAfter}`)
for (const result of results) {
  console.log(`- ${result.name}: ${result.answer.split('\n')[0]}`)
}
