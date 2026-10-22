package com.arelore.server.core.biz.word.user;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.arelore.server.core.biz.word.user.dto.UserWordPreferenceRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordPreferenceResponse;
import com.arelore.server.core.biz.word.user.entity.UserWordPreference;
import com.arelore.server.core.biz.word.user.mapper.UserWordPreferenceMapper;
import com.arelore.server.core.biz.word.user.support.WordUserIdStrSupport;
import com.arelore.server.core.service.BaseServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserWordPreferenceServiceImpl
    extends BaseServiceImpl<UserWordPreferenceRequest, UserWordPreferenceResponse, UserWordPreference>
    implements UserWordPreferenceService {

    public static final String STUDY_MODE_SEE_MEANING = "see_meaning";
    public static final String STUDY_MODE_LISTEN_SPELL = "listen_spell";
    public static final String STUDY_MODE_DICTATION = "dictation";
    public static final String STUDY_MODE_SEE_PICK_IMAGE = "see_pick_image";

    public static final int DEFAULT_SPELL_SPEED = 5;
    public static final int DEFAULT_WORD_REPEAT_COUNT = 1;

    private static final Set<String> ALLOWED_STUDY_MODES = new HashSet<>(Arrays.asList(
        STUDY_MODE_SEE_MEANING,
        STUDY_MODE_LISTEN_SPELL,
        STUDY_MODE_DICTATION,
        STUDY_MODE_SEE_PICK_IMAGE
    ));

    private final UserWordPreferenceMapper mapper;

    public UserWordPreferenceServiceImpl(UserWordPreferenceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected UserWordPreferenceMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<UserWordPreferenceResponse> responseClass() {
        return UserWordPreferenceResponse.class;
    }

    @Override
    protected UserWordPreferenceResponse toResponse(UserWordPreference entity) {
        UserWordPreferenceResponse response = super.toResponse(entity);
        WordUserIdStrSupport.fillIdStr(response);
        fillPreferenceFields(response);
        return response;
    }

    @Override
    protected LambdaQueryWrapper<UserWordPreference> buildWrapper(UserWordPreferenceRequest request) {
        WordUserIdStrSupport.applyIdStrToUserId(request);
        LambdaQueryWrapper<UserWordPreference> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
        if (request.getUserId() != null) {
            wrapper.eq(UserWordPreference::getUserId, request.getUserId());
        }
        wrapper.orderByDesc(UserWordPreference::getId);
        return wrapper;
    }

    @Override
    public UserWordPreferenceResponse getOrDefault(BigDecimal userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        UserWordPreferenceRequest query = new UserWordPreferenceRequest();
        query.setUserId(userId);
        List<UserWordPreferenceResponse> list = list(query);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return buildDefaults(userId);
    }

    @Override
    public UserWordPreferenceResponse savePreference(UserWordPreferenceRequest request) {
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        UserWordPreferenceRequest query = new UserWordPreferenceRequest();
        query.setUserId(request.getUserId());
        List<UserWordPreferenceResponse> existsList = list(query);
        UserWordPreferenceResponse exists = existsList.isEmpty() ? null : existsList.get(0);

        JSONObject ext = parseExt(exists == null ? null : exists.getExtInfo());
        // 未传的字段保留原值，缺省则用默认
        String studyMode = request.getStudyMode() != null
            ? requireStudyMode(request.getStudyMode())
            : defaultStudyMode(ext.getString("studyMode"));
        int spellSpeed = request.getSpellSpeed() != null
            ? requireSpellSpeed(request.getSpellSpeed())
            : defaultSpellSpeed(ext.getInteger("spellSpeed"));
        int wordRepeatCount = request.getWordRepeatCount() != null
            ? requireWordRepeatCount(request.getWordRepeatCount())
            : defaultWordRepeatCount(ext.getInteger("wordRepeatCount"));

        ext.put("studyMode", studyMode);
        ext.put("spellSpeed", spellSpeed);
        ext.put("wordRepeatCount", wordRepeatCount);

        UserWordPreferenceRequest save = new UserWordPreferenceRequest();
        save.setUserId(request.getUserId());
        save.setExtInfo(ext.toJSONString());
        if (exists == null) {
            create(save);
        } else {
            save.setId(exists.getId());
            update(save);
        }
        return getOrDefault(request.getUserId());
    }

    private UserWordPreferenceResponse buildDefaults(BigDecimal userId) {
        UserWordPreferenceResponse defaults = new UserWordPreferenceResponse();
        defaults.setUserId(userId);
        defaults.setIdStr(userId.toPlainString());
        defaults.setStudyMode(STUDY_MODE_SEE_MEANING);
        defaults.setSpellSpeed(DEFAULT_SPELL_SPEED);
        defaults.setWordRepeatCount(DEFAULT_WORD_REPEAT_COUNT);
        return defaults;
    }

    private void fillPreferenceFields(UserWordPreferenceResponse response) {
        if (response == null) {
            return;
        }
        JSONObject ext = parseExt(response.getExtInfo());
        response.setStudyMode(defaultStudyMode(ext.getString("studyMode")));
        response.setSpellSpeed(defaultSpellSpeed(ext.getInteger("spellSpeed")));
        response.setWordRepeatCount(defaultWordRepeatCount(ext.getInteger("wordRepeatCount")));
    }

    private static String requireStudyMode(String raw) {
        if (!StringUtils.hasText(raw)) {
            return STUDY_MODE_SEE_MEANING;
        }
        String mode = raw.trim();
        if (!ALLOWED_STUDY_MODES.contains(mode)) {
            throw new IllegalArgumentException("不支持的背词模式: " + mode);
        }
        if (STUDY_MODE_SEE_PICK_IMAGE.equals(mode)) {
            throw new IllegalArgumentException("看词选图暂未开放");
        }
        return mode;
    }

    private static String defaultStudyMode(String raw) {
        if (!StringUtils.hasText(raw)) {
            return STUDY_MODE_SEE_MEANING;
        }
        String mode = raw.trim();
        return ALLOWED_STUDY_MODES.contains(mode) ? mode : STUDY_MODE_SEE_MEANING;
    }

    private static int requireSpellSpeed(Integer raw) {
        if (raw == null) {
            return DEFAULT_SPELL_SPEED;
        }
        if (raw < 0 || raw > 10) {
            throw new IllegalArgumentException("拼读语速须在 0~10");
        }
        return raw;
    }

    private static int defaultSpellSpeed(Integer raw) {
        if (raw == null || raw < 0 || raw > 10) {
            return DEFAULT_SPELL_SPEED;
        }
        return raw;
    }

    private static int requireWordRepeatCount(Integer raw) {
        if (raw == null) {
            return DEFAULT_WORD_REPEAT_COUNT;
        }
        if (raw < 1 || raw > 3) {
            throw new IllegalArgumentException("连读次数须在 1~3");
        }
        return raw;
    }

    private static int defaultWordRepeatCount(Integer raw) {
        if (raw == null || raw < 1 || raw > 3) {
            return DEFAULT_WORD_REPEAT_COUNT;
        }
        return raw;
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
}
