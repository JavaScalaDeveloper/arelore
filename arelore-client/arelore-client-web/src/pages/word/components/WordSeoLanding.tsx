import React from 'react';
import { Button, Card, Col, Row, Typography } from 'antd';
import { useI18n, useLocaleNavigate } from '../../../locale';

const { Title, Paragraph, Text } = Typography;

/**
 * 未登录可见的 SEO 落地内容：足量可读文案 + 中性表述（避免标签页/旁人一眼看出「摸鱼」）。
 */
export default function WordSeoLanding() {
  const navigate = useLocaleNavigate();
  const { t } = useI18n();

  return (
    <div className="page word-seo-landing">
      <header className="word-seo-hero">
        <Title level={1} style={{ marginBottom: 8 }}>
          {t('word.brand')}
        </Title>
        <Paragraph className="word-seo-lead">{t('word.landing.lead')}</Paragraph>
        <Button type="primary" size="large" onClick={() => navigate('/word/login')}>
          {t('word.landing.ctaLogin')}
        </Button>
      </header>

      <section className="word-seo-section" aria-labelledby="word-why">
        <Title id="word-why" level={2}>
          {t('word.landing.whyTitle')}
        </Title>
        <Paragraph>{t('word.landing.whyP1')}</Paragraph>
        <Paragraph>{t('word.landing.whyP2')}</Paragraph>
      </section>

      <section className="word-seo-section" aria-labelledby="word-modes">
        <Title id="word-modes" level={2}>
          {t('word.landing.modesTitle')}
        </Title>
        <div className="word-seo-features">
          <Card size="small">
            <Title level={3} style={{ fontSize: 16, marginTop: 0 }}>
              {t('word.landing.modeMeaningTitle')}
            </Title>
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>
              {t('word.landing.modeMeaningBody')}
            </Paragraph>
          </Card>
          <Card size="small">
            <Title level={3} style={{ fontSize: 16, marginTop: 0 }}>
              {t('word.landing.modeListenTitle')}
            </Title>
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>
              {t('word.landing.modeListenBody')}
            </Paragraph>
          </Card>
          <Card size="small">
            <Title level={3} style={{ fontSize: 16, marginTop: 0 }}>
              {t('word.landing.modeDictationTitle')}
            </Title>
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>
              {t('word.landing.modeDictationBody')}
            </Paragraph>
          </Card>
        </div>
      </section>

      <section className="word-seo-section" aria-labelledby="word-scenes">
        <Title id="word-scenes" level={2}>
          {t('word.landing.scenesTitle')}
        </Title>
        <Row gutter={[12, 12]}>
          <Col xs={24} sm={12}>
            <Card size="small" title={t('word.landing.sceneCommute')}>
              <Text type="secondary">{t('word.landing.sceneCommuteBody')}</Text>
            </Card>
          </Col>
          <Col xs={24} sm={12}>
            <Card size="small" title={t('word.landing.sceneWait')}>
              <Text type="secondary">{t('word.landing.sceneWaitBody')}</Text>
            </Card>
          </Col>
          <Col xs={24} sm={12}>
            <Card size="small" title={t('word.landing.sceneDesk')}>
              <Text type="secondary">{t('word.landing.sceneDeskBody')}</Text>
            </Card>
          </Col>
          <Col xs={24} sm={12}>
            <Card size="small" title={t('word.landing.sceneExam')}>
              <Text type="secondary">{t('word.landing.sceneExamBody')}</Text>
            </Card>
          </Col>
        </Row>
      </section>

      <section className="word-seo-section" aria-labelledby="word-howto">
        <Title id="word-howto" level={2}>
          {t('word.landing.howtoTitle')}
        </Title>
        <ol className="word-seo-steps">
          <li>
            <Text strong>{t('word.landing.stepLogin')}</Text>
            <Paragraph type="secondary">{t('word.landing.stepLoginBody')}</Paragraph>
          </li>
          <li>
            <Text strong>{t('word.landing.stepBook')}</Text>
            <Paragraph type="secondary">{t('word.landing.stepBookBody')}</Paragraph>
          </li>
          <li>
            <Text strong>{t('word.landing.stepMode')}</Text>
            <Paragraph type="secondary">{t('word.landing.stepModeBody')}</Paragraph>
          </li>
          <li>
            <Text strong>{t('word.landing.stepStart')}</Text>
            <Paragraph type="secondary">{t('word.landing.stepStartBody')}</Paragraph>
          </li>
        </ol>
      </section>

      <section className="word-seo-faq" aria-labelledby="word-faq">
        <Title id="word-faq" level={2}>
          {t('word.landing.faqTitle')}
        </Title>
        <Card size="small" style={{ marginBottom: 12 }}>
          <Text strong>{t('word.landing.faqScreenQ')}</Text>
          <Paragraph type="secondary" style={{ marginBottom: 0, marginTop: 6 }}>
            {t('word.landing.faqScreenA')}
          </Paragraph>
        </Card>
        <Card size="small" style={{ marginBottom: 12 }}>
          <Text strong>{t('word.landing.faqAccentQ')}</Text>
          <Paragraph type="secondary" style={{ marginBottom: 0, marginTop: 6 }}>
            {t('word.landing.faqAccentA')}
          </Paragraph>
        </Card>
        <Card size="small" style={{ marginBottom: 12 }}>
          <Text strong>{t('word.landing.faqWrongQ')}</Text>
          <Paragraph type="secondary" style={{ marginBottom: 0, marginTop: 6 }}>
            {t('word.landing.faqWrongA')}
          </Paragraph>
        </Card>
        <Card size="small" style={{ marginBottom: 12 }}>
          <Text strong>{t('word.landing.faqSwitchQ')}</Text>
          <Paragraph type="secondary" style={{ marginBottom: 0, marginTop: 6 }}>
            {t('word.landing.faqSwitchA')}
          </Paragraph>
        </Card>
        <Card size="small">
          <Text strong>{t('word.landing.faqPayQ')}</Text>
          <Paragraph type="secondary" style={{ marginBottom: 0, marginTop: 6 }}>
            {t('word.landing.faqPayA')}
          </Paragraph>
        </Card>
      </section>

      <div style={{ marginTop: 8, marginBottom: 32 }}>
        <Button type="primary" size="large" onClick={() => navigate('/word/login')}>
          {t('word.landing.ctaNow')}
        </Button>
      </div>
    </div>
  );
}
