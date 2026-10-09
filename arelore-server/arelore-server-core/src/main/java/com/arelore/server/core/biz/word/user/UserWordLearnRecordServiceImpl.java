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
import com.arelore.server.core.biz.word.user.dto.UserWordStudyPlanRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordStudyPlanResponse;
import com.arelore.server.core.biz.word.user.dto.UserWordStudySessionRequest;
import com.arelore.server.core.biz.word.user.dto.UserWordStudySessionResponse;
import com.arelore.server.core.biz.word.user.entity.UserWordLearnRecord;
import com.arelore.server.core.biz.word.user.mapper.UserWordLearnRecordMapper;
import com.arelore.server.core.service.BaseServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
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
        boolean newDay = !today.equals(planExt.getString("studyDate"));
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
        }

        List<String> todayLearnCodes = readStringList(planExt, "todayLearnWordCodes");
        List<String> todayReviewCodes = readStringList(planExt, "todayReviewWordCodes");
        boolean codesChanged = newDay;

        if (todayLearnCodes.size() < learnLimit) {
            Set<String> exists = new HashSet<>(todayLearnCodes);
            int need = learnLimit - todayLearnCodes.size();
            List<AdminWordEntry> more = entryMapper.selectUnlearned(request.getUserId(), bookCode, need);
            for (AdminWordEntry entry : more) {
                if (entry == null || !StringUtils.hasText(entry.getWordCode())) {
                    continue;
                }
                if (exists.add(entry.getWordCode())) {
                    todayLearnCodes.add(entry.getWordCode());
                    codesChanged = true;
                    if (todayLearnCodes.size() >= learnLimit) {
                        break;
                    }
                }
            }
        }

        List<String> countedLearn = readStringList(planExt, "countedLearnWordCodes");
        List<String> countedReview = readStringList(planExt, "countedReviewWordCodes");
        int learnDone = planExt.containsKey("learnDone") ? planExt.getIntValue("learnDone") : countedLearn.size();
        int reviewDone = planExt.containsKey("reviewDone") ? planExt.getIntValue("reviewDone") : countedReview.size();

        // 复习：上一学习日（不含今天）新学过的词；未开始复习时可重建（兼容旧配额逻辑）
        if (todayReviewCodes.isEmpty() || reviewDone == 0) {
            List<String> rebuilt = new ArrayList<>();
            List<AdminWordEntry> candidates = entryMapper.selectForReview(
                request.getUserId(), bookCode, reviewLimit
            );
            Set<String> blocked = new HashSet<>(todayLearnCodes);
            for (AdminWordEntry entry : candidates) {
                if (entry == null || !StringUtils.hasText(entry.getWordCode())) {
                    continue;
                }
                if (blocked.add(entry.getWordCode())) {
                    rebuilt.add(entry.getWordCode());
                    if (rebuilt.size() >= reviewLimit) {
                        break;
                    }
                }
            }
            if (!rebuilt.equals(todayReviewCodes)) {
                todayReviewCodes = rebuilt;
                codesChanged = true;
            }
        }

        int learnTodo = Math.max(0, todayLearnCodes.size() - learnDone);
        int reviewTodo = Math.max(0, todayReviewCodes.size() - reviewDone);

        if (codesChanged || newDay) {
            planExt.put("todayLearnWordCodes", toJsonArray(todayLearnCodes));
            planExt.put("todayReviewWordCodes", toJsonArray(todayReviewCodes));
            planExt.put("learnDone", learnDone);
            planExt.put("learnTodo", learnTodo);
            planExt.put("reviewDone", reviewDone);
            planExt.put("reviewTodo", reviewTodo);
            planExt.put("studyDate", today);
            studyPlanService.saveDailyProgress(plan.getId(), planExt.toJSONString());
            syncCurrentFromPlan(current, request.getUserId(), bookCode, planExt);
        } else {
            planExt.put("learnTodo", learnTodo);
            planExt.put("reviewTodo", reviewTodo);
            syncCurrentFromPlan(current, request.getUserId(), bookCode, planExt);
        }

        // 未完成今日配额：只返回未作答词，便于断点续学；已完成则可重复练今日全部词
        boolean dayFinished = learnTodo <= 0 && reviewTodo <= 0
            && (!todayLearnCodes.isEmpty() || !todayReviewCodes.isEmpty());
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
        Map<String, List<String>> picturesByWord = loadBasePicturesByWord(languageCode, learnEntries, reviewEntries);
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
        response.setLearnTotal(todayLearnCodes.size());
        response.setReviewTotal(todayReviewCodes.size());
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
        boolean sameDay = today.equals(planExt.getString("studyDate"));
        int prevDayCount = Math.min(reviewLimit, entryMapper.countPreviousLearnDay(current.getUserId(), bookCode));

        if (!sameDay) {
            // 新的一天尚未开练：新学=计划配额与剩余词取小；复习=上一学习日新学数
            int learnTodo = Math.min(dailyNew, remainWords);
            current.setLearnDone(0);
            current.setLearnTodo(learnTodo);
            current.setReviewDone(0);
            current.setReviewTodo(prevDayCount);
            return current;
        }

        List<String> todayReviewCodes = readStringList(planExt, "todayReviewWordCodes");
        int reviewDone = planExt.containsKey("reviewDone") ? planExt.getIntValue("reviewDone") : nvl(current.getReviewDone());
        int reviewTodo;
        if (!todayReviewCodes.isEmpty()) {
            reviewTodo = Math.max(0, todayReviewCodes.size() - reviewDone);
        } else {
            // 兼容旧数据：曾把配额写入 reviewTodo，无实际复习词时归零
            reviewTodo = prevDayCount > 0 ? Math.max(0, prevDayCount - reviewDone) : 0;
        }
        current.setReviewDone(reviewDone);
        current.setReviewTodo(reviewTodo);

        List<String> todayLearnCodes = readStringList(planExt, "todayLearnWordCodes");
        int learnDone = planExt.containsKey("learnDone") ? planExt.getIntValue("learnDone") : nvl(current.getLearnDone());
        int learnTodo;
        if (!todayLearnCodes.isEmpty()) {
            learnTodo = Math.max(0, todayLearnCodes.size() - learnDone);
        } else {
            learnTodo = planExt.containsKey("learnTodo")
                ? planExt.getIntValue("learnTodo")
                : Math.min(dailyNew, remainWords);
        }
        current.setLearnDone(learnDone);
        current.setLearnTodo(learnTodo);
        return current;
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
        studyPlanService.saveDailyProgress(plan.getId(), planExt.toJSONString());
        syncCurrentFromPlan(current, request.getUserId(), bookCode, planExt);

        UserWordStudyAnswerResponse response = new UserWordStudyAnswerResponse();
        response.setLearnDone(learnDone);
        response.setLearnTodo(learnTodo);
        response.setReviewDone(reviewDone);
        response.setReviewTodo(reviewTodo);
        return response;
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
        Map<String, List<String>> picturesByWord
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
        Map<String, List<String>> picturesByWord
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
        List<String> pictures = picturesByWord == null || entry.getWord() == null
            ? List.of()
            : picturesByWord.getOrDefault(entry.getWord(), List.of());
        card.setPictures(new ArrayList<>(pictures));
        if (!pictures.isEmpty()) {
            card.setPicture(pictures.get(0));
        }
        JSONObject remMethod = ext.getJSONObject("remMethod");
        if (remMethod != null) {
            String mnemonic = remMethod.getString("val");
            if (StringUtils.hasText(mnemonic)) {
                card.setMnemonic(mnemonic.trim());
            }
        }
        List<UserWordStudyExampleResponse> examples = buildExamples(ext);
        card.setExamples(examples);
        if (!examples.isEmpty()) {
            card.setExampleEn(examples.get(0).getEn());
            card.setExampleCn(examples.get(0).getCn());
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
    private final Map<String, List<String>> loadBasePicturesByWord(
        String languageCode,
        List<AdminWordEntry>... entryLists
    ) {
        Map<String, List<String>> result = new HashMap<>();
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

    private List<String> extractPictures(String extInfo) {
        List<String> pictures = new ArrayList<>();
        JSONObject ext = parseExt(extInfo);
        JSONArray arr = ext.getJSONArray("pictures");
        if (arr == null || arr.isEmpty()) {
            return pictures;
        }
        LinkedHashSet<String> unique = new LinkedHashSet<>();
        for (int i = 0; i < arr.size(); i++) {
            Object item = arr.get(i);
            String url = null;
            if (item instanceof String) {
                url = (String) item;
            } else if (item instanceof JSONObject) {
                JSONObject obj = (JSONObject) item;
                url = firstNonBlank(obj.getString("url"), obj.getString("image"));
            }
            url = YoudaoDictClient.normalizePictureUrl(url);
            if (StringUtils.hasText(url)) {
                unique.add(url);
            }
        }
        pictures.addAll(unique);
        return pictures;
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
