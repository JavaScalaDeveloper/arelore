package com.arelore.server.core.biz.word.user;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.arelore.server.core.biz.word.admin.AdminWordBookService;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBookResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordCurrentBookRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordCurrentBookResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyPlanRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyPlanResponse;
import com.arelore.server.core.biz.word.user.entity.UserWordStudyPlan;
import com.arelore.server.core.biz.word.user.mapper.UserWordStudyPlanMapper;
import com.arelore.server.core.service.BaseServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class UserWordStudyPlanServiceImpl
    extends BaseServiceImpl<UserWordStudyPlanRequest, UserWordStudyPlanResponse, UserWordStudyPlan>
    implements UserWordStudyPlanService {
    private static final Set<String> ALLOWED_RATIOS = new HashSet<>(Arrays.asList("1:1", "1:2", "1:3"));

    private final UserWordStudyPlanMapper mapper;
    private final AdminWordBookService bookService;
    private final UserWordCurrentBookService currentBookService;

    public UserWordStudyPlanServiceImpl(
        UserWordStudyPlanMapper mapper,
        AdminWordBookService bookService,
        UserWordCurrentBookService currentBookService
    ) {
        this.mapper = mapper;
        this.bookService = bookService;
        this.currentBookService = currentBookService;
    }

    @Override
    protected UserWordStudyPlanMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<UserWordStudyPlanResponse> responseClass() {
        return UserWordStudyPlanResponse.class;
    }

    @Override
    protected UserWordStudyPlanResponse toResponse(UserWordStudyPlan entity) {
        UserWordStudyPlanResponse response = super.toResponse(entity);
        com.arelore.server.core.biz.word.user.support.WordUserIdStrSupport.fillIdStr(response);
        return response;
    }

    @Override
    protected LambdaQueryWrapper<UserWordStudyPlan> buildWrapper(UserWordStudyPlanRequest request) {
        com.arelore.server.core.biz.word.user.support.WordUserIdStrSupport.applyIdStrToUserId(request);
        LambdaQueryWrapper<UserWordStudyPlan> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
        if (request.getUserId() != null) {
            wrapper.eq(UserWordStudyPlan::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getBookCode())) {
            wrapper.eq(UserWordStudyPlan::getBookCode, request.getBookCode());
        }
        wrapper.orderByDesc(UserWordStudyPlan::getId);
        return wrapper;
    }

    @Override
    public List<UserWordStudyPlanResponse> list(UserWordStudyPlanRequest request) {
        List<UserWordStudyPlanResponse> list = super.list(request);
        for (UserWordStudyPlanResponse item : list) {
            fillBook(item);
        }
        return list;
    }

    @Override
    public UserWordStudyPlanResponse confirm(UserWordStudyPlanRequest request) {
        normalizeAndValidate(request, true);
        UserWordStudyPlanRequest query = new UserWordStudyPlanRequest();
        query.setUserId(request.getUserId());
        query.setBookCode(request.getBookCode());
        List<UserWordStudyPlanResponse> existsList = list(query);
        UserWordStudyPlanResponse exists = existsList.isEmpty() ? null : existsList.get(0);

        // 换书前：把旧词书今日进度写回对应 plan，避免进度串书
        UserWordCurrentBookRequest byUser = new UserWordCurrentBookRequest();
        byUser.setUserId(request.getUserId());
        List<UserWordCurrentBookResponse> userCurrentList = currentBookService.list(byUser);
        UserWordCurrentBookResponse userCurrent = userCurrentList.isEmpty() ? null : userCurrentList.get(0);
        if (userCurrent != null
            && StringUtils.hasText(userCurrent.getBookCode())
            && !userCurrent.getBookCode().equals(request.getBookCode())
        ) {
            persistCurrentProgressToPlan(userCurrent);
        }

        if (exists == null) {
            super.create(request);
        } else {
            request.setId(exists.getId());
            // 保留历史 plan 行，仅覆盖计划字段；今日进度在 normalize 已按新计划重置
            UserWordStudyPlan patch = new UserWordStudyPlan();
            patch.setId(exists.getId());
            patch.setNewReviewRatio(request.getNewReviewRatio());
            patch.setDailyNewCount(request.getDailyNewCount());
            patch.setDailyReviewCount(request.getDailyReviewCount());
            patch.setPlanDays(request.getPlanDays());
            patch.setExtInfo(request.getExtInfo());
            mapper.updateById(patch);
        }

        UserWordCurrentBookRequest currentReq = new UserWordCurrentBookRequest();
        currentReq.setUserId(request.getUserId());
        currentReq.setBookCode(request.getBookCode());
        currentReq.setLearnDone(0);
        currentReq.setLearnTodo(request.getDailyNewCount());
        currentReq.setReviewDone(0);
        currentReq.setReviewTodo(request.getDailyReviewCount());
        // 当前词书行只镜像展示；今日词表落在 plan.ext_info（按书隔离）
        currentReq.setExtInfo(null);
        if (userCurrent == null) {
            currentBookService.create(currentReq);
        } else {
            currentReq.setId(userCurrent.getId());
            currentBookService.update(currentReq);
        }

        List<UserWordStudyPlanResponse> latest = list(query);
        return latest.isEmpty() ? null : latest.get(0);
    }

    @Override
    public void saveDailyProgress(Long planId, String extInfo) {
        if (planId == null) {
            return;
        }
        UserWordStudyPlan patch = new UserWordStudyPlan();
        patch.setId(planId);
        patch.setExtInfo(extInfo);
        mapper.updateById(patch);
    }

    @Override
    public int create(UserWordStudyPlanRequest request) {
        normalizeAndValidate(request, true);
        return super.create(request);
    }

    @Override
    public int update(UserWordStudyPlanRequest request) {
        normalizeAndValidate(request, false);
        return super.update(request);
    }

    private void normalizeAndValidate(UserWordStudyPlanRequest request, boolean resetDailyProgress) {
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (!StringUtils.hasText(request.getBookCode())) {
            throw new IllegalArgumentException("单词本code不能为空");
        }
        AdminWordBookResponse book = bookService.getByCode(request.getBookCode().trim());
        if (book == null || book.getStatus() != null && book.getStatus() == 0) {
            throw new IllegalArgumentException("单词本不存在或已停用");
        }
        request.setBookCode(request.getBookCode().trim());

        String ratio = StringUtils.hasText(request.getNewReviewRatio())
            ? request.getNewReviewRatio().trim()
            : "1:1";
        if (!ALLOWED_RATIOS.contains(ratio)) {
            throw new IllegalArgumentException("新学复习比例仅支持 1:1 / 1:2 / 1:3");
        }
        request.setNewReviewRatio(ratio);

        Integer dailyNew = request.getDailyNewCount() == null ? 10 : request.getDailyNewCount();
        if (dailyNew < 10 || dailyNew > 100 || dailyNew % 10 != 0) {
            throw new IllegalArgumentException("每日新学个数须为 10~100 且为 10 的倍数");
        }
        request.setDailyNewCount(dailyNew);

        int reviewPart = Integer.parseInt(ratio.substring(ratio.indexOf(':') + 1));
        request.setDailyReviewCount(dailyNew * reviewPart);

        int wordCount = book.getWordCount() == null ? 0 : book.getWordCount();
        int planDays = dailyNew <= 0 ? 0 : (int) Math.ceil(wordCount * 1.0 / dailyNew);
        request.setPlanDays(planDays);
        request.setBookName(book.getName());
        request.setWordCount(wordCount);
        normalizeExtInfo(request, book.getLanguageCode(), resetDailyProgress);
    }

    private void normalizeExtInfo(UserWordStudyPlanRequest request, String languageCode, boolean resetDailyProgress) {
        JSONObject ext;
        try {
            ext = StringUtils.hasText(request.getExtInfo())
                ? JSON.parseObject(request.getExtInfo())
                : new JSONObject();
            if (ext == null) {
                ext = new JSONObject();
            }
        } catch (Exception e) {
            ext = new JSONObject();
        }
        if ("EN".equalsIgnoreCase(languageCode)) {
            Integer voiceType = ext.getInteger("voiceType");
            if (voiceType == null || (voiceType != 1 && voiceType != 2)) {
                voiceType = 2;
            }
            ext.put("voiceType", voiceType);
        } else {
            ext.remove("voiceType");
        }
        if (resetDailyProgress) {
            String today = LocalDate.now().toString();
            ext.put("studyDate", today);
            ext.put("todayLearnWordCodes", new JSONArray());
            ext.put("todayReviewWordCodes", new JSONArray());
            ext.put("countedLearnWordCodes", new JSONArray());
            ext.put("countedReviewWordCodes", new JSONArray());
            ext.put("learnDone", 0);
            ext.put("learnTodo", request.getDailyNewCount());
            ext.put("reviewDone", 0);
            ext.put("reviewTodo", request.getDailyReviewCount());
        }
        request.setExtInfo(ext.isEmpty() ? null : ext.toJSONString());
    }

    private void persistCurrentProgressToPlan(UserWordCurrentBookResponse current) {
        if (current == null || current.getUserId() == null || !StringUtils.hasText(current.getBookCode())) {
            return;
        }
        UserWordStudyPlanRequest query = new UserWordStudyPlanRequest();
        query.setUserId(current.getUserId());
        query.setBookCode(current.getBookCode());
        List<UserWordStudyPlanResponse> plans = list(query);
        if (plans.isEmpty()) {
            return;
        }
        UserWordStudyPlanResponse plan = plans.get(0);
        JSONObject ext = parseExt(plan.getExtInfo());
        // 若 current.ext 仍残留旧结构，优先合并今日词表
        JSONObject curExt = parseExt(current.getExtInfo());
        if (curExt.containsKey("todayLearnWordCodes")) {
            ext.put("todayLearnWordCodes", curExt.getJSONArray("todayLearnWordCodes"));
        }
        if (curExt.containsKey("todayReviewWordCodes")) {
            ext.put("todayReviewWordCodes", curExt.getJSONArray("todayReviewWordCodes"));
        }
        if (curExt.containsKey("countedLearnWordCodes")) {
            ext.put("countedLearnWordCodes", curExt.getJSONArray("countedLearnWordCodes"));
        }
        if (curExt.containsKey("countedReviewWordCodes")) {
            ext.put("countedReviewWordCodes", curExt.getJSONArray("countedReviewWordCodes"));
        }
        if (StringUtils.hasText(curExt.getString("studyDate"))) {
            ext.put("studyDate", curExt.getString("studyDate"));
        }
        ext.put("learnDone", nvl(current.getLearnDone()));
        ext.put("learnTodo", nvl(current.getLearnTodo()));
        ext.put("reviewDone", nvl(current.getReviewDone()));
        ext.put("reviewTodo", nvl(current.getReviewTodo()));
        saveDailyProgress(plan.getId(), ext.toJSONString());
    }

    private JSONObject parseExt(String extInfo) {
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

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private void fillBook(UserWordStudyPlanResponse plan) {
        if (plan == null || !StringUtils.hasText(plan.getBookCode())) {
            return;
        }
        AdminWordBookResponse book = bookService.getByCode(plan.getBookCode());
        if (book == null) {
            return;
        }
        plan.setBookName(book.getName());
        plan.setWordCount(book.getWordCount());
    }
}
