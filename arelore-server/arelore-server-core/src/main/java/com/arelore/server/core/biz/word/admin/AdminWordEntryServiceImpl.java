package com.arelore.server.core.biz.word.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.arelore.server.core.common.dto.PageResult;
import com.arelore.server.core.service.BaseServiceImpl;
import com.arelore.server.core.biz.word.admin.dto.AdminWordEntryRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordEntryResponse;
import com.arelore.server.core.biz.word.admin.entity.AdminWordEntry;
import com.arelore.server.core.biz.word.admin.mapper.AdminWordEntryMapper;
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
        // 列表不查 ext_info，避免大 JSON 拖垮分页与网络传输；详情走 getById / detail
        wrapper.select(
            AdminWordEntry::getId,
            AdminWordEntry::getCreateTime,
            AdminWordEntry::getModifyTime,
            AdminWordEntry::getBookCode,
            AdminWordEntry::getWordCode,
            AdminWordEntry::getWord,
            AdminWordEntry::getSortNo
        );
        if (request == null) {
            return wrapper;
        }
        if (request.getId() != null) {
            wrapper.eq(AdminWordEntry::getId, request.getId());
        }
        if (StringUtils.hasText(request.getBookCode())) {
            wrapper.eq(AdminWordEntry::getBookCode, request.getBookCode());
        }
        if (StringUtils.hasText(request.getWordCode())) {
            wrapper.eq(AdminWordEntry::getWordCode, request.getWordCode());
        }
        if (StringUtils.hasText(request.getWord())) {
            wrapper.likeRight(AdminWordEntry::getWord, request.getWord().trim());
        }
        // 管理端分页不做 order by，避免大表 filesort
        return wrapper;
    }

    @Override
    public PageResult<AdminWordEntryResponse> pageQuery(AdminWordEntryRequest request) {
        if (request == null) {
            request = new AdminWordEntryRequest();
        }
        int pageNum = request.getPageNum() == null || request.getPageNum() <= 0 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() <= 0 ? 20 : request.getPageSize();
        if (pageSize > 100) {
            pageSize = 100;
        }
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);

        // 全表扫描代价高；管理端必须按词本查。带 book_code 的 COUNT 走索引，可返回真实 total
        if (!StringUtils.hasText(request.getBookCode())) {
            return PageResult.empty(pageNum, pageSize);
        }

        Page<AdminWordEntry> page = new Page<>(pageNum, pageSize);
        Page<AdminWordEntry> result = mapper.selectPage(page, buildWrapper(request));
        PageResult<AdminWordEntryResponse> pageResult =
            PageResult.of(toResponses(result.getRecords()), pageNum, pageSize, result.getTotal());
        String bookName = resolveBookName(request.getBookCode());
        if (pageResult.getList() != null) {
            for (AdminWordEntryResponse item : pageResult.getList()) {
                if (item != null) {
                    item.setExtInfo(null);
                    item.setBookName(bookName);
                }
            }
        }
        return pageResult;
    }

    @Override
    public AdminWordEntryResponse getById(Long id) {
        AdminWordEntryResponse response = super.getById(id);
        if (response != null && StringUtils.hasText(response.getBookCode())) {
            response.setBookName(resolveBookName(response.getBookCode()));
        }
        return response;
    }

    private String resolveBookName(String bookCode) {
        if (!StringUtils.hasText(bookCode)) {
            return null;
        }
        var book = bookService.getByCode(bookCode);
        return book == null ? null : book.getName();
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
