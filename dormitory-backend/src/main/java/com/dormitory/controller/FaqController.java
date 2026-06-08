package com.dormitory.controller;

import com.dormitory.common.PreAuth;
import com.dormitory.common.Result;
import com.dormitory.common.RoleConstant;
import com.dormitory.entity.Faq;
import com.dormitory.mapper.FaqMapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * FAQ 知识库管理（供 AI 智能问答引用）
 */
@Api(tags = "FAQ知识库")
@RestController
@RequestMapping("/faq")
@RequiredArgsConstructor
public class FaqController {

    private final FaqMapper faqMapper;

    @ApiOperation("全部FAQ列表")
    @GetMapping("/list")
    public Result<List<Faq>> list() {
        return Result.success(faqMapper.selectList(null));
    }

    @ApiOperation("新增FAQ")
    @PostMapping
    @PreAuth(RoleConstant.ADMIN)
    public Result<Void> add(@RequestBody Faq faq) {
        faq.setId(null);
        faqMapper.insert(faq);
        return Result.success();
    }

    @ApiOperation("修改FAQ")
    @PutMapping
    @PreAuth(RoleConstant.ADMIN)
    public Result<Void> edit(@RequestBody Faq faq) {
        faqMapper.updateById(faq);
        return Result.success();
    }

    @ApiOperation("删除FAQ")
    @DeleteMapping("/{id}")
    @PreAuth(RoleConstant.ADMIN)
    public Result<Void> delete(@PathVariable Long id) {
        faqMapper.deleteById(id);
        return Result.success();
    }
}
