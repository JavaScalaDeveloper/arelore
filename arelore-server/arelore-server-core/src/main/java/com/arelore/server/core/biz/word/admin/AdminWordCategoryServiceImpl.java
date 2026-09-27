package com.arelore.server.core.biz.word.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.service.BaseServiceImpl;
import com.arelore.server.core.biz.word.admin.dto.AdminWordCategoryRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordCategoryResponse;
import com.arelore.server.core.biz.word.admin.entity.AdminWordCategory;
import com.arelore.server.core.biz.word.admin.mapper.AdminWordCategoryMapper;
import com.arelore.server.core.biz.word.admin.AdminWordCategoryService;
import com.arelore.server.core.biz.word.admin.support.WordCodes;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminWordCategoryServiceImpl
    extends BaseServiceImpl<AdminWordCategoryRequest, AdminWordCategoryResponse, AdminWordCategory>
    implements AdminWordCategoryService {
    private final AdminWordCategoryMapper mapper;

    public AdminWordCategoryServiceImpl(AdminWordCategoryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected AdminWordCategoryMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<AdminWordCategoryResponse> responseClass() {
        return AdminWordCategoryResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<AdminWordCategory> buildWrapper(AdminWordCategoryRequest request) {
        LambdaQueryWrapper<AdminWordCategory> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
        if (StringUtils.hasText(request.getLanguageCode())) {
            wrapper.eq(AdminWordCategory::getLanguageCode, request.getLanguageCode());
        }
        if (StringUtils.hasText(request.getCode())) {
            wrapper.eq(AdminWordCategory::getCode, request.getCode());
        }
        if (StringUtils.hasText(request.getName())) {
            wrapper.like(AdminWordCategory::getName, request.getName());
        }
        if (request.getStatus() != null) {
            wrapper.eq(AdminWordCategory::getStatus, request.getStatus());
        }
        wrapper.orderByAsc(AdminWordCategory::getSortNo).orderByDesc(AdminWordCategory::getId);
        return wrapper;
    }

    @Override
    public int create(AdminWordCategoryRequest request) {
        WordCodes.require("languageCode", request.getLanguageCode());
        WordCodes.require("code", request.getCode());
        return super.create(request);
    }

    @Override
    public int update(AdminWordCategoryRequest request) {
        AdminWordCategory exists = request.getId() == null ? null : mapper.selectById(request.getId());
        if (exists == null) {
            throw new IllegalArgumentException("分类不存在");
        }
        request.setLanguageCode(exists.getLanguageCode());
        request.setCode(exists.getCode());
        return super.update(request);
    }
}
