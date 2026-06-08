package com.dormitory.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dormitory.common.BusinessException;
import com.dormitory.common.PageResult;
import com.dormitory.dto.PageQuery;
import com.dormitory.entity.SysUser;
import com.dormitory.mapper.SysUserMapper;
import com.dormitory.service.SysUserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户服务实现
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private static final String DEFAULT_PASSWORD = "123456";
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public PageResult<SysUser> page(PageQuery query, String role) {
        IPage<SysUser> page = lambdaQuery()
                .eq(StringUtils.hasText(role), SysUser::getRole, role)
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(SysUser::getUsername, query.getKeyword())
                        .or().like(SysUser::getRealName, query.getKeyword()))
                .orderByDesc(SysUser::getId)
                .page(new Page<>(query.getPageNum(), query.getPageSize()));
        page.getRecords().forEach(u -> u.setPassword(null));
        return PageResult.of(page);
    }

    @Override
    public void add(SysUser user) {
        long count = lambdaQuery().eq(SysUser::getUsername, user.getUsername()).count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        user.setId(null);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        save(user);
    }

    @Override
    public void edit(SysUser user) {
        SysUser db = getById(user.getId());
        if (db == null) {
            throw new BusinessException("用户不存在");
        }
        // 不允许通过此接口改密码与用户名
        user.setPassword(null);
        user.setUsername(null);
        updateById(user);
    }

    @Override
    public void resetPassword(Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        updateById(user);
    }

    @Override
    public void changePassword(Long userId, String oldPwd, String newPwd) {
        SysUser user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPwd, user.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(passwordEncoder.encode(newPwd));
        updateById(update);
    }
}
