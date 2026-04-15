import React, { useEffect, useMemo, useState } from 'react';
import { Button, Card, Space, Tag, Typography } from 'antd';
import { DownOutlined, UpOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { userApi } from '../../api/user';

const { Title, Paragraph, Text } = Typography;
const MBTI_TYPE_CODE = 'MBTI';
const COLOR_TYPE_CODE = 'COLOR4_INTL_V1';

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

const MobileHomePage: React.FC = () => {
  const navigate = useNavigate();
  const [currentMbti, setCurrentMbti] = useState<string>('');
  const [currentColor, setCurrentColor] = useState<string>('');
  const [meaningByTypeAndResult, setMeaningByTypeAndResult] = useState<Record<string, Record<string, string | ResultMeaningDetail>>>({});
  const [mbtiDetailOpen, setMbtiDetailOpen] = useState<boolean>(false);
  const [colorDetailOpen, setColorDetailOpen] = useState<boolean>(false);

  const mbtiMeaning = useMemo(
    () => (currentMbti ? (meaningByTypeAndResult[MBTI_TYPE_CODE]?.[currentMbti] || '') : ''),
    [currentMbti, meaningByTypeAndResult]
  );
  const colorMeaning = useMemo(
    () => (currentColor ? (meaningByTypeAndResult[COLOR_TYPE_CODE]?.[currentColor] || '') : ''),
    [currentColor, meaningByTypeAndResult]
  );

  useEffect(() => {
    const loadCurrentResult = async () => {
      const currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}');
      if (!currentUser?.id) {
        setCurrentMbti('');
        setCurrentColor('');
        return;
      }
      try {
        const [mbtiRes, colorRes, typeRes] = await Promise.all([
          userApi.getCurrentDetectionResult({ userId: currentUser.id, typeCode: MBTI_TYPE_CODE }),
          userApi.getCurrentDetectionResult({ userId: currentUser.id, typeCode: COLOR_TYPE_CODE }),
          userApi.getDetectionTypeAll()
        ]);
        setCurrentMbti(mbtiRes.data?.userDetectResult || '');
        setCurrentColor(colorRes.data?.userDetectResult || '');
        setMeaningByTypeAndResult(buildMeaningMap(typeRes.data || []));
      } catch (error) {
        setCurrentMbti('');
        setCurrentColor('');
      }
    };
    loadCurrentResult();
  }, []);

  const buildMeaningMap = (typeList: DetectionTypeItem[]) => {
    const result: Record<string, Record<string, string | ResultMeaningDetail>> = {};
    for (const type of typeList) {
      if (!type?.typeCode) {
        continue;
      }
      try {
        const parsed = type.extraInfo ? JSON.parse(type.extraInfo) : {};
        const scoring = parsed?.scoring || {};
        const map = parsed?.resultMeanings || parsed?.meanings || scoring?.resultMeanings || {};
        if (map && typeof map === 'object') {
          result[type.typeCode] = map;
        }
      } catch (error) {
        // ignore malformed extraInfo
      }
    }
    return result;
  };

  const renderMeaningSummary = (meaning: string | ResultMeaningDetail | '') => {
    if (!meaning) {
      return '完成测试后，可在此查看你的个性化说明。';
    }
    if (typeof meaning === 'string') {
      return meaning;
    }
    return meaning.summary || '可点击详情查看完整解读。';
  };

  const renderMeaningDetail = (meaning: string | ResultMeaningDetail | '') => {
    if (!meaning || typeof meaning === 'string') {
      return null;
    }
    return (
      <Space direction="vertical" size={6} style={{ width: '100%' }}>
        {meaning.strengths ? <Text style={{ color: '#389e0d' }}>优势：{meaning.strengths}</Text> : null}
        {meaning.risks ? <Text style={{ color: '#cf1322' }}>风险点：{meaning.risks}</Text> : null}
        {meaning.suggestedRoles ? <Text>建议方向：{meaning.suggestedRoles}</Text> : null}
        {meaning.communicationTips ? <Text>沟通建议：{meaning.communicationTips}</Text> : null}
      </Space>
    );
  };

  return (
    <div
      style={{
        minHeight: 'calc(100vh - 56px)',
        background: '#f5f7fb',
        padding: 16
      }}
    >
      <Card style={{ maxWidth: 760, margin: '0 auto', borderRadius: 12 }}>
        <Title level={3} style={{ marginTop: 0 }}>移动端首页</Title>
        <Paragraph type="secondary" style={{ marginBottom: 16 }}>
          可在此查看你的最新测评结果，并继续完成新的测试。
        </Paragraph>

        <Space direction="vertical" size={12} style={{ width: '100%' }}>
          <Card
            size="small"
            title="MBTI 结果"
            extra={
              currentMbti ? (
                <Button
                  type="text"
                  size="small"
                  style={{ paddingInline: 4 }}
                  onClick={() => setMbtiDetailOpen((v) => !v)}
                  icon={mbtiDetailOpen ? <UpOutlined /> : <DownOutlined />}
                />
              ) : null
            }
          >
            <div style={{ marginBottom: 8 }}>
              <Text>当前 MBTI：</Text>
              {currentMbti ? (
                <Tag color="blue" style={{ marginLeft: 8 }}>{currentMbti}</Tag>
              ) : (
                <Tag style={{ marginLeft: 8 }}>暂无结果</Tag>
              )}
            </div>
            <div style={{ marginBottom: 0, color: '#666' }}>
              <Paragraph style={{ marginBottom: 8, color: '#666' }}>
                {renderMeaningSummary(mbtiMeaning)}
              </Paragraph>
              {mbtiDetailOpen ? <div style={{ marginTop: 8 }}>{renderMeaningDetail(mbtiMeaning)}</div> : null}
            </div>
          </Card>

          <Card
            size="small"
            title="性格色彩结果"
            extra={
              currentColor ? (
                <Button
                  type="text"
                  size="small"
                  style={{ paddingInline: 4 }}
                  onClick={() => setColorDetailOpen((v) => !v)}
                  icon={colorDetailOpen ? <UpOutlined /> : <DownOutlined />}
                />
              ) : null
            }
          >
            <div style={{ marginBottom: 8 }}>
              <Text>当前主色：</Text>
              {currentColor ? (
                <Tag color="magenta" style={{ marginLeft: 8 }}>{currentColor}</Tag>
              ) : (
                <Tag style={{ marginLeft: 8 }}>暂无结果</Tag>
              )}
            </div>
            <div style={{ marginBottom: 0, color: '#666' }}>
              <Paragraph style={{ marginBottom: 8, color: '#666' }}>
                {renderMeaningSummary(colorMeaning)}
              </Paragraph>
              {colorDetailOpen ? <div style={{ marginTop: 8 }}>{renderMeaningDetail(colorMeaning)}</div> : null}
            </div>
          </Card>
        </Space>

        <Space direction="vertical" style={{ width: '100%', marginTop: 16 }}>
          <Button
            type="primary"
            size="large"
            block
            style={{ height: 44, borderRadius: 10, fontWeight: 600 }}
            onClick={() => navigate('/mobile/mbti')}
          >
            开始 MBTI 测试
          </Button>
          <Button
            size="large"
            block
            style={{ height: 44, borderRadius: 10, fontWeight: 600, borderColor: '#722ed1', color: '#722ed1' }}
            onClick={() => navigate('/mobile/color-test')}
          >
            开始性格色彩测试
          </Button>
        </Space>
      </Card>
    </div>
  );
};

export default MobileHomePage;
