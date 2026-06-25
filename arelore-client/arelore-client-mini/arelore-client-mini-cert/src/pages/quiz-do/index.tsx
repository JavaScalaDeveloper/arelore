import React, { useEffect, useState } from 'react';
import Taro, { useDidShow } from '@tarojs/taro';
import { Image, ScrollView, Text, View } from '@tarojs/components';
import { post } from '../../utils/request';
import { isSubjectiveQuestion, parseQuestionExtraInfo } from '../../utils/quizExamResult';
import './index.scss';

const IMG_HASH_REGEX = /(?:【img:([0-9a-fA-F]{64})】|\(img:([0-9a-fA-F]{64})\))/g;

const QuizDoPage = () => {
  const [typeCode, setTypeCode] = useState('');
  const [typeName, setTypeName] = useState('');
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [answers, setAnswers] = useState({});
  const [imageSrcMap, setImageSrcMap] = useState({});
  const [imageLoadingMap, setImageLoadingMap] = useState({});
  const [currentIndex, setCurrentIndex] = useState(0);
  const [favoriteMap, setFavoriteMap] = useState({});
  const [showAnswerCard, setShowAnswerCard] = useState(false);
  const [showAnalysis, setShowAnalysis] = useState(false);
  useDidShow(() => {
    const instance = Taro.getCurrentInstance();
    const tc = decodeURIComponent(instance?.router?.params?.typeCode || '');
    const tn = decodeURIComponent(instance?.router?.params?.typeName || '');
    setTypeCode(tc);
    setTypeName(tn);
    if (!tc) {
      setQuestions([]);
      setError('缺少试卷编号');
      return;
    }
    (async () => {
      try {
        setLoading(true);
        setError('');
        const resp = await post('/user/detection/question/all', { typeCode: tc });
        if (resp && resp.code === 200) {
          const list = resp.data || [];
          setQuestions(list);
          clearLocalProgress();
          setImageSrcMap({});
          setImageLoadingMap({});
          await loadFavoriteMap(tc);
          await restoreOrResetProgress(tc, list);
          return;
        }
        setQuestions([]);
        setError(resp?.message || '查询题目失败');
      } catch (e) {
        setQuestions([]);
        setError(e?.errMsg || '查询题目失败');
      } finally {
        setLoading(false);
      }
    })();
  });

  const onSelectOption = (questionCode, optionKey) => {
    setAnswers((prev) => ({ ...prev, [questionCode]: optionKey }));
  };

  const buildAnsweredQuestions = () =>
    questions
      .map((q, idx) => {
        const code = q?.questionCode || `q_${idx}`;
        const selectedOptionKey = answers[code];
        if (!selectedOptionKey) return null;
        return {
          questionId: q?.id,
          questionCode: q?.questionCode,
          selectedOptionKey
        };
      })
      .filter(Boolean);

  const getCurrentUserId = () => {
    const currentUser = Taro.getStorageSync('currentUser') || {};
    return String(currentUser?.userId || currentUser?.id || '');
  };

  const clearLocalProgress = () => {
    setAnswers({});
    setCurrentIndex(0);
    setShowAnswerCard(false);
  };

  const parseExtraInfo = (extraInfo) => parseQuestionExtraInfo(extraInfo);

  const getQuestionDisplayText = (q) => {
    if (!q) return '';
    const ext = parseExtraInfo(q.extraInfo);
    const name = q.questionName || '';
    if (isSubjectiveQuestion(ext)) {
      return stripImageMarkers(ext.fullQuestionName || name);
    }
    if (ext.fullQuestionName && (name.endsWith('…') || name.length >= 250)) {
      return stripImageMarkers(ext.fullQuestionName);
    }
    return stripImageMarkers(name);
  };

  const buildAnswerMapByExtraInfo = (extra) => {
    const list = Array.isArray(extra?.answeredQuestions) ? extra.answeredQuestions : [];
    const answerMap = {};
    list.forEach((item) => {
      const code = item?.questionCode;
      const selected = item?.selectedOptionKey;
      if (code && selected) {
        answerMap[code] = selected;
      }
    });
    return answerMap;
  };

  const getRestoreIndex = (extra, list, answerMap) => {
    const idxFromProgress = Number(extra?.progress?.currentIndex);
    if (Number.isInteger(idxFromProgress) && idxFromProgress >= 0) {
      return Math.min(idxFromProgress, Math.max(list.length - 1, 0));
    }
    const answeredCodes = Object.keys(answerMap);
    if (answeredCodes.length === 0) return 0;
    const lastCode = answeredCodes[answeredCodes.length - 1];
    const idxByCode = list.findIndex((q, i) => (q?.questionCode || `q_${i}`) === lastCode);
    return idxByCode >= 0 ? idxByCode : 0;
  };

  const redoCurrentResult = async (userId, tc) => {
    const resp = await post('/user/detection/result/redo', { userId, typeCode: tc });
    return !!(resp && resp.code === 200);
  };

  const loadFavoriteMap = async (tc) => {
    const userId = getCurrentUserId();
    if (!userId || !tc) {
      setFavoriteMap({});
      return;
    }
    const resp = await post('/user/detection/question/favorite/list', {
      userId,
      questionTypeCode: tc
    });
    if (!resp || resp.code !== 200 || !Array.isArray(resp.data)) {
      setFavoriteMap({});
      return;
    }
    const nextMap = {};
    resp.data.forEach((code) => {
      if (code) nextMap[code] = true;
    });
    setFavoriteMap(nextMap);
  };

  const toggleFavorite = async () => {
    if (!currentQuestionCode || !typeCode) return;
    const userId = getCurrentUserId();
    if (!userId) {
      Taro.showToast({ title: '请先登录后收藏', icon: 'none' });
      return;
    }
    const nextFavorite = !currentFavorite;
    const resp = await post('/user/detection/question/favorite/toggle', {
      userId,
      questionTypeCode: typeCode,
      questionCode: currentQuestionCode,
      favorite: nextFavorite,
      extraInfo: JSON.stringify({ from: 'mini-quiz-do' })
    });
    if (!resp || resp.code !== 200) {
      Taro.showToast({ title: resp?.message || '操作失败', icon: 'none' });
      return;
    }
    setFavoriteMap((prev) => ({ ...prev, [currentQuestionCode]: nextFavorite }));
    Taro.showToast({ title: nextFavorite ? '收藏成功' : '已取消收藏', icon: 'none' });
  };

  const restoreOrResetProgress = async (tc, list) => {
    const userId = getCurrentUserId();
    if (!userId || !tc || !Array.isArray(list) || list.length === 0) {
      clearLocalProgress();
      return;
    }
    const currentResp = await post('/user/detection/result/current', { userId, typeCode: tc });
    const current = currentResp?.code === 200 ? currentResp?.data : null;
    if (!current) {
      clearLocalProgress();
      return;
    }
    const hasSubmittedBefore = !!String(current?.userDetectResult || '').trim();
    if (hasSubmittedBefore) {
      await redoCurrentResult(userId, tc);
      clearLocalProgress();
      return;
    }
    const extra = parseExtraInfo(current?.extraInfo);
    const answerMap = buildAnswerMapByExtraInfo(extra);
    if (Object.keys(answerMap).length === 0) {
      clearLocalProgress();
      return;
    }
    const choice = await Taro.showModal({
      title: '检测到历史进度',
      content: '是否继续上次答题进度？',
      confirmText: '继续做题',
      cancelText: '重新做题'
    });
    if (choice.confirm) {
      setAnswers(answerMap);
      setCurrentIndex(getRestoreIndex(extra, list, answerMap));
      return;
    }
    await redoCurrentResult(userId, tc);
    clearLocalProgress();
  };

  const saveProgress = async (submitPaperFlag) => {
    if (!typeCode) return { ok: true };
    const userId = getCurrentUserId();
    if (!userId) {
      Taro.showToast({ title: '请先登录后答题', icon: 'none' });
      return { ok: false };
    }
    const payload = {
      userId,
      userDetectTypeCode: typeCode,
      submitPaper: submitPaperFlag,
      answeredQuestions: buildAnsweredQuestions(),
      extraInfo: JSON.stringify({
        currentIndex,
        total: questions.length,
        answeredCount: Object.keys(answers).length,
        submitPaper: submitPaperFlag
      })
    };
    const resp = await post('/user/detection/result/save', payload);
    if (!resp || resp.code !== 200) {
      Taro.showToast({ title: resp?.message || '保存进度失败', icon: 'none' });
      return { ok: false };
    }
    if (submitPaperFlag) {
      return {
        ok: true,
        detectResult: resp.data?.detectResult ?? '',
        extraInfo: resp.data?.extraInfo ?? ''
      };
    }
    return { ok: true };
  };

  const confirmAndSubmitPaper = async () => {
    const userId = getCurrentUserId();
    if (!userId) {
      Taro.showToast({ title: '请先登录后答题', icon: 'none' });
      return;
    }
    if (!questions.length) return;
    const answeredCount = Object.keys(answers).length;
    const unanswered = questions.length - answeredCount;
    const confirm = await Taro.showModal({
      title: '确认交卷',
      content:
        answeredCount === 0
          ? '当前尚未作答任何题目，交卷后按规则计分（未作答不得分）。确定交卷吗？'
          : `还有 ${unanswered} 题未作答，未作答不得分。确定交卷吗？`
    });
    if (!confirm.confirm) return;
    const r = await saveProgress(true);
    if (!r.ok) return;
    setShowAnswerCard(false);
    let extraParsed = {};
    try {
      extraParsed = typeof r.extraInfo === 'string' ? JSON.parse(r.extraInfo || '{}') : r.extraInfo || {};
    } catch (e) {
      extraParsed = {};
    }
    Taro.setStorageSync('quizLastResult', {
      typeCode,
      typeName,
      detectResult: r.detectResult || '',
      extraInfo: extraParsed,
      savedAt: Date.now()
    });
    Taro.redirectTo({
      url: `/pages/quiz-result/index?typeCode=${encodeURIComponent(typeCode)}&typeName=${encodeURIComponent(typeName || '')}`
    });
  };

  const goPrev = async () => {
    if (currentIndex <= 0) return;
    const r = await saveProgress(false);
    if (!r.ok) return;
    setCurrentIndex((idx) => (idx <= 0 ? 0 : idx - 1));
  };

  const goNext = async () => {
    if (currentIndex >= questions.length - 1) return;
    const r = await saveProgress(false);
    if (!r.ok) return;
    setCurrentIndex((idx) => (idx >= questions.length - 1 ? idx : idx + 1));
  };

  const submitPaper = async () => {
    await confirmAndSubmitPaper();
  };

  const redoPaper = async () => {
    const userId = getCurrentUserId();
    if (!userId || !typeCode) {
      Taro.showToast({ title: '缺少重做参数', icon: 'none' });
      return;
    }
    const confirm = await Taro.showModal({
      title: '确认重做',
      content: '将清空当前考试进度并从第一题重新开始，是否继续？'
    });
    if (!confirm.confirm) return;
    const resp = await post('/user/detection/result/redo', { userId, typeCode });
    if (!resp || resp.code !== 200) {
      Taro.showToast({ title: resp?.message || '重做失败', icon: 'none' });
      return;
    }
    clearLocalProgress();
    Taro.showToast({ title: '已重置当前进度', icon: 'success' });
  };

  const parseOptions = (q) => {
    if (!q?.options) return [];
    try {
      const arr = JSON.parse(q.options);
      return Array.isArray(arr) ? arr : [];
    } catch (e) {
      return [];
    }
  };

  const extractImageHashes = (text) => {
    const hashes = [];
    if (!text) return hashes;
    let m = IMG_HASH_REGEX.exec(text);
    while (m) {
      const hash = (m[1] || m[2] || '').toLowerCase();
      if (hash) hashes.push(hash);
      m = IMG_HASH_REGEX.exec(text);
    }
    IMG_HASH_REGEX.lastIndex = 0;
    return hashes;
  };

  const stripImageMarkers = (text) => {
    if (!text) return '';
    return text
      .replace(/【img:[0-9a-fA-F]{64}】/g, '')
      .replace(/\(img:[0-9a-fA-F]{64}\)/g, '')
      .trim();
  };

  /** 将含 【img:hash】 / (img:hash) 的字符串拆成文本与图片片段，供解析等区域渲染 */
  const splitTextWithImageMarkers = (text) => {
    if (!text) return [{ type: 'text', content: '' }];
    const parts = [];
    let lastIndex = 0;
    IMG_HASH_REGEX.lastIndex = 0;
    let m = IMG_HASH_REGEX.exec(text);
    while (m) {
      if (m.index > lastIndex) {
        parts.push({ type: 'text', content: text.slice(lastIndex, m.index) });
      }
      const hash = (m[1] || m[2] || '').toLowerCase();
      if (hash) parts.push({ type: 'img', hash });
      lastIndex = m.index + m[0].length;
      m = IMG_HASH_REGEX.exec(text);
    }
    IMG_HASH_REGEX.lastIndex = 0;
    if (lastIndex < text.length) {
      parts.push({ type: 'text', content: text.slice(lastIndex) });
    }
    if (parts.length === 0) parts.push({ type: 'text', content: text });
    return parts;
  };

  const loadImagesByHashes = async (hashes) => {
    const missing = hashes.filter((h) => !imageSrcMap[h] && !imageLoadingMap[h]);
    if (missing.length === 0) return;

    const nextLoadingMap = { ...imageLoadingMap };
    missing.forEach((h) => {
      nextLoadingMap[h] = true;
    });
    setImageLoadingMap(nextLoadingMap);

    const loadedMap = {};
    await Promise.all(
      missing.map(async (hash) => {
        try {
          const resp = await post('/user/common-binary-file/get-by-hash', { hashValue: hash });
          if (resp && resp.code === 200 && resp.data?.base64Data) {
            const contentType = resp.data.contentType || 'image/png';
            loadedMap[hash] = `data:${contentType};base64,${resp.data.base64Data}`;
          }
        } catch (e) {
          // 单个图片失败不影响整体题目渲染
        } finally {
          setImageLoadingMap((prev) => ({ ...prev, [hash]: false }));
        }
      })
    );
    if (Object.keys(loadedMap).length > 0) {
      setImageSrcMap((prev) => ({ ...prev, ...loadedMap }));
    }
  };

  const currentQuestion = questions[currentIndex] || null;
  const progress = questions.length === 0 ? 0 : Math.round(((currentIndex + 1) / questions.length) * 100);
  const currentExtra = currentQuestion ? parseExtraInfo(currentQuestion.extraInfo) : {};
  const currentQuestionType = currentExtra?.questionType || currentQuestion?.questionDescription || '单选题';
  const currentIsSubjective = isSubjectiveQuestion(currentExtra);
  const currentDisplayText = getQuestionDisplayText(currentQuestion);
  const currentOptions = currentQuestion ? parseOptions(currentQuestion) : [];

  const currentQuestionCode = currentQuestion?.questionCode || `q_${currentIndex}`;
  const currentFavorite = !!favoriteMap[currentQuestionCode];
  const analysisText = (() => {
    if (!currentQuestion) return '';
    const selected = answers[currentQuestionCode];
    if (!selected) return '你尚未选择答案。';
    let explanation = '';
    try {
      const ext = currentQuestion.extraInfo ? JSON.parse(currentQuestion.extraInfo) : {};
      explanation = ext?.fullExplanation || ext?.explanation || '';
    } catch (e) {
      explanation = '';
    }
    const base = `你当前选择了 ${selected} 选项。`;
    if (explanation) {
      return `${base}\n\n【参考解析】\n${explanation}`;
    }
    return `${base}\n\n该题暂未配置解析。`;
  })();

  useEffect(() => {
    if (!currentQuestion) return;
    const hashes = new Set();
    extractImageHashes(currentDisplayText).forEach((h) => hashes.add(h));
    currentOptions.forEach((op) => {
      extractImageHashes(op?.text).forEach((h) => hashes.add(h));
    });
    let explanation = '';
    try {
      const ext = currentQuestion.extraInfo ? JSON.parse(currentQuestion.extraInfo) : {};
      explanation = ext?.fullExplanation || ext?.explanation || '';
    } catch (e) {
      explanation = '';
    }
    extractImageHashes(explanation).forEach((h) => hashes.add(h));
    loadImagesByHashes(Array.from(hashes));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [currentQuestionCode]);

  return (
    <View className='page'>
      <View className='header'>
        <View className='title'>{typeName || typeCode || '做题'}</View>
        <View className='sub'>已答 {Object.keys(answers).length} / {questions.length}</View>
        <View className='metaRow'>
          <Text className='metaType'>{currentQuestionType}</Text>
          <Text className='metaNo'>
            第 {questions.length === 0 ? 0 : currentIndex + 1} 题 / 共 {questions.length} 题
          </Text>
        </View>
        <View className='progressTrack'>
          <View className='progressFill' style={{ width: `${progress}%` }} />
        </View>
      </View>

      {loading ? <View className='tip'>加载题目中...</View> : null}
      {error ? <View className='tip error'>{error}</View> : null}

      {!loading && !error && currentQuestion ? (
        <View className='card'>
          {currentIsSubjective ? (
            <ScrollView scrollY className='qScroll' enhanced showScrollbar>
              <View className='qTitle'>
                {currentIndex + 1}. {currentExtra?.displayTitle || currentQuestion.questionName || '案例分析'}
              </View>
              <Text className='qBody' selectable userSelect>
                {currentDisplayText || '题目'}
              </Text>
            </ScrollView>
          ) : (
            <View className='qTitle'>
              {currentIndex + 1}. {currentDisplayText || '题目'}
            </View>
          )}
          {extractImageHashes(currentDisplayText).map((hash) =>
            imageSrcMap[hash] ? (
              <Image key={`${currentQuestionCode}_img_${hash}`} className='questionImg' mode='widthFix' src={imageSrcMap[hash]} />
            ) : null
          )}
          {currentIsSubjective ? (
            <View className='subjectiveHint'>
              <Text className='subjectiveHintText'>本题为案例分析，请自备纸笔作答；交卷后不计入客观得分。</Text>
            </View>
          ) : null}
          {currentOptions.map((op) => {
            const active = answers[currentQuestionCode] === op.key;
            const opHashes = extractImageHashes(op.text);
            return (
              <View
                key={`${currentQuestionCode}_${op.key}`}
                className={`option ${active ? 'active' : ''}`}
                onClick={() => onSelectOption(currentQuestionCode, op.key)}
              >
                <Text className='optKey'>{op.key}.</Text>
                <View className='optBody'>
                  <Text className='optText'>{stripImageMarkers(op.text)}</Text>
                  {opHashes.map((hash) =>
                    imageSrcMap[hash] ? (
                      <Image
                        key={`${currentQuestionCode}_${op.key}_img_${hash}`}
                        className='optionImg'
                        mode='widthFix'
                        src={imageSrcMap[hash]}
                      />
                    ) : null
                  )}
                </View>
              </View>
            );
          })}

          <View className='navRow'>
            <View className={`navBtn ${currentIndex === 0 ? 'disabled' : ''}`} onClick={goPrev}>
              <Text className='navBtnText'>上一题</Text>
            </View>
            {currentIndex === questions.length - 1 ? (
              <View className='navBtn' onClick={submitPaper}>
                <Text className='navBtnText'>交卷</Text>
              </View>
            ) : (
              <View className='navBtn' onClick={goNext}>
                <Text className='navBtnText'>下一题</Text>
              </View>
            )}
          </View>
        </View>
      ) : null}

      <View className='bottomTools'>
        <View className='toolItem' onClick={toggleFavorite}>
          <Text className='toolIcon'>{currentFavorite ? '★' : '☆'}</Text>
          <Text className='toolText'>收藏</Text>
        </View>
        <View className='toolItem' onClick={() => setShowAnalysis(true)}>
          <Text className='toolIcon'>🧠</Text>
          <Text className='toolText'>查看解析</Text>
        </View>
        <View className='toolItem' onClick={() => setShowAnswerCard(true)}>
          <Text className='toolIcon'>🗂️</Text>
          <Text className='toolText'>答题卡</Text>
        </View>
      </View>

      {showAnswerCard ? (
        <View className='overlay' onClick={() => setShowAnswerCard(false)}>
          <View className='sheet' onClick={(e) => e.stopPropagation()}>
            <View className='sheetTitle'>答题卡（点击跳转）</View>
            <View className='sheetGrid'>
              {questions.map((q, idx) => {
                const code = q.questionCode || `q_${idx}`;
                const answered = !!answers[code];
                const current = idx === currentIndex;
                return (
                  <View
                    key={code}
                    className={`sheetItem ${answered ? 'answered' : ''} ${current ? 'current' : ''}`}
                    onClick={() => {
                      setCurrentIndex(idx);
                      setShowAnswerCard(false);
                    }}
                  >
                    <Text className='sheetItemText'>{idx + 1}</Text>
                  </View>
                );
              })}
            </View>
            <View className='submitPaperSheetBtn' onClick={confirmAndSubmitPaper}>
              <Text className='submitPaperSheetBtnText'>交卷并查看得分</Text>
            </View>
            <View className='redoSheetBtn' onClick={redoPaper}>
              <Text className='redoSheetBtnText'>重做本套题</Text>
            </View>
          </View>
        </View>
      ) : null}

      {showAnalysis ? (
        <View className='overlay' onClick={() => setShowAnalysis(false)}>
          <View className='sheet' onClick={(e) => e.stopPropagation()}>
            <View className='sheetTitle'>题目解析</View>
            <View className='analysisText'>
              {splitTextWithImageMarkers(analysisText).map((part, i) => {
                if (part.type === 'text') {
                  return (
                    <Text key={`analysis_${i}`} className='analysisTextChunk'>
                      {part.content}
                    </Text>
                  );
                }
                const src = imageSrcMap[part.hash];
                return src ? (
                  <Image
                    key={`analysis_${i}_img`}
                    className='questionImg'
                    mode='widthFix'
                    src={src}
                  />
                ) : (
                  <Text key={`analysis_${i}_wait`} className='analysisImgHint'>
                    图片加载中…
                  </Text>
                );
              })}
            </View>
            <View className='closeBtn' onClick={() => setShowAnalysis(false)}>
              <Text className='closeBtnText'>我知道了</Text>
            </View>
          </View>
        </View>
      ) : null}
    </View>
  );
};

export default QuizDoPage;

