package com.arelore.server.core.biz.word.admin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.arelore.server.core.service.BaseServiceImpl;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoAdoptPicturesRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoPictureSearchResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoSyncResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoYoudaoTestResponse;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBookResponse;
import com.arelore.server.core.biz.word.admin.entity.AdminWordBaseInfo;
import com.arelore.server.core.biz.word.admin.entity.AdminWordEntry;
import com.arelore.server.core.biz.word.admin.mapper.AdminWordBaseInfoMapper;
import com.arelore.server.core.biz.word.admin.mapper.AdminWordEntryMapper;
import com.arelore.server.core.biz.word.admin.support.StockImageClient;
import com.arelore.server.core.biz.word.admin.support.YoudaoDictClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
    private final StockImageClient stockImageClient;

    public AdminWordBaseInfoServiceImpl(
        AdminWordBaseInfoMapper mapper,
        AdminWordEntryMapper entryMapper,
        AdminWordBookService bookService,
        StockImageClient stockImageClient
    ) {
        this.mapper = mapper;
        this.entryMapper = entryMapper;
        this.bookService = bookService;
        this.stockImageClient = stockImageClient;
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
            // 管理端分页不做 order by，避免大表 filesort 超时
            return wrapper;
        }
        if (StringUtils.hasText(request.getLanguageCode())) {
            wrapper.eq(AdminWordBaseInfo::getLanguageCode, request.getLanguageCode().trim());
        }
        if (StringUtils.hasText(request.getWord())) {
            wrapper.likeRight(AdminWordBaseInfo::getWord, request.getWord().trim());
        }
        // 管理端分页不做 order by，避免大表 filesort 超时
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

    @Override
    public AdminWordBaseInfoYoudaoTestResponse testYoudao(String word) {
        AdminWordBaseInfoYoudaoTestResponse resp = new AdminWordBaseInfoYoudaoTestResponse();
        String q = word == null ? "" : word.trim();
        resp.setWord(q);
        if (!StringUtils.hasText(q)) {
            resp.setSuccess(false);
            resp.setError("word 不能为空");
            return resp;
        }
        try {
            YoudaoDictClient.RawFetch raw = YoudaoDictClient.fetchRoot(q);
            resp.setHttpStatus(raw.httpStatus);
            resp.setElapsedMs(raw.elapsedMs);
            JSONObject root = raw.root;
            JSONObject meta = root.getJSONObject("meta");
            List<String> dicts = new ArrayList<>();
            if (meta != null) {
                JSONArray arr = meta.getJSONArray("dicts");
                if (arr != null) {
                    for (int i = 0; i < arr.size(); i++) {
                        String d = arr.getString(i);
                        if (StringUtils.hasText(d)) {
                            dicts.add(d);
                        }
                    }
                }
            }
            resp.setDicts(dicts);
            boolean hasPicDict = root.containsKey("pic_dict") || dicts.contains("pic_dict");
            resp.setHasPicDict(hasPicDict);
            JSONObject slim = YoudaoDictClient.toSlimExt(root);
            resp.setSlimExt(slim);
            List<String> urls = new ArrayList<>();
            JSONArray pics = slim.getJSONArray("pictures");
            if (pics != null) {
                for (int i = 0; i < pics.size(); i++) {
                    JSONObject p = pics.getJSONObject(i);
                    if (p != null && StringUtils.hasText(p.getString("url"))) {
                        urls.add(p.getString("url"));
                    }
                }
            }
            resp.setPictureUrls(urls);
            resp.setPictureCount(urls.size());
            resp.setSuccess(true);
            if (!hasPicDict) {
                resp.setNote("有道 jsonapi 未返回 pic_dict（抽象词常见）。可改用 Unsplash 图搜补图。");
            } else if (urls.isEmpty()) {
                resp.setNote("有 pic_dict 但未解析到可用图片 URL。");
            } else {
                resp.setNote("已从 pic_dict 解析到配图。");
            }
        } catch (Exception e) {
            resp.setSuccess(false);
            resp.setError(shortError(e.getMessage()));
            resp.setNote("请求有道失败，未落库。");
        }
        return resp;
    }

    @Override
    public AdminWordBaseInfoPictureSearchResponse searchStockPictures(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID 不能为空");
        }
        AdminWordBaseInfo row = mapper.selectById(id);
        if (row == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        AdminWordBaseInfoPictureSearchResponse resp = new AdminWordBaseInfoPictureSearchResponse();
        resp.setId(row.getId());
        resp.setWord(row.getWord());
        String source = stockImageClient.activeSource();
        resp.setSource(source);
        try {
            List<Map<String, String>> candidates = stockImageClient.search(row.getWord());
            resp.setCandidates(candidates);
            resp.setNote("候选仅本次返回、不落库；未采纳下次搜图不会保留。当前图源 "
                + source + "：无水印、可免费使用（建议保留摄影师署名）。");
            // 明确清理历史误存的候选，避免未采纳图残留
            JSONObject ext = parseExt(row.getExtInfo());
            if (ext.containsKey("pictureCandidates")) {
                ext.remove("pictureCandidates");
                row.setExtInfo(ext.toJSONString());
                mapper.updateById(row);
            }
            return resp;
        } catch (Exception e) {
            throw new IllegalStateException(shortError(e.getMessage()), e);
        }
    }

    @Override
    public AdminWordBaseInfoResponse adoptPictures(AdminWordBaseInfoAdoptPicturesRequest request) {
        if (request == null || request.getId() == null) {
            throw new IllegalArgumentException("ID 不能为空");
        }
        List<String> urls = normalizeAdoptUrls(request.getPictureUrls());
        if (urls.isEmpty()) {
            throw new IllegalArgumentException("请至少勾选 1 张图片采纳");
        }
        AdminWordBaseInfo row = mapper.selectById(request.getId());
        if (row == null) {
            throw new IllegalArgumentException("记录不存在");
        }
        String pictureSource = StringUtils.hasText(stockImageClient.activeSource())
            ? stockImageClient.activeSource()
            : "UNSPLASH";
        JSONObject ext = parseExt(row.getExtInfo());
        JSONArray pictures = new JSONArray();
        for (String url : urls) {
            JSONObject item = new JSONObject();
            item.put("url", url);
            item.put("source", pictureSource);
            pictures.add(item);
        }
        ext.put("pictures", pictures);
        ext.put("pictureSource", pictureSource);
        ext.put("pictureConfirmed", true);
        ext.put("pictureConfirmedAt", LocalDateTime.now().format(SYNC_TIME_FMT));
        ext.remove("pictureCandidates");
        ext.remove("syncError");
        row.setExtInfo(ext.toJSONString());
        mapper.updateById(row);
        return getById(row.getId());
    }

    private void upsertFromYoudao(String languageCode, String word, AdminWordBaseInfoSyncResponse result) {
        AdminWordBaseInfo exists = mapper.selectOne(new LambdaQueryWrapper<AdminWordBaseInfo>()
            .eq(AdminWordBaseInfo::getLanguageCode, languageCode)
            .eq(AdminWordBaseInfo::getWord, word)
            .last("limit 1"));
        try {
            JSONObject slim = YoudaoDictClient.fetchSlimExt(word);
            if (!isUsefulSlim(slim)) {
                // 本次无有效数据：不覆盖上次成功结果，只记短错误
                retainOnFailure(exists, languageCode, word, "无有效数据", result);
                return;
            }
            slim.put("lastSyncTime", LocalDateTime.now().format(SYNC_TIME_FMT));
            slim.remove("syncError");
            slim.remove("lastFailTime");
            slim.remove("pictureCandidates");
            if (exists != null) {
                JSONObject oldExt = parseExt(exists.getExtInfo());
                // 人工确认过的配图：禁止自动覆盖
                if (Boolean.TRUE.equals(oldExt.getBoolean("pictureConfirmed"))) {
                    preserveConfirmedPictures(slim, oldExt);
                } else if (pictureCount(slim) == 0) {
                    // 新结果无图但旧结果有图：保留旧图，避免偶发空 pic_dict 冲掉历史配图
                    JSONArray oldPics = oldExt.getJSONArray("pictures");
                    if (oldPics != null && !oldPics.isEmpty()) {
                        slim.put("pictures", oldPics);
                    }
                }
            }
            saveExt(exists, languageCode, word, slim.toJSONString(), result);
        } catch (Exception e) {
            String msg = shortError(e.getMessage());
            log.warn("sync word failed language={} word={}: {}", languageCode, word, msg);
            retainOnFailure(exists, languageCode, word, msg, result);
        }
    }

    private static void preserveConfirmedPictures(JSONObject slim, JSONObject oldExt) {
        JSONArray oldPics = oldExt.getJSONArray("pictures");
        if (oldPics != null) {
            slim.put("pictures", oldPics);
        }
        slim.put("pictureConfirmed", true);
        if (StringUtils.hasText(oldExt.getString("pictureConfirmedAt"))) {
            slim.put("pictureConfirmedAt", oldExt.getString("pictureConfirmedAt"));
        }
        if (StringUtils.hasText(oldExt.getString("pictureSource"))) {
            slim.put("pictureSource", oldExt.getString("pictureSource"));
        }
    }

    private static List<String> normalizeAdoptUrls(List<String> raw) {
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        if (raw != null) {
            for (String url : raw) {
                String value = YoudaoDictClient.normalizePictureUrl(url);
                if (StringUtils.hasText(value)) {
                    unique.add(value);
                }
            }
        }
        return new ArrayList<>(unique);
    }

    /** 异常/无数据时：保留原 pictures/音标等，仅写入短错误信息 */
    private void retainOnFailure(
        AdminWordBaseInfo exists,
        String languageCode,
        String word,
        String errorMsg,
        AdminWordBaseInfoSyncResponse result
    ) {
        result.setFailed(result.getFailed() + 1);
        JSONObject ext = exists == null ? new JSONObject() : parseExt(exists.getExtInfo());
        if (!ext.containsKey("source")) {
            ext.put("source", "YOUDAO");
        }
        ext.put("syncError", shortError(errorMsg));
        ext.put("lastFailTime", LocalDateTime.now().format(SYNC_TIME_FMT));
        if (exists == null) {
            AdminWordBaseInfo row = new AdminWordBaseInfo();
            row.setLanguageCode(languageCode);
            row.setWord(word);
            row.setExtInfo(ext.toJSONString());
            mapper.insert(row);
            result.setInserted(result.getInserted() + 1);
        } else {
            exists.setExtInfo(ext.toJSONString());
            mapper.updateById(exists);
            // 不计入 updated：业务数据未刷新
        }
    }

    private void saveExt(
        AdminWordBaseInfo exists,
        String languageCode,
        String word,
        String extInfo,
        AdminWordBaseInfoSyncResponse result
    ) {
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

    private static boolean isUsefulSlim(JSONObject slim) {
        if (slim == null || slim.isEmpty()) {
            return false;
        }
        if (pictureCount(slim) > 0) {
            return true;
        }
        return StringUtils.hasText(slim.getString("ukphone")) || StringUtils.hasText(slim.getString("usphone"));
    }

    private static int pictureCount(JSONObject slim) {
        if (slim == null) {
            return 0;
        }
        JSONArray pics = slim.getJSONArray("pictures");
        return pics == null ? 0 : pics.size();
    }

    private static JSONObject parseExt(String extInfo) {
        if (!StringUtils.hasText(extInfo)) {
            return new JSONObject();
        }
        try {
            JSONObject obj = JSON.parseObject(extInfo);
            return obj == null ? new JSONObject() : obj;
        } catch (Exception e) {
            return new JSONObject();
        }
    }

    private static String shortError(String message) {
        if (!StringUtils.hasText(message)) {
            return "同步失败";
        }
        String value = message.trim().replaceAll("\\s+", " ");
        if (value.length() > 80) {
            return value.substring(0, 80);
        }
        return value;
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
