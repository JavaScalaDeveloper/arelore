package com.arelore.server.core.biz.word.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.service.BaseServiceImpl;
import com.arelore.server.core.biz.word.admin.dto.AdminWordLanguageRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordLanguageResponse;
import com.arelore.server.core.biz.word.admin.entity.AdminWordLanguage;
import com.arelore.server.core.biz.word.admin.mapper.AdminWordLanguageMapper;
import com.arelore.server.core.biz.word.admin.AdminWordLanguageService;
import com.arelore.server.core.biz.word.admin.support.WordCodes;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminWordLanguageServiceImpl
    extends BaseServiceImpl<AdminWordLanguageRequest, AdminWordLanguageResponse, AdminWordLanguage>
    implements AdminWordLanguageService {
    private final AdminWordLanguageMapper mapper;

    public AdminWordLanguageServiceImpl(AdminWordLanguageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected AdminWordLanguageMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<AdminWordLanguageResponse> responseClass() {
        return AdminWordLanguageResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<AdminWordLanguage> buildWrapper(AdminWordLanguageRequest request) {
        LambdaQueryWrapper<AdminWordLanguage> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
        if (StringUtils.hasText(request.getCode())) {
            wrapper.eq(AdminWordLanguage::getCode, request.getCode());
        }
        if (StringUtils.hasText(request.getName())) {
            wrapper.like(AdminWordLanguage::getName, request.getName());
        }
        if (request.getStatus() != null) {
            wrapper.eq(AdminWordLanguage::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(AdminWordLanguage::getId);
        return wrapper;
    }

    @Override
    public int create(AdminWordLanguageRequest request) {
        WordCodes.require("code", request.getCode());
        return super.create(request);
    }

    @Override
    public int update(AdminWordLanguageRequest request) {
        AdminWordLanguage exists = request.getId() == null ? null : mapper.selectById(request.getId());
        if (exists == null) {
            throw new IllegalArgumentException("语种不存在");
        }
        request.setCode(exists.getCode());
        return super.update(request);
    }
}
