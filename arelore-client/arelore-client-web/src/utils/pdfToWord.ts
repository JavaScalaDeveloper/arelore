import * as pdfjsLib from 'pdfjs-dist';
import { Document, Packer, Paragraph, TextRun } from 'docx';

let workerConfigured = false;

/** 必须在首次调用 getDocument 前执行（同域加载 public/pdf.worker.min.js） */
export function configurePdfJsWorker(): void {
  if (workerConfigured || typeof window === 'undefined') {
    return;
  }
  const base = process.env.PUBLIC_URL || '';
  pdfjsLib.GlobalWorkerOptions.workerSrc = `${base}/pdf.worker.min.js`;
  workerConfigured = true;
}

export async function extractTextFromPdf(data: ArrayBuffer): Promise<string> {
  configurePdfJsWorker();
  const loadingTask = pdfjsLib.getDocument({ data: data.slice(0) });
  const pdf = await loadingTask.promise;
  const parts: string[] = [];

  for (let i = 1; i <= pdf.numPages; i += 1) {
    const page = await pdf.getPage(i);
    const content = await page.getTextContent();
    const line = content.items
      .map((item) => ('str' in item && typeof item.str === 'string' ? item.str : ''))
      .join(' ')
      .replace(/\s+/g, ' ')
      .trim();
    parts.push(`【第 ${i} 页】\n${line || '（本页无可复制文本，可能为扫描图或纯图片 PDF）'}`);
  }

  return parts.join('\n\n');
}

function splitBlocks(text: string, maxLen = 8000): string[] {
  if (!text.trim()) {
    return [];
  }
  const paras = text.split(/\n{2,}/);
  const out: string[] = [];
  for (const p of paras) {
    const t = p.trim();
    if (!t) continue;
    if (t.length <= maxLen) {
      out.push(t);
      continue;
    }
    for (let i = 0; i < t.length; i += maxLen) {
      out.push(t.slice(i, i + maxLen));
    }
  }
  return out;
}

export async function buildDocxBlobFromPlainText(
  text: string,
  title?: string
): Promise<Blob> {
  const blocks = splitBlocks(text);
  const children: Paragraph[] = [];

  if (title?.trim()) {
    children.push(
      new Paragraph({
        children: [
          new TextRun({
            text: title.trim(),
            bold: true,
            size: 28,
          }),
        ],
      })
    );
    children.push(new Paragraph({ children: [] }));
  }

  if (blocks.length === 0) {
    children.push(
      new Paragraph({
        children: [new TextRun('（文档为空）')],
      })
    );
  } else {
    for (const block of blocks) {
      const lines = block.split('\n');
      for (const line of lines) {
        children.push(
          new Paragraph({
            children: [new TextRun(line || ' ')],
          })
        );
      }
      children.push(new Paragraph({ children: [] }));
    }
  }

  const doc = new Document({
    sections: [
      {
        properties: {},
        children,
      },
    ],
  });

  return Packer.toBlob(doc);
}
