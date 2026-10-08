package com.arelore.server.core.biz.word.admin;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.arelore.server.core.service.BaseServiceImpl;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoSyncResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBookResponse;
import com.arelore.server.core.biz.word.admin.entity.AdminWordBaseInfo;
import com.arelore.server.core.biz.word.admin.entity.AdminWordEntry;
import com.arelore.server.core.biz.word.admin.mapper.AdminWordBaseInfoMapper;
import com.arelore.server.core.biz.word.admin.mapper.AdminWordEntryMapper;
import com.arelore.server.core.biz.word.admin.support.YoudaoDictClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AdminWordBaseInfoServiceImpl
    extends BaseServiceImpl<AdminWordBaseInfoRequest, AdminWordBaseInfoResponse, AdminWordBaseInfo>
    implements AdminWordBaseInfoService {
    private static final Logger log = LoggerFactory.getLogger(AdminWordBaseInfoServiceImpl.class);
    private static final int ENTRY_PAGE_SIZE = 200;
    private static final long YOUDAO_GAP_MS = 200L;
    private static final DateTimeFormatter SYNC_TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final AdminWordBaseInfoMapper mapper;
    private final AdminWordEntryMapper entryMapper;
    private final AdminWordBookService bookService;

    public AdminWordBaseInfoServiceImpl(
        AdminWordBaseInfoMapper mapper,
        AdminWordEntryMapper entryMapper,
        AdminWordBookService bookService
    ) {
        this.mapper = mapper;
        this.entryMapper = entryMapper;
        this.bookService = bookService;
    }

    @Override
    protected AdminWordBaseInfoMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<AdminWordBaseInfoResponse> responseClass() {
        return AdminWordBaseInfoResponse.class;
    }

    @Override
    protected LambdaQueryWrapper<AdminWordBaseInfo> buildWrapper(AdminWordBaseInfoRequest request) {
        LambdaQueryWrapper<AdminWordBaseInfo> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            return wrapper.orderByDesc(AdminWordBaseInfo::getId);
        }
        if (StringUtils.hasText(request.getLanguageCode())) {
            wrapper.eq(AdminWordBaseInfo::getLanguageCode, request.getLanguageCode().trim());
        }
        if (StringUtils.hasText(request.getWord())) {
            wrapper.likeRight(AdminWordBaseInfo::getWord, request.getWord().trim());
        }
        wrapper.orderByDesc(AdminWordBaseInfo::getId);
        return wrapper;
    }

    @Override
    public AdminWordBaseInfoSyncResponse syncFromEntries() {
        AdminWordBaseInfoSyncResponse result = new AdminWordBaseInfoSyncResponse();
        Map<String, String> bookLanguageCache = new HashMap<>();
        Set<String> seen = new HashSet<>();
        long lastId = 0L;
        int pageNum = 1;

        while (true) {
            LambdaQueryWrapper<AdminWordEntry> wrapper = new LambdaQueryWrapper<>();
            wrapper.select(AdminWordEntry::getId, AdminWordEntry::getBookCode, AdminWordEntry::getWord)
                .gt(AdminWordEntry::getId, lastId)
                .orderByAsc(AdminWordEntry::getId);
            Page<AdminWordEntry> page = new Page<>(1, ENTRY_PAGE_SIZE, false);
            List<AdminWordEntry> rows = entryMapper.selectPage(page, wrapper).getRecords();
            if (rows == null || rows.isEmpty()) {
                break;
            }
            for (AdminWordEntry entry : rows) {
                if (entry == null || entry.getId() == null) {
                    continue;
                }
                lastId = entry.getId();
                result.setScanned(result.getScanned() + 1);
                String word = entry.getWord() == null ? "" : entry.getWord().trim();
                if (!StringUtils.hasText(word)) {
                    result.setSkipped(result.getSkipped() + 1);
                    continue;
                }
                String languageCode = resolveLanguageCode(entry.getBookCode(), bookLanguageCache);
                if (!StringUtils.hasText(languageCode)) {
                    languageCode = "EN";
                }
                String key = languageCode + "\0" + word;
                if (!seen.add(key)) {
                    continue;
                }
                result.setDistinct(result.getDistinct() + 1);
                try {
                    upsertFromYoudao(languageCode, word, result);
                    Thread.sleep(YOUDAO_GAP_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("sync interrupted at word={}", word);
                    return result;
                } catch (Exception e) {
                    result.setFailed(result.getFailed() + 1);
                    log.warn("sync word failed language={} word={}: {}", languageCode, word, e.getMessage());
                }
            }
            if (rows.size() < ENTRY_PAGE_SIZE) {
                break;
            }
            pageNum++;
            if (pageNum > 100_000) {
                log.error("sync safety break after too many pages");
                break;
            }
        }
        return result;
    }

    private void upsertFromYoudao(String languageCode, String word, AdminWordBaseInfoSyncResponse result)
        throws Exception {
        JSONObject slim = YoudaoDictClient.fetchSlimExt(word);
        slim.put("lastSyncTime", LocalDateTime.now().format(SYNC_TIME_FMT));
        String extInfo = slim.toJSONString();

        AdminWordBaseInfo exists = mapper.selectOne(new LambdaQueryWrapper<AdminWordBaseInfo>()
            .eq(AdminWordBaseInfo::getLanguageCode, languageCode)
            .eq(AdminWordBaseInfo::getWord, word)
            .last("limit 1"));
        if (exists == null) {
            AdminWordBaseInfo row = new AdminWordBaseInfo();
            row.setLanguageCode(languageCode);
            row.setWord(word);
            row.setExtInfo(extInfo);
            mapper.insert(row);
            result.setInserted(result.getInserted() + 1);
        } else {
            exists.setExtInfo(extInfo);
            mapper.updateById(exists);
            result.setUpdated(result.getUpdated() + 1);
        }
    }

    private String resolveLanguageCode(String bookCode, Map<String, String> cache) {
        if (!StringUtils.hasText(bookCode)) {
            return "EN";
        }
        String code = bookCode.trim();
        if (cache.containsKey(code)) {
            return cache.get(code);
        }
        AdminWordBookResponse book = bookService.getByCode(code);
        String languageCode = book != null && StringUtils.hasText(book.getLanguageCode())
            ? book.getLanguageCode().trim()
            : "EN";
        cache.put(code, languageCode);
        return languageCode;
    }
}
