package com.arelore.server.core.biz.word.user;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.arelore.server.core.biz.word.admin.AdminWordBookService;
import com.arelore.server.core.biz.word.admin.dto.AdminWordBookResponse;
import com.arelore.server.core.biz.word.admin.entity.AdminWordBaseInfo;
import com.arelore.server.core.biz.word.admin.entity.AdminWordEntry;
import com.arelore.server.core.biz.word.admin.mapper.AdminWordBaseInfoMapper;
import com.arelore.server.core.biz.word.admin.mapper.AdminWordEntryMapper;
import com.arelore.server.core.biz.word.admin.support.YoudaoDictClient;
import com.arelore.server.core.biz.word.user.dto.UserWordCurrentBookRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordCurrentBookResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordLearnRecordRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordLearnRecordResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyAnswerRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyAnswerResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyCardResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyExampleResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyPhraseResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyRelatedGroupResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyRelatedItemResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudySynonymGroupResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyPlanRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyPlanResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudySessionRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordStudySessionResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyStatsResponse;
import com.arelore.server.core.biz.word.user.entity.UserWordLearnRecord;
import com.arelore.server.core.biz.word.user.mapper.UserWordLearnRecordMapper;
import com.arelore.server.core.service.BaseServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service
public class UserWordLearnRecordServiceImpl
    extends BaseServiceImpl<UserWordLearnRecordRequest, UserWordLearnRecordResponse, UserWordLearnRecord>
    implements UserWordLearnRecordService {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserWordLearnRecordMapper mapper;
    private final AdminWordEntryMapper entryMapper;
    private final AdminWordBaseInfoMapper baseInfoMapper;
    private final AdminWordBookService bookService;
    private final UserWordCurrentBookService currentBookService;
    private final UserWordStudyPlanService studyPlanService;

    public UserWordLearnRecordServiceImpl(
        UserWordLearnRecordMapper mapper,
        AdminWordEntryMapper entryMapper,
        AdminWordBaseInfoMapper baseInfoMapper,
        AdminWordBookService bookService,
        UserWordCurrentBookService currentBookService,
        UserWordStudyPlanService studyPlanService
    ) {
        this.mapper = mapper;
        this.entryMapper = entryMapper;
        this.baseInfoMapper = baseInfoMapper;
        this.bookService = bookService;
        this.currentBookService = currentBookService;
        this.studyPlanService = studyPlanService;
    }

    @Override
    protected UserWordLearnRecordMapper mapper() {
        return mapper;
    }

    @Override
    protected Class<UserWordLearnRecordResponse> responseClass() {
        return UserWordLearnRecordResponse.class;
    }

    @Override
    protected UserWordLearnRecordResponse toResponse(UserWordLearnRecord entity) {
        UserWordLearnRecordResponse response = super.toResponse(entity);
        com.arelore.server.core.biz.word.user.support.WordUserIdStrSupport.fillIdStr(response);
        return response;
    }

    @Override
    protected LambdaQueryWrapper<UserWordLearnRecord> buildWrapper(UserWordLearnRecordRequest request) {
        com.arelore.server.core.biz.word.user.support.WordUserIdStrSupport.applyIdStrToUserId(request);
        LambdaQueryWrapper<UserWordLearnRecord> wrapper = new LambdaQueryWrapper<>();
        if (request == null) {
            return wrapper;
        }
        if (request.getUserId() != null) {
            wrapper.eq(UserWordLearnRecord::getUserId, request.getUserId());
        }
        if (StringUtils.hasText(request.getBookCode())) {
            wrapper.eq(UserWordLearnRecord::getBookCode, request.getBookCode());
        }
        if (StringUtils.hasText(request.getWordCode())) {
            wrapper.eq(UserWordLearnRecord::getWordCode, request.getWordCode());
        }
        wrapper.orderByDesc(UserWordLearnRecord::getModifyTime);
        return wrapper;
    }

    @Override
    public UserWordStudySessionResponse createSession(UserWordStudySessionRequest request) {
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        UserWordCurrentBookResponse current = requireCurrentBook(request.getUserId(), request.getBookCode());
        String bookCode = current.getBookCode();

        UserWordStudyPlanRequest planQuery = new UserWordStudyPlanRequest();
        planQuery.setUserId(request.getUserId());
        planQuery.setBookCode(bookCode);
        List<UserWordStudyPlanResponse> plans = studyPlanService.list(planQuery);
        if (plans.isEmpty()) {
            throw new IllegalArgumentException("请先为当前单词本确认学习计划");
        }
        UserWordStudyPlanResponse plan = plans.get(0);

        int learnLimit = plan.getDailyNewCount() != null && plan.getDailyNewCount() > 0
            ? plan.getDailyNewCount() : 10;
        int reviewLimit = plan.getDailyReviewCount() != null && plan.getDailyReviewCount() > 0
            ? plan.getDailyReviewCount() : 10;
        int voiceType = readVoiceType(plan.getExtInfo());

        JSONObject planExt = parseExt(plan.getExtInfo());
        String today = LocalDate.now().toString();
        int lookbackDays = readReviewLookbackDays(planExt);
        // 学习记录为权威：今日已新学/复习数量（防 ABA 清 plan 后重发配额）
        int learnedToday = entryMapper.countLearnedToday(request.getUserId(), bookCode);
        int reviewedToday = entryMapper.countReviewedToday(request.getUserId(), bookCode);
        int candidateCount = entryMapper.countReviewCandidates(request.getUserId(), bookCode, lookbackDays);
        int reviewTarget = resolveReviewTarget(
            reviewLimit, candidateCount, learnLimit, planExt, learnedToday, reviewedToday);
        boolean newDay = !today.equals(planExt.getString("studyDate"))
            && learnedToday <= 0 && reviewedToday <= 0;
        if (newDay) {
            planExt.put("studyDate", today);
            planExt.put("todayLearnWordCodes", new JSONArray());
            planExt.put("todayReviewWordCodes", new JSONArray());
            planExt.put("countedLearnWordCodes", new JSONArray());
            planExt.put("countedReviewWordCodes", new JSONArray());
            planExt.put("learnDone", 0);
            planExt.put("learnTodo", 0);
            planExt.put("reviewDone", 0);
            planExt.put("reviewTodo", 0);
        } else {
            planExt.put("studyDate", today);
        }

        List<String> todayLearnCodes = readStringList(planExt, "todayLearnWordCodes");
        List<String> todayReviewCodes = readStringList(planExt, "todayReviewWordCodes");
        List<String> countedLearn = readStringList(planExt, "countedLearnWordCodes");
        List<String> countedReview = readStringList(planExt, "countedReviewWordCodes");
        int planLearnDone = planExt.containsKey("learnDone")
            ? planExt.getIntValue("learnDone") : countedLearn.size();
        int planReviewDone = planExt.containsKey("reviewDone")
            ? planExt.getIntValue("reviewDone") : countedReview.size();
        AdminWordBookResponse bookMeta = bookService.getByCode(bookCode);
        int bookWordCount = bookMeta != null && bookMeta.getWordCount() != null ? bookMeta.getWordCount() : 0;
        int learnedTotal = entryMapper.countLearned(request.getUserId(), bookCode);
        int remainWords = Math.max(0, bookWordCount - learnedTotal);

        int[] learnProgress = resolveDailyLearnProgress(
            learnLimit, planLearnDone, learnedToday, countedLearn.size(), todayLearnCodes, remainWords);
        int learnDone = learnProgress[0];
        int learnTodo = learnProgress[1];
        int[] reviewProgress = resolveDailyReviewProgress(
            reviewTarget, planReviewDone, reviewedToday, countedReview.size(), todayReviewCodes);
        int reviewDone = reviewProgress[0];
        int reviewTodo = reviewProgress[1];

        // 今日新学已达配额：禁止再抽新词；已有完整今日词表则冻结
        boolean learnQuotaFilled = learnTodo <= 0 && learnDone > 0;
        boolean hasFullLearnList = !todayLearnCodes.isEmpty() && todayLearnCodes.size() >= learnDone;
        if (!learnQuotaFilled && todayLearnCodes.isEmpty()) {
            // 词表丢失：只补「剩余待办」个新词，待办仍按配额公式（daily - done）
            int need = learnTodo;
            Set<String> existsCodes = new HashSet<>();
            List<AdminWordEntry> more = entryMapper.selectUnlearned(request.getUserId(), bookCode, need);
            for (AdminWordEntry entry : more) {
                if (entry == null || !StringUtils.hasText(entry.getWordCode())) {
                    continue;
                }
                if (existsCodes.add(entry.getWordCode())) {
                    todayLearnCodes.add(entry.getWordCode());
                    if (todayLearnCodes.size() >= need) {
                        break;
                    }
                }
            }
            // 补完后重算：词表是「仅剩待学」子集，todo 仍用配额
            learnProgress = resolveDailyLearnProgress(
                learnLimit, learnDone, learnedToday, countedLearn.size(), todayLearnCodes, remainWords);
            learnDone = learnProgress[0];
            learnTodo = learnProgress[1];
        } else if (!learnQuotaFilled && !hasFullLearnList && todayLearnCodes.size() < learnTodo) {
            // 词表偏短时补足到剩余待办数
            Set<String> existsCodes = new HashSet<>(todayLearnCodes);
            int need = Math.max(0, learnTodo - todayLearnCodes.size());
            List<AdminWordEntry> more = entryMapper.selectUnlearned(request.getUserId(), bookCode, need);
            for (AdminWordEntry entry : more) {
                if (entry == null || !StringUtils.hasText(entry.getWordCode())) {
                    continue;
                }
                if (existsCodes.add(entry.getWordCode())) {
                    todayLearnCodes.add(entry.getWordCode());
                    if (todayLearnCodes.size() >= learnTodo) {
                        break;
                    }
                }
            }
        }

        // 复习：配额已完成则不再重建；否则仅在无词表时构建
        boolean reviewQuotaFilled = reviewTodo <= 0 && (reviewDone > 0 || reviewTarget == 0);
        if (!reviewQuotaFilled && todayReviewCodes.isEmpty()) {
            List<String> rebuilt = new ArrayList<>();
            List<AdminWordEntry> candidates = entryMapper.selectForReview(
                request.getUserId(), bookCode, reviewTarget, lookbackDays
            );
            Set<String> blocked = new HashSet<>(todayLearnCodes);
            for (AdminWordEntry entry : candidates) {
                if (entry == null || !StringUtils.hasText(entry.getWordCode())) {
                    continue;
                }
                if (blocked.add(entry.getWordCode())) {
                    rebuilt.add(entry.getWordCode());
                    if (rebuilt.size() >= reviewTarget) {
                        break;
                    }
                }
            }
            todayReviewCodes = rebuilt;
            reviewProgress = resolveDailyReviewProgress(
                reviewTarget, reviewDone, reviewedToday, countedReview.size(), todayReviewCodes);
            reviewDone = reviewProgress[0];
            reviewTodo = reviewProgress[1];
        }

        // 同日完成数只增不减，防止开练回写时把已展示的 13 写成更小值
        learnDone = Math.max(learnDone, planLearnDone);
        reviewDone = Math.max(reviewDone, planReviewDone);
        learnTodo = Math.max(0, Math.min(learnTodo, Math.max(0, learnLimit - learnDone)));
        reviewTodo = Math.max(0, Math.min(reviewTodo, Math.max(0, reviewTarget - reviewDone)));

        // 始终回写：校准后的完成数落库
        planExt.put("todayLearnWordCodes", toJsonArray(todayLearnCodes));
        planExt.put("todayReviewWordCodes", toJsonArray(todayReviewCodes));
        planExt.put("learnDone", learnDone);
        planExt.put("learnTodo", learnTodo);
        planExt.put("reviewDone", reviewDone);
        planExt.put("reviewTodo", reviewTodo);
        planExt.put("studyDate", today);
        studyPlanService.saveDailyProgress(plan.getId(), planExt.toJSONString());
        syncCurrentFromPlan(current, request.getUserId(), bookCode, planExt);

        // 未完成今日配额：只返回未作答词；已完成（含 plan 词表丢失但记录已满配额）可重复练
        boolean dayFinished = learnTodo <= 0 && reviewTodo <= 0
            && (learnQuotaFilled || reviewQuotaFilled || learnedToday > 0 || reviewedToday > 0
            || !todayLearnCodes.isEmpty() || !todayReviewCodes.isEmpty());
        List<String> sessionLearnCodes;
        List<String> sessionReviewCodes;
        if (dayFinished) {
            sessionLearnCodes = todayLearnCodes;
            sessionReviewCodes = todayReviewCodes;
        } else {
            sessionLearnCodes = filterRemaining(todayLearnCodes, countedLearn);
            sessionReviewCodes = filterRemaining(todayReviewCodes, countedReview);
        }

        List<AdminWordEntry> learnEntries = loadEntriesByCodes(bookCode, sessionLearnCodes);
        List<AdminWordEntry> reviewEntries = loadEntriesByCodes(bookCode, sessionReviewCodes);
        String languageCode = resolveBookLanguageCode(bookCode);
        Map<String, WordPictureBundle> picturesByWord = loadBasePicturesByWord(languageCode, learnEntries, reviewEntries);
        List<UserWordStudyCardResponse> learnCards = toCards(learnEntries, "learn", voiceType, picturesByWord);
        List<UserWordStudyCardResponse> reviewCards = toCards(reviewEntries, "review", voiceType, picturesByWord);
        // 先复习未完成词，再学新词
        List<UserWordStudyCardResponse> mixed = concatReviewThenLearn(reviewCards, learnCards);

        UserWordStudySessionResponse response = new UserWordStudySessionResponse();
        response.setBookCode(bookCode);
        response.setBookName(current.getBookName());
        response.setVoiceType(voiceType);
        response.setLearnDone(learnDone);
        response.setReviewDone(reviewDone);
        // 总数按每日配额展示（13+7=20），避免词表被收成「仅剩待学」后显示成 7/7
        response.setLearnTotal(Math.max(learnDone + learnTodo, todayLearnCodes.size()));
        response.setReviewTotal(Math.max(reviewDone + reviewTodo, todayReviewCodes.size()));
        response.setCards(mixed);
        return response;
    }

    @Override
    public UserWordCurrentBookResponse enrichHome(UserWordCurrentBookResponse current) {
        if (current == null || current.getUserId() == null || !StringUtils.hasText(current.getBookCode())) {
            return current;
        }
        String bookCode = current.getBookCode();
        int wordCount = current.getWordCount() == null ? 0 : current.getWordCount();
        int learnedCount = entryMapper.countLearned(current.getUserId(), bookCode);
        current.setLearnedCount(learnedCount);
        int progressPercent = wordCount <= 0 ? 0 : Math.min(100, (int) Math.round(learnedCount * 100.0 / wordCount));
        current.setProgressPercent(progressPercent);

        UserWordStudyPlanRequest planQuery = new UserWordStudyPlanRequest();
        planQuery.setUserId(current.getUserId());
        planQuery.setBookCode(bookCode);
        List<UserWordStudyPlanResponse> plans = studyPlanService.list(planQuery);
        if (plans.isEmpty()) {
            current.setPlanDays(0);
            current.setRemainingDays(0);
            return current;
        }
        UserWordStudyPlanResponse plan = plans.get(0);
        int dailyNew = plan.getDailyNewCount() != null && plan.getDailyNewCount() > 0
            ? plan.getDailyNewCount() : 10;
        int reviewLimit = plan.getDailyReviewCount() != null && plan.getDailyReviewCount() > 0
            ? plan.getDailyReviewCount() : dailyNew;
        int planDays = plan.getPlanDays() != null ? plan.getPlanDays() : 0;
        current.setPlanDays(planDays);
        int remainWords = Math.max(0, wordCount - learnedCount);
        int remainingDays = dailyNew <= 0 ? 0 : (int) Math.ceil(remainWords * 1.0 / dailyNew);
        current.setRemainingDays(remainingDays);

        JSONObject planExt = parseExt(plan.getExtInfo());
        String today = LocalDate.now().toString();
        int lookbackDays = readReviewLookbackDays(planExt);
        int learnedToday = entryMapper.countLearnedToday(current.getUserId(), bookCode);
        int reviewedToday = entryMapper.countReviewedToday(current.getUserId(), bookCode);
        int candidateCount = entryMapper.countReviewCandidates(current.getUserId(), bookCode, lookbackDays);
        int reviewTarget = resolveReviewTarget(
            reviewLimit, candidateCount, dailyNew, planExt, learnedToday, reviewedToday);
        boolean sameDay = today.equals(planExt.getString("studyDate")) || learnedToday > 0 || reviewedToday > 0;

        if (!sameDay) {
            // 新的一天尚未开练：新学=计划配额与剩余词取小；复习=回溯池 ∩ 上限（可软着陆）
            int learnTodo = Math.min(dailyNew, remainWords);
            current.setLearnDone(0);
            current.setLearnTodo(learnTodo);
            current.setReviewDone(0);
            current.setReviewTodo(reviewTarget);
            return current;
        }

        List<String> todayReviewCodes = readStringList(planExt, "todayReviewWordCodes");
        List<String> countedReview = readStringList(planExt, "countedReviewWordCodes");
        int planReviewDone = planExt.containsKey("reviewDone")
            ? planExt.getIntValue("reviewDone") : nvl(current.getReviewDone());
        int[] reviewProgress = resolveDailyReviewProgress(
            reviewTarget, planReviewDone, reviewedToday, countedReview.size(), todayReviewCodes);
        current.setReviewDone(reviewProgress[0]);
        current.setReviewTodo(reviewProgress[1]);

        List<String> todayLearnCodes = readStringList(planExt, "todayLearnWordCodes");
        List<String> countedLearn = readStringList(planExt, "countedLearnWordCodes");
        int planLearnDone = planExt.containsKey("learnDone")
            ? planExt.getIntValue("learnDone") : nvl(current.getLearnDone());
        int[] learnProgress = resolveDailyLearnProgress(
            dailyNew, planLearnDone, learnedToday, countedLearn.size(), todayLearnCodes, remainWords);
        current.setLearnDone(learnProgress[0]);
        current.setLearnTodo(learnProgress[1]);
        return current;
    }

    /**
     * 今日新学进度：[done, todo]
     * done = max(plan完成, counted认识数)；二者都空才用今日建档数兜底。
     * 同日不把已完成调低；词表被截成「仅剩待学」时 todo=配额-done。
     */
    private static int[] resolveDailyLearnProgress(
        int dailyNew,
        int planLearnDone,
        int learnedToday,
        int countedLearnSize,
        List<String> todayLearnCodes,
        int remainWords
    ) {
        int quota = Math.max(0, dailyNew);
        int done = Math.max(Math.max(0, planLearnDone), Math.max(0, countedLearnSize));
        if (done <= 0 && learnedToday > 0) {
            done = Math.min(quota, learnedToday);
        } else {
            done = Math.min(quota, done);
        }
        int codeSize = todayLearnCodes == null ? 0 : todayLearnCodes.size();
        int todo;
        if (codeSize > 0 && codeSize >= done) {
            todo = Math.max(0, codeSize - done);
        } else {
            todo = Math.max(0, quota - done);
        }
        if (remainWords >= 0) {
            todo = Math.min(todo, remainWords);
        }
        if (done + todo > quota) {
            todo = Math.max(0, quota - done);
        }
        return new int[]{done, todo};
    }

    /** 今日复习进度：[done, todo] */
    private static int[] resolveDailyReviewProgress(
        int reviewTarget,
        int planReviewDone,
        int reviewedToday,
        int countedReviewSize,
        List<String> todayReviewCodes
    ) {
        int target = Math.max(0, reviewTarget);
        int done = Math.max(Math.max(0, planReviewDone), Math.max(0, countedReviewSize));
        if (done <= 0 && reviewedToday > 0) {
            done = Math.min(target, reviewedToday);
        } else {
            done = Math.min(target, done);
        }
        int codeSize = todayReviewCodes == null ? 0 : todayReviewCodes.size();
        int todo;
        if (codeSize > 0 && codeSize >= done) {
            todo = Math.max(0, codeSize - done);
        } else {
            todo = Math.max(0, target - done);
        }
        if (done + todo > target) {
            todo = Math.max(0, target - done);
        }
        return new int[]{done, todo};
    }

    @Override
    public UserWordStudyAnswerResponse answer(UserWordStudyAnswerRequest request) {
        if (request == null || request.getUserId() == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        if (!StringUtils.hasText(request.getBookCode())) {
            throw new IllegalArgumentException("单词本code不能为空");
        }
        if (!StringUtils.hasText(request.getWordCode())) {
            throw new IllegalArgumentException("单词code不能为空");
        }
        if (request.getRememberFlag() == null) {
            throw new IllegalArgumentException("请选择认识或不认识");
        }
        String mode = StringUtils.hasText(request.getMode()) ? request.getMode().trim() : "learn";
        if (!"learn".equals(mode) && !"review".equals(mode)) {
            throw new IllegalArgumentException("mode 仅支持 learn / review");
        }

        String bookCode = request.getBookCode().trim();
        String wordCode = request.getWordCode().trim();
        UserWordCurrentBookResponse current = requireCurrentBook(request.getUserId(), bookCode);

        UserWordStudyPlanRequest planQuery = new UserWordStudyPlanRequest();
        planQuery.setUserId(request.getUserId());
        planQuery.setBookCode(bookCode);
        List<UserWordStudyPlanResponse> plans = studyPlanService.list(planQuery);
        if (plans.isEmpty()) {
            throw new IllegalArgumentException("请先为当前单词本确认学习计划");
        }
        UserWordStudyPlanResponse plan = plans.get(0);

        LambdaQueryWrapper<AdminWordEntry> entryWrapper = new LambdaQueryWrapper<>();
        entryWrapper.eq(AdminWordEntry::getBookCode, bookCode).eq(AdminWordEntry::getWordCode, wordCode);
        AdminWordEntry entry = entryMapper.selectOne(entryWrapper);
        if (entry == null) {
            throw new IllegalArgumentException("词条不存在");
        }

        UserWordLearnRecordRequest query = new UserWordLearnRecordRequest();
        query.setUserId(request.getUserId());
        query.setBookCode(bookCode);
        query.setWordCode(wordCode);
        List<UserWordLearnRecordResponse> existsList = list(query);
        UserWordLearnRecordResponse exists = existsList.isEmpty() ? null : existsList.get(0);

        JSONObject ext = parseExt(exists == null ? null : exists.getExtInfo());
        ext.put("word", entry.getWord());
        ext.put("rememberFlag", request.getRememberFlag());
        ext.put("lastLearnTime", LocalDateTime.now().format(TIME_FMT));
        if ("review".equals(mode)) {
            ext.put("reviewCount", ext.getIntValue("reviewCount") + 1);
        } else {
            ext.put("learnCount", ext.getIntValue("learnCount") + 1);
        }

        UserWordLearnRecordRequest save = new UserWordLearnRecordRequest();
        save.setUserId(request.getUserId());
        save.setBookCode(bookCode);
        save.setWordCode(wordCode);
        save.setExtInfo(ext.toJSONString());
        if (exists == null) {
            create(save);
        } else {
            save.setId(exists.getId());
            update(save);
        }

        JSONObject planExt = parseExt(plan.getExtInfo());
        String today = LocalDate.now().toString();
        if (!today.equals(planExt.getString("studyDate"))) {
            // 跨日作答极少见；待办在 createSession 按实际上一学习日重算
            planExt.put("studyDate", today);
            planExt.put("countedLearnWordCodes", new JSONArray());
            planExt.put("countedReviewWordCodes", new JSONArray());
            planExt.put("learnDone", 0);
            planExt.put("learnTodo", 0);
            planExt.put("reviewDone", 0);
            planExt.put("reviewTodo", 0);
        }
        List<String> countedLearn = readStringList(planExt, "countedLearnWordCodes");
        List<String> countedReview = readStringList(planExt, "countedReviewWordCodes");

        int learnDone = planExt.getIntValue("learnDone");
        int learnTodo = planExt.containsKey("learnTodo") ? planExt.getIntValue("learnTodo") : nvl(current.getLearnTodo());
        int reviewDone = planExt.getIntValue("reviewDone");
        int reviewTodo = planExt.containsKey("reviewTodo") ? planExt.getIntValue("reviewTodo") : nvl(current.getReviewTodo());

        // 仅「认识」才计入本轮完成；不认识的词会在本轮队列中再次出现
        boolean remembered = Boolean.TRUE.equals(request.getRememberFlag());
        if (remembered) {
            if ("review".equals(mode)) {
                if (!countedReview.contains(wordCode) && reviewTodo > 0) {
                    countedReview.add(wordCode);
                    reviewDone += 1;
                    reviewTodo = Math.max(0, reviewTodo - 1);
                }
            } else if (!countedLearn.contains(wordCode) && learnTodo > 0) {
                countedLearn.add(wordCode);
                learnDone += 1;
                learnTodo = Math.max(0, learnTodo - 1);
            }
        }
        planExt.put("countedLearnWordCodes", toJsonArray(countedLearn));
        planExt.put("countedReviewWordCodes", toJsonArray(countedReview));
        planExt.put("learnDone", learnDone);
        planExt.put("learnTodo", learnTodo);
        planExt.put("reviewDone", reviewDone);
        planExt.put("reviewTodo", reviewTodo);
        planExt.put("studyDate", today);
        appendStudyDate(planExt, today);
        studyPlanService.saveDailyProgress(plan.getId(), planExt.toJSONString());
        syncCurrentFromPlan(current, request.getUserId(), bookCode, planExt);

        UserWordStudyAnswerResponse response = new UserWordStudyAnswerResponse();
        response.setLearnDone(learnDone);
        response.setLearnTodo(learnTodo);
        response.setReviewDone(reviewDone);
        response.setReviewTodo(reviewTodo);
        return response;
    }

    @Override
    public UserWordStudyStatsResponse studyStats(BigDecimal userId) {
        UserWordStudyStatsResponse resp = new UserWordStudyStatsResponse();
        if (userId == null) {
            return resp;
        }
        TreeSet<LocalDate> days = new TreeSet<>();

        // 计划里维护的 studyDates（答题时写入）
        UserWordStudyPlanRequest planQuery = new UserWordStudyPlanRequest();
        planQuery.setUserId(userId);
        List<UserWordStudyPlanResponse> plans = studyPlanService.list(planQuery);
        if (plans != null) {
            for (UserWordStudyPlanResponse plan : plans) {
                if (plan == null) {
                    continue;
                }
                collectStudyDatesFromExt(parseExt(plan.getExtInfo()), days);
            }
        }

        // 历史记录回填：create/modify/lastLearnTime 的日期
        LambdaQueryWrapper<UserWordLearnRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(
                UserWordLearnRecord::getCreateTime,
                UserWordLearnRecord::getModifyTime,
                UserWordLearnRecord::getExtInfo
            )
            .eq(UserWordLearnRecord::getUserId, userId);
        List<UserWordLearnRecord> rows = mapper.selectList(wrapper);
        if (rows != null) {
            for (UserWordLearnRecord row : rows) {
                if (row == null) {
                    continue;
                }
                addDate(days, row.getCreateTime());
                addDate(days, row.getModifyTime());
                JSONObject ext = parseExt(row.getExtInfo());
                addDateFromText(days, ext.getString("lastLearnTime"));
            }
        }

        resp.setTotalStudyDays(days.size());
        // 连续：从昨日往前，今日不计入；昨日未学则为 0
        int streak = 0;
        LocalDate cursor = LocalDate.now().minusDays(1);
        while (days.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        resp.setConsecutiveStudyDays(streak);
        return resp;
    }

    private static void appendStudyDate(JSONObject planExt, String today) {
        if (planExt == null || !StringUtils.hasText(today)) {
            return;
        }
        JSONArray arr = planExt.getJSONArray("studyDates");
        if (arr == null) {
            arr = new JSONArray();
        }
        for (int i = 0; i < arr.size(); i++) {
            if (today.equals(arr.getString(i))) {
                planExt.put("studyDates", arr);
                return;
            }
        }
        arr.add(today);
        planExt.put("studyDates", arr);
    }

    private static void collectStudyDatesFromExt(JSONObject ext, Set<LocalDate> days) {
        if (ext == null || days == null) {
            return;
        }
        addDateFromText(days, ext.getString("studyDate"));
        JSONArray arr = ext.getJSONArray("studyDates");
        if (arr == null) {
            return;
        }
        for (int i = 0; i < arr.size(); i++) {
            addDateFromText(days, arr.getString(i));
        }
    }

    private static void addDate(Set<LocalDate> days, LocalDateTime dateTime) {
        if (days != null && dateTime != null) {
            days.add(dateTime.toLocalDate());
        }
    }

    private static void addDateFromText(Set<LocalDate> days, String text) {
        if (days == null || !StringUtils.hasText(text)) {
            return;
        }
        String value = text.trim();
        try {
            if (value.length() >= 10) {
                days.add(LocalDate.parse(value.substring(0, 10)));
            }
        } catch (DateTimeParseException ignored) {
            // 忽略非法日期
        }
    }

    private UserWordCurrentBookResponse requireCurrentBook(java.math.BigDecimal userId, String bookCode) {
        UserWordCurrentBookRequest query = new UserWordCurrentBookRequest();
        query.setUserId(userId);
        List<UserWordCurrentBookResponse> list = currentBookService.list(query);
        UserWordCurrentBookResponse current = list.isEmpty() ? null : list.get(0);
        if (current == null || !StringUtils.hasText(current.getBookCode())) {
            throw new IllegalArgumentException("请先选择单词本并确认学习计划");
        }
        if (StringUtils.hasText(bookCode) && !bookCode.trim().equals(current.getBookCode())) {
            throw new IllegalArgumentException("当前单词本与请求不一致，请先切换词书");
        }
        return current;
    }

    private List<UserWordStudyCardResponse> toCards(
        List<AdminWordEntry> entries,
        String mode,
        int voiceType,
        Map<String, WordPictureBundle> picturesByWord
    ) {
        List<UserWordStudyCardResponse> cards = new ArrayList<>();
        if (entries == null) {
            return cards;
        }
        for (AdminWordEntry entry : entries) {
            cards.add(toCard(entry, mode, voiceType, picturesByWord));
        }
        return cards;
    }

    private UserWordStudyCardResponse toCard(
        AdminWordEntry entry,
        String mode,
        int voiceType,
        Map<String, WordPictureBundle> picturesByWord
    ) {
        UserWordStudyCardResponse card = new UserWordStudyCardResponse();
        card.setMode(mode);
        card.setWordCode(entry.getWordCode());
        card.setWord(entry.getWord());
        JSONObject ext = parseExt(entry.getExtInfo());
        if (voiceType == 1) {
            card.setPhonetic(firstNonBlank(ext.getString("ukphone"), ext.getString("usphone"), ext.getString("phone")));
        } else {
            card.setPhonetic(firstNonBlank(ext.getString("usphone"), ext.getString("ukphone"), ext.getString("phone")));
        }
        card.setMeaning(buildMeaning(ext));
        WordPictureBundle bundle = picturesByWord == null || entry.getWord() == null
            ? null
            : picturesByWord.get(entry.getWord());
        List<String> pictures = bundle == null ? List.of() : bundle.urls;
        card.setPictures(new ArrayList<>(pictures));
        card.setPictureFileIds(bundle == null ? new ArrayList<>() : new ArrayList<>(bundle.fileIds));
        if (bundle != null && bundle.meaningAudioFileId != null) {
            card.setMeaningAudioFileId(bundle.meaningAudioFileId);
        }
        if (!pictures.isEmpty()) {
            card.setPicture(pictures.get(0));
        }
        // 词书无释义时，回退 base_info.meanings（有道同步）
        if (!StringUtils.hasText(card.getMeaning()) && bundle != null && StringUtils.hasText(bundle.meaningText)) {
            card.setMeaning(bundle.meaningText);
        }
        if (!StringUtils.hasText(card.getPhonetic()) && bundle != null) {
            if (voiceType == 1) {
                card.setPhonetic(firstNonBlank(bundle.ukphone, bundle.usphone));
            } else {
                card.setPhonetic(firstNonBlank(bundle.usphone, bundle.ukphone));
            }
        }
        JSONObject remMethod = ext.getJSONObject("remMethod");
        if (remMethod != null) {
            String mnemonic = remMethod.getString("val");
            if (StringUtils.hasText(mnemonic)) {
                card.setMnemonic(mnemonic.trim());
            }
        }
        List<UserWordStudyExampleResponse> examples = buildExamples(ext);
        if (examples.isEmpty() && bundle != null && bundle.examples != null) {
            examples = new ArrayList<>(bundle.examples);
        }
        card.setExamples(examples);
        if (!examples.isEmpty()) {
            card.setExampleEn(examples.get(0).getEn());
            card.setExampleCn(examples.get(0).getCn());
        }
        if (bundle != null) {
            card.setPhrases(bundle.phrases == null ? new ArrayList<>() : new ArrayList<>(bundle.phrases));
            card.setSynonyms(bundle.synonyms == null ? new ArrayList<>() : new ArrayList<>(bundle.synonyms));
            card.setRelatedWords(bundle.relatedWords == null ? new ArrayList<>() : new ArrayList<>(bundle.relatedWords));
            if (StringUtils.hasText(bundle.etym)) {
                card.setEtym(bundle.etym);
            }
        }
        return card;
    }

    private String resolveBookLanguageCode(String bookCode) {
        if (!StringUtils.hasText(bookCode)) {
            return "EN";
        }
        AdminWordBookResponse book = bookService.getByCode(bookCode.trim());
        if (book != null && StringUtils.hasText(book.getLanguageCode())) {
            return book.getLanguageCode().trim();
        }
        return "EN";
    }

    @SafeVarargs
    private final Map<String, WordPictureBundle> loadBasePicturesByWord(
        String languageCode,
        List<AdminWordEntry>... entryLists
    ) {
        Map<String, WordPictureBundle> result = new HashMap<>();
        Set<String> words = new LinkedHashSet<>();
        if (entryLists != null) {
            for (List<AdminWordEntry> list : entryLists) {
                if (list == null) {
                    continue;
                }
                for (AdminWordEntry entry : list) {
                    if (entry != null && StringUtils.hasText(entry.getWord())) {
                        words.add(entry.getWord());
                    }
                }
            }
        }
        if (words.isEmpty()) {
            return result;
        }
        String lang = StringUtils.hasText(languageCode) ? languageCode.trim() : "EN";
        List<AdminWordBaseInfo> rows = baseInfoMapper.selectList(new LambdaQueryWrapper<AdminWordBaseInfo>()
            .eq(AdminWordBaseInfo::getLanguageCode, lang)
            .in(AdminWordBaseInfo::getWord, words)
            .select(AdminWordBaseInfo::getWord, AdminWordBaseInfo::getExtInfo));
        if (rows == null) {
            return result;
        }
        for (AdminWordBaseInfo row : rows) {
            if (row == null || !StringUtils.hasText(row.getWord())) {
                continue;
            }
            result.put(row.getWord(), extractPictures(row.getExtInfo()));
        }
        return result;
    }

    /** base_info 富化：配图 + 有道例句/词组/近义/派生等 */
    private static final class WordPictureBundle {
        private final List<String> urls = new ArrayList<>();
        private final List<Long> fileIds = new ArrayList<>();
        private Long meaningAudioFileId;
        private String meaningText;
        private String ukphone;
        private String usphone;
        private List<UserWordStudyExampleResponse> examples = new ArrayList<>();
        private List<UserWordStudyPhraseResponse> phrases = new ArrayList<>();
        private List<UserWordStudySynonymGroupResponse> synonyms = new ArrayList<>();
        private List<UserWordStudyRelatedGroupResponse> relatedWords = new ArrayList<>();
        private String etym;
    }

    private WordPictureBundle extractPictures(String extInfo) {
        WordPictureBundle bundle = new WordPictureBundle();
        JSONObject ext = parseExt(extInfo);
        JSONObject meaningAudio = ext.getJSONObject("meaningAudio");
        if (meaningAudio != null) {
            Long audioFileId = meaningAudio.getLong("fileId");
            if (audioFileId != null) {
                bundle.meaningAudioFileId = audioFileId;
            }
        }
        bundle.ukphone = ext.getString("ukphone");
        bundle.usphone = ext.getString("usphone");
        bundle.meaningText = buildMeaningFromBaseMeanings(ext);
        bundle.examples = buildExamples(ext);
        bundle.phrases = buildPhrases(ext);
        bundle.synonyms = buildSynonyms(ext);
        bundle.relatedWords = buildRelatedWords(ext);
        if (StringUtils.hasText(ext.getString("etym"))) {
            bundle.etym = ext.getString("etym").trim();
        }
        JSONArray arr = ext.getJSONArray("pictures");
        if (arr == null || arr.isEmpty()) {
            return bundle;
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (int i = 0; i < arr.size(); i++) {
            Object item = arr.get(i);
            String url = null;
            Long fileId = null;
            if (item instanceof String) {
                url = (String) item;
            } else if (item instanceof JSONObject) {
                JSONObject obj = (JSONObject) item;
                url = firstNonBlank(obj.getString("url"), obj.getString("image"));
                fileId = obj.getLong("fileId");
            }
            url = YoudaoDictClient.normalizePictureUrl(url);
            if (StringUtils.hasText(url) && unique.add(url)) {
                bundle.urls.add(url);
            }
            if (fileId != null && !bundle.fileIds.contains(fileId)) {
                bundle.fileIds.add(fileId);
            }
        }
        return bundle;
    }

    private String buildMeaning(JSONObject ext) {
        JSONArray trans = ext.getJSONArray("trans");
        if (trans == null || trans.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < trans.size(); i++) {
            JSONObject item = trans.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String pos = item.getString("pos");
            String cn = firstNonBlank(item.getString("tranCn"), item.getString("tranOther"));
            if (!StringUtils.hasText(cn)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("；");
            }
            if (StringUtils.hasText(pos)) {
                sb.append(pos).append(". ");
            }
            sb.append(cn.trim());
        }
        return sb.toString();
    }

    /** base_info.meanings[{pos,trans}] */
    private String buildMeaningFromBaseMeanings(JSONObject ext) {
        JSONArray meanings = ext.getJSONArray("meanings");
        if (meanings == null || meanings.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < meanings.size(); i++) {
            JSONObject item = meanings.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String pos = item.getString("pos");
            String cn = item.getString("trans");
            if (!StringUtils.hasText(cn)) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("；");
            }
            if (StringUtils.hasText(pos)) {
                sb.append(pos.trim());
                if (!pos.trim().endsWith(".")) {
                    sb.append('.');
                }
                sb.append(' ');
            }
            sb.append(cn.trim());
        }
        return sb.toString();
    }

    private List<UserWordStudyPhraseResponse> buildPhrases(JSONObject ext) {
        List<UserWordStudyPhraseResponse> list = new ArrayList<>();
        JSONArray arr = ext.getJSONArray("phrases");
        if (arr == null || arr.isEmpty()) {
            return list;
        }
        int limit = Math.min(arr.size(), 6);
        for (int i = 0; i < limit; i++) {
            JSONObject item = arr.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String en = item.getString("en");
            if (!StringUtils.hasText(en)) {
                continue;
            }
            UserWordStudyPhraseResponse row = new UserWordStudyPhraseResponse();
            row.setEn(en.trim());
            String cn = item.getString("cn");
            if (StringUtils.hasText(cn)) {
                row.setCn(cn.trim());
            }
            list.add(row);
        }
        return list;
    }

    private List<UserWordStudySynonymGroupResponse> buildSynonyms(JSONObject ext) {
        List<UserWordStudySynonymGroupResponse> list = new ArrayList<>();
        JSONArray arr = ext.getJSONArray("synonyms");
        if (arr == null || arr.isEmpty()) {
            return list;
        }
        int limit = Math.min(arr.size(), 6);
        for (int i = 0; i < limit; i++) {
            JSONObject item = arr.getJSONObject(i);
            if (item == null) {
                continue;
            }
            JSONArray words = item.getJSONArray("words");
            if (words == null || words.isEmpty()) {
                continue;
            }
            UserWordStudySynonymGroupResponse row = new UserWordStudySynonymGroupResponse();
            row.setPos(item.getString("pos"));
            row.setTran(item.getString("tran"));
            List<String> wordList = new ArrayList<>();
            for (int j = 0; j < words.size() && wordList.size() < 6; j++) {
                String w = words.getString(j);
                if (StringUtils.hasText(w)) {
                    wordList.add(w.trim());
                }
            }
            if (wordList.isEmpty()) {
                continue;
            }
            row.setWords(wordList);
            list.add(row);
        }
        return list;
    }

    private List<UserWordStudyRelatedGroupResponse> buildRelatedWords(JSONObject ext) {
        List<UserWordStudyRelatedGroupResponse> list = new ArrayList<>();
        JSONArray arr = ext.getJSONArray("relatedWords");
        if (arr == null || arr.isEmpty()) {
            return list;
        }
        int limit = Math.min(arr.size(), 6);
        for (int i = 0; i < limit; i++) {
            JSONObject item = arr.getJSONObject(i);
            if (item == null) {
                continue;
            }
            JSONArray words = item.getJSONArray("words");
            if (words == null || words.isEmpty()) {
                continue;
            }
            UserWordStudyRelatedGroupResponse row = new UserWordStudyRelatedGroupResponse();
            row.setPos(item.getString("pos"));
            List<UserWordStudyRelatedItemResponse> wordList = new ArrayList<>();
            for (int j = 0; j < words.size() && wordList.size() < 8; j++) {
                JSONObject w = words.getJSONObject(j);
                if (w == null) {
                    continue;
                }
                String word = w.getString("word");
                if (!StringUtils.hasText(word)) {
                    continue;
                }
                UserWordStudyRelatedItemResponse ri = new UserWordStudyRelatedItemResponse();
                ri.setWord(word.trim());
                String tran = w.getString("tran");
                if (StringUtils.hasText(tran)) {
                    ri.setTran(tran.trim());
                }
                wordList.add(ri);
            }
            if (wordList.isEmpty()) {
                continue;
            }
            row.setWords(wordList);
            list.add(row);
        }
        return list;
    }

    private List<UserWordStudyExampleResponse> buildExamples(JSONObject ext) {
        List<UserWordStudyExampleResponse> list = new ArrayList<>();
        JSONObject sentence = ext.getJSONObject("sentence");
        if (sentence == null) {
            return list;
        }
        JSONArray sentences = sentence.getJSONArray("sentences");
        if (sentences == null || sentences.isEmpty()) {
            return list;
        }
        int limit = Math.min(sentences.size(), 3);
        for (int i = 0; i < limit; i++) {
            JSONObject item = sentences.getJSONObject(i);
            if (item == null) {
                continue;
            }
            String en = firstNonBlank(item.getString("sContent"), stripHtml(item.getString("sContent_eng")));
            String cn = item.getString("sCn");
            if (!StringUtils.hasText(en) && !StringUtils.hasText(cn)) {
                continue;
            }
            UserWordStudyExampleResponse example = new UserWordStudyExampleResponse();
            example.setEn(StringUtils.hasText(en) ? en.trim() : "");
            example.setCn(StringUtils.hasText(cn) ? cn.trim() : "");
            list.add(example);
        }
        return list;
    }

    private String stripHtml(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return value.replaceAll("<[^>]+>", "").trim();
    }

    private List<UserWordStudyCardResponse> concatReviewThenLearn(
        List<UserWordStudyCardResponse> reviewCards,
        List<UserWordStudyCardResponse> learnCards
    ) {
        List<UserWordStudyCardResponse> mixed = new ArrayList<>(
            (reviewCards == null ? 0 : reviewCards.size()) + (learnCards == null ? 0 : learnCards.size())
        );
        if (reviewCards != null) {
            mixed.addAll(reviewCards);
        }
        if (learnCards != null) {
            mixed.addAll(learnCards);
        }
        return mixed;
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

    private int readVoiceType(String extInfo) {
        JSONObject ext = parseExt(extInfo);
        Integer voiceType = ext.getInteger("voiceType");
        if (voiceType != null && (voiceType == 1 || voiceType == 2)) {
            return voiceType;
        }
        return 2;
    }

    /** 复习回溯：最近 N 个有新学的日历日，默认 3 */
    private static int readReviewLookbackDays(JSONObject planExt) {
        if (planExt == null) {
            return 3;
        }
        Integer n = planExt.getInteger("reviewLookbackDays");
        if (n == null || n < 1 || n > 7) {
            return 3;
        }
        return n;
    }

    /**
     * 今日复习目标 = min(计划上限, 候选数)；断签≥2 天且开启软着陆时再压到约「每日新学」量。
     */
    private static int resolveReviewTarget(
        int reviewLimit,
        int candidateCount,
        int dailyNew,
        JSONObject planExt,
        int learnedToday,
        int reviewedToday
    ) {
        int cap = Math.max(0, reviewLimit);
        int raw = Math.min(cap, Math.max(0, candidateCount));
        boolean softLand = planExt == null || !Boolean.FALSE.equals(planExt.getBoolean("softLandEnabled"));
        if (!softLand) {
            return raw;
        }
        if (learnedToday > 0 || reviewedToday > 0) {
            return raw;
        }
        int gapDays = resolveStudyGapDays(planExt);
        if (gapDays < 2) {
            return raw;
        }
        int softCap = Math.max(10, dailyNew > 0 ? dailyNew : 10);
        return Math.min(raw, softCap);
    }

    private static int resolveStudyGapDays(JSONObject planExt) {
        if (planExt == null) {
            return 0;
        }
        LocalDate last = null;
        JSONArray dates = planExt.getJSONArray("studyDates");
        if (dates != null) {
            for (int i = 0; i < dates.size(); i++) {
                String s = dates.getString(i);
                if (!StringUtils.hasText(s)) {
                    continue;
                }
                try {
                    LocalDate d = LocalDate.parse(s.trim());
                    if (last == null || d.isAfter(last)) {
                        last = d;
                    }
                } catch (DateTimeParseException ignored) {
                    // skip
                }
            }
        }
        if (last == null) {
            String studyDate = planExt.getString("studyDate");
            if (StringUtils.hasText(studyDate)) {
                try {
                    last = LocalDate.parse(studyDate.trim());
                } catch (DateTimeParseException ignored) {
                    return 0;
                }
            }
        }
        if (last == null) {
            return 0;
        }
        long gap = ChronoUnit.DAYS.between(last, LocalDate.now());
        return gap < 0 ? 0 : (int) gap;
    }

    private List<String> readStringList(JSONObject ext, String key) {
        List<String> result = new ArrayList<>();
        if (ext == null || !StringUtils.hasText(key)) {
            return result;
        }
        JSONArray arr = ext.getJSONArray(key);
        if (arr == null || arr.isEmpty()) {
            return result;
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (int i = 0; i < arr.size(); i++) {
            String value = arr.getString(i);
            if (StringUtils.hasText(value)) {
                unique.add(value.trim());
            }
        }
        result.addAll(unique);
        return result;
    }

    private List<String> filterRemaining(List<String> allCodes, List<String> countedCodes) {
        List<String> remaining = new ArrayList<>();
        if (allCodes == null || allCodes.isEmpty()) {
            return remaining;
        }
        Set<String> counted = new HashSet<>();
        if (countedCodes != null) {
            counted.addAll(countedCodes);
        }
        for (String code : allCodes) {
            if (StringUtils.hasText(code) && !counted.contains(code)) {
                remaining.add(code);
            }
        }
        return remaining;
    }

    private JSONArray toJsonArray(List<String> values) {
        JSONArray arr = new JSONArray();
        if (values != null) {
            arr.addAll(values);
        }
        return arr;
    }

    private List<AdminWordEntry> loadEntriesByCodes(String bookCode, List<String> wordCodes) {
        if (!StringUtils.hasText(bookCode) || wordCodes == null || wordCodes.isEmpty()) {
            return new ArrayList<>();
        }
        List<AdminWordEntry> rows = entryMapper.selectByWordCodes(bookCode, wordCodes);
        Map<String, AdminWordEntry> byCode = new HashMap<>();
        if (rows != null) {
            for (AdminWordEntry row : rows) {
                if (row != null && StringUtils.hasText(row.getWordCode())) {
                    byCode.put(row.getWordCode(), row);
                }
            }
        }
        List<AdminWordEntry> ordered = new ArrayList<>();
        for (String code : wordCodes) {
            AdminWordEntry entry = byCode.get(code);
            if (entry != null) {
                ordered.add(entry);
            }
        }
        return ordered;
    }

    private void saveCurrentExtOnly(
        UserWordCurrentBookResponse current,
        java.math.BigDecimal userId,
        String bookCode,
        JSONObject ext
    ) {
        saveCurrentProgress(
            current,
            userId,
            bookCode,
            nvl(current.getLearnDone()),
            nvl(current.getLearnTodo()),
            nvl(current.getReviewDone()),
            nvl(current.getReviewTodo()),
            ext
        );
    }

    private void syncCurrentFromPlan(
        UserWordCurrentBookResponse current,
        java.math.BigDecimal userId,
        String bookCode,
        JSONObject planExt
    ) {
        int learnDone = planExt.getIntValue("learnDone");
        int learnTodo = planExt.containsKey("learnTodo") ? planExt.getIntValue("learnTodo") : nvl(current.getLearnTodo());
        int reviewDone = planExt.getIntValue("reviewDone");
        int reviewTodo = planExt.containsKey("reviewTodo") ? planExt.getIntValue("reviewTodo") : nvl(current.getReviewTodo());
        saveCurrentProgress(current, userId, bookCode, learnDone, learnTodo, reviewDone, reviewTodo, null);
    }

    private void saveCurrentProgress(
        UserWordCurrentBookResponse current,
        java.math.BigDecimal userId,
        String bookCode,
        int learnDone,
        int learnTodo,
        int reviewDone,
        int reviewTodo,
        JSONObject ext
    ) {
        UserWordCurrentBookRequest currentUpdate = new UserWordCurrentBookRequest();
        currentUpdate.setId(current.getId());
        currentUpdate.setUserId(userId);
        currentUpdate.setBookCode(bookCode);
        currentUpdate.setLearnDone(learnDone);
        currentUpdate.setLearnTodo(learnTodo);
        currentUpdate.setReviewDone(reviewDone);
        currentUpdate.setReviewTodo(reviewTodo);
        // 今日词表改存 plan.ext_info；current.ext_info 清空，避免串书
        currentUpdate.setExtInfo(ext == null ? null : ext.toJSONString());
        currentBookService.update(currentUpdate);
        current.setLearnDone(learnDone);
        current.setLearnTodo(learnTodo);
        current.setReviewDone(reviewDone);
        current.setReviewTodo(reviewTodo);
        current.setExtInfo(currentUpdate.getExtInfo());
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value.trim();
            }
        }
        return "";
    }
}
