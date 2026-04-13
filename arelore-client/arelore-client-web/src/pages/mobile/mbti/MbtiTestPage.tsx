import React, { useMemo, useState } from 'react';
import { Button, Card, Progress, Radio, Space, Typography } from 'antd';

const { Title, Paragraph, Text } = Typography;

type Dimension = 'E' | 'I' | 'S' | 'N' | 'T' | 'F' | 'J' | 'P';

interface Question {
  id: number;
  text: string;
  optionA: { label: string; dimension: Dimension };
  optionB: { label: string; dimension: Dimension };
}

const QUESTIONS: Question[] = [
  { id: 1, text: '在聚会中，你通常会？', optionA: { label: '主动和很多人交流', dimension: 'E' }, optionB: { label: '更倾向和少数熟人聊天', dimension: 'I' } },
  { id: 2, text: '你更相信什么？', optionA: { label: '可验证的事实与细节', dimension: 'S' }, optionB: { label: '灵感、趋势和可能性', dimension: 'N' } },
  { id: 3, text: '做决策时你更看重？', optionA: { label: '逻辑与客观标准', dimension: 'T' }, optionB: { label: '感受与人际影响', dimension: 'F' } },
  { id: 4, text: '你更喜欢哪种工作方式？', optionA: { label: '计划明确，按步骤执行', dimension: 'J' }, optionB: { label: '保持灵活，随时调整', dimension: 'P' } },
  { id: 5, text: '周末后你通常感觉？', optionA: { label: '和人相处后更有能量', dimension: 'E' }, optionB: { label: '独处后更有能量', dimension: 'I' } },
  { id: 6, text: '学习新知识时你更喜欢？', optionA: { label: '先看具体案例', dimension: 'S' }, optionB: { label: '先理解整体框架', dimension: 'N' } },
  { id: 7, text: '面对冲突时你更倾向？', optionA: { label: '讲道理，快速解决问题', dimension: 'T' }, optionB: { label: '先共情，再沟通方案', dimension: 'F' } },
  { id: 8, text: '对截止日期你的态度是？', optionA: { label: '提前规划并尽早完成', dimension: 'J' }, optionB: { label: '临近截止时效率更高', dimension: 'P' } }
];

const MbtiTestPage: React.FC = () => {
  const [answers, setAnswers] = useState<Record<number, Dimension>>({});
  const [result, setResult] = useState<string>('');

  const progress = useMemo(() => Math.round((Object.keys(answers).length / QUESTIONS.length) * 100), [answers]);

  const handleSelect = (questionId: number, dimension: Dimension) => {
    setAnswers((prev) => ({ ...prev, [questionId]: dimension }));
  };

  const handleSubmit = () => {
    if (Object.keys(answers).length < QUESTIONS.length) {
      return;
    }

    const score: Record<Dimension, number> = { E: 0, I: 0, S: 0, N: 0, T: 0, F: 0, J: 0, P: 0 };
    Object.values(answers).forEach((dimension) => {
      score[dimension] += 1;
    });

    const mbti = `${score.E >= score.I ? 'E' : 'I'}${score.S >= score.N ? 'S' : 'N'}${score.T >= score.F ? 'T' : 'F'}${score.J >= score.P ? 'J' : 'P'}`;
    setResult(mbti);
  };

  const handleReset = () => {
    setAnswers({});
    setResult('');
  };

  return (
    <div style={{ minHeight: '100vh', background: '#f5f7fb', padding: '16px' }}>
      <Card style={{ maxWidth: 760, margin: '0 auto' }}>
        <Title level={3} style={{ marginTop: 0 }}>MBTI 性格测试</Title>
        <Paragraph type="secondary">请根据你的第一直觉作答，全部完成后可查看测试结果。</Paragraph>
        <Progress percent={progress} size="small" />
        <Space direction="vertical" size="middle" style={{ width: '100%', marginTop: 12 }}>
          {QUESTIONS.map((item) => (
            <Card key={item.id} size="small">
              <Text strong>{item.id}. {item.text}</Text>
              <div style={{ marginTop: 8 }}>
                <Radio.Group
                  value={answers[item.id]}
                  onChange={(e) => handleSelect(item.id, e.target.value as Dimension)}
                >
                  <Space direction="vertical">
                    <Radio value={item.optionA.dimension}>{item.optionA.label}</Radio>
                    <Radio value={item.optionB.dimension}>{item.optionB.label}</Radio>
                  </Space>
                </Radio.Group>
              </div>
            </Card>
          ))}
        </Space>
        <Space style={{ marginTop: 16 }}>
          <Button type="primary" onClick={handleSubmit} disabled={progress < 100}>查看结果</Button>
          <Button onClick={handleReset}>重置</Button>
        </Space>
        {result ? (
          <Card style={{ marginTop: 16, background: '#f6ffed', borderColor: '#b7eb8f' }}>
            <Text>你的 MBTI 类型是：</Text>
            <Title level={2} style={{ margin: '8px 0 0' }}>{result}</Title>
          </Card>
        ) : null}
      </Card>
    </div>
  );
};

export default MbtiTestPage;
