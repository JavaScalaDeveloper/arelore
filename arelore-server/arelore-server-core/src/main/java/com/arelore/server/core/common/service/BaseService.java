package com.arelore.server.core.common.service;

import com.arelore.server.common.dto.PageResult;

import java.util.List;

/**
 * 通用基础服务接口：
 * - 支持增删改查、分页查询
 * - Request/Response 都应为同一数据库实体类的子类（建议：Request extends Response extends Entity）
 */
public interface BaseService<REQ, RES> {

    PageResult<RES> pageQuery(REQ request);

    List<RES> list(REQ request);

    RES getById(Long id);

    void create(REQ request);

    void update(REQ request);

    void deleteById(Long id);
}

