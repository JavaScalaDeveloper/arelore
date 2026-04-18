import React, { useEffect, useMemo, useState } from 'react';
import { Button, Card, Empty, Progress, Radio, Space, Spin, Typography, message } from 'antd';
import { userApi } from '../../../api/user';
import { useNavigate } from 'react-router-dom';

const { Title, Paragraph, Text } = Typography;

interface Question {
  id: number | string;
  questionCode?: string;
  text: string;
  options: Array<{ key: string; label: string }>;
}

const COLOR_TYPE_CODE = 'COLOR4_INTL_V1';

interface BackendQuestion {
  id: number;
  questionCode: string;
  questionName: string;
  questionOrder: number;
  options?: string;
}

interface QuestionOption {
  key?: string;
  text?: string;
}

interface DetectionTypeItem {
  typeCode?: string;
  extraInfo?: string;
}

interface ResultMeaningDetail {
  summary?: string;
  strengths?: string;
  risks?: string;
  suggestedRoles?: string;
  communicationTips?: string;
}

const ColorPersonalityTestPage: React.FC = () => {
  const navigate = useNavigate();
  const [questions, setQuestions] = useState<Question[]>([]);
  const [loading, setLoading] = useState<boolean>(false);
  const [submitLoading, setSubmitLoading] = useState<boolean>(false);
  const [answers, setAnswers] = useState<Record<string, string>>({});
  const [meaningByResult, setMeaningByResult] = useState<Record<string, string | ResultMeaningDetail>>({});

  useEffect(() => {
    const loadQuestions = async () => {
      try {
        setLoading(true);
        const res = await userApi.getDetectionQuestionAll({ typeCode: COLOR_TYPE_CODE });
        const rawList: BackendQuestion[] = res.data || [];
        const mapped = rawList
          .sort((a, b) => (a.questionOrder || 0) - (b.questionOrder || 0))
          .map((item) => {
            let optionList: QuestionOption[] = [];
            try {
              optionList = item.options ? JSON.parse(item.options) : [];
            } catch (error) {
              optionList = [];
            }
            return {
              id: item.id,
              questionCode: item.questionCode,
              text: item.questionName,
              options: optionList
                .map((option, index) => ({
                  key: option.key || String.fromCharCode(65 + index),
                  label: option.text || `选项${index + 1}`
                }))
                .filter((option) => Boolean(option.key))
            };
          })
          .filter((item) => item.options.length >= 2);
        setQuestions(mapped);
        const typeRes = await userApi.getDetectionTypeAll();
        setMeaningByResult(extractMeaningMap(typeRes.data || [], COLOR_TYPE_CODE));
      } catch (error) {
        message.error('加载性格色彩题目失败');
      } finally {
        setLoading(false);
      }
    };
    loadQuestions();
  }, []);

  const extractMeaningMap = (typeList: DetectionTypeItem[], typeCode: string) => {
    const type = typeList.find((item) => item?.typeCode === typeCode);
    if (!type?.extraInfo) {
      return {};
    }
    try {
      const parsed = JSON.parse(type.extraInfo);
      return parsed?.resultMeanings || parsed?.meanings || parsed?.scoring?.resultMeanings || {};
    } catch (error) {
      return {};
    }
  };

  const progress = useMemo(
    () => (questions.length ? Math.round((Object.keys(answers).length / questions.length) * 100) : 0),
    [answers, questions.length]
  );

  const handleSelect = (questionId: number | string, selectedOptionKey: string) => {
    setAnswers((prev) => ({ ...prev, [String(questionId)]: selectedOptionKey }));
  };

  const handleSubmit = async () => {
    if (Object.keys(answers).length < questions.length || questions.length === 0) {
      message.warning('请先完成所有题目');
      return;
    }

    const currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}');
    const answeredQuestions = questions
      .filter((question) => Boolean(answers[String(question.id)]))
      .map((question) => ({
        questionId: question.id,
        questionCode: question.questionCode,
        selectedOptionKey: answers[String(question.id)]
      }));

    try {
      setSubmitLoading(true);
      const res = await userApi.saveDetectionResult({
        userId: currentUser?.id || 'anonymous',
        userDetectTypeCode: COLOR_TYPE_CODE,
        answeredQuestions
      });
      const detectResult = res.data?.detectResult || '';
      if (!detectResult) {
        message.error('结果计算失败，请重试');
        return;
      }
      navigate('/mobile/color-test/result', {
        replace: true,
        state: {
          result: detectResult,
          meaning: meaningByResult[detectResult] || ''
        }
      });
    } catch (error) {
      message.error('结果保存失败');
    } finally {
      setSubmitLoading(false);
    }
  };

  const handleReset = () => {
    setAnswers({});
  };

  return (
    <div style={{ minHeight: 'calc(100vh - 56px)', background: '#f5f7fb', padding: '16px' }}>
      <Card style={{ maxWidth: 760, margin: '0 auto' }}>
        <Title level={3} style={{ marginTop: 0 }}>性格色彩测试（国际标准版）</Title>
        <Paragraph type="secondary">题目来自后端题库（typeCode=COLOR4_INTL_V1），由后端统一算分。</Paragraph>
        <Space style={{ marginBottom: 12 }}>
          <Button onClick={() => navigate('/mobile/home')}>首页</Button>
        </Space>
        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px 0' }}>
            <Spin />
          </div>
        ) : questions.length === 0 ? (
          <div style={{ marginTop: 16 }}>
            <Empty description="暂无可用性格色彩题目" />
          </div>
        ) : (
          <Space direction="vertical" size="middle" style={{ width: '100%', marginTop: 12 }}>
            {questions.map((item, index) => (
              <Card key={item.id} size="small">
                <Text strong>{index + 1}. {item.text}</Text>
                <div style={{ marginTop: 8 }}>
                  <Radio.Group
                    value={answers[String(item.id)]}
                    onChange={(e) => handleSelect(item.id, e.target.value as string)}
                  >
                    <Space direction="vertical">
                      {item.options.map((option) => (
                        <Radio key={`${item.id}-${option.key}`} value={option.key}>
                          {option.label}
                        </Radio>
                      ))}
                    </Space>
                  </Radio.Group>
                </div>
              </Card>
            ))}
          </Space>
        )}
        <div style={{ marginTop: 16 }}>
          <Progress percent={progress} size="small" />
        </div>
        <Space style={{ marginTop: 16 }}>
          <Button type="primary" loading={submitLoading} onClick={handleSubmit} disabled={progress < 100 || loading || questions.length === 0}>查看结果</Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>
        <Space style={{ marginTop: 16 }}>
          <Button onClick={() => navigate('/mobile/color-test/history')}>查看历史检测结果</Button>
        </Space>
      </Card>
    </div>
  );
};

export default ColorPersonalityTestPage;

