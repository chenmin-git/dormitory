package com.dormitory.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dormitory.ai.AiAssistantService;
import com.dormitory.ai.SparkService;
import com.dormitory.common.BusinessException;
import com.dormitory.common.PageResult;
import com.dormitory.common.PreAuth;
import com.dormitory.common.Result;
import com.dormitory.common.RoleConstant;
import com.dormitory.entity.AiChatLog;
import com.dormitory.entity.AiReport;
import com.dormitory.mapper.AiChatLogMapper;
import com.dormitory.mapper.AiReportMapper;
import com.dormitory.util.UserContext;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * AI 能力接口（讯飞星火）
 */
@Api(tags = "AI智能模块")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final SparkService sparkService;
    private final AiAssistantService aiAssistantService;
    private final AiReportMapper aiReportMapper;
    private final AiChatLogMapper aiChatLogMapper;

    @ApiOperation("智能问答助手（支持办理报修/请假/查询等业务）")
    @PostMapping("/chat")
    public Result<String> chat(@RequestBody Map<String, String> body) {
        String question = body.get("question");
        if (!StringUtils.hasText(question)) {
            throw new BusinessException("问题不能为空");
        }
        return Result.success(aiAssistantService.assist(
                UserContext.getUserId(), UserContext.getRole(), question));
    }

    @ApiOperation("我的对话历史")
    @GetMapping("/chat/history")
    public Result<List<AiChatLog>> chatHistory(@RequestParam(defaultValue = "20") Integer limit) {
        return Result.success(aiChatLogMapper.recentByUser(UserContext.getUserId(), limit));
    }

    @ApiOperation("生成数据分析报告（并存入历史）")
    @GetMapping("/report")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<String> report() {
        String content = sparkService.analyzeReport();
        // 存入历史
        AiReport record = new AiReport();
        record.setContent(content);
        record.setCreatorId(UserContext.getUserId());
        aiReportMapper.insert(record);
        return Result.success(content);
    }

    @ApiOperation("历史分析报告分页")
    @GetMapping("/report/history")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<PageResult<Map<String, Object>>> reportHistory(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        IPage<Map<String, Object>> page = aiReportMapper.pageDetail(new Page<>(pageNum, pageSize));
        return Result.success(PageResult.of(page));
    }

    @ApiOperation("删除历史报告")
    @DeleteMapping("/report/{id}")
    @PreAuth({RoleConstant.ADMIN, RoleConstant.MANAGER})
    public Result<Void> deleteReport(@PathVariable Long id) {
        aiReportMapper.deleteById(id);
        return Result.success();
    }
}
