import React, { useEffect, useState } from 'react';
import Taro, { useDidShow } from '@tarojs/taro';
import { Image, Text, View } from '@tarojs/components';
import { post } from '../../utils/request';
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
          setCurrentIndex(0);
          setAnswers({});
          setFavoriteMap({});
          setImageSrcMap({});
          setImageLoadingMap({});
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

  const goPrev = () => {
    setCurrentIndex((idx) => (idx <= 0 ? 0 : idx - 1));
  };

  const goNext = () => {
    setCurrentIndex((idx) => (idx >= questions.length - 1 ? idx : idx + 1));
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
  const currentQuestionType = (() => {
    if (!currentQuestion) return '单选题';
    try {
      const info = currentQuestion.extraInfo ? JSON.parse(currentQuestion.extraInfo) : {};
      return info?.questionType || '单选题';
    } catch (e) {
      return '单选题';
    }
  })();

  const currentQuestionCode = currentQuestion?.questionCode || `q_${currentIndex}`;
  const currentFavorite = !!favoriteMap[currentQuestionCode];
  const analysisText = (() => {
    if (!currentQuestion) return '';
    const selected = answers[currentQuestionCode];
    if (!selected) return '你尚未选择答案。';
    return `你当前选择了 ${selected} 选项。该题暂未配置标准解析，后续可在题库中补充 explanation 字段。`;
  })();

  useEffect(() => {
    if (!currentQuestion) return;
    const hashes = new Set();
    extractImageHashes(currentQuestion.questionName).forEach((h) => hashes.add(h));
    parseOptions(currentQuestion).forEach((op) => {
      extractImageHashes(op?.text).forEach((h) => hashes.add(h));
    });
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
          <View className='qTitle'>
            {currentIndex + 1}. {stripImageMarkers(currentQuestion.questionName) || '题目'}
          </View>
          {extractImageHashes(currentQuestion.questionName).map((hash) =>
            imageSrcMap[hash] ? (
              <Image key={`${currentQuestionCode}_img_${hash}`} className='questionImg' mode='widthFix' src={imageSrcMap[hash]} />
            ) : null
          )}
          {parseOptions(currentQuestion).map((op) => {
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
            <View className={`navBtn ${currentIndex === questions.length - 1 ? 'disabled' : ''}`} onClick={goNext}>
              <Text className='navBtnText'>下一题</Text>
            </View>
          </View>
        </View>
      ) : null}

      <View className='bottomTools'>
        <View
          className='toolItem'
          onClick={() => setFavoriteMap((prev) => ({ ...prev, [currentQuestionCode]: !prev[currentQuestionCode] }))}
        >
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
          </View>
        </View>
      ) : null}

      {showAnalysis ? (
        <View className='overlay' onClick={() => setShowAnalysis(false)}>
          <View className='sheet' onClick={(e) => e.stopPropagation()}>
            <View className='sheetTitle'>题目解析</View>
            <View className='analysisText'>{analysisText}</View>
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

