package com.arelore.server.core.common.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.arelore.server.common.dto.PageResult;
import com.arelore.server.core.common.service.BaseService;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

/**
 * 通用基础服务实现（MyBatis-Plus）。
 *
 * @param <REQ> 请求对象（建议 extends RES）
 * @param <RES> 响应对象/持久化对象（建议 extends Entity）
 */
public abstract class BaseServiceImpl<REQ extends RES, RES> implements BaseService<REQ, RES> {

    protected abstract BaseMapper<RES> mapper();

    protected LambdaQueryWrapper<RES> buildWrapper(REQ request) {
        return new LambdaQueryWrapper<>();
    }

    @Override
    public PageResult<RES> pageQuery(REQ request) {
        int pageNum = readInt(request, "getPageNum", 1);
        int pageSize = readInt(request, "getPageSize", 10);
        Page<RES> page = new Page<>(pageNum, pageSize);
        Page<RES> result = mapper().selectPage(page, buildWrapper(request));
        return PageResult.of(result.getRecords(), pageNum, pageSize, result.getTotal());
    }

    @Override
    public List<RES> list(REQ request) {
        if (request == null) {
            return Collections.emptyList();
        }
        return mapper().selectList(buildWrapper(request));
    }

    @Override
    public RES getById(Long id) {
        return mapper().selectById(id);
    }

    @Override
    public void create(REQ request) {
        mapper().insert(request);
    }

    @Override
    public void update(REQ request) {
        mapper().updateById(request);
    }

    @Override
    public void deleteById(Long id) {
        mapper().deleteById(id);
    }

    private int readInt(Object target, String methodName, int defaultValue) {
        if (target == null) {
            return defaultValue;
        }
        Method m = ReflectionUtils.findMethod(target.getClass(), methodName);
        if (m == null) {
            return defaultValue;
        }
        try {
            Object v = ReflectionUtils.invokeMethod(m, target);
            if (v instanceof Number n) {
                return n.intValue();
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}

