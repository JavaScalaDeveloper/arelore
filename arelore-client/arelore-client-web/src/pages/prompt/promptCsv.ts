import { AiPromptItem } from '../../api/prompt';

const HEADERS = [
  'promptUuid',
  'title',
  'summary',
  'promptText',
  'configTags',
  'manualTags',
  'publishStatus',
] as const;

function escapeCsvCell(value: string): string {
  const v = value ?? '';
  if (/[",\r\n]/.test(v)) {
    return `"${v.replace(/"/g, '""')}"`;
  }
  return v;
}

function tagsToCell(list?: string[], json?: string): string {
  if (list && list.length) {
    return list.join('|');
  }
  return parseTagsField(json).join('|');
}

/** 导出为 UTF-8 BOM CSV；manualTags 仅备份展示，再导入时不会覆盖人工标签 */
export function promptsToCsv(items: AiPromptItem[]): string {
  const lines = [HEADERS.join(',')];
  for (const item of items) {
    const row = [
      item.promptUuid || '',
      item.title || '',
      item.summary || '',
      item.promptText || '',
      tagsToCell(item.configTagList, item.configTags),
      tagsToCell(item.manualTagList, item.manualTags),
      item.publishStatus || 'DRAFT',
    ].map(escapeCsvCell);
    lines.push(row.join(','));
  }
  return `\uFEFF${lines.join('\r\n')}`;
}

export function downloadCsv(filename: string, csv: string) {
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}

function parseTagsField(raw?: string): string[] {
  if (!raw) return [];
  const t = raw.trim();
  if (!t) return [];
  if (t.startsWith('[')) {
    try {
      const arr = JSON.parse(t);
      return Array.isArray(arr) ? arr.map(String).filter(Boolean) : [];
    } catch {
      // fallthrough
    }
  }
  return t.split(/[|;，、]/).map((s) => s.trim()).filter(Boolean);
}

/** 简易 CSV 解析（支持引号转义） */
function parseCsvRows(text: string): string[][] {
  const src = text.replace(/^\uFEFF/, '');
  const rows: string[][] = [];
  let row: string[] = [];
  let cell = '';
  let inQuotes = false;
  for (let i = 0; i < src.length; i++) {
    const ch = src[i];
    const next = src[i + 1];
    if (inQuotes) {
      if (ch === '"' && next === '"') {
        cell += '"';
        i++;
      } else if (ch === '"') {
        inQuotes = false;
      } else {
        cell += ch;
      }
      continue;
    }
    if (ch === '"') {
      inQuotes = true;
      continue;
    }
    if (ch === ',') {
      row.push(cell);
      cell = '';
      continue;
    }
    if (ch === '\n' || (ch === '\r' && next === '\n')) {
      row.push(cell);
      cell = '';
      if (row.some((c) => c.trim() !== '')) {
        rows.push(row);
      }
      row = [];
      if (ch === '\r') i++;
      continue;
    }
    if (ch === '\r') {
      row.push(cell);
      cell = '';
      if (row.some((c) => c.trim() !== '')) {
        rows.push(row);
      }
      row = [];
      continue;
    }
    cell += ch;
  }
  row.push(cell);
  if (row.some((c) => c.trim() !== '')) {
    rows.push(row);
  }
  return rows;
}

/**
 * 解析 CSV 为导入条目。
 * 只取 configTags（兼容旧列名 importTags/tags）；manualTags 列即使存在也会被服务端忽略。
 */
export function csvToPrompts(text: string): AiPromptItem[] {
  const rows = parseCsvRows(text);
  if (rows.length < 2) {
    return [];
  }
  const header = rows[0].map((h) => h.trim());
  const idx = (name: string) => {
    const i = header.findIndex((h) => h.toLowerCase() === name.toLowerCase());
    return i;
  };
  const iUuid = idx('promptUuid');
  const iTitle = idx('title');
  const iSummary = idx('summary');
  const iText = idx('promptText');
  let iConfig = idx('configTags');
  if (iConfig < 0) {
    iConfig = idx('importTags');
  }
  if (iConfig < 0) {
    iConfig = idx('tags');
  }
  const iStatus = idx('publishStatus');
  if (iTitle < 0 && iText < 0) {
    throw new Error('CSV 表头缺少 title / promptText');
  }

  const items: AiPromptItem[] = [];
  for (let r = 1; r < rows.length; r++) {
    const cols = rows[r];
    const get = (i: number) => (i >= 0 && i < cols.length ? cols[i] : '');
    const title = get(iTitle);
    const promptText = get(iText);
    if (!title.trim() && !promptText.trim()) {
      continue;
    }
    items.push({
      promptUuid: get(iUuid).trim() || undefined,
      title: title.trim(),
      summary: get(iSummary),
      promptText,
      configTagList: parseTagsField(get(iConfig)),
      publishStatus: (get(iStatus).trim() || 'DRAFT') as AiPromptItem['publishStatus'],
    });
  }
  return items;
}
