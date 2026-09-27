package com.arelore.server.core.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.arelore.server.core.common.dto.PageResult;
import com.arelore.server.core.service.BaseService;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 通用基础服务实现（MyBatis-Plus）。
 *
 * @param <REQ> 请求对象（建议 extends RES）
 * @param <RES> 响应对象（继承实体类）
 * @param <ENTITY> 实体类（Mapper 的真实返回类型）
 */
public abstract class BaseServiceImpl<REQ extends RES, RES, ENTITY> implements BaseService<REQ, RES> {

    protected abstract BaseMapper<ENTITY> mapper();

    protected abstract Class<RES> responseClass();

    protected LambdaQueryWrapper<ENTITY> buildWrapper(REQ request) {
        return new LambdaQueryWrapper<>();
    }

    @Override
    public PageResult<RES> pageQuery(REQ request) {
        int pageNum = readInt(request, "getPageNum", 1);
        int pageSize = readInt(request, "getPageSize", 10);
        Page<ENTITY> page = new Page<>(pageNum, pageSize);
        Page<ENTITY> result = mapper().selectPage(page, buildWrapper(request));
        return PageResult.of(toResponses(result.getRecords()), pageNum, pageSize, result.getTotal());
    }

    @Override
    public List<RES> list(REQ request) {
        if (request == null) {
            return Collections.emptyList();
        }
        return toResponses(mapper().selectList(buildWrapper(request)));
    }

    @Override
    public RES getById(Long id) {
        ENTITY entity = mapper().selectById(id);
        return toResponse(entity);
    }

    @Override
    public int create(REQ request) {
        // Request/Response 均继承实体类，因此可以直接作为实体插入
        return mapper().insert((ENTITY) request);
    }

    @Override
    public int update(REQ request) {
        return mapper().updateById((ENTITY) request);
    }

    @Override
    public int deleteById(Long id) {
        return mapper().deleteById(id);
    }

    protected RES toResponse(ENTITY entity) {
        if (entity == null) {
            return null;
        }
        Class<RES> resClz = responseClass();
        if (resClz.isInstance(entity)) {
            return resClz.cast(entity);
        }
        try {
            RES res = resClz.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(entity, res);
            return res;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to convert entity to response: " + resClz.getName(), e);
        }
    }

    protected List<RES> toResponses(List<ENTITY> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }
        List<RES> list = new ArrayList<>(entities.size());
        for (ENTITY e : entities) {
            list.add(toResponse(e));
        }
        return list;
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

