package com.arelore.server.core.biz.word.user;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.arelore.server.core.biz.word.admin.AdminWordBookService;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBookResponse;
import com.arelore.server.core.biz.word.admin.mapper.AdminWordEntryMapper;
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
    private final AdminWordEntryMapper entryMapper;

    public UserWordStudyPlanServiceImpl(
        UserWordStudyPlanMapper mapper,
        AdminWordBookService bookService,
        UserWordCurrentBookService currentBookService,
        AdminWordEntryMapper entryMapper
    ) {
        this.mapper = mapper;
        this.bookService = bookService;
        this.currentBookService = currentBookService;
        this.entryMapper = entryMapper;
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
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (!StringUtils.hasText(request.getBookCode())) {
            throw new IllegalArgumentException("单词本code不能为空");
        }

        // 换书前：把旧词书今日进度写回对应 plan，避免进度串书 / 丢失
        UserWordCurrentBookRequest byUser = new UserWordCurrentBookRequest();
        byUser.setUserId(request.getUserId());
        List<UserWordCurrentBookResponse> userCurrentList = currentBookService.list(byUser);
        UserWordCurrentBookResponse userCurrent = userCurrentList.isEmpty() ? null : userCurrentList.get(0);
        if (userCurrent != null
            && StringUtils.hasText(userCurrent.getBookCode())
            && !userCurrent.getBookCode().equals(request.getBookCode().trim())
        ) {
            persistCurrentProgressToPlan(userCurrent);
        }

        UserWordStudyPlanRequest query = new UserWordStudyPlanRequest();
        query.setUserId(request.getUserId());
        query.setBookCode(request.getBookCode().trim());
        List<UserWordStudyPlanResponse> existsList = list(query);
        UserWordStudyPlanResponse exists = existsList.isEmpty() ? null : existsList.get(0);

        String targetBook = request.getBookCode().trim();
        // 目标词书今日已开练/已完成（plan 或学习记录）：禁止重置配额（防 ABA）
        boolean preserveToday = hasTodayProgress(exists)
            || entryMapper.countLearnedToday(request.getUserId(), targetBook) > 0
            || entryMapper.countReviewedToday(request.getUserId(), targetBook) > 0;
        if (preserveToday) {
            JSONObject merged = exists != null ? parseExt(exists.getExtInfo()) : new JSONObject();
            JSONObject reqExt = parseExt(request.getExtInfo());
            if (reqExt.containsKey("voiceType")) {
                merged.put("voiceType", reqExt.get("voiceType"));
            }
            if (reqExt.containsKey("reviewLookbackDays")) {
                merged.put("reviewLookbackDays", reqExt.get("reviewLookbackDays"));
            }
            if (reqExt.containsKey("softLandEnabled")) {
                merged.put("softLandEnabled", reqExt.get("softLandEnabled"));
            }
            Integer dailyNew = request.getDailyNewCount() != null
                ? request.getDailyNewCount()
                : (exists != null ? exists.getDailyNewCount() : null);
            reconcileTodayProgressFromRecords(merged, request.getUserId(), targetBook, dailyNew);
            request.setExtInfo(merged.toJSONString());
        }

        normalizeAndValidate(request, !preserveToday);

        // 重置当日进度时仍保留历史 studyDates（累计学习用）
        if (!preserveToday && exists != null) {
            JSONObject oldExt = parseExt(exists.getExtInfo());
            JSONArray studyDates = oldExt.getJSONArray("studyDates");
            if (studyDates != null && !studyDates.isEmpty()) {
                JSONObject newExt = parseExt(request.getExtInfo());
                newExt.put("studyDates", studyDates);
                request.setExtInfo(newExt.toJSONString());
            }
        }

        if (exists == null) {
            super.create(request);
        } else {
            request.setId(exists.getId());
            UserWordStudyPlan patch = new UserWordStudyPlan();
            patch.setId(exists.getId());
            patch.setNewReviewRatio(request.getNewReviewRatio());
            patch.setDailyNewCount(request.getDailyNewCount());
            patch.setDailyReviewCount(request.getDailyReviewCount());
            patch.setPlanDays(request.getPlanDays());
            patch.setExtInfo(request.getExtInfo());
            mapper.updateById(patch);
        }

        // 当前词书行只镜像展示；今日词表/完成数以 plan.ext_info 为准
        UserWordCurrentBookRequest currentReq = new UserWordCurrentBookRequest();
        currentReq.setUserId(request.getUserId());
        currentReq.setBookCode(request.getBookCode());
        applyPlanProgressToCurrent(currentReq, parseExt(request.getExtInfo()), request.getDailyNewCount());
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
        Integer lookback = ext.getInteger("reviewLookbackDays");
        if (lookback == null || lookback < 1 || lookback > 7) {
            lookback = 3;
        }
        ext.put("reviewLookbackDays", lookback);
        if (!ext.containsKey("softLandEnabled")) {
            ext.put("softLandEnabled", true);
        } else {
            Boolean soft = ext.getBoolean("softLandEnabled");
            ext.put("softLandEnabled", soft == null || soft);
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
            ext.put("reviewTodo", 0);
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
        // 同日完成数只增不减；待办按词表完整性选择公式（防截断词表把 13 已完成算坏）
        boolean sameDay = LocalDate.now().toString().equals(ext.getString("studyDate"));
        int dailyNew = 0;
        try {
            UserWordStudyPlanResponse p = plan;
            if (p.getDailyNewCount() != null && p.getDailyNewCount() > 0) {
                dailyNew = p.getDailyNewCount();
            }
        } catch (Exception ignored) {
            // ignore
        }
        if (dailyNew <= 0) {
            dailyNew = Math.max(nvl(current.getLearnDone()) + nvl(current.getLearnTodo()), 10);
        }
        int learnDone = Math.max(ext.getIntValue("learnDone"), nvl(current.getLearnDone()));
        int reviewDone = Math.max(ext.getIntValue("reviewDone"), nvl(current.getReviewDone()));
        if (!sameDay && learnDone <= 0 && reviewDone <= 0) {
            ext.put("learnDone", nvl(current.getLearnDone()));
            ext.put("learnTodo", nvl(current.getLearnTodo()));
            ext.put("reviewDone", nvl(current.getReviewDone()));
            ext.put("reviewTodo", nvl(current.getReviewTodo()));
        } else {
            ext.put("studyDate", LocalDate.now().toString());
            ext.put("learnDone", learnDone);
            ext.put("reviewDone", reviewDone);
            int learnCodes = ext.getJSONArray("todayLearnWordCodes") == null
                ? 0 : ext.getJSONArray("todayLearnWordCodes").size();
            int reviewCodes = ext.getJSONArray("todayReviewWordCodes") == null
                ? 0 : ext.getJSONArray("todayReviewWordCodes").size();
            // 词表长度 < 已完成：说明词表被收成「仅剩待学」，待办必须用配额公式
            if (learnCodes > 0 && learnCodes >= learnDone) {
                ext.put("learnTodo", Math.max(0, learnCodes - learnDone));
            } else {
                ext.put("learnTodo", Math.max(0, dailyNew - learnDone));
            }
            if (reviewCodes > 0 && reviewCodes >= reviewDone) {
                ext.put("reviewTodo", Math.max(0, reviewCodes - reviewDone));
            } else {
                ext.put("reviewTodo", Math.max(0, nvl(current.getReviewTodo())));
            }
        }
        saveDailyProgress(plan.getId(), ext.toJSONString());
    }

    /**
     * 目标词书今日是否已有进度：studyDate=今天则一律保留（避免清掉 learnDone）；
     * 或学习记录表已有今日新学/复习。
     */
    private boolean hasTodayProgress(UserWordStudyPlanResponse exists) {
        if (exists == null || exists.getUserId() == null || !StringUtils.hasText(exists.getBookCode())) {
            return false;
        }
        JSONObject ext = parseExt(exists.getExtInfo());
        // 只要标记过今天，就禁止重置（含已完成数），防止 ABA 把 13 写成 12
        if (LocalDate.now().toString().equals(ext.getString("studyDate"))) {
            return true;
        }
        return entryMapper.countLearnedToday(exists.getUserId(), exists.getBookCode()) > 0
            || entryMapper.countReviewedToday(exists.getUserId(), exists.getBookCode()) > 0;
    }

    /**
     * 用学习记录校准今日完成/待办：即便 plan 词表被换书逻辑清空，已学过的配额也不能再发。
     */
    private void reconcileTodayProgressFromRecords(
        JSONObject ext,
        java.math.BigDecimal userId,
        String bookCode,
        Integer dailyNewCount
    ) {
        if (ext == null || userId == null || !StringUtils.hasText(bookCode)) {
            return;
        }
        int dailyNew = dailyNewCount == null || dailyNewCount <= 0 ? 10 : dailyNewCount;
        // confirm 时尚不知 reviewLimit，复习侧用记录数与已有 todo/done 取大
        int learnedToday = entryMapper.countLearnedToday(userId, bookCode);
        int reviewedToday = entryMapper.countReviewedToday(userId, bookCode);
        if (learnedToday <= 0 && reviewedToday <= 0) {
            return;
        }
        String today = LocalDate.now().toString();
        ext.put("studyDate", today);

        JSONArray learnCodes = ext.getJSONArray("todayLearnWordCodes");
        JSONArray countedLearn = ext.getJSONArray("countedLearnWordCodes");
        int prevDone = Math.max(0, ext.getIntValue("learnDone"));
        int countedSize = countedLearn == null ? 0 : countedLearn.size();
        // 完成数取 plan 与 counted 较大值；都空才用建档数（防 13→12，且可用 counted 救回）
        int learnDone = Math.max(prevDone, countedSize);
        if (learnDone <= 0) {
            learnDone = learnedToday;
        }
        learnDone = Math.min(dailyNew, learnDone);
        int codeSize = learnCodes == null ? 0 : learnCodes.size();
        int learnTodo;
        if (codeSize > 0 && codeSize >= learnDone) {
            learnTodo = Math.max(0, codeSize - learnDone);
        } else {
            learnTodo = Math.max(0, dailyNew - learnDone);
        }
        if (learnDone + learnTodo > dailyNew) {
            learnTodo = Math.max(0, dailyNew - learnDone);
        }
        ext.put("learnDone", learnDone);
        ext.put("learnTodo", learnTodo);

        int reviewDone = Math.max(ext.getIntValue("reviewDone"), reviewedToday);
        ext.put("reviewDone", reviewDone);
        JSONArray reviewCodes = ext.getJSONArray("todayReviewWordCodes");
        int reviewCodeSize = reviewCodes == null ? 0 : reviewCodes.size();
        if (reviewCodeSize > 0 && reviewCodeSize >= reviewDone) {
            ext.put("reviewTodo", Math.max(0, reviewCodeSize - reviewDone));
        } else if (reviewedToday > 0) {
            // 有复习记录但词表丢失：不把待办抬回去刷复习；保留已有 reviewTodo 或 0
            if (!ext.containsKey("reviewTodo")) {
                ext.put("reviewTodo", 0);
            }
        }
    }

    /** 用 plan 今日进度镜像到 current_book 展示字段 */
    private void applyPlanProgressToCurrent(
        UserWordCurrentBookRequest currentReq,
        JSONObject planExt,
        Integer dailyNewCount
    ) {
        int dailyNew = dailyNewCount == null || dailyNewCount <= 0 ? 10 : dailyNewCount;
        String today = LocalDate.now().toString();
        if (planExt != null && today.equals(planExt.getString("studyDate"))) {
            int learnDone = planExt.getIntValue("learnDone");
            int reviewDone = planExt.getIntValue("reviewDone");
            JSONArray learnCodes = planExt.getJSONArray("todayLearnWordCodes");
            JSONArray reviewCodes = planExt.getJSONArray("todayReviewWordCodes");
            int codeSize = learnCodes == null ? 0 : learnCodes.size();
            int learnTodo;
            if (planExt.containsKey("learnTodo")
                && (codeSize <= 0 || codeSize < learnDone)) {
                // 已按配额公式写好的 learnTodo（含 ABA 恢复）优先
                learnTodo = Math.max(0, planExt.getIntValue("learnTodo"));
            } else if (codeSize > 0 && codeSize >= learnDone) {
                learnTodo = Math.max(0, codeSize - learnDone);
            } else {
                learnTodo = Math.max(0, dailyNew - learnDone);
            }
            int reviewTodo;
            if (reviewCodes != null && !reviewCodes.isEmpty()) {
                reviewTodo = Math.max(0, reviewCodes.size() - reviewDone);
            } else {
                reviewTodo = planExt.getIntValue("reviewTodo");
            }
            currentReq.setLearnDone(learnDone);
            currentReq.setLearnTodo(learnTodo);
            currentReq.setReviewDone(reviewDone);
            currentReq.setReviewTodo(reviewTodo);
            return;
        }
        currentReq.setLearnDone(0);
        currentReq.setLearnTodo(dailyNew);
        currentReq.setReviewDone(0);
        currentReq.setReviewTodo(0);
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
