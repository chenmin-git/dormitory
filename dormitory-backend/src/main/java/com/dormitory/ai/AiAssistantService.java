package com.dormitory.ai;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dormitory.common.RoleConstant;
import com.dormitory.entity.*;
import com.dormitory.mapper.*;
import com.dormitory.service.LeaveService;
import com.dormitory.service.RepairService;
import com.dormitory.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI 智能助手（Agent）：在普通问答之上，支持通过自然语言办理业务。
 * 识别意图后调用对应业务服务，所有动作都有兜底，识别失败回退为普通问答。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiAssistantService {

    private final SparkClient sparkClient;
    private final SparkService sparkService;
    private final RepairService repairService;
    private final LeaveService leaveService;
    private final StudentService studentService;
    private final BedMapper bedMapper;
    private final RoomMapper roomMapper;
    private final BuildingMapper buildingMapper;
    private final AiChatLogMapper aiChatLogMapper;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 助手对话入口：识别意图 → 办理业务 / 查询 / 普通问答。
     */
    public String assist(Long userId, String role, String question) {
        JSONObject intent = recognizeIntent(question);
        String action = intent.getStr("intent", "qa");

        switch (action) {
            case "submit_repair":
                return logged(userId, question, submitRepair(userId, role, intent, question));
            case "request_leave":
                return logged(userId, question, requestLeave(userId, role, intent));
            case "query_my_repairs":
                return logged(userId, question, queryMyRepairs(userId));
            case "query_my_leaves":
                return logged(userId, question, queryMyLeaves(userId));
            case "query_my_dorm":
                return logged(userId, question, queryMyDorm(userId));
            default:
                // 普通问答（内部已记录日志）
                return sparkService.chat(userId, question);
        }
    }

    // ============ 意图识别 ============

    private JSONObject recognizeIntent(String question) {
        JSONObject localIntent = recognizeIntentLocally(question);
        try {
            String now = LocalDateTime.now().format(DTF);
            String sys = "你是宿舍管理系统的意图识别器。当前时间是 " + now + "。"
                    + "请判断用户消息属于哪种意图，并抽取参数，只返回 JSON：\n"
                    + "intent 取值之一：\n"
                    + "- submit_repair（用户想报修/反映宿舍设施故障），抽取 title(简短标题) 和 description(详细描述)\n"
                    + "- request_leave（用户想请假或晚归登记），抽取 leaveType(请假或晚归)、reason、startTime、endTime；"
                    + "时间用 yyyy-MM-dd HH:mm:ss 格式，依据当前时间把'明天''周末''今晚'等换算成具体时间，无法确定则留空字符串\n"
                    + "- query_my_repairs（查询我的报修记录）\n"
                    + "- query_my_leaves（查询我的请假/晚归记录）\n"
                    + "- query_my_dorm（查询我住在哪/我的宿舍信息）\n"
                    + "- qa（其它咨询类问题）\n"
                    + "返回格式：{\"intent\":\"\",\"title\":\"\",\"description\":\"\",\"leaveType\":\"\",\"reason\":\"\",\"startTime\":\"\",\"endTime\":\"\"}";
            String content = sparkClient.chat(sys, question, true);
            int s = content.indexOf('{'), e = content.lastIndexOf('}');
            if (s >= 0 && e > s) {
                content = content.substring(s, e + 1);
            }
            return mergeIntent(JSONUtil.parseObj(content), localIntent);
        } catch (Exception ex) {
            log.warn("意图识别失败，使用本地规则兜底：{}", ex.getMessage());
            return localIntent;
        }
    }

    private JSONObject mergeIntent(JSONObject aiIntent, JSONObject localIntent) {
        String aiAction = aiIntent.getStr("intent", "qa");
        String localAction = localIntent.getStr("intent", "qa");
        if ("qa".equals(aiAction) && !"qa".equals(localAction)) {
            return localIntent;
        }
        if (aiAction.equals(localAction)) {
            for (String key : new String[]{"title", "description", "leaveType", "reason", "startTime", "endTime"}) {
                if (StrUtil.isBlank(aiIntent.getStr(key)) && StrUtil.isNotBlank(localIntent.getStr(key))) {
                    aiIntent.set(key, localIntent.getStr(key));
                }
            }
        }
        return aiIntent;
    }

    private JSONObject recognizeIntentLocally(String question) {
        String q = question == null ? "" : question.trim();
        JSONObject intent = baseIntent("qa");
        if (StrUtil.isBlank(q)) {
            return intent;
        }
        if (containsAny(q, "住哪", "住在哪里", "住哪个", "哪个房间", "我的宿舍", "宿舍信息")) {
            return baseIntent("query_my_dorm");
        }
        if (containsAny(q, "报修记录", "我的报修", "查报修", "查询报修", "报修进度")) {
            return baseIntent("query_my_repairs");
        }
        if (containsAny(q, "请假记录", "晚归记录", "我的请假", "我的晚归", "查请假", "查询请假")) {
            return baseIntent("query_my_leaves");
        }
        if (containsAny(q, "请假", "晚归")) {
            intent = baseIntent("request_leave");
            intent.set("leaveType", q.contains("晚归") ? "晚归" : "请假");
            intent.set("reason", inferLeaveReason(q));
            fillLeaveTime(q, intent);
            return intent;
        }
        if (isRepairProblem(q)) {
            intent = baseIntent("submit_repair");
            intent.set("title", inferRepairTitle(q));
            intent.set("description", q);
            return intent;
        }
        return intent;
    }

    private JSONObject baseIntent(String action) {
        return new JSONObject()
                .set("intent", action)
                .set("title", "")
                .set("description", "")
                .set("leaveType", "")
                .set("reason", "")
                .set("startTime", "")
                .set("endTime", "");
    }

    private boolean isRepairProblem(String q) {
        if (containsAny(q, "如何报修", "怎么报修", "申请报修")) {
            return false;
        }
        return q.contains("报修") || containsAny(q,
                "关不上", "锁不上", "漏水", "断电", "没电", "无法上网", "没网",
                "坏了", "坏掉", "故障", "松动", "堵了", "破了", "异响");
    }

    private String inferRepairTitle(String q) {
        if (containsAny(q, "窗")) {
            return "宿舍窗户故障";
        }
        if (containsAny(q, "门", "锁")) {
            return "宿舍门锁故障";
        }
        if (containsAny(q, "水", "漏", "龙头")) {
            return "宿舍水电故障";
        }
        if (containsAny(q, "网", "wifi", "WiFi")) {
            return "宿舍网络故障";
        }
        if (containsAny(q, "床", "柜", "桌", "椅")) {
            return "宿舍家具故障";
        }
        return StrUtil.sub(q, 0, 15);
    }

    private String inferLeaveReason(String q) {
        if (q.contains("回家")) {
            return "回家";
        }
        if (q.contains("看病") || q.contains("医院")) {
            return "就医";
        }
        if (q.contains("办事")) {
            return "个人办事";
        }
        return "个人事由";
    }

    private void fillLeaveTime(String q, JSONObject intent) {
        LocalDate today = LocalDate.now();
        Matcher range = Pattern.compile("(\\d{1,2})月(\\d{1,2})日\\s*(\\d{1,2})(?:点|:00)?\\s*(?:到|至|-|—|~)\\s*(?:(\\d{1,2})月(\\d{1,2})日\\s*)?(\\d{1,2})(?:点|:00)?").matcher(q);
        if (range.find()) {
            int month = Integer.parseInt(range.group(1));
            int day = Integer.parseInt(range.group(2));
            int startHour = Integer.parseInt(range.group(3));
            int endMonth = StrUtil.isBlank(range.group(4)) ? month : Integer.parseInt(range.group(4));
            int endDay = StrUtil.isBlank(range.group(5)) ? day : Integer.parseInt(range.group(5));
            int endHour = Integer.parseInt(range.group(6));
            intent.set("startTime", LocalDateTime.of(today.getYear(), month, day, startHour, 0).format(DTF));
            intent.set("endTime", LocalDateTime.of(today.getYear(), endMonth, endDay, endHour, 0).format(DTF));
            return;
        }
        if (q.contains("明天") && containsAny(q, "一天", "全天")) {
            LocalDate tomorrow = today.plusDays(1);
            intent.set("startTime", LocalDateTime.of(tomorrow, LocalTime.of(8, 0)).format(DTF));
            intent.set("endTime", LocalDateTime.of(tomorrow, LocalTime.of(20, 0)).format(DTF));
        }
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // ============ 业务办理 ============

    private String submitRepair(Long userId, String role, JSONObject intent, String question) {
        if (!RoleConstant.STUDENT.equals(role)) {
            return "报修办理仅学生账号可用。如需帮助可以咨询我宿舍相关问题。";
        }
        Student student = studentService.getByUserId(userId);
        if (student == null) {
            return "你的账号未关联学生信息，暂时无法提交报修。";
        }
        String title = intent.getStr("title");
        String desc = intent.getStr("description");
        if (StrUtil.isBlank(desc)) {
            desc = question;
        }
        if (StrUtil.isBlank(title)) {
            title = StrUtil.sub(desc, 0, 15);
        }
        Repair repair = new Repair();
        repair.setTitle(title);
        repair.setDescription(desc);
        repairService.submit(userId, repair); // 内部自动关联房间 + AI 分类
        return "✅ 已为你提交报修：「" + title + "」\n"
                + "AI 判定 —— 类别【" + repair.getAiCategory() + "】 · 优先级【" + repair.getAiPriority() + "】\n"
                + "当前状态：待派单，宿管会尽快处理，你可以在「报修管理」中查看进度。";
    }

    private String requestLeave(Long userId, String role, JSONObject intent) {
        if (!RoleConstant.STUDENT.equals(role)) {
            return "请假/晚归办理仅学生账号可用。";
        }
        Student student = studentService.getByUserId(userId);
        if (student == null) {
            return "你的账号未关联学生信息，暂时无法提交申请。";
        }
        String type = intent.getStr("leaveType");
        if (!"请假".equals(type) && !"晚归".equals(type)) {
            type = "请假";
        }
        String reason = intent.getStr("reason");
        if (StrUtil.isBlank(reason)) {
            reason = "个人事由";
        }
        String startStr = intent.getStr("startTime");
        String endStr = intent.getStr("endTime");
        LocalDateTime start = parse(startStr);
        LocalDateTime end = parse(endStr);
        if (start == null || end == null) {
            return "好的，为你办理" + type + "。还需要你告诉我【开始时间】和【结束时间】，"
                    + "例如：「请假，6月10日8:00 到 6月10日18:00，回家办事」。";
        }
        LeaveRecord record = new LeaveRecord();
        record.setType(type);
        record.setReason(reason);
        record.setStartTime(start);
        record.setEndTime(end);
        leaveService.submit(userId, record);
        return "✅ 已为你提交" + type + "申请：\n"
                + "事由：" + reason + "\n"
                + "时间：" + start.format(DTF) + " ~ " + end.format(DTF) + "\n"
                + "当前状态：待审批，请等待宿管审批。";
    }

    // ============ 业务查询 ============

    private String queryMyRepairs(Long userId) {
        Student student = studentService.getByUserId(userId);
        if (student == null) {
            return "你的账号未关联学生信息，无法查询报修记录。";
        }
        List<Repair> list = repairService.lambdaQuery()
                .eq(Repair::getStudentId, student.getId())
                .orderByDesc(Repair::getId)
                .last("limit 10")
                .list();
        if (list.isEmpty()) {
            return "你目前还没有任何报修记录。如需报修，直接告诉我故障情况即可，例如「宿舍水龙头漏水」。";
        }
        StringBuilder sb = new StringBuilder("你最近的报修记录（共 " + list.size() + " 条）：\n");
        int i = 1;
        for (Repair r : list) {
            sb.append(i++).append(". 「").append(r.getTitle()).append("」 ")
                    .append("[").append(r.getStatus()).append("]")
                    .append(r.getAiCategory() != null ? " · " + r.getAiCategory() : "")
                    .append("\n");
        }
        return sb.toString().trim();
    }

    private String queryMyLeaves(Long userId) {
        Student student = studentService.getByUserId(userId);
        if (student == null) {
            return "你的账号未关联学生信息，无法查询请假记录。";
        }
        List<LeaveRecord> list = leaveService.lambdaQuery()
                .eq(LeaveRecord::getStudentId, student.getId())
                .orderByDesc(LeaveRecord::getId)
                .last("limit 10")
                .list();
        if (list.isEmpty()) {
            return "你目前还没有任何请假/晚归记录。如需申请，告诉我类型、时间和事由即可。";
        }
        StringBuilder sb = new StringBuilder("你最近的请假/晚归记录（共 " + list.size() + " 条）：\n");
        int i = 1;
        for (LeaveRecord r : list) {
            sb.append(i++).append(". ").append(r.getType()).append(" · ")
                    .append(r.getStartTime().format(DTF)).append(" ~ ").append(r.getEndTime().format(DTF))
                    .append(" [").append(r.getStatus()).append("]\n");
        }
        return sb.toString().trim();
    }

    private String queryMyDorm(Long userId) {
        Student student = studentService.getByUserId(userId);
        if (student == null) {
            return "你的账号未关联学生信息。";
        }
        if (student.getBedId() == null) {
            return "你当前还没有分配宿舍床位，请联系宿管办理入住。";
        }
        Bed bed = bedMapper.selectById(student.getBedId());
        Room room = bed != null ? roomMapper.selectById(bed.getRoomId()) : null;
        Building building = room != null ? buildingMapper.selectById(room.getBuildingId()) : null;
        return "你的宿舍信息：\n"
                + "楼栋：" + (building != null ? building.getName() : "-") + "\n"
                + "房间：" + (room != null ? room.getRoomNo() : "-") + "\n"
                + "床位：" + (bed != null ? bed.getBedNo() : "-");
    }

    // ============ 工具方法 ============

    private LocalDateTime parse(String s) {
        if (StrUtil.isBlank(s)) {
            return null;
        }
        try {
            return LocalDateTime.parse(s.trim(), DTF);
        } catch (Exception e) {
            return null;
        }
    }

    /** 记录动作类对话日志并返回回复 */
    private String logged(Long userId, String question, String answer) {
        try {
            AiChatLog log = new AiChatLog();
            log.setUserId(userId);
            log.setQuestion(question);
            log.setAnswer(answer);
            aiChatLogMapper.insert(log);
        } catch (Exception ignore) {
        }
        return answer;
    }
}
