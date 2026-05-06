/**
 * 软考刷题目录与「试卷列表」页共用：顺序决定 paperIdx 与接口 subject 字符串。
 * 使用 .js 避免 babel-loader 对部分 .ts 未走 TypeScript 解析导致编译失败。
 */
export const SOFT_EXAM_CATALOG = [
  { level: '高级', items: ['信息系统项目管理师', '系统分析师', '系统架构设计师'] },
  { level: '中级', items: ['软件设计师', '网络工程师', '信息系统管理工程师', '系统集成项目管理工程师'] },
  { level: '初级', items: ['程序员', '网络管理员', '信息处理技术员'] }
];

export const SOFT_EXAM_PAPER_ORDER = SOFT_EXAM_CATALOG.flatMap((g) => g.items);

export function softExamSubjectByPaperIndex(index) {
  if (!Number.isFinite(index) || index < 0 || index >= SOFT_EXAM_PAPER_ORDER.length) {
    return null;
  }
  return SOFT_EXAM_PAPER_ORDER[index];
}

export function softExamPaperIndexOfSubject(subject) {
  return SOFT_EXAM_PAPER_ORDER.indexOf(subject);
}
