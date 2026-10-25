package com.arelore.server.core.biz.prompt.user;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptImportResult;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptRequest;
import com.arelore.server.core.biz.prompt.user.dto.UserAiPromptResponse;
import com.arelore.server.core.biz.prompt.user.entity.UserAiPrompt;
import com.arelore.server.core.biz.prompt.user.mapper.UserAiPromptMapper;
import com.arelore.server.core.biz.word.user.support.WordUserIdStrSupport;
import com.arelore.server.core.service.BaseServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UserAiPromptServiceImpl
    extends BaseServiceImpl<UserAiPromptRequest, UserAiPromptResponse, UserAiPrompt>
    implements UserAiPromptService {

    public static final String STATUS_NORMAL = "NORMAL";
    public static final String STATUS_DELETED = "DELETED";
    public static final String PUBLISH_DRAFT = "DRAFT";
    public static final String PUBLISH_PUBLISHED = "PUBLISHED";
    public static final String PUBLISH_OFFLINE = "OFFLINE";
    public static final int DAILY_CREATE_LIMIT = 100;
    public static final int PROMPT_TEXT_MAX_CHARS = 65535;

    private final UserAiPromptMapper mapper;
    private final UserAiPromptTagService tagService;

    public UserAiPromptServiceImpl(UserAiPromptMapper mapper, UserAiPromptTagService tagService) {
        this.mapper = mapper;
        this.tagService = tagService;
    }

    @Override
    protected UserAiPromptMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<UserAiPromptResponse> responseClass() {
        return UserAiPromptResponse.class;
    }

    @Override
    protected UserAiPromptResponse toResponse(UserAiPrompt entity) {
        UserAiPromptResponse response = super.toResponse(entity);
        if (response == null) {
            return null;
        }
        WordUserIdStrSupport.fillIdStr(response);
        List<String> configList = parseTagList(response.getConfigTags());
        List<String> manualList = parseTagList(response.getManualTags());
        response.setConfigTagList(configList);
        response.setManualTagList(manualList);
        response.setTagList(mergeTagLists(configList, manualList));
        response.setSummary(readSummary(response.getExtInfo()));
        return response;
    }

    @Override
    protected LambdaQueryWrapper<UserAiPrompt> buildWrapper(UserAiPromptRequest request) {
        WordUserIdStrSupport.applyIdStrToUserId(request);
        LambdaQueryWrapper<UserAiPrompt> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
        if (request.getUserId() != null) {
            wrapper.eq(UserAiPrompt::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(UserAiPrompt::getStatus, request.getStatus().trim());
        } else {
            wrapper.eq(UserAiPrompt::getStatus, STATUS_NORMAL);
        }
        if (StringUtils.hasText(request.getPublishStatus())) {
            wrapper.eq(UserAiPrompt::getPublishStatus, request.getPublishStatus().trim());
        }
        if (StringUtils.hasText(request.getPromptUuid())) {
            wrapper.eq(UserAiPrompt::getPromptUuid, request.getPromptUuid().trim());
        }
        if (request.getId() != null) {
            wrapper.eq(UserAiPrompt::getId, request.getId());
        }
        String titleKw = StringUtils.hasText(request.getTitleKeyword())
            ? request.getTitleKeyword().trim()
            : (StringUtils.hasText(request.getTitle()) ? request.getTitle().trim() : null);
        if (StringUtils.hasText(titleKw)) {
            wrapper.like(UserAiPrompt::getTitle, titleKw);
        }
        // MySQL 8 JSON 多值索引：用 MEMBER OF / JSON_CONTAINS，勿再对 JSON 列 LIKE
        if (StringUtils.hasText(request.getConfigTagName())) {
            applyJsonTagContains(wrapper, "config_tags", request.getConfigTagName().trim());
        }
        if (StringUtils.hasText(request.getManualTagName())) {
            applyJsonTagContains(wrapper, "manual_tags", request.getManualTagName().trim());
        }
        List<String> tagNames = normalizeNames(request.getTagNames());
        if (!tagNames.isEmpty()) {
            applyAnyTagMatch(wrapper, tagNames);
        } else if (StringUtils.hasText(request.getTagName())) {
            applyAnyTagMatch(wrapper, Collections.singletonList(request.getTagName().trim()));
        }
        wrapper.orderByDesc(UserAiPrompt::getModifyTime).orderByDesc(UserAiPrompt::getId);
        return wrapper;
    }

    /**
     * 命中 config_tags 或 manual_tags 中任一标签名（OR）。
     * 例：标签为 a,b,c 时查 b 或 c → tagNames=["b","c"]，可走 JSON 多值索引。
     */
    private static void applyAnyTagMatch(LambdaQueryWrapper<UserAiPrompt> wrapper, List<String> tagNames) {
        if (CollectionUtils.isEmpty(tagNames)) {
            return;
        }
        wrapper.and(outer -> {
            for (String raw : tagNames) {
                if (!StringUtils.hasText(raw)) {
                    continue;
                }
                String name = raw.trim();
                outer.or(inner -> inner
                    .apply("JSON_CONTAINS(config_tags, JSON_QUOTE({0}))", name)
                    .or()
                    .apply("JSON_CONTAINS(manual_tags, JSON_QUOTE({0}))", name));
            }
        });
    }

    private static void applyJsonTagContains(
        LambdaQueryWrapper<UserAiPrompt> wrapper,
        String column,
        String tagName
    ) {
        // column 仅允许白名单，防注入
        if (!"config_tags".equals(column) && !"manual_tags".equals(column)) {
            throw new IllegalArgumentException("非法标签列");
        }
        wrapper.apply("JSON_CONTAINS(" + column + ", JSON_QUOTE({0}))", tagName);
    }

    @Override
    public UserAiPromptResponse saveOrUpsert(UserAiPromptRequest request) {
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        boolean importMode = Boolean.TRUE.equals(request.getImportMode());
        // 页面编辑可带 id；导入/覆盖按 prompt_uuid；新建由服务端生成 uuid
        UserAiPrompt exists = findOwnedById(request.getUserId(), request.getId());
        if (exists == null) {
            exists = findByUserAndUuid(request.getUserId(), request.getPromptUuid());
        }
        normalizeContent(request, exists, importMode);

        if (exists == null) {
            assertDailyQuota(request.getUserId(), 1);
            // 页面新建：无 uuid → 服务端生成；CSV 导入可自带 uuid（首次写入后供下次覆盖）
            if (!StringUtils.hasText(request.getPromptUuid())) {
                request.setPromptUuid(newPromptUuid());
            }
            request.setId(null);
            request.setStatus(STATUS_NORMAL);
            if (!StringUtils.hasText(request.getPublishStatus())) {
                request.setPublishStatus(PUBLISH_DRAFT);
            }
            create(request);
            ensureConfigDict(request);
            return getByUuid(request.getUserId(), request.getPromptUuid());
        }
        request.setId(exists.getId());
        request.setPromptUuid(exists.getPromptUuid());
        request.setStatus(STATUS_NORMAL);
        if (!StringUtils.hasText(request.getPublishStatus())) {
            request.setPublishStatus(exists.getPublishStatus());
        }
        update(request);
        ensureConfigDict(request);
        return getByUuid(request.getUserId(), exists.getPromptUuid());
    }

    @Override
    public UserAiPromptImportResult importBatch(BigDecimal userId, UserAiPromptRequest request) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        List<UserAiPromptRequest> items = request == null ? null : request.getItems();
        if (CollectionUtils.isEmpty(items)) {
            throw new IllegalArgumentException("导入列表不能为空");
        }
        int needCreate = 0;
        for (UserAiPromptRequest item : items) {
            if (item == null) {
                continue;
            }
            String uuid = StringUtils.hasText(item.getPromptUuid()) ? item.getPromptUuid().trim() : null;
            if (uuid == null || findByUserAndUuid(userId, uuid) == null) {
                needCreate++;
            }
        }
        assertDailyQuota(userId, needCreate);

        UserAiPromptImportResult result = new UserAiPromptImportResult();
        for (UserAiPromptRequest item : items) {
            if (item == null) {
                result.setSkipped(result.getSkipped() + 1);
                continue;
            }
            try {
                item.setUserId(userId);
                item.setImportMode(true);
                boolean existed = StringUtils.hasText(item.getPromptUuid())
                    && findByUserAndUuid(userId, item.getPromptUuid().trim()) != null;
                saveOrUpsert(item);
                if (existed) {
                    result.setUpdated(result.getUpdated() + 1);
                } else {
                    result.setCreated(result.getCreated() + 1);
                }
            } catch (IllegalArgumentException e) {
                result.setSkipped(result.getSkipped() + 1);
            }
        }
        result.setMessage(String.format(
            Locale.ROOT,
            "导入完成：新增%d，覆盖%d，跳过%d（仅覆盖配置标签，人工标签保留）",
            result.getCreated(),
            result.getUpdated(),
            result.getSkipped()
        ));
        return result;
    }

    @Override
    public int softDelete(BigDecimal userId, Long id) {
        if (userId == null || id == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        UserAiPrompt entity = mapper.selectById(id);
        if (entity == null || entity.getUserId() == null || entity.getUserId().compareTo(userId) != 0) {
            throw new IllegalArgumentException("记录不存在");
        }
        entity.setStatus(STATUS_DELETED);
        return mapper.updateById(entity);
    }

    private void ensureConfigDict(UserAiPromptRequest request) {
        List<String> configNames = request.getConfigTagList();
        if (configNames == null) {
            configNames = parseTagList(request.getConfigTags());
        }
        tagService.ensureTags(request.getUserId(), UserAiPromptTagServiceImpl.TYPE_CONFIG, configNames);
        if (!Boolean.TRUE.equals(request.getImportMode())) {
            List<String> manualNames = request.getManualTagList();
            if (manualNames == null) {
                manualNames = parseTagList(request.getManualTags());
            }
            tagService.ensureTags(request.getUserId(), UserAiPromptTagServiceImpl.TYPE_MANUAL, manualNames);
        }
    }

    private UserAiPromptResponse getByUuid(BigDecimal userId, String promptUuid) {
        UserAiPromptRequest query = new UserAiPromptRequest();
        query.setUserId(userId);
        query.setPromptUuid(promptUuid);
        query.setStatus(STATUS_NORMAL);
        List<UserAiPromptResponse> list = list(query);
        return list.isEmpty() ? null : list.get(0);
    }

    private UserAiPrompt findOwnedById(BigDecimal userId, Long id) {
        if (userId == null || id == null) {
            return null;
        }
        UserAiPrompt entity = mapper.selectById(id);
        if (entity == null || entity.getUserId() == null || entity.getUserId().compareTo(userId) != 0) {
            return null;
        }
        if (STATUS_DELETED.equals(entity.getStatus())) {
            return null;
        }
        return entity;
    }

    private UserAiPrompt findByUserAndUuid(BigDecimal userId, String promptUuid) {
        if (userId == null || !StringUtils.hasText(promptUuid)) {
            return null;
        }
        LambdaQueryWrapper<UserAiPrompt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAiPrompt::getUserId, userId)
            .eq(UserAiPrompt::getPromptUuid, promptUuid.trim())
            .last("limit 1");
        return mapper.selectOne(wrapper);
    }

    private void assertDailyQuota(BigDecimal userId, int addCount) {
        if (addCount <= 0) {
            return;
        }
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(LocalTime.MAX);
        LambdaQueryWrapper<UserAiPrompt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserAiPrompt::getUserId, userId)
            .ge(UserAiPrompt::getCreateTime, start)
            .le(UserAiPrompt::getCreateTime, end);
        Long count = mapper.selectCount(wrapper);
        long used = count == null ? 0L : count;
        if (used + addCount > DAILY_CREATE_LIMIT) {
            throw new IllegalArgumentException(
                "今日新增已达上限" + DAILY_CREATE_LIMIT + "条（已用" + used + "，本次需新增" + addCount + "）"
            );
        }
    }

    private void normalizeContent(UserAiPromptRequest request, UserAiPrompt exists, boolean importMode) {
        if (request.getTitle() == null) {
            request.setTitle("");
        } else {
            request.setTitle(request.getTitle().trim());
            if (request.getTitle().length() > 255) {
                throw new IllegalArgumentException("标题最长255字符");
            }
        }
        if (!StringUtils.hasText(request.getPromptText())) {
            throw new IllegalArgumentException("提示词不能为空");
        }
        if (request.getPromptText().length() > PROMPT_TEXT_MAX_CHARS) {
            throw new IllegalArgumentException("提示词最长" + PROMPT_TEXT_MAX_CHARS + "字符");
        }
        if (StringUtils.hasText(request.getPromptUuid())) {
            String uuid = request.getPromptUuid().trim().replace("-", "");
            if (uuid.length() != 32) {
                throw new IllegalArgumentException("promptUuid须为32位（UUID去横线）");
            }
            request.setPromptUuid(uuid.toLowerCase(Locale.ROOT));
        } else {
            // 新建由服务端生成；更新时会回填已有 uuid
            request.setPromptUuid(null);
        }

        List<String> configTags = resolveTagList(
            request.getConfigTagList(),
            request.getConfigTags(),
            // 兼容旧字段 tagList 作为配置标签（仅导入模式）
            importMode ? request.getTagList() : null
        );
        request.setConfigTagList(configTags);
        request.setConfigTags(JSON.toJSONString(configTags));

        if (importMode) {
            // 导入：保留库内人工标签（JSON NOT NULL，空则 []）
            String keepManual = exists != null ? exists.getManualTags() : "[]";
            if (!StringUtils.hasText(keepManual) || "null".equalsIgnoreCase(keepManual.trim())) {
                keepManual = "[]";
            }
            request.setManualTags(keepManual);
            request.setManualTagList(parseTagList(keepManual));
        } else {
            List<String> manualTags = resolveTagList(
                request.getManualTagList(),
                request.getManualTags(),
                null
            );
            request.setManualTagList(manualTags);
            request.setManualTags(JSON.toJSONString(manualTags));
        }
        // JSON 列 NOT NULL：保证写入合法数组
        if (!StringUtils.hasText(request.getConfigTags())) {
            request.setConfigTags("[]");
        }
        if (!StringUtils.hasText(request.getManualTags())) {
            request.setManualTags("[]");
        }

        request.setPublishStatus(normalizePublishStatus(request.getPublishStatus()));
        request.setExtInfo(mergeExtInfo(request.getExtInfo(), request.getSummary()));
    }

    private static List<String> resolveTagList(List<String> list, String json, List<String> fallback) {
        if (list != null) {
            return normalizeNames(list);
        }
        if (StringUtils.hasText(json)) {
            return parseTagList(json);
        }
        if (fallback != null) {
            return normalizeNames(fallback);
        }
        return Collections.emptyList();
    }

    private static List<String> normalizeNames(List<String> raw) {
        if (CollectionUtils.isEmpty(raw)) {
            return Collections.emptyList();
        }
        Set<String> set = new LinkedHashSet<>();
        for (String name : raw) {
            if (StringUtils.hasText(name)) {
                String t = name.trim();
                if (t.length() <= 64) {
                    set.add(t);
                }
            }
        }
        return new ArrayList<>(set);
    }

    private static List<String> mergeTagLists(List<String> configList, List<String> manualList) {
        Set<String> set = new LinkedHashSet<>();
        if (configList != null) {
            set.addAll(configList);
        }
        if (manualList != null) {
            set.addAll(manualList);
        }
        return new ArrayList<>(set);
    }

    private static String normalizePublishStatus(String raw) {
        if (!StringUtils.hasText(raw)) {
            return PUBLISH_DRAFT;
        }
        String status = raw.trim().toUpperCase(Locale.ROOT);
        if (PUBLISH_DRAFT.equals(status) || PUBLISH_PUBLISHED.equals(status) || PUBLISH_OFFLINE.equals(status)) {
            return status;
        }
        throw new IllegalArgumentException("不支持的发布状态: " + raw);
    }

    private static String mergeExtInfo(String extInfo, String summary) {
        JSONObject ext;
        try {
            ext = StringUtils.hasText(extInfo) ? JSON.parseObject(extInfo) : new JSONObject();
            if (ext == null) {
                ext = new JSONObject();
            }
        } catch (Exception e) {
            ext = new JSONObject();
        }
        if (summary != null) {
            ext.put("summary", summary);
        }
        return ext.isEmpty() ? null : ext.toJSONString();
    }

    private static String readSummary(String extInfo) {
        if (!StringUtils.hasText(extInfo)) {
            return "";
        }
        try {
            JSONObject ext = JSON.parseObject(extInfo);
            if (ext == null) {
                return "";
            }
            String summary = ext.getString("summary");
            return summary == null ? "" : summary;
        } catch (Exception e) {
            return "";
        }
    }

    private static List<String> parseTagList(String tagsJson) {
        if (!StringUtils.hasText(tagsJson)) {
            return Collections.emptyList();
        }
        try {
            JSONArray arr = JSON.parseArray(tagsJson);
            if (arr == null || arr.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> list = new ArrayList<>(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                String name = arr.getString(i);
                if (StringUtils.hasText(name)) {
                    list.add(name.trim());
                }
            }
            return list;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public static String newPromptUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
