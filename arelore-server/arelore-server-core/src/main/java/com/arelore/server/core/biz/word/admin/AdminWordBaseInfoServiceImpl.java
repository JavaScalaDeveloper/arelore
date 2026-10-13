package com.arelore.server.core.biz.word.admin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.arelore.server.core.service.BaseServiceImpl;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoAdoptPicturesRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoBatchSearchRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoBatchSearchResponse;
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
    /** 批量搜图每批从库拉取的无图词数 */
    private static final int BATCH_SEARCH_PAGE_SIZE = 50;
    private static final long YOUDAO_GAP_MS = 200L;
    private static final long PIXABAY_GAP_MS = 250L;
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
        // 多标签 AND：JSON_CONTAINS；无图额外兼容历史无 tags 数据
        List<String> selectedTags = resolveSelectedTags(request);
        boolean filterNoPicture = Boolean.FALSE.equals(request.getHasPicture())
            || selectedTags.contains("no_picture");
        boolean filterHasPicture = Boolean.TRUE.equals(request.getHasPicture())
            || selectedTags.contains("has_picture");
        if (filterNoPicture) {
            applyNoPictureFilter(wrapper);
        } else if (filterHasPicture) {
            applyHasPictureFilter(wrapper);
        }
        for (String t : selectedTags) {
            if ("no_picture".equals(t) || "has_picture".equals(t)) {
                continue;
            }
            applyTagContains(wrapper, t);
        }
        if (request.getPictureConfirmed() != null) {
            applyTagContains(wrapper, Boolean.TRUE.equals(request.getPictureConfirmed()) ? "confirmed" : "unconfirmed");
        }
        if (StringUtils.hasText(request.getPictureSource())) {
            applyTagContains(wrapper, "source_" + request.getPictureSource().trim().toLowerCase());
        }
        // 管理端分页不做 order by，避免大表 filesort 超时
        return wrapper;
    }

    private static List<String> resolveSelectedTags(AdminWordBaseInfoRequest request) {
        LinkedHashSet<String> tags = new LinkedHashSet<>();
        if (request.getTags() != null) {
            for (String t : request.getTags()) {
                if (!StringUtils.hasText(t)) {
                    continue;
                }
                String safe = t.trim().toLowerCase().replaceAll("[^a-z0-9_]", "");
                if (StringUtils.hasText(safe)) {
                    tags.add(safe);
                }
            }
        }
        if (tags.isEmpty() && StringUtils.hasText(request.getTag())) {
            String safe = request.getTag().trim().toLowerCase().replaceAll("[^a-z0-9_]", "");
            if (StringUtils.hasText(safe)) {
                tags.add(safe);
            }
        }
        return new ArrayList<>(tags);
    }

    private static String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private static void applyNoPictureFilter(LambdaQueryWrapper<AdminWordBaseInfo> wrapper) {
        wrapper.and(w -> w
            .apply("JSON_CONTAINS(COALESCE(ext_info, '{}'), {0}, '$.tags')", "\"no_picture\"")
            .or()
            .isNull(AdminWordBaseInfo::getExtInfo)
            .or()
            .apply("JSON_EXTRACT(ext_info, '$.pictures') IS NULL")
            .or()
            .apply("JSON_LENGTH(COALESCE(JSON_EXTRACT(ext_info, '$.pictures'), JSON_ARRAY())) = 0")
        );
    }

    private static void applyHasPictureFilter(LambdaQueryWrapper<AdminWordBaseInfo> wrapper) {
        wrapper.and(w -> w
            .apply("JSON_CONTAINS(COALESCE(ext_info, '{}'), {0}, '$.tags')", "\"has_picture\"")
            .or()
            .apply("JSON_LENGTH(COALESCE(JSON_EXTRACT(ext_info, '$.pictures'), JSON_ARRAY())) > 0")
        );
    }

    private static void applyTagContains(LambdaQueryWrapper<AdminWordBaseInfo> wrapper, String tag) {
        if (!StringUtils.hasText(tag)) {
            return;
        }
        // 仅允许安全标签字符，避免注入
        String safe = tag.trim().toLowerCase().replaceAll("[^a-z0-9_]", "");
        if (!StringUtils.hasText(safe)) {
            return;
        }
        wrapper.apply("JSON_CONTAINS(COALESCE(ext_info, '{}'), {0}, '$.tags')", "\"" + safe + "\"");
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
                upsertWordOnly(languageCode, word, result);
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
        // 第二阶段：对无释义词查有道补释义/音标（带空格跳过；不改已有配图）
        fillMissingMeaningsFromYoudao(result);
        if (!StringUtils.hasText(result.getNote())) {
            result.setNote(String.format(
                "词形同步完成；补释义：扫描 %d，写入 %d，失败 %d，跳过 %d",
                result.getMeaningScanned(),
                result.getMeaningFilled(),
                result.getMeaningFailed(),
                result.getMeaningSkipped()
            ));
        }
        return result;
    }

    /**
     * 分页扫描无释义词，调有道写入 meanings/音标；带空格词跳过；保留已有 pictures。
     */
    private void fillMissingMeaningsFromYoudao(AdminWordBaseInfoSyncResponse result) {
        long lastId = 0L;
        int pageRounds = 0;
        while (pageRounds < 100_000) {
            pageRounds++;
            LambdaQueryWrapper<AdminWordBaseInfo> wrapper = new LambdaQueryWrapper<>();
            applyNoMeaningFilter(wrapper);
            wrapper.gt(AdminWordBaseInfo::getId, lastId)
                .orderByAsc(AdminWordBaseInfo::getId)
                .last("LIMIT " + ENTRY_PAGE_SIZE);
            List<AdminWordBaseInfo> rows = mapper.selectList(wrapper);
            if (rows == null || rows.isEmpty()) {
                break;
            }
            for (AdminWordBaseInfo row : rows) {
                if (row == null || row.getId() == null) {
                    continue;
                }
                lastId = row.getId();
                result.setMeaningScanned(result.getMeaningScanned() + 1);
                String word = row.getWord() == null ? "" : row.getWord().trim();
                if (!StringUtils.hasText(word) || wordContainsSpace(word)) {
                    result.setMeaningSkipped(result.getMeaningSkipped() + 1);
                    continue;
                }
                JSONObject ext = parseExt(row.getExtInfo());
                if (hasMeanings(ext)) {
                    // 标签滞后 / 已有释义：刷新标签后跳过，避免重复打有道
                    refreshMetaTags(ext);
                    row.setExtInfo(ext.toJSONString());
                    mapper.updateById(row);
                    result.setMeaningSkipped(result.getMeaningSkipped() + 1);
                    continue;
                }
                try {
                    boolean filled = fillMeaningFromYoudao(row, ext);
                    if (filled) {
                        result.setMeaningFilled(result.getMeaningFilled() + 1);
                    } else {
                        result.setMeaningFailed(result.getMeaningFailed() + 1);
                    }
                    Thread.sleep(YOUDAO_GAP_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    result.setNote("补释义被中断；已扫描 " + result.getMeaningScanned() + " 个无释义词");
                    return;
                } catch (Exception e) {
                    result.setMeaningFailed(result.getMeaningFailed() + 1);
                    markMeaningSyncError(row, ext, e.getMessage());
                    log.warn("fill meaning failed id={} word={}: {}",
                        row.getId(), word, shortError(e.getMessage()));
                    try {
                        Thread.sleep(YOUDAO_GAP_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        result.setNote("补释义被中断；已扫描 " + result.getMeaningScanned() + " 个无释义词");
                        return;
                    }
                }
            }
            if (rows.size() < ENTRY_PAGE_SIZE) {
                break;
            }
        }
    }

    /** 无释义过滤：兼容历史无 tags / meanings 为空 */
    private static void applyNoMeaningFilter(LambdaQueryWrapper<AdminWordBaseInfo> wrapper) {
        wrapper.and(w -> w
            .apply("JSON_CONTAINS(COALESCE(ext_info, '{}'), {0}, '$.tags')", "\"no_meaning\"")
            .or()
            .isNull(AdminWordBaseInfo::getExtInfo)
            .or()
            .apply("JSON_EXTRACT(ext_info, '$.meanings') IS NULL")
            .or()
            .apply("JSON_LENGTH(COALESCE(JSON_EXTRACT(ext_info, '$.meanings'), JSON_ARRAY())) = 0")
        );
    }

    private static boolean wordContainsSpace(String word) {
        if (!StringUtils.hasText(word)) {
            return false;
        }
        for (int i = 0; i < word.length(); i++) {
            if (Character.isWhitespace(word.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasMeanings(JSONObject ext) {
        if (ext == null) {
            return false;
        }
        JSONArray meanings = ext.getJSONArray("meanings");
        return meanings != null && !meanings.isEmpty();
    }

    /**
     * 有道补释义/音标；不覆盖已有配图与确认状态。
     *
     * @return true 已写入至少一条释义
     */
    private boolean fillMeaningFromYoudao(AdminWordBaseInfo row, JSONObject ext) throws Exception {
        JSONObject slim = YoudaoDictClient.fetchSlimExt(row.getWord());
        JSONArray meanings = slim.getJSONArray("meanings");
        boolean hasMeaning = meanings != null && !meanings.isEmpty();
        boolean hasPhone = StringUtils.hasText(slim.getString("ukphone"))
            || StringUtils.hasText(slim.getString("usphone"));
        if (!hasMeaning && !hasPhone) {
            markMeaningSyncError(row, ext, "有道无释义");
            return false;
        }
        if (hasMeaning) {
            ext.put("meanings", meanings);
        }
        if (StringUtils.hasText(slim.getString("ukphone"))) {
            ext.put("ukphone", slim.getString("ukphone"));
        }
        if (StringUtils.hasText(slim.getString("usphone"))) {
            ext.put("usphone", slim.getString("usphone"));
        }
        // 仅在尚未有来源时标记；不改 pictures / pictureConfirmed
        if (!StringUtils.hasText(ext.getString("source")) || "ENTRY".equalsIgnoreCase(ext.getString("source"))) {
            ext.put("source", "YOUDAO");
        }
        ext.remove("syncError");
        ext.put("lastMeaningSyncTime", LocalDateTime.now().format(SYNC_TIME_FMT));
        refreshMetaTags(ext);
        row.setExtInfo(ext.toJSONString());
        mapper.updateById(row);
        return hasMeaning;
    }

    private void markMeaningSyncError(AdminWordBaseInfo row, JSONObject ext, String errorMsg) {
        if (row == null) {
            return;
        }
        JSONObject target = ext == null ? parseExt(row.getExtInfo()) : ext;
        target.put("syncError", shortError(errorMsg));
        target.put("lastFailTime", LocalDateTime.now().format(SYNC_TIME_FMT));
        refreshMetaTags(target);
        row.setExtInfo(target.toJSONString());
        mapper.updateById(row);
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
                resp.setNote("有道 jsonapi 未返回 pic_dict（抽象词常见）。可改用 Pixabay 图搜补图。");
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
            resp.setNote("候选仅本次返回、不落库；未采纳下次搜图不会保留。当前图源 Pixabay。");
            JSONObject ext = parseExt(row.getExtInfo());
            if (ext.containsKey("pictureCandidates")) {
                ext.remove("pictureCandidates");
                refreshMetaTags(ext);
                row.setExtInfo(ext.toJSONString());
                mapper.updateById(row);
            }
            return resp;
        } catch (Exception e) {
            throw new IllegalStateException(shortError(e.getMessage()), e);
        }
    }

    @Override
    public AdminWordBaseInfoBatchSearchResponse batchSearchPictures(AdminWordBaseInfoBatchSearchRequest request) {
        AdminWordBaseInfoBatchSearchRequest req = request == null ? new AdminWordBaseInfoBatchSearchRequest() : request;
        // limit<=0 / null：扫完全部无图词；否则最多处理 limit 个
        int maxWords = req.getLimit() == null || req.getLimit() <= 0
            ? Integer.MAX_VALUE
            : Math.max(1, Math.min(50_000, req.getLimit()));
        int perWord = req.getPicturesPerWord() == null ? 4 : Math.max(1, Math.min(12, req.getPicturesPerWord()));
        String tag = StringUtils.hasText(req.getTag()) ? req.getTag().trim() : "no_picture";
        String source = StringUtils.hasText(req.getSource()) ? req.getSource().trim().toUpperCase() : "PIXABAY";
        if (!"YOUDAO".equals(source) && !"PIXABAY".equals(source)) {
            throw new IllegalArgumentException("source 仅支持 YOUDAO 或 PIXABAY");
        }
        boolean youdao = "YOUDAO".equals(source);
        long gapMs = youdao ? YOUDAO_GAP_MS : PIXABAY_GAP_MS;

        AdminWordBaseInfoBatchSearchResponse result = new AdminWordBaseInfoBatchSearchResponse();
        result.setSource(source);

        // 按 id 游标分页；失败词本轮跳过、下次可再跑
        long lastId = 0L;
        int pageRounds = 0;
        while (result.getScanned() < maxWords && pageRounds < 100_000) {
            pageRounds++;
            AdminWordBaseInfoRequest query = new AdminWordBaseInfoRequest();
            query.setLanguageCode(req.getLanguageCode());
            query.setTag(tag);
            if ("no_picture".equalsIgnoreCase(tag)) {
                query.setHasPicture(false);
            }
            int fetchSize = Math.min(BATCH_SEARCH_PAGE_SIZE, maxWords - result.getScanned());
            LambdaQueryWrapper<AdminWordBaseInfo> wrapper = buildWrapper(query)
                .gt(AdminWordBaseInfo::getId, lastId)
                .orderByAsc(AdminWordBaseInfo::getId)
                .last("LIMIT " + fetchSize);
            List<AdminWordBaseInfo> rows = mapper.selectList(wrapper);
            if (rows == null || rows.isEmpty()) {
                break;
            }
            for (AdminWordBaseInfo row : rows) {
                if (row == null || row.getId() == null) {
                    continue;
                }
                lastId = row.getId();
                if (result.getScanned() >= maxWords) {
                    break;
                }
                result.setScanned(result.getScanned() + 1);
                if (!StringUtils.hasText(row.getWord())) {
                    result.setSkipped(result.getSkipped() + 1);
                    continue;
                }
                JSONObject ext = parseExt(row.getExtInfo());
                if (pictureCount(ext) > 0) {
                    // 已有图（含已确认）：批量搜图跳过
                    result.setSkipped(result.getSkipped() + 1);
                    continue;
                }
                try {
                    boolean filled = youdao
                        ? fillPicturesFromYoudao(row, ext, result)
                        : fillPicturesFromPixabay(row, ext, perWord, result);
                    if (filled) {
                        Thread.sleep(gapMs);
                    }
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    result.setNote("批量搜图被中断；已处理 " + result.getScanned() + " 个词");
                    return result;
                } catch (Exception e) {
                    result.setFailed(result.getFailed() + 1);
                    log.warn("batch search failed source={} id={} word={}: {}",
                        source, row.getId(), row.getWord(), shortError(e.getMessage()));
                }
            }
            if (rows.size() < fetchSize) {
                break;
            }
        }
        if (result.getScanned() == 0) {
            result.setNote("没有需要搜图的单词");
        } else if (youdao) {
            result.setNote("已分页扫完无图词；有道配图已确认可用，释义/音标等一并落库。");
        } else {
            result.setNote("已分页扫完无图词并写入配图（仍为未确认），请人工核对后采纳锁定；图源 Pixabay。");
        }
        return result;
    }

    /** Pixabay：写入未确认配图；无候选计 failed */
    private boolean fillPicturesFromPixabay(
        AdminWordBaseInfo row,
        JSONObject ext,
        int perWord,
        AdminWordBaseInfoBatchSearchResponse result
    ) throws Exception {
        List<Map<String, String>> candidates = stockImageClient.search(row.getWord(), 1);
        JSONArray pictures = new JSONArray();
        int take = Math.min(perWord, candidates.size());
        for (int i = 0; i < take; i++) {
            Map<String, String> c = candidates.get(i);
            if (c == null || !StringUtils.hasText(c.get("url"))) {
                continue;
            }
            JSONObject item = new JSONObject();
            item.put("url", YoudaoDictClient.normalizePictureUrl(c.get("url")));
            item.put("source", "PIXABAY");
            if (StringUtils.hasText(c.get("photographer"))) {
                item.put("photographer", c.get("photographer"));
            }
            pictures.add(item);
        }
        if (pictures.isEmpty()) {
            result.setFailed(result.getFailed() + 1);
            return false;
        }
        ext.put("pictures", pictures);
        ext.put("pictureSource", "PIXABAY");
        ext.put("pictureConfirmed", false);
        ext.remove("pictureConfirmedAt");
        ext.remove("syncError");
        ext.put("lastBatchSearchTime", LocalDateTime.now().format(SYNC_TIME_FMT));
        refreshMetaTags(ext);
        row.setExtInfo(ext.toJSONString());
        mapper.updateById(row);
        result.setFilled(result.getFilled() + 1);
        return true;
    }

    /** 有道：有图则写入并直接确认；释义/音标等一并落库；无图但有释义仍保存，计 failed */
    private boolean fillPicturesFromYoudao(
        AdminWordBaseInfo row,
        JSONObject ext,
        AdminWordBaseInfoBatchSearchResponse result
    ) throws Exception {
        JSONObject slim = YoudaoDictClient.fetchSlimExt(row.getWord());
        boolean hasPic = pictureCount(slim) > 0;
        JSONArray meanings = slim.getJSONArray("meanings");
        boolean hasMeaning = meanings != null && !meanings.isEmpty();
        boolean hasPhone = StringUtils.hasText(slim.getString("ukphone"))
            || StringUtils.hasText(slim.getString("usphone"));
        if (!hasPic && !hasMeaning && !hasPhone) {
            result.setFailed(result.getFailed() + 1);
            return false;
        }
        if (hasPic) {
            ext.put("pictures", slim.getJSONArray("pictures"));
            ext.put("pictureSource", "YOUDAO");
            ext.put("pictureConfirmed", true);
            ext.put("pictureConfirmedAt", LocalDateTime.now().format(SYNC_TIME_FMT));
        }
        ext.put("source", "YOUDAO");
        if (hasMeaning) {
            ext.put("meanings", meanings);
        }
        if (StringUtils.hasText(slim.getString("ukphone"))) {
            ext.put("ukphone", slim.getString("ukphone"));
        }
        if (StringUtils.hasText(slim.getString("usphone"))) {
            ext.put("usphone", slim.getString("usphone"));
        }
        ext.remove("syncError");
        ext.remove("pictureCandidates");
        ext.put("lastBatchSearchTime", LocalDateTime.now().format(SYNC_TIME_FMT));
        refreshMetaTags(ext);
        row.setExtInfo(ext.toJSONString());
        mapper.updateById(row);
        if (hasPic) {
            result.setFilled(result.getFilled() + 1);
        } else {
            // 无图但已写入释义/音标
            result.setFailed(result.getFailed() + 1);
        }
        return true;
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
        String pictureSource = StringUtils.hasText(request.getPictureSource())
            ? request.getPictureSource().trim().toUpperCase()
            : (StringUtils.hasText(stockImageClient.activeSource())
                ? stockImageClient.activeSource()
                : "PIXABAY");
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
        refreshMetaTags(ext);
        row.setExtInfo(ext.toJSONString());
        mapper.updateById(row);
        return getById(row.getId());
    }

    /** 仅落库语种+词形；已存在则跳过，不调有道、不改配图 */
    private void upsertWordOnly(String languageCode, String word, AdminWordBaseInfoSyncResponse result) {
        AdminWordBaseInfo exists = mapper.selectOne(new LambdaQueryWrapper<AdminWordBaseInfo>()
            .eq(AdminWordBaseInfo::getLanguageCode, languageCode)
            .eq(AdminWordBaseInfo::getWord, word)
            .last("limit 1"));
        if (exists != null) {
            result.setSkipped(result.getSkipped() + 1);
            return;
        }
        JSONObject ext = new JSONObject();
        ext.put("source", "ENTRY");
        ext.put("lastSyncTime", LocalDateTime.now().format(SYNC_TIME_FMT));
        refreshMetaTags(ext);
        saveExt(null, languageCode, word, ext.toJSONString(), result);
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

    /**
     * 根据配图/确认/来源/释义刷新检索标签（写入 ext_info，无需额外表字段）。
     * 标签示例：no_picture、has_picture、confirmed、source_pixabay、has_meaning、sync_error
     */
    static void refreshMetaTags(JSONObject ext) {
        if (ext == null) {
            return;
        }
        int picCount = pictureCount(ext);
        boolean hasPicture = picCount > 0;
        boolean confirmed = Boolean.TRUE.equals(ext.getBoolean("pictureConfirmed"));
        String pictureSource = ext.getString("pictureSource");
        if (!StringUtils.hasText(pictureSource)) {
            pictureSource = hasPicture ? "YOUDAO" : "NONE";
        } else {
            pictureSource = pictureSource.trim().toUpperCase();
        }
        if (!hasPicture) {
            pictureSource = "NONE";
        }
        ext.put("pictureSource", pictureSource);
        ext.put("hasPicture", hasPicture);
        ext.put("pictureCount", picCount);

        JSONArray meanings = ext.getJSONArray("meanings");
        boolean hasMeaning = meanings != null && !meanings.isEmpty();
        boolean syncError = StringUtils.hasText(ext.getString("syncError"));

        LinkedHashSet<String> tags = new LinkedHashSet<>();
        tags.add(hasPicture ? "has_picture" : "no_picture");
        tags.add(confirmed ? "confirmed" : "unconfirmed");
        tags.add("source_" + pictureSource.toLowerCase());
        tags.add(hasMeaning ? "has_meaning" : "no_meaning");
        if (syncError) {
            tags.add("sync_error");
        }
        ext.put("tags", new ArrayList<>(tags));
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
