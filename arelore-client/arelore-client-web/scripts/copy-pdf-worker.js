/**
 * 将 pdfjs-dist 的 worker 复制到 public，供浏览器加载（与 pdfjs-dist 版本一致）。
 */
const fs = require('fs');
const path = require('path');

const root = path.join(__dirname, '..');
const src = path.join(root, 'node_modules', 'pdfjs-dist', 'build', 'pdf.worker.min.js');
const dest = path.join(root, 'public', 'pdf.worker.min.js');

if (!fs.existsSync(src)) {
  console.warn('[copy-pdf-worker] 跳过：未找到', src);
  process.exit(0);
}
fs.mkdirSync(path.dirname(dest), { recursive: true });
fs.copyFileSync(src, dest);
console.log('[copy-pdf-worker] 已复制到 public/pdf.worker.min.js');
