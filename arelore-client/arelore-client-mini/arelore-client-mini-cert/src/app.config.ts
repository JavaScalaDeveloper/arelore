export default defineAppConfig({
  pages: [
    'pages/home/index',
    'pages/quiz/index',
    'pages/profile/index',
    'pages/quiz-paper-list/index',
    'pages/quiz-do/index',
    'pages/quiz-result/index',
    'pages/exam-history/index',
    'pages/favorites/index',
    'pages/favorite-detail/index',
    'pages/login/index'
  ],
  window: {
    navigationBarTitleText: '考证宝',
    navigationBarTextStyle: 'black',
    navigationBarBackgroundColor: '#ffffff',
    backgroundColor: '#f5f7fb'
  },
  tabBar: {
    custom: true,
    color: '#666666',
    selectedColor: '#07c160',
    backgroundColor: '#ffffff',
    borderStyle: 'white',
    list: [
      { pagePath: 'pages/home/index', text: '首页' },
      { pagePath: 'pages/quiz/index', text: '刷题' },
      { pagePath: 'pages/profile/index', text: '个人' }
    ]
  }
});

