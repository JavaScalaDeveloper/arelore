package com.arelore.server.core.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页数据封装
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 数据列表
     */
    private List<T> list;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 构建分页结果
     *
     * @param list     数据列表
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @param total    总记录数
     * @param <T>      数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> list, Integer pageNum, Integer pageSize, Long total) {
        int totalPages = (int) Math.ceil((double) total / pageSize);
        boolean hasPrevious = pageNum > 1;
        boolean hasNext = pageNum < totalPages;

        return new PageResult<>(pageNum, pageSize, total, totalPages, list, hasPrevious, hasNext);
    }

    /**
     * 构建空的分页结果
     *
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @param <T>      数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> empty(Integer pageNum, Integer pageSize) {
        return new PageResult<>(pageNum, pageSize, 0L, 0, List.of(), false, false);
    }
}
