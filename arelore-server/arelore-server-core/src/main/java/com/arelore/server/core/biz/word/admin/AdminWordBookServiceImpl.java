package com.arelore.server.core.biz.word.admin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.common.dto.PageResult;
import com.arelore.server.core.service.BaseServiceImpl;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBookRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBookResponse;
import com.arelore.server.core.biz.word.admin.entity.AdminWordBook;
import com.arelore.server.core.biz.word.admin.entity.AdminWordEntry;
import com.arelore.server.core.biz.word.admin.mapper.AdminWordBookMapper;
import com.arelore.server.core.biz.word.admin.mapper.AdminWordEntryMapper;
import com.arelore.server.core.biz.word.admin.AdminWordBookService;
import com.arelore.server.core.biz.word.admin.support.WordCodes;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AdminWordBookServiceImpl
    extends BaseServiceImpl<AdminWordBookRequest, AdminWordBookResponse, AdminWordBook>
    implements AdminWordBookService {
    private final AdminWordBookMapper mapper;
    private final AdminWordEntryMapper entryMapper;

    public AdminWordBookServiceImpl(AdminWordBookMapper mapper, AdminWordEntryMapper entryMapper) {
        this.mapper = mapper;
        this.entryMapper = entryMapper;
    }

    @Override
    protected AdminWordBookMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<AdminWordBookResponse> responseClass() {
        return AdminWordBookResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<AdminWordBook> buildWrapper(AdminWordBookRequest request) {
        LambdaQueryWrapper<AdminWordBook> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
        if (StringUtils.hasText(request.getCode())) {
            wrapper.eq(AdminWordBook::getCode, request.getCode());
        }
        if (StringUtils.hasText(request.getName())) {
            wrapper.like(AdminWordBook::getName, request.getName());
        }
        if (StringUtils.hasText(request.getLanguageCode())) {
            wrapper.eq(AdminWordBook::getLanguageCode, request.getLanguageCode());
        }
        if (StringUtils.hasText(request.getCategoryCode())) {
            wrapper.eq(AdminWordBook::getCategoryCode, request.getCategoryCode());
        }
        if (request.getStatus() != null) {
            wrapper.eq(AdminWordBook::getStatus, request.getStatus());
        }
        wrapper.orderByDesc(AdminWordBook::getId);
        return wrapper;
    }

    @Override
    public List<AdminWordBookResponse> list(AdminWordBookRequest request) {
        List<AdminWordBookResponse> list = super.list(request);
        for (AdminWordBookResponse item : list) {
            fillExtFields(item);
        }
        return list;
    }

    @Override
    public PageResult<AdminWordBookResponse> pageQuery(AdminWordBookRequest request) {
        PageResult<AdminWordBookResponse> page = super.pageQuery(request);
        if (page.getList() != null) {
            for (AdminWordBookResponse item : page.getList()) {
                fillExtFields(item);
            }
        }
        return page;
    }

    @Override
    public AdminWordBookResponse getById(Long id) {
        AdminWordBookResponse response = super.getById(id);
        fillExtFields(response);
        return response;
    }

    @Override
    public int create(AdminWordBookRequest request) {
        WordCodes.require("code", request.getCode());
        WordCodes.require("languageCode", request.getLanguageCode());
        WordCodes.require("categoryCode", request.getCategoryCode());
        return super.create(request);
    }

    @Override
    public int update(AdminWordBookRequest request) {
        AdminWordBook exists = request.getId() == null ? null : mapper.selectById(request.getId());
        if (exists == null) {
            throw new IllegalArgumentException("单词本不存在");
        }
        request.setCode(exists.getCode());
        request.setLanguageCode(exists.getLanguageCode());
        request.setCategoryCode(exists.getCategoryCode());
        return super.update(request);
    }

    @Override
    public AdminWordBookResponse getByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        LambdaQueryWrapper<AdminWordBook> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AdminWordBook::getCode, code);
        AdminWordBookResponse response = toResponse(mapper.selectOne(wrapper));
        fillExtFields(response);
        return response;
    }

    @Override
    public void refreshWordCount(String bookCode) {
        if (!StringUtils.hasText(bookCode)) {
            return;
        }
        LambdaQueryWrapper<AdminWordEntry> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(AdminWordEntry::getBookCode, bookCode);
        Long count = entryMapper.selectCount(countWrapper);
        AdminWordBookResponse book = getByCode(bookCode);
        if (book == null || book.getId() == null) {
            return;
        }
        AdminWordBook update = new AdminWordBook();
        update.setId(book.getId());
        update.setWordCount(count == null ? 0 : count.intValue());
        mapper.updateById(update);
    }

    private void fillExtFields(AdminWordBookResponse book) {
        if (book == null || !StringUtils.hasText(book.getExtInfo())) {
            return;
        }
        try {
            JSONObject ext = JSON.parseObject(book.getExtInfo());
            if (ext == null) {
                return;
            }
            String cover = ext.getString("cover");
            if (StringUtils.hasText(cover)) {
                book.setCover(cover.trim());
            }
            JSONObject origin = ext.getJSONObject("bookOrigin");
            if (origin != null) {
                String originName = origin.getString("originName");
                if (StringUtils.hasText(originName)) {
                    book.setOriginName(originName.trim());
                }
            }
        } catch (Exception ignored) {
            // extInfo 非 JSON 时忽略，不影响列表主流程
        }
    }
}
