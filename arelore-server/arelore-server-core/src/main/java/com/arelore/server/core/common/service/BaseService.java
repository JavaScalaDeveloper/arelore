package com.arelore.server.core.common.service;

import com.arelore.server.core.common.dto.PageResult;

import java.util.List;

/**
 * 通用基础服务接口：
 * - 支持增删改查、分页查询
 * - Request/Response 都应为同一数据库实体类的子类（建议：Request extends Response extends Entity）
 */
public interface BaseService<REQ, RES> {
    /**
     * 分页查询。
     *
     * @param request 查询请求（可包含分页参数与过滤条件）
     * @return 分页结果
     */
    PageResult<RES> pageQuery(REQ request);

    /**
     * 列表查询（不分页）。
     *
     * @param request 查询请求（过滤条件）
     * @return 查询结果列表
     */
    List<RES> list(REQ request);

    /**
     * 按主键查询单条数据。
     *
     * @param id 主键ID
     * @return 响应对象，不存在时返回 null
     */
    RES getById(Long id);

    /**
     * 新增一条数据。
     *
     * @param request 新增请求
     * @return 受影响行数
     */
    int create(REQ request);

    /**
     * 按主键更新一条数据。
     *
     * @param request 更新请求（需包含主键）
     * @return 受影响行数
     */
    int update(REQ request);

    /**
     * 按主键删除一条数据。
     *
     * @param id 主键ID
     * @return 受影响行数
     */
    int deleteById(Long id);
}

