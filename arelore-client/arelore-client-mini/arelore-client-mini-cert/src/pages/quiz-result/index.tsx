import React, { useEffect, useMemo, useState } from 'react';
import Taro, { useDidShow } from '@tarojs/taro';
import { Image, Text, View } from '@tarojs/components';
import { post } from '../../utils/request';
import {
  RESULT_STATUS_CLASS,
  classifyAnswerItem,
  inferCorrectKeys,
  parseQuestionExtraInfo,
  parseUserKeys
} from '../../utils/quizExamResult';
import './index.scss';

const IMG_HASH_REGEX = /(?:【img:([0-9a-fA-F]{64})】|\(img:([0-9a-fA-F]{64})\))/g;

function extractImageHashes(text) {
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
}

function splitTextWithImageMarkers(text) {
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
}

function parseOptions(raw) {
  if (!raw) return [];
  try {
    const arr = JSON.parse(raw);
    return Array.isArray(arr) ? arr : [];
  } catch (e) {
    return [];
  }
}

function buildDetailMap(list) {
  const m = {};
  (list || []).forEach((d) => {
    const code = d?.questionCode;
    if (code) m[code] = d;
  });
  return m;
}

function resolveStem(q) {
  let stem = q?.questionName || '';
  try {
    const ext = parseQuestionExtraInfo(q?.extraInfo);
    if (ext?.fullQuestionName) stem = ext.fullQuestionName;
  } catch (e) {
    /* ignore */
  }
  return stem;
}

function buildExplanation(q, detail) {
  try {
    const ext = parseQuestionExtraInfo(q?.extraInfo);
    let exp = ext?.fullExplanation || ext?.explanation || '';
    if (!exp && detail?.selectedOptionText && detail.selectedOptionText !== '(未作答)') {
      exp = '';
    }
    if (!exp) return '本题暂未配置解析。';
    return exp;
  } catch (e) {
    return '本题暂未配置解析。';
  }
}

