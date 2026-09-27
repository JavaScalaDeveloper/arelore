package com.arelore.server.core.biz.word.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.service.BaseServiceImpl;
import com.arelore.server.core.biz.word.admin.dto.AdminWordEntryRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordEntryResponse;
import com.arelore.server.core.biz.word.admin.entity.AdminWordEntry;
import com.arelore.server.core.biz.word.admin.mapper.AdminWordEntryMapper;
import com.arelore.server.core.biz.word.admin.AdminWordBookService;
import com.arelore.server.core.biz.word.admin.AdminWordEntryService;
import com.arelore.server.core.biz.word.admin.support.WordCodes;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AdminWordEntryServiceImpl
    extends BaseServiceImpl<AdminWordEntryRequest, AdminWordEntryResponse, AdminWordEntry>
    implements AdminWordEntryService {
    private final AdminWordEntryMapper mapper;
    private final AdminWordBookService bookService;

    public AdminWordEntryServiceImpl(AdminWordEntryMapper mapper, @Lazy AdminWordBookService bookService) {
        this.mapper = mapper;
        this.bookService = bookService;
    }

    @Override
    protected AdminWordEntryMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<AdminWordEntryResponse> responseClass() {
        return AdminWordEntryResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<AdminWordEntry> buildWrapper(AdminWordEntryRequest request) {
        LambdaQueryWrapper<AdminWordEntry> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
        if (StringUtils.hasText(request.getBookCode())) {
            wrapper.eq(AdminWordEntry::getBookCode, request.getBookCode());
        }
        if (StringUtils.hasText(request.getWordCode())) {
            wrapper.eq(AdminWordEntry::getWordCode, request.getWordCode());
        }
        if (StringUtils.hasText(request.getWord())) {
            wrapper.like(AdminWordEntry::getWord, request.getWord());
        }
        wrapper.orderByAsc(AdminWordEntry::getSortNo).orderByDesc(AdminWordEntry::getId);
        return wrapper;
    }

    @Override
    public int create(AdminWordEntryRequest request) {
        WordCodes.require("bookCode", request.getBookCode());
        WordCodes.require("wordCode", request.getWordCode());
        int rows = super.create(request);
        bookService.refreshWordCount(request.getBookCode());
        return rows;
    }

    @Override
    public int update(AdminWordEntryRequest request) {
        AdminWordEntry exists = request.getId() == null ? null : mapper.selectById(request.getId());
        if (exists == null) {
            throw new IllegalArgumentException("词条不存在");
        }
        request.setBookCode(exists.getBookCode());
        request.setWordCode(exists.getWordCode());
        int rows = super.update(request);
        bookService.refreshWordCount(exists.getBookCode());
        return rows;
    }

    @Override
    public int deleteById(Long id) {
        AdminWordEntry exists = mapper.selectById(id);
        int rows = super.deleteById(id);
        if (exists != null) {
            bookService.refreshWordCount(exists.getBookCode());
        }
        return rows;
    }
}
