package com.arelore.server.core.biz.word.admin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.arelore.server.core.service.BaseServiceImpl;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoAdoptPicturesRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoBatchMeaningAudioRequest;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBaseInfoBatchMeaningAudioResponse;
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
import com.arelore.server.core.biz.word.admin.dto.AdminWordPictureContentResponse;
import com.arelore.server.core.biz.word.admin.support.MeaningSpeechTextBuilder;
import com.arelore.server.core.biz.word.admin.support.EdgeTtsClient;
import com.arelore.server.core.biz.word.admin.support.StockImageClient;
import com.arelore.server.core.biz.word.admin.support.WordPictureBinaryStore;
import com.arelore.server.core.biz.word.admin.support.YoudaoDictClient;
import com.arelore.server.core.common.time.ChinaTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
    /** 默认 TTS 客户端并发；服务端通常 TTS_MAX_CONCURRENT=1，多请求会排队 */
    private static final int DEFAULT_TTS_PARALLELISM = 2;
    private final AdminWordBaseInfoMapper mapper;
    private final AdminWordEntryMapper entryMapper;
    private final AdminWordBookService bookService;
    private final StockImageClient stockImageClient;
    private final WordPictureBinaryStore pictureBinaryStore;
    private final EdgeTtsClient edgeTtsClient;
    private final int ttsParallelism;

    public AdminWordBaseInfoServiceImpl(
        AdminWordBaseInfoMapper mapper,
        AdminWordEntryMapper entryMapper,
        AdminWordBookService bookService,
        StockImageClient stockImageClient,
        WordPictureBinaryStore pictureBinaryStore,
        EdgeTtsClient edgeTtsClient,
        @Value("${arelore.tts.edge.parallelism:4}") int ttsParallelism
    ) {
        this.mapper = mapper;
        this.entryMapper = entryMapper;
        this.bookService = bookService;
        this.stockImageClient = stockImageClient;
        this.pictureBinaryStore = pictureBinaryStore;
        this.edgeTtsClient = edgeTtsClient;
        this.ttsParallelism = ttsParallelism > 0 ? Math.min(8, ttsParallelism) : DEFAULT_TTS_PARALLELISM;
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
        // 多标签 AND：JSON_CONTAINS；无图/无释义语音额外兼容历史无 tags 数据
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
        boolean filterNoMeaningAudio = selectedTags.contains("no_meaning_audio");
        boolean filterHasMeaningAudio = selectedTags.contains("has_meaning_audio");
        if (filterNoMeaningAudio) {
            applyNoMeaningAudioFilter(wrapper);
        } else if (filterHasMeaningAudio) {
            applyHasMeaningAudioFilter(wrapper);
        }
        boolean filterNoExample = selectedTags.contains("no_example");
        boolean filterHasExample = selectedTags.contains("has_example");
        if (filterNoExample) {
            applyNoExampleFilter(wrapper);
        } else if (filterHasExample) {
            applyHasExampleFilter(wrapper);
        }
        for (String t : selectedTags) {
            if ("no_picture".equals(t) || "has_picture".equals(t)
                || "no_meaning_audio".equals(t) || "has_meaning_audio".equals(t)
                || "no_example".equals(t) || "has_example".equals(t)) {
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

    /** 无释义语音：兼容历史无 tags / meaningAudio 为空 */
    private static void applyNoMeaningAudioFilter(LambdaQueryWrapper<AdminWordBaseInfo> wrapper) {
        wrapper.and(w -> w
            .apply("JSON_CONTAINS(COALESCE(ext_info, '{}'), {0}, '$.tags')", "\"no_meaning_audio\"")
            .or()
            .isNull(AdminWordBaseInfo::getExtInfo)
            .or()
            .apply("JSON_EXTRACT(ext_info, '$.meaningAudio.fileId') IS NULL")
        );
    }

    private static void applyHasMeaningAudioFilter(LambdaQueryWrapper<AdminWordBaseInfo> wrapper) {
        wrapper.and(w -> w
            .apply("JSON_CONTAINS(COALESCE(ext_info, '{}'), {0}, '$.tags')", "\"has_meaning_audio\"")
            .or()
            .apply("JSON_EXTRACT(ext_info, '$.meaningAudio.fileId') IS NOT NULL")
        );
    }

    /** 无例句：兼容历史无 tags / sentence 为空 */
    private static void applyNoExampleFilter(LambdaQueryWrapper<AdminWordBaseInfo> wrapper) {
        wrapper.and(w -> w
            .apply("JSON_CONTAINS(COALESCE(ext_info, '{}'), {0}, '$.tags')", "\"no_example\"")
            .or()
            .isNull(AdminWordBaseInfo::getExtInfo)
            .or()
            .apply("JSON_EXTRACT(ext_info, '$.sentence.sentences') IS NULL")
            .or()
            .apply("JSON_LENGTH(COALESCE(JSON_EXTRACT(ext_info, '$.sentence.sentences'), JSON_ARRAY())) = 0")
        );
    }

    private static void applyHasExampleFilter(LambdaQueryWrapper<AdminWordBaseInfo> wrapper) {
        wrapper.and(w -> w
            .apply("JSON_CONTAINS(COALESCE(ext_info, '{}'), {0}, '$.tags')", "\"has_example\"")
            .or()
            .apply("JSON_LENGTH(COALESCE(JSON_EXTRACT(ext_info, '$.sentence.sentences'), JSON_ARRAY())) > 0")
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
        // 第二阶段：无释义 / 无例句等词查有道补释义、音标、例句、词组、近义、派生（带空格跳过；不改已有配图）
        fillMissingMeaningsFromYoudao(result);
        if (!StringUtils.hasText(result.getNote())) {
            result.setNote(String.format(
                "词形同步完成；补词典：扫描 %d，写入 %d，失败 %d，跳过 %d",
                result.getMeaningScanned(),
                result.getMeaningFilled(),
                result.getMeaningFailed(),
                result.getMeaningSkipped()
            ));
        }
        return result;
    }

    /**
     * 分页扫描缺释义或缺例句的词，调有道写入 meanings/音标/例句/词组/近义/派生；带空格词跳过；保留已有 pictures。
     */
    private void fillMissingMeaningsFromYoudao(AdminWordBaseInfoSyncResponse result) {
        long lastId = 0L;
        int pageRounds = 0;
        while (pageRounds < 100_000) {
            pageRounds++;
            LambdaQueryWrapper<AdminWordBaseInfo> wrapper = new LambdaQueryWrapper<>();
            applyNeedYoudaoEnrichFilter(wrapper);
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
                if (hasMeanings(ext) && hasExamples(ext) && hasPhrases(ext)) {
                    // 标签滞后：刷新后跳过，避免重复打有道
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
                    result.setNote("补词典被中断；已扫描 " + result.getMeaningScanned() + " 个待补词");
                    return;
                } catch (Exception e) {
                    result.setMeaningFailed(result.getMeaningFailed() + 1);
                    markMeaningSyncError(row, ext, e.getMessage());
                    log.warn("fill dict enrich failed id={} word={}: {}",
                        row.getId(), word, shortError(e.getMessage()));
                    try {
                        Thread.sleep(YOUDAO_GAP_MS);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        result.setNote("补词典被中断；已扫描 " + result.getMeaningScanned() + " 个待补词");
                        return;
                    }
                }
            }
            if (rows.size() < ENTRY_PAGE_SIZE) {
                break;
            }
        }
    }

    /** 缺释义或缺例句：兼容历史无 tags */
    private static void applyNeedYoudaoEnrichFilter(LambdaQueryWrapper<AdminWordBaseInfo> wrapper) {
        wrapper.and(w -> w
            .apply("JSON_CONTAINS(COALESCE(ext_info, '{}'), {0}, '$.tags')", "\"no_meaning\"")
            .or()
            .apply("JSON_CONTAINS(COALESCE(ext_info, '{}'), {0}, '$.tags')", "\"no_example\"")
            .or()
            .isNull(AdminWordBaseInfo::getExtInfo)
            .or()
            .apply("JSON_EXTRACT(ext_info, '$.meanings') IS NULL")
            .or()
            .apply("JSON_LENGTH(COALESCE(JSON_EXTRACT(ext_info, '$.meanings'), JSON_ARRAY())) = 0")
            .or()
            .apply("JSON_EXTRACT(ext_info, '$.sentence.sentences') IS NULL")
            .or()
            .apply("JSON_LENGTH(COALESCE(JSON_EXTRACT(ext_info, '$.sentence.sentences'), JSON_ARRAY())) = 0")
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

    private static boolean hasExamples(JSONObject ext) {
        if (ext == null) {
            return false;
        }
        JSONObject sentence = ext.getJSONObject("sentence");
        if (sentence == null) {
            return false;
        }
        JSONArray sentences = sentence.getJSONArray("sentences");
        return sentences != null && !sentences.isEmpty();
    }

    private static boolean hasPhrases(JSONObject ext) {
        if (ext == null) {
            return false;
        }
        JSONArray phrases = ext.getJSONArray("phrases");
        return phrases != null && !phrases.isEmpty();
    }

    /**
     * 有道补释义/音标/例句/词组/近义/派生/词源摘要；不覆盖已有配图与确认状态。
     * 释义/音标仅在本地为空时写入；例句等富化字段在本地为空时写入。
     *
     * @return true 本次至少写入了一项词典内容
     */
    private boolean fillMeaningFromYoudao(AdminWordBaseInfo row, JSONObject ext) throws Exception {
        JSONObject slim = YoudaoDictClient.fetchSlimExt(row.getWord());
        JSONArray meanings = slim.getJSONArray("meanings");
        boolean slimHasMeaning = meanings != null && !meanings.isEmpty();
        boolean slimHasPhone = StringUtils.hasText(slim.getString("ukphone"))
            || StringUtils.hasText(slim.getString("usphone"));
        boolean slimHasExample = slim.getJSONObject("sentence") != null
            && slim.getJSONObject("sentence").getJSONArray("sentences") != null
            && !slim.getJSONObject("sentence").getJSONArray("sentences").isEmpty();
        boolean slimHasPhrase = slim.getJSONArray("phrases") != null && !slim.getJSONArray("phrases").isEmpty();
        boolean slimHasSynonym = slim.getJSONArray("synonyms") != null && !slim.getJSONArray("synonyms").isEmpty();
        boolean slimHasRelated = slim.getJSONArray("relatedWords") != null && !slim.getJSONArray("relatedWords").isEmpty();
        boolean slimHasEtym = StringUtils.hasText(slim.getString("etym"));
        if (!slimHasMeaning && !slimHasPhone && !slimHasExample && !slimHasPhrase
            && !slimHasSynonym && !slimHasRelated && !slimHasEtym) {
            markMeaningSyncError(row, ext, "有道无可用词典字段");
            return false;
        }
        boolean wrote = false;
        if (slimHasMeaning && !hasMeanings(ext)) {
            ext.put("meanings", meanings);
            wrote = true;
        }
        if (StringUtils.hasText(slim.getString("ukphone")) && !StringUtils.hasText(ext.getString("ukphone"))) {
            ext.put("ukphone", slim.getString("ukphone"));
            wrote = true;
        }
        if (StringUtils.hasText(slim.getString("usphone")) && !StringUtils.hasText(ext.getString("usphone"))) {
            ext.put("usphone", slim.getString("usphone"));
            wrote = true;
        }
        if (slimHasExample && !hasExamples(ext)) {
            ext.put("sentence", slim.getJSONObject("sentence"));
            wrote = true;
        }
        if (slimHasPhrase && !hasPhrases(ext)) {
            ext.put("phrases", slim.getJSONArray("phrases"));
            wrote = true;
        }
        if (slimHasSynonym && (ext.getJSONArray("synonyms") == null || ext.getJSONArray("synonyms").isEmpty())) {
            ext.put("synonyms", slim.getJSONArray("synonyms"));
            wrote = true;
        }
        if (slimHasRelated && (ext.getJSONArray("relatedWords") == null || ext.getJSONArray("relatedWords").isEmpty())) {
            ext.put("relatedWords", slim.getJSONArray("relatedWords"));
            wrote = true;
        }
        if (slimHasEtym && !StringUtils.hasText(ext.getString("etym"))) {
            ext.put("etym", slim.getString("etym"));
            wrote = true;
        }
        // 仅在尚未有来源时标记；不改 pictures / pictureConfirmed
        if (!StringUtils.hasText(ext.getString("source")) || "ENTRY".equalsIgnoreCase(ext.getString("source"))) {
            ext.put("source", "YOUDAO");
        }
        ext.remove("syncError");
        ext.put("lastMeaningSyncTime", ChinaTime.nowDateTime());
        refreshMetaTags(ext);
        row.setExtInfo(ext.toJSONString());
        mapper.updateById(row);
        return wrote;
    }

    private void markMeaningSyncError(AdminWordBaseInfo row, JSONObject ext, String errorMsg) {
        if (row == null) {
            return;
        }
        JSONObject target = ext == null ? parseExt(row.getExtInfo()) : ext;
        target.put("syncError", shortError(errorMsg));
        target.put("lastFailTime", ChinaTime.nowDateTime());
        refreshMetaTags(target);
        row.setExtInfo(target.toJSONString());
        mapper.updateById(row);
    }

    /** 释义 TTS 失败：写入 meaningAudioError，并打 meaning_audio_error 标签便于筛选重跑 */
    private void markMeaningAudioError(AdminWordBaseInfo row, String errorMsg) {
        if (row == null || row.getId() == null) {
            return;
        }
        try {
            JSONObject ext = parseExt(row.getExtInfo());
            ext.put("meaningAudioError", shortMeaningAudioError(errorMsg));
            ext.put("meaningAudioErrorAt", ChinaTime.nowDateTime());
            refreshMetaTags(ext);
            row.setExtInfo(ext.toJSONString());
            mapper.updateById(row);
        } catch (Exception ex) {
            log.warn("mark meaning audio error failed id={}: {}", row.getId(), ex.getMessage());
        }
    }

    private static void clearMeaningAudioError(JSONObject ext) {
        if (ext == null) {
            return;
        }
        ext.remove("meaningAudioError");
        ext.remove("meaningAudioErrorAt");
    }

    /** 截断 TTS 错误；去掉 curl 重放段，避免把整段朗读稿塞进 ext_info */
    private static String shortMeaningAudioError(String message) {
        if (!StringUtils.hasText(message)) {
            return "TTS失败";
        }
        String value = message.trim().replaceAll("\\s+", " ");
        int curlIdx = value.indexOf("| curl:");
        if (curlIdx < 0) {
            curlIdx = value.indexOf(" curl=");
        }
        if (curlIdx > 0) {
            value = value.substring(0, curlIdx).trim();
        }
        if (value.length() > 160) {
            return value.substring(0, 160);
        }
        return value;
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
            query.setWord(req.getWord());
            query.setTags(req.getTags());
            query.setTag(req.getTag());
            query.setHasPicture(req.getHasPicture());
            query.setPictureConfirmed(req.getPictureConfirmed());
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
                if (pictureCount(ext) > 0 && (youdao || !pixabayPicturesNeedStore(ext))) {
                    // 已落库或非 Pixabay 外链：不再搜。仅有过期 Pixabay 地址的词走下面的重新搜图。
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
            result.setNote("已按当前筛选重新搜图，并把 Pixabay 图片写入 common_binary_file。采纳时未勾选的图会物理删除。");
        }
        return result;
    }

    /** 只有过期外链、还没有 fileId 的 Pixabay 图，需要重新搜再保存 */
    private static boolean pixabayPicturesNeedStore(JSONObject ext) {
        if (ext == null) {
            return false;
        }
        String pictureSource = ext.getString("pictureSource");
        JSONArray pictures = ext.getJSONArray("pictures");
        if (pictures == null || pictures.isEmpty()) {
            return false;
        }
        boolean pixabay = "PIXABAY".equalsIgnoreCase(pictureSource);
        boolean missing = false;
        for (int i = 0; i < pictures.size(); i++) {
            JSONObject item = pictures.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String source = item.getString("source");
            if ("PIXABAY".equalsIgnoreCase(source)) {
                pixabay = true;
            }
            if (item.getLong("fileId") == null && StringUtils.hasText(item.getString("url"))) {
                missing = true;
            }
        }
        return pixabay && missing;
    }

    /**
     * 刚搜到的地址按大图、中图、预览依次下载。旧的 pixabay.com/get 外链已过期，不在这里使用。
     */
    private JSONObject saveFreshPixabayImage(String word, Map<String, String> candidate) {
        if (candidate == null) {
            return null;
        }
        String[] urls = new String[] {
            YoudaoDictClient.normalizePictureUrl(candidate.get("url")),
            YoudaoDictClient.normalizePictureUrl(candidate.get("webformatUrl")),
            YoudaoDictClient.normalizePictureUrl(candidate.get("thumbUrl"))
        };
        String photographer = candidate.get("photographer");
        Exception last = null;
        String tried = "";
        for (String url : urls) {
            if (!StringUtils.hasText(url) || url.equals(tried)) {
                continue;
            }
            tried = url;
            try {
                return pictureBinaryStore.saveRemoteImage(url, "PIXABAY", photographer);
            } catch (Exception e) {
                last = e;
                log.warn("pixabay image store failed word={} url={}: {}",
                    word, url, shortError(e.getMessage()));
            }
        }
        if (last == null) {
            log.warn("pixabay image store skipped word={}: empty url", word);
        }
        return null;
    }

    /** Pixabay：先搜图，再把新地址下载进文件表；无候选计 failed */
    private boolean fillPicturesFromPixabay(
        AdminWordBaseInfo row,
        JSONObject ext,
        int perWord,
        AdminWordBaseInfoBatchSearchResponse result
    ) throws Exception {
        List<Map<String, String>> candidates = stockImageClient.search(row.getWord(), 1);
        Set<Long> previousIds = WordPictureBinaryStore.fileIdsOf(ext.getJSONArray("pictures"));
        JSONArray pictures = new JSONArray();
        List<Long> storedIds = new ArrayList<>();
        int take = Math.min(perWord, candidates.size());
        try {
            for (int i = 0; i < take; i++) {
                Map<String, String> c = candidates.get(i);
                if (c == null || !StringUtils.hasText(c.get("url"))) {
                    continue;
                }
                JSONObject item = saveFreshPixabayImage(row.getWord(), c);
                if (item == null) {
                    continue;
                }
                pictures.add(item);
                Long fileId = item.getLong("fileId");
                if (fileId != null) {
                    storedIds.add(fileId);
                }
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
            ext.put("lastBatchSearchTime", ChinaTime.nowDateTime());
            refreshMetaTags(ext);
            row.setExtInfo(ext.toJSONString());
            mapper.updateById(row);
            Set<Long> dropped = new LinkedHashSet<>(previousIds);
            dropped.removeAll(storedIds);
            pictureBinaryStore.release(dropped);
            result.setFilled(result.getFilled() + 1);
            return true;
        } catch (Exception e) {
            pictureBinaryStore.release(storedIds);
            throw e;
        }
    }

    /** 有道：有图则写入并直接确认；释义/音标/例句/词组等一并落库；无图但有词典内容仍保存，计 failed */
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
        boolean slimHasExample = slim.getJSONObject("sentence") != null
            && slim.getJSONObject("sentence").getJSONArray("sentences") != null
            && !slim.getJSONObject("sentence").getJSONArray("sentences").isEmpty();
        boolean slimHasPhrase = slim.getJSONArray("phrases") != null && !slim.getJSONArray("phrases").isEmpty();
        boolean slimHasSynonym = slim.getJSONArray("synonyms") != null && !slim.getJSONArray("synonyms").isEmpty();
        boolean slimHasRelated = slim.getJSONArray("relatedWords") != null
            && !slim.getJSONArray("relatedWords").isEmpty();
        boolean slimHasEtym = StringUtils.hasText(slim.getString("etym"));
        if (!hasPic && !hasMeaning && !hasPhone && !slimHasExample && !slimHasPhrase
            && !slimHasSynonym && !slimHasRelated && !slimHasEtym) {
            result.setFailed(result.getFailed() + 1);
            return false;
        }
        if (hasPic) {
            ext.put("pictures", slim.getJSONArray("pictures"));
            ext.put("pictureSource", "YOUDAO");
            ext.put("pictureConfirmed", true);
            ext.put("pictureConfirmedAt", ChinaTime.nowDateTime());
        }
        ext.put("source", "YOUDAO");
        // 释义/音标：有则覆盖（搜图路径本身就是有道刷新）
        if (hasMeaning) {
            ext.put("meanings", meanings);
        }
        if (StringUtils.hasText(slim.getString("ukphone"))) {
            ext.put("ukphone", slim.getString("ukphone"));
        }
        if (StringUtils.hasText(slim.getString("usphone"))) {
            ext.put("usphone", slim.getString("usphone"));
        }
        // 例句等：本地为空时补写，避免冲掉已有手工数据
        if (slimHasExample && !hasExamples(ext)) {
            ext.put("sentence", slim.getJSONObject("sentence"));
        }
        if (slimHasPhrase && !hasPhrases(ext)) {
            ext.put("phrases", slim.getJSONArray("phrases"));
        }
        if (slimHasSynonym && (ext.getJSONArray("synonyms") == null || ext.getJSONArray("synonyms").isEmpty())) {
            ext.put("synonyms", slim.getJSONArray("synonyms"));
        }
        if (slimHasRelated && (ext.getJSONArray("relatedWords") == null || ext.getJSONArray("relatedWords").isEmpty())) {
            ext.put("relatedWords", slim.getJSONArray("relatedWords"));
        }
        if (slimHasEtym && !StringUtils.hasText(ext.getString("etym"))) {
            ext.put("etym", slim.getString("etym"));
        }
        ext.remove("syncError");
        ext.remove("pictureCandidates");
        ext.put("lastBatchSearchTime", ChinaTime.nowDateTime());
        refreshMetaTags(ext);
        row.setExtInfo(ext.toJSONString());
        mapper.updateById(row);
        if (hasPic) {
            result.setFilled(result.getFilled() + 1);
        } else {
            // 无图但已写入释义/音标/例句等
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
        JSONArray previous = ext.getJSONArray("pictures");
        Set<Long> previousIds = WordPictureBinaryStore.fileIdsOf(previous);
        JSONArray pictures = new JSONArray();
        Set<Long> keptIds = new LinkedHashSet<>();
        List<Long> newlyStored = new ArrayList<>();
        try {
            for (String url : urls) {
                JSONObject reused = findStoredPicture(previous, url);
                if (reused != null) {
                    pictures.add(reused);
                    Long fileId = reused.getLong("fileId");
                    if (fileId != null) {
                        keptIds.add(fileId);
                    }
                    continue;
                }
                if ("PIXABAY".equals(pictureSource)) {
                    JSONObject saved = pictureBinaryStore.saveRemoteImage(url, pictureSource, null);
                    pictures.add(saved);
                    Long fileId = saved.getLong("fileId");
                    if (fileId != null) {
                        keptIds.add(fileId);
                        newlyStored.add(fileId);
                    }
                    continue;
                }
                JSONObject item = new JSONObject();
                item.put("url", url);
                item.put("source", pictureSource);
                pictures.add(item);
            }
            ext.put("pictures", pictures);
            ext.put("pictureSource", pictureSource);
            ext.put("pictureConfirmed", true);
            ext.put("pictureConfirmedAt", ChinaTime.nowDateTime());
            ext.remove("pictureCandidates");
            ext.remove("syncError");
            refreshMetaTags(ext);
            row.setExtInfo(ext.toJSONString());
            mapper.updateById(row);
        } catch (RuntimeException e) {
            pictureBinaryStore.release(newlyStored);
            throw e;
        } catch (Exception e) {
            pictureBinaryStore.release(newlyStored);
            throw new IllegalStateException("保存采纳图片失败: " + shortError(e.getMessage()));
        }
        Set<Long> dropped = new LinkedHashSet<>(previousIds);
        dropped.removeAll(keptIds);
        pictureBinaryStore.release(dropped);
        return getById(row.getId());
    }

    @Override
    public AdminWordBaseInfoBatchMeaningAudioResponse batchGenerateMeaningAudio(
        AdminWordBaseInfoBatchMeaningAudioRequest request
    ) {
        if (!edgeTtsClient.isEnabled()) {
            throw new IllegalStateException(
                "Edge TTS 未启用：请安装 edge-tts/imageio-ffmpeg，并配置 arelore.tts.edge.script 指向 speak.py");
        }
        AdminWordBaseInfoBatchMeaningAudioRequest req =
            request == null ? new AdminWordBaseInfoBatchMeaningAudioRequest() : request;
        int maxWords = req.getLimit() == null || req.getLimit() <= 0
            ? Integer.MAX_VALUE
            : Math.max(1, Math.min(50_000, req.getLimit()));
        boolean force = Boolean.TRUE.equals(req.getForce());
        int parallelism = resolveTtsParallelism(req.getParallelism());

        AdminWordBaseInfoBatchMeaningAudioResponse result = new AdminWordBaseInfoBatchMeaningAudioResponse();
        result.setSource("EDGE_TTS");
        result.setVoice(edgeTtsClient.getVoice());
        result.setParallelism(parallelism);

        AtomicInteger scanned = new AtomicInteger();
        AtomicInteger filled = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();
        AtomicInteger skipped = new AtomicInteger();

        ExecutorService pool = Executors.newFixedThreadPool(parallelism, r -> {
            Thread t = new Thread(r, "meaning-tts-" + System.identityHashCode(r));
            t.setDaemon(true);
            return t;
        });
        try {
            long lastId = 0L;
            int pageRounds = 0;
            while (scanned.get() < maxWords && pageRounds < 100_000) {
                pageRounds++;
                AdminWordBaseInfoRequest query = new AdminWordBaseInfoRequest();
                query.setLanguageCode(req.getLanguageCode());
                query.setWord(req.getWord());
                query.setTags(req.getTags());
                query.setTag(req.getTag());
                query.setHasPicture(req.getHasPicture());
                query.setPictureConfirmed(req.getPictureConfirmed());
                int fetchSize = Math.min(BATCH_SEARCH_PAGE_SIZE, maxWords - scanned.get());
                if (fetchSize <= 0) {
                    break;
                }
                LambdaQueryWrapper<AdminWordBaseInfo> wrapper = buildWrapper(query)
                    .gt(AdminWordBaseInfo::getId, lastId)
                    .orderByAsc(AdminWordBaseInfo::getId)
                    .last("LIMIT " + fetchSize);
                List<AdminWordBaseInfo> rows = mapper.selectList(wrapper);
                if (rows == null || rows.isEmpty()) {
                    break;
                }
                lastId = rows.get(rows.size() - 1).getId();

                List<AdminWordBaseInfo> batch = new ArrayList<>();
                for (AdminWordBaseInfo row : rows) {
                    if (row == null || row.getId() == null) {
                        continue;
                    }
                    if (scanned.get() + batch.size() >= maxWords) {
                        break;
                    }
                    batch.add(row);
                }
                if (batch.isEmpty()) {
                    break;
                }

                List<Future<?>> futures = new ArrayList<>(batch.size());
                for (AdminWordBaseInfo row : batch) {
                    futures.add(pool.submit(() -> {
                        scanned.incrementAndGet();
                        if (!StringUtils.hasText(row.getWord())) {
                            skipped.incrementAndGet();
                            return;
                        }
                        try {
                            boolean ok = generateMeaningAudioForRow(row, force);
                            if (ok) {
                                filled.incrementAndGet();
                            } else {
                                skipped.incrementAndGet();
                            }
                        } catch (Exception e) {
                            failed.incrementAndGet();
                            log.warn("batch meaning audio failed id={} word={}: {}",
                                row.getId(), row.getWord(), e.getMessage());
                            markMeaningAudioError(row, e.getMessage());
                        }
                    }));
                }
                for (Future<?> f : futures) {
                    try {
                        f.get();
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        result.setNote("批量生成释义语音被中断；已处理 " + scanned.get() + " 个词");
                        result.setScanned(scanned.get());
                        result.setFilled(filled.get());
                        result.setFailed(failed.get());
                        result.setSkipped(skipped.get());
                        return result;
                    } catch (Exception e) {
                        failed.incrementAndGet();
                        log.warn("batch meaning audio future failed: {}", e.getMessage());
                    }
                }
                if (rows.size() < fetchSize) {
                    break;
                }
            }
        } finally {
            pool.shutdown();
            try {
                if (!pool.awaitTermination(30, TimeUnit.SECONDS)) {
                    pool.shutdownNow();
                }
            } catch (InterruptedException ie) {
                pool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        result.setScanned(scanned.get());
        result.setFilled(filled.get());
        result.setFailed(failed.get());
        result.setSkipped(skipped.get());
        if (result.getScanned() == 0) {
            result.setNote("没有需要生成释义语音的单词");
        } else {
            result.setNote("已按当前筛选用 Edge TTS 并行生成释义语音（parallelism=" + parallelism
                + "，32k/16kHz 压缩）。已有 fileId 则覆盖 common_binary_file，否则新增；"
                + "meaningAudio 写入 hashValue（=表 hash_value hex）。"
                + "释义未变则跳过（force=true 可强制重生成）。"
                + "失败词已打标签 meaning_audio_error，可筛该标签后重跑。");
        }
        return result;
    }

    private int resolveTtsParallelism(Integer requestParallelism) {
        if (requestParallelism != null && requestParallelism > 0) {
            return Math.min(8, requestParallelism);
        }
        return ttsParallelism;
    }

    /**
     * @return true=新写入或覆盖了语音；false=无释义或已存在且 hash 一致（跳过）
     */
    private boolean generateMeaningAudioForRow(AdminWordBaseInfo row, boolean force) throws Exception {
        JSONObject ext = parseExt(row.getExtInfo());
        String speechText = MeaningSpeechTextBuilder.fromExt(ext);
        if (!StringUtils.hasText(speechText)) {
            return false;
        }
        String speechHash = sha256Hex(speechText);
        JSONObject existing = ext.getJSONObject("meaningAudio");
        Long oldFileId = existing == null ? null : existing.getLong("fileId");
        // 仅当 fileId 对应二进制仍存在、且释义 hash 未变时才跳过（避免删库后仍被当成「已有语音」）
        if (!force
            && oldFileId != null
            && speechHash.equals(existing.getString("speechHash"))
            && pictureBinaryStore.exists(oldFileId)) {
            // 已有语音：清失败标、补打 has_meaning_audio 后跳过
            String before = row.getExtInfo();
            clearMeaningAudioError(ext);
            refreshMetaTags(ext);
            String after = ext.toJSONString();
            if (!after.equals(before == null ? "" : before)) {
                row.setExtInfo(after);
                // MP updateById 会带上内存里的旧 modifyTime；显式刷新，避免 ON UPDATE 被覆盖
                row.setModifyTime(ChinaTime.now());
                mapper.updateById(row);
            }
            return false;
        }

        byte[] mp3 = edgeTtsClient.synthesize(speechText);
        // 溯源：把 base_info.id（及词形）写入 common_binary_file.ext_json，便于按 id 反查
        JSONObject binaryExt = new JSONObject();
        binaryExt.put("wordBaseInfoId", row.getId());
        if (StringUtils.hasText(row.getWord())) {
            binaryExt.put("word", row.getWord().trim());
        }
        if (StringUtils.hasText(row.getLanguageCode())) {
            binaryExt.put("languageCode", row.getLanguageCode().trim());
        }
        // 已有 fileId → 原地覆盖；无则新增；同内容哈希已存在则复用
        JSONObject saved = pictureBinaryStore.saveOrOverwriteBytes(
            oldFileId, mp3, "audio/mpeg", "EDGE_TTS", binaryExt
        );
        Long newFileId = saved.getLong("fileId");
        String hashHex = saved.getString("hashValue");
        if (!StringUtils.hasText(hashHex)) {
            hashHex = saved.getString("fileHash");
        }

        JSONObject meaningAudio = new JSONObject();
        meaningAudio.put("fileId", newFileId);
        meaningAudio.put("fileHash", hashHex);
        // hashValue/fileHash = 文件内容 SHA-256 的 hex；表里是 BINARY(32)，对账 SQL：
        // WHERE hash_value = UNHEX('<hashValue>')  或  WHERE id = <fileId>
        // speechHash 是释义文本 hash，不在 common_binary_file 里
        meaningAudio.put("hashValue", hashHex);
        meaningAudio.put("speechHash", speechHash);
        meaningAudio.put("source", "EDGE_TTS");
        meaningAudio.put("voice", edgeTtsClient.getVoice());
        meaningAudio.put("bitrate", edgeTtsClient.getBitrate());
        meaningAudio.put("sampleRate", edgeTtsClient.getSampleRate());
        meaningAudio.put("contentType", "audio/mpeg");
        meaningAudio.put("fileSize", saved.getInteger("fileSize"));
        meaningAudio.put("updatedAt", ChinaTime.nowDateTime());
        ext.put("meaningAudio", meaningAudio);
        clearMeaningAudioError(ext);
        refreshMetaTags(ext);
        row.setExtInfo(ext.toJSONString());
        row.setModifyTime(ChinaTime.now());
        mapper.updateById(row);

        if (oldFileId != null && !oldFileId.equals(newFileId)) {
            pictureBinaryStore.release(Collections.singleton(oldFileId));
        }
        return true;
    }

    private static String sha256Hex(String text) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public AdminWordPictureContentResponse pictureContent(Long fileId) {
        if (fileId == null) {
            throw new IllegalArgumentException("fileId 不能为空");
        }
        AdminWordPictureContentResponse content = pictureBinaryStore.loadContent(fileId);
        if (content == null) {
            throw new IllegalArgumentException("文件不存在");
        }
        return content;
    }

    private static JSONObject findStoredPicture(JSONArray pictures, String url) {
        if (pictures == null || !StringUtils.hasText(url)) {
            return null;
        }
        for (int i = 0; i < pictures.size(); i++) {
            JSONObject item = pictures.getJSONObject(i);
            if (item == null || item.getLong("fileId") == null) {
                continue;
            }
            String stored = YoudaoDictClient.normalizePictureUrl(item.getString("url"));
            if (url.equals(stored)) {
                return item;
            }
        }
        return null;
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
        ext.put("lastSyncTime", ChinaTime.nowDateTime());
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
     * 根据配图/确认/来源/释义/释义语音刷新检索标签（写入 ext_info，无需额外表字段）。
     * 标签示例：no_picture、has_picture、has_meaning_audio、meaning_audio_error、confirmed、source_pixabay、has_meaning、sync_error
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
        boolean hasExample = hasExamples(ext);
        boolean syncError = StringUtils.hasText(ext.getString("syncError"));
        boolean meaningAudioError = StringUtils.hasText(ext.getString("meaningAudioError"));
        JSONObject meaningAudio = ext.getJSONObject("meaningAudio");
        boolean hasMeaningAudio = meaningAudio != null && meaningAudio.getLong("fileId") != null;

        LinkedHashSet<String> tags = new LinkedHashSet<>();
        tags.add(hasPicture ? "has_picture" : "no_picture");
        tags.add(confirmed ? "confirmed" : "unconfirmed");
        tags.add("source_" + pictureSource.toLowerCase());
        tags.add(hasMeaning ? "has_meaning" : "no_meaning");
        tags.add(hasExample ? "has_example" : "no_example");
        tags.add(hasMeaningAudio ? "has_meaning_audio" : "no_meaning_audio");
        if (syncError) {
            tags.add("sync_error");
        }
        if (meaningAudioError) {
            tags.add("meaning_audio_error");
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