const QuizResultPage = () => {
  const [typeCode, setTypeCode] = useState('');
  const [typeName, setTypeName] = useState('');
  const [detectResult, setDetectResult] = useState('');
  const [extraPayload, setExtraPayload] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [imageSrcMap, setImageSrcMap] = useState({});
  const [analysisQuestion, setAnalysisQuestion] = useState(null);
  const [analysisDetail, setAnalysisDetail] = useState(null);

  useDidShow(() => {
    const instance = Taro.getCurrentInstance();
    const tc = decodeURIComponent(instance?.router?.params?.typeCode || '');
    const tn = decodeURIComponent(instance?.router?.params?.typeName || '');
    setTypeCode(tc);
    setTypeName(tn);

    const cached = Taro.getStorageSync('quizLastResult');
    if (!tc || !cached || cached.typeCode !== tc) {
      setError('缺少本次答卷数据，请先交卷或从考试记录重新进入');
      setExtraPayload(null);
      setLoading(false);
      return;
    }
    setDetectResult(cached.detectResult || '');
    setExtraPayload(cached.extraInfo || null);

    (async () => {
      try {
        setLoading(true);
        setError('');
        const resp = await post('/user/detection/question/all', { typeCode: tc });
        if (resp && resp.code === 200) {
          setQuestions(Array.isArray(resp.data) ? resp.data : []);
          return;
        }
        setQuestions([]);
        setError(resp?.message || '加载试卷题目失败');
      } catch (e) {
        setQuestions([]);
        setError(e?.errMsg || '加载试卷题目失败');
      } finally {
        setLoading(false);
      }
    })();
  });

  const detailMap = useMemo(() => buildDetailMap(extraPayload?.answeredQuestions), [extraPayload]);

  const rows = useMemo(() => {
    return (questions || []).map((q, idx) => {
      const code = q?.questionCode || `q_${idx}`;
      const detail = detailMap[code] || {};
      const status = classifyAnswerItem(q, detail);
      return { q, idx, code, detail, status };
    });
  }, [questions, detailMap]);

  const stats = useMemo(() => {
    const s = { correct: 0, wrong: 0, partial: 0, unanswered: 0, subjective: 0 };
    rows.forEach((r) => {
      const k = r.status;
      if (k in s) s[k] += 1;
    });
    return s;
  }, [rows]);

  useEffect(() => {
    if (!analysisQuestion) return;
    const stem = resolveStem(analysisQuestion);
    const exp = buildExplanation(analysisQuestion, analysisDetail);
    const opts = parseOptions(analysisQuestion.options);
    const hashes = [];
    extractImageHashes(stem).forEach((h) => hashes.push(h));
    extractImageHashes(exp).forEach((h) => hashes.push(h));
    opts.forEach((op) => extractImageHashes(op?.text).forEach((h) => hashes.push(h)));
    const unique = Array.from(new Set(hashes));
    if (unique.length === 0) return;

    let cancelled = false;
    (async () => {
      const loadedMap = {};
      await Promise.all(
        unique.map(async (hash) => {
          try {
            const resp = await post('/user/common-binary-file/get-by-hash', { hashValue: hash });
            if (resp && resp.code === 200 && resp.data?.base64Data) {
              const contentType = resp.data.contentType || 'image/png';
              loadedMap[hash] = `data:${contentType};base64,${resp.data.base64Data}`;
            }
          } catch (e) {
            /* ignore */
          }
        })
      );
      if (!cancelled && Object.keys(loadedMap).length > 0) {
        setImageSrcMap((prev) => ({ ...prev, ...loadedMap }));
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [analysisQuestion, analysisDetail]);

  const openAnalysis = (q, detail) => {
    setAnalysisQuestion(q);
    setAnalysisDetail(detail);
  };

  const renderRichText = (text, keyPrefix) =>
    splitTextWithImageMarkers(text || '').map((part, i) => {
      if (part.type === 'text') {
        return (
          <Text key={`${keyPrefix}_${i}`} className='analysisChunk'>
            {part.content}
          </Text>
        );
      }
      const src = imageSrcMap[part.hash];
      return src ? (
        <Image key={`${keyPrefix}_img_${i}`} className='questionImg' mode='widthFix' src={src} />
      ) : (
        <Text key={`${keyPrefix}_wait_${i}`} className='analysisChunk'>
          图片加载中…
        </Text>
      );
    });

  const onDone = () => {
    try {
      const pages = Taro.getCurrentPages();
      if (pages.length > 1) {
        Taro.navigateBack({
          fail: () => {
            Taro.switchTab({ url: '/pages/quiz/index' });
          }
        });
      } else {
        Taro.switchTab({ url: '/pages/quiz/index' });
      }
    } catch (_e) {
      Taro.switchTab({ url: '/pages/quiz/index' });
    }
  };

  const overlayCorrectKeys = analysisQuestion
    ? inferCorrectKeys(parseQuestionExtraInfo(analysisQuestion.extraInfo), parseOptions(analysisQuestion.options))
    : [];

  const overlayUserKeys =
    analysisQuestion && analysisDetail ? parseUserKeys(analysisDetail.selectedOptionKey || '') : [];

  return (
    <View className='page'>
      <View className='headCard'>
        <Text className='paperTitle'>{typeName || typeCode || '考试结果'}</Text>
        {detectResult ? <Text className='scoreBig'>得分：{detectResult}</Text> : null}
        <Text className='metaRow'>
          {extraPayload?.totalQuestions != null ? `共 ${extraPayload.totalQuestions} 题` : `共 ${questions.length} 题`}
          {extraPayload?.answeredCount != null ? ` · 已作答 ${extraPayload.answeredCount}` : ''}
          {extraPayload?.unansweredCount != null ? ` · 未作答 ${extraPayload.unansweredCount}` : ''}
        </Text>
      </View>

      {loading ? <View className='tip'>加载中...</View> : null}
      {error ? <View className='tip error'>{error}</View> : null}

      {!loading && !error && rows.length > 0 ? (
        <>
          <View className='legend'>
            <View className='legendItem'>
              <View className='legendDot correct' />
              <Text className='legendText'>正确 {stats.correct}</Text>
            </View>
            <View className='legendItem'>
              <View className='legendDot wrong' />
              <Text className='legendText'>错误 {stats.wrong}</Text>
            </View>
            <View className='legendItem'>
              <View className='legendDot partial' />
              <Text className='legendText'>半对 {stats.partial}</Text>
            </View>
            <View className='legendItem'>
              <View className='legendDot unanswered' />
              <Text className='legendText'>未作答 {stats.unanswered}</Text>
            </View>
            <View className='legendItem'>
              <View className='legendDot subjective' />
              <Text className='legendText'>主观 {stats.subjective}</Text>
            </View>
          </View>

          <View className='gridCard'>
            <Text className='gridTitle'>答题卡（点击查看解析）</Text>
            <View className='sheetGrid'>
              {rows.map((r) => (
                <View
                  key={r.code}
                  className={`sheetItem ${RESULT_STATUS_CLASS[r.status] || 'cellUnanswered'}`}
                  onClick={() => openAnalysis(r.q, r.detail)}
                >
                  <Text className='sheetItemText'>{r.idx + 1}</Text>
                </View>
              ))}
            </View>
          </View>
        </>
      ) : null}

      <View className='footerBar'>
        <View className='footerBtn' onClick={onDone}>
          <Text className='footerBtnText'>返回</Text>
        </View>
      </View>

      {analysisQuestion ? (
        <View className='overlay' onClick={() => setAnalysisQuestion(null)}>
          <View className='sheet' onClick={(e) => e.stopPropagation()}>
            <View className='sheetTitle'>题目解析（仅浏览）</View>
            <Text className='analysisLabel'>题干</Text>
            <View className='analysisBody'>{renderRichText(resolveStem(analysisQuestion), 'stem')}</View>

            {parseOptions(analysisQuestion.options).length > 0 ? (
              <View className='analysisSection'>
                <Text className='analysisLabel'>选项</Text>
                {parseOptions(analysisQuestion.options).map((op) => {
                  const key = String(op.key || '').toUpperCase();
                  const userPick = overlayUserKeys.includes(key);
                  const isCorrectOpt = overlayCorrectKeys.includes(key);
                  let rowCls = 'optRow';
                  if (userPick) rowCls += ' userPicked';
                  if (isCorrectOpt) rowCls += ' correctOpt';
                  return (
                    <View key={`${analysisQuestion.questionCode}_${op.key}`} className={rowCls}>
                      <View className='optKeyRow'>
                        <Text className='optKey'>{op.key}.</Text>
                        {userPick ? <Text className='tagTiny'>你的选择</Text> : null}
                        {isCorrectOpt ? <Text className='tagTiny'>标答</Text> : null}
                      </View>
                      <View className='optText'>{renderRichText(op.text || '', `op_${op.key}`)}</View>
                    </View>
                  );
                })}
              </View>
            ) : null}

            <View className='analysisSection'>
              <Text className='analysisLabel'>参考答案键</Text>
              <Text className='analysisBody'>
                {overlayCorrectKeys && overlayCorrectKeys.length > 0 ? overlayCorrectKeys.join('、') : '题库未标注'}
              </Text>
            </View>

            <View className='analysisSection'>
              <Text className='analysisLabel'>解析</Text>
              <View className='analysisBody'>{renderRichText(buildExplanation(analysisQuestion, analysisDetail), 'exp')}</View>
            </View>

            <View className='closeBtn' onClick={() => setAnalysisQuestion(null)}>
              <Text className='closeBtnText'>关闭</Text>
            </View>
          </View>
        </View>
      ) : null}
    </View>
  );
};

export default QuizResultPage;
