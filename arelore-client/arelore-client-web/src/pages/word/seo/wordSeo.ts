/** 墨鱼背词 SEO 文案与结构化数据（标签页标题刻意中性，避免旁人看出在背单词） */

export const WORD_BRAND = '墨鱼背词';

/** 浏览器标签 / OG 标题：对外中性，不出现英语/背词/听写等字眼 */
export const WORD_SEO = {
  title: '墨鱼工作台',
  shortTitle: '墨鱼',
  description:
    '墨鱼背词是面向碎片时间的英语单词学习工具，支持看词识意、听力跟读与听写默写。可在通勤、排队、讲座间隙用听觉练习巩固词汇与拼写，配合词书与复习计划稳步提升单词量。',
  keywords: [
    '墨鱼背词',
    '英语单词学习',
    '在线背单词',
    '单词听写',
    '听力背单词',
    '碎片时间学英语',
    '英语词汇记忆',
    '看词识意',
    '单词复习计划',
    '英语拼写练习',
  ].join(','),
  path: '/word',
};

export function absoluteWordUrl(origin?: string): string {
  const base = (origin || (typeof window !== 'undefined' ? window.location.origin : '')).replace(/\/$/, '');
  return `${base}${WORD_SEO.path}`;
}

export function buildWordJsonLd(origin?: string) {
  const url = absoluteWordUrl(origin);
  return {
    '@context': 'https://schema.org',
    '@graph': [
      {
        '@type': 'WebApplication',
        name: WORD_BRAND,
        alternateName: ['墨鱼听写背词', 'Arelore Word', '英语单词学习'],
        url,
        applicationCategory: 'EducationalApplication',
        operatingSystem: 'Web Browser',
        description: WORD_SEO.description,
        inLanguage: ['zh-CN', 'en'],
        offers: {
          '@type': 'Offer',
          price: '0',
          priceCurrency: 'CNY',
        },
        featureList: [
          '看词识意（图文释义）',
          '听力跟读与拼读',
          '听写默写',
          '词书与每日复习计划',
          '美音 / 英音切换',
        ],
      },
      {
        '@type': 'WebPage',
        name: WORD_SEO.title,
        description: WORD_SEO.description,
        url,
        inLanguage: 'zh-CN',
        isPartOf: {
          '@type': 'WebSite',
          name: 'Arelore',
          url: origin || '',
        },
        about: {
          '@type': 'Thing',
          name: '英语词汇学习',
        },
      },
      {
        '@type': 'HowTo',
        name: '如何用墨鱼背词开始学习',
        description: '三步开始背单词：登录、选词书、选择学习模式。',
        step: [
          {
            '@type': 'HowToStep',
            name: '登录账号',
            text: '使用邮箱验证码登录墨鱼背词。',
          },
          {
            '@type': 'HowToStep',
            name: '选择单词书',
            text: '在词书页挑选词本并确认每日新学与复习计划。',
          },
          {
            '@type': 'HowToStep',
            name: '开始练习',
            text: '在设置中选择看词识意、听力练习或听写模式，进入学习页开始。',
          },
        ],
      },
      {
        '@type': 'FAQPage',
        mainEntity: [
          {
            '@type': 'Question',
            name: '墨鱼背词适合什么人？',
            acceptedAnswer: {
              '@type': 'Answer',
              text:
                '适合希望利用碎片时间积累英语词汇的学习者，尤其偏好听力、听写，或希望减少长时间盯屏的用户。',
            },
          },
          {
            '@type': 'Question',
            name: '有哪些学习模式？',
            acceptedAnswer: {
              '@type': 'Answer',
              text:
                '支持看词识意（看单词回忆词义）、听力练习（听单词与释义）、听写默写（听音拼写）。可在设置中切换。',
            },
          },
          {
            '@type': 'Question',
            name: '支持美音和英音吗？',
            acceptedAnswer: {
              '@type': 'Answer',
              text: '支持。学习过程中可切换美音与英音发音，便于对照口音差异。',
            },
          },
          {
            '@type': 'Question',
            name: '听写模式怎么用？',
            acceptedAnswer: {
              '@type': 'Answer',
              text:
                '播放单词发音后，用键盘或字母键拼写；拼对进入下一词，拼错会提示，也可标记不认识后查看正确答案。',
            },
          },
          {
            '@type': 'Question',
            name: '需要安装 App 吗？',
            acceptedAnswer: {
              '@type': 'Answer',
              text: '网页即可使用，打开浏览器访问即可开始学习，无需单独安装客户端。',
            },
          },
        ],
      },
    ],
  };
}
