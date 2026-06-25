/**
 * NCRE 刷题目录与「试卷列表」页共用：顺序决定 paperIdx 与接口 subject 字符串。
 */
export const NCRE_EXAM_CATALOG = [
  {
    level: '一级',
    items: ['计算机基础及WPS Office应用', '计算机基础及MS Office应用']
  },
  {
    level: '二级',
    items: [
      'C语言程序设计',
      'Java语言程序设计',
      'Python语言程序设计',
      'Web程序设计',
      'MS Office高级应用'
    ]
  },
  {
    level: '三级',
    items: ['网络技术', '数据库技术', '信息安全技术', '嵌入式系统开发技术']
  },
  {
    level: '四级',
    items: ['网络工程师', '数据库工程师', '信息安全工程师', '嵌入式系统开发工程师']
  }
];

export const NCRE_EXAM_PAPER_ORDER = NCRE_EXAM_CATALOG.flatMap((g) => g.items);

export function ncreSubjectByPaperIndex(index) {
  if (!Number.isFinite(index) || index < 0 || index >= NCRE_EXAM_PAPER_ORDER.length) {
    return null;
  }
  return NCRE_EXAM_PAPER_ORDER[index];
}

export function ncrePaperIndexOfSubject(subject) {
  return NCRE_EXAM_PAPER_ORDER.indexOf(subject);
}
