import React from 'react';
import { Button, Card, Col, Row, Typography } from 'antd';
import { useLocaleNavigate } from '../../../locale';
import { WORD_BRAND } from '../seo/wordSeo';

const { Title, Paragraph, Text } = Typography;

/**
 * 未登录可见的 SEO 落地内容：足量可读文案 + 中性表述（避免标签页/旁人一眼看出「摸鱼」）。
 */
export default function WordSeoLanding() {
  const navigate = useLocaleNavigate();

  return (
    <div className="page word-seo-landing">
      <header className="word-seo-hero">
        <Title level={1} style={{ marginBottom: 8 }}>
          {WORD_BRAND}
        </Title>
        <Paragraph className="word-seo-lead">
          面向碎片时间的英语单词学习工具。结合<strong>视觉记忆</strong>、<strong>听力跟读</strong>与
          <strong>听写默写</strong>，帮你在通勤、排队、讲座间隙稳步提升词汇量与拼写能力。
        </Paragraph>
        <Button type="primary" size="large" onClick={() => navigate('/word/login')}>
          邮箱登录，开始学习
        </Button>
      </header>

      <section className="word-seo-section" aria-labelledby="word-why">
        <Title id="word-why" level={2}>
          为什么选择墨鱼背词
        </Title>
        <Paragraph>
          传统背单词往往需要长时间盯着屏幕刷列表。墨鱼背词强调「听」与「写」：你可以戴上耳机听单词与释义，
          也可以在需要巩固拼写时切换到听写。词书与每日计划会帮你安排新学与复习，减少自己排计划的负担。
        </Paragraph>
        <Paragraph>
          无论是备考词汇、职场英语，还是日常积累，都可以先选一本词书，再按自己的节奏练习。学习进度会保存在账号中，换设备登录后也能继续。
        </Paragraph>
      </section>

      <section className="word-seo-section" aria-labelledby="word-modes">
        <Title id="word-modes" level={2}>
          三种学习模式
        </Title>
        <div className="word-seo-features">
          <Card size="small">
            <Title level={3} style={{ fontSize: 16, marginTop: 0 }}>
              看词识意
            </Title>
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>
              先看到单词，回忆中文词义；不确定时可看例句提示。适合安静环境、希望强化形义对应的时段。
            </Paragraph>
          </Card>
          <Card size="small">
            <Title level={3} style={{ fontSize: 16, marginTop: 0 }}>
              听力练习
            </Title>
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>
              自动播放单词、字母拼读与中文释义，少占视觉注意力，适合戴耳机的碎片时段。
            </Paragraph>
          </Card>
          <Card size="small">
            <Title level={3} style={{ fontSize: 16, marginTop: 0 }}>
              听写默写
            </Title>
            <Paragraph type="secondary" style={{ marginBottom: 0 }}>
              听发音后拼写单词，巩固拼写与听力；可切换美音 / 英音，输错会即时反馈。
            </Paragraph>
          </Card>
        </div>
      </section>

      <section className="word-seo-section" aria-labelledby="word-scenes">
        <Title id="word-scenes" level={2}>
          适合的使用场景
        </Title>
        <Row gutter={[12, 12]}>
          <Col xs={24} sm={12}>
            <Card size="small" title="通勤与排队">
              <Text type="secondary">用听力练习过一遍今日单词，下车前完成一轮复习。</Text>
            </Card>
          </Col>
          <Col xs={24} sm={12}>
            <Card size="small" title="讲座与等待间隙">
              <Text type="secondary">短时间听写几个难词，强化拼写记忆。</Text>
            </Card>
          </Col>
          <Col xs={24} sm={12}>
            <Card size="small" title="书桌前系统学习">
              <Text type="secondary">看词识意 + 词书计划，完成每日新学与复习配额。</Text>
            </Card>
          </Col>
          <Col xs={24} sm={12}>
            <Card size="small" title="备考词汇冲刺">
              <Text type="secondary">按词书推进，进度可追踪，避免只刷不复习。</Text>
            </Card>
          </Col>
        </Row>
      </section>

      <section className="word-seo-section" aria-labelledby="word-howto">
        <Title id="word-howto" level={2}>
          如何开始
        </Title>
        <ol className="word-seo-steps">
          <li>
            <Text strong>登录</Text>
            <Paragraph type="secondary">使用邮箱验证码登录账号。</Paragraph>
          </li>
          <li>
            <Text strong>选择词书</Text>
            <Paragraph type="secondary">进入「词书」挑选词本，并确认每日新学 / 复习数量。</Paragraph>
          </li>
          <li>
            <Text strong>设置模式</Text>
            <Paragraph type="secondary">在「设置」中选择看词识意、听力练习或听写。</Paragraph>
          </li>
          <li>
            <Text strong>开始学习</Text>
            <Paragraph type="secondary">回到首页点击「开始学习」，按计划完成今日任务。</Paragraph>
          </li>
        </ol>
      </section>

      <section className="word-seo-faq" aria-labelledby="word-faq">
        <Title id="word-faq" level={2}>
          常见问题
        </Title>
        <Card size="small" style={{ marginBottom: 12 }}>
          <Text strong>需要一直盯着屏幕吗？</Text>
          <Paragraph type="secondary" style={{ marginBottom: 0, marginTop: 6 }}>
            不一定。听力练习以听为主；听写与看词识意可按场景切换。
          </Paragraph>
        </Card>
        <Card size="small" style={{ marginBottom: 12 }}>
          <Text strong>支持美音和英音吗？</Text>
          <Paragraph type="secondary" style={{ marginBottom: 0, marginTop: 6 }}>
            支持，学习页可切换发音口音。
          </Paragraph>
        </Card>
        <Card size="small" style={{ marginBottom: 12 }}>
          <Text strong>听写输错会怎样？</Text>
          <Paragraph type="secondary" style={{ marginBottom: 0, marginTop: 6 }}>
            会提示错误并可重试；不认识时可查看正确答案后继续。
          </Paragraph>
        </Card>
        <Card size="small" style={{ marginBottom: 12 }}>
          <Text strong>可以换词书吗？</Text>
          <Paragraph type="secondary" style={{ marginBottom: 0, marginTop: 6 }}>
            可以。在词书页重新选择并确认计划即可。
          </Paragraph>
        </Card>
        <Card size="small">
          <Text strong>是否收费？</Text>
          <Paragraph type="secondary" style={{ marginBottom: 0, marginTop: 6 }}>
            网页学习功能可直接使用；具体词书与能力以产品内说明为准。
          </Paragraph>
        </Card>
      </section>

      <div style={{ marginTop: 8, marginBottom: 32 }}>
        <Button type="primary" size="large" onClick={() => navigate('/word/login')}>
          立即登录
        </Button>
      </div>
    </div>
  );
}
