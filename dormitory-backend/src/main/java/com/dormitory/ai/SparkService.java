package com.dormitory.ai;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.dormitory.entity.AiChatLog;
import com.dormitory.entity.Faq;
import com.dormitory.mapper.AiChatLogMapper;
import com.dormitory.mapper.FaqMapper;
import com.dormitory.service.StatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 业务服务：封装星火大模型的三大应用场景。
 * 设计原则：AI 仅做增强，所有调用都有兜底，不阻塞主业务。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SparkService {

    private final SparkClient sparkClient;
    private final FaqMapper faqMapper;
    private final AiChatLogMapper aiChatLogMapper;
    private final StatService statService;

    /** 报修分类候选 */
    private static final String CATEGORIES = "水电、网络、家具、门窗、其他";
    private static final String PRIORITIES = "紧急、一般、低";

    // ============ 场景一：智能问答助手 ============

    /**
     * 宿舍管理智能问答。将 FAQ 知识库注入上下文做轻量 RAG。
     */
    public String chat(Long userId, String question) {
        String faqContext = buildFaqContext();
        String systemPrompt = "你是一个宿舍管理系统的智能助手，负责解答学生和宿管关于宿舍生活、规章制度、报修、请假等问题。"
                + "请用简洁、友好的中文回答。以下是宿舍常见问题知识库，请优先参考：\n" + faqContext
                + "\n如果问题超出知识库范围，请基于常识合理回答，但不要编造具体的校规数字。";

        String answer;
        try {
            answer = sparkClient.chat(systemPrompt, question, false);
        } catch (Exception e) {
            log.warn("AI问答调用失败，使用本地FAQ兜底。原因：{}", e.getMessage());
            answer = answerFromFaq(question);
        }

        // 记录对话日志
        try {
            AiChatLog logEntity = new AiChatLog();
            logEntity.setUserId(userId);
            logEntity.setQuestion(question);
            logEntity.setAnswer(answer);
            aiChatLogMapper.insert(logEntity);
        } catch (Exception e) {
            log.warn("保存AI对话日志失败", e);
        }
        return answer;
    }

    private String buildFaqContext() {
        List<Faq> faqs = faqMapper.selectList(null);
        return faqs.stream()
                .map(f -> "问：" + f.getQuestion() + " 答：" + f.getAnswer())
                .collect(Collectors.joining("\n"));
    }

    // ============ 场景二：报修智能分类 ============

    /**
     * 根据报修标题与描述，调用 AI 判断类别与优先级。
     * 失败时返回兜底值「其他/一般」，保证报修流程不被阻断。
     *
     * @return [category, priority]
     */
    public String[] classifyRepair(String title, String description) {
        String[] fallback = classifyRepairLocally(title, description);
        try {
            String systemPrompt = "你是宿舍报修工单分类助手。请根据报修内容判断【类别】和【紧急程度】，"
                    + "类别只能是其中之一：" + CATEGORIES + "；"
                    + "紧急程度只能是其中之一：" + PRIORITIES + "。"
                    + "只返回 JSON，格式为 {\"category\":\"类别\",\"priority\":\"紧急程度\"}，不要有多余文字。";
            String userPrompt = "报修标题：" + title + "\n报修描述：" + description;

            String content = sparkClient.chat(systemPrompt, userPrompt, true);
            JSONObject json = JSONUtil.parseObj(extractJson(content));
            String category = json.getStr("category", "其他");
            String priority = json.getStr("priority", "一般");
            // 校验合法性
            if (!CATEGORIES.contains(category)) {
                category = "其他";
            }
            if (!PRIORITIES.contains(priority)) {
                priority = "一般";
            }
            if (!"其他".equals(fallback[0])) {
                category = fallback[0];
            }
            if ("紧急".equals(fallback[1])) {
                priority = "紧急";
            }
            return new String[]{category, priority};
        } catch (Exception e) {
            log.warn("报修AI分类失败，使用兜底值。原因：{}", e.getMessage());
            return fallback;
        }
    }

    /** 从模型返回中提取 JSON 片段（容错） */
    private String extractJson(String content) {
        if (content == null) {
            return "{}";
        }
        int start = content.indexOf('{');
        int end = content.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return content;
    }

    private String answerFromFaq(String question) {
        String q = question == null ? "" : question;
        List<Faq> faqs = faqMapper.selectList(null);
        for (Faq faq : faqs) {
            String haystack = (faq.getQuestion() == null ? "" : faq.getQuestion())
                    + (faq.getAnswer() == null ? "" : faq.getAnswer())
                    + (faq.getCategory() == null ? "" : faq.getCategory());
            for (String keyword : new String[]{"电磁炉", "大功率", "电器", "门禁", "报修", "请假", "晚归", "卫生"}) {
                if (q.contains(keyword) && haystack.contains(keyword)) {
                    return "根据 FAQ 知识库：" + faq.getAnswer();
                }
            }
        }
        return "AI 服务暂时不可用。我可以继续帮你办理报修、请假/晚归申请，"
                + "也可以回答门禁、用电安全、卫生检查等宿舍常见问题。";
    }

    private String[] classifyRepairLocally(String title, String description) {
        String text = ((title == null ? "" : title) + " " + (description == null ? "" : description)).toLowerCase();
        String category = "其他";
        if (containsAny(text, "窗", "门", "门锁", "锁", "合页")) {
            category = "门窗";
        } else if (containsAny(text, "水", "漏", "龙头", "下水", "电", "灯", "插座", "断电", "没电")) {
            category = "水电";
        } else if (containsAny(text, "网", "wifi", "wi-fi", "网络", "无法上网")) {
            category = "网络";
        } else if (containsAny(text, "床", "柜", "衣柜", "桌", "椅")) {
            category = "家具";
        }

        String priority = "一般";
        if (containsAny(text, "关不上", "锁不上", "漏水", "积水", "断电", "没电", "冒烟", "着火", "安全")) {
            priority = "紧急";
        } else if (containsAny(text, "轻微", "偶尔", "不影响使用")) {
            priority = "低";
        }
        return new String[]{category, priority};
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    // ============ 场景三：数据分析报告 ============

    /**
     * 基于统计指标生成自然语言分析报告。
     */
    public String analyzeReport() {
        Map<String, Object> stat = statService.overview();
        String metrics = "宿舍系统当前关键指标如下：\n"
                + "- 学生总数：" + stat.get("studentCount") + " 人\n"
                + "- 房间总数：" + stat.get("roomCount") + " 间\n"
                + "- 床位总数：" + stat.get("bedTotal") + "，已入住：" + stat.get("bedUsed")
                + "，入住率：" + stat.get("occupancyRate") + "%\n"
                + "- 待处理报修：" + stat.get("pendingRepair") + " 单\n"
                + "- 报修状态分布：" + stat.get("repairStatus") + "\n"
                + "- 报修分类统计：" + stat.get("repairByCategory") + "\n"
                + "- 待审批请假/晚归：" + stat.get("pendingLeave") + " 条\n"
                + "- 请假总数：" + stat.get("leaveCount") + "，晚归总数：" + stat.get("lateCount") + "\n"
                + "- 卫生平均分：" + stat.get("hygieneAvgScore") + " 分";

        String systemPrompt = "你是宿舍管理数据分析师。请根据提供的指标，用中文写一段 150 字左右的分析报告，"
                + "包括：整体运行情况总结、值得关注的问题、以及 2-3 条具体管理建议。语言专业、条理清晰。";

        try {
            return sparkClient.chat(systemPrompt, metrics, false);
        } catch (Exception e) {
            log.warn("AI分析报告调用失败，使用本地分析兜底。原因：{}", e.getMessage());
            return "当前宿舍运行整体平稳，入住率为 " + stat.get("occupancyRate") + "%，"
                    + "待处理报修 " + stat.get("pendingRepair") + " 单，待审批请假/晚归 "
                    + stat.get("pendingLeave") + " 条。建议宿管优先处理紧急报修，"
                    + "持续关注高频报修类别，并结合卫生平均分安排重点宿舍复查。";
        }
    }
}
