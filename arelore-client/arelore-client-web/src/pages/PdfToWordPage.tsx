import React, { useState } from 'react';
import { Card, Upload, Button, Space, message, Typography, Alert } from 'antd';
import { UploadOutlined, DownloadOutlined, FileWordOutlined } from '@ant-design/icons';
import { extractTextFromPdf, buildDocxBlobFromPlainText } from '../utils/pdfToWord';

const { Title, Paragraph } = Typography;

const MAX_MB = 32;

const PdfToWordPage: React.FC = () => {
  const [pdfFile, setPdfFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [lastBlob, setLastBlob] = useState<Blob | null>(null);
  const [outName, setOutName] = useState<string>('');

  const beforeUpload = (file: File) => {
    const isPdf =
      file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf');
    if (!isPdf) {
      message.error('请上传 PDF 文件');
      return Upload.LIST_IGNORE;
    }
    if (file.size / 1024 / 1024 > MAX_MB) {
      message.error(`文件不能超过 ${MAX_MB}MB`);
      return Upload.LIST_IGNORE;
    }
    setPdfFile(file);
    setLastBlob(null);
    return false;
  };

  const readFileAsArrayBuffer = (file: File): Promise<ArrayBuffer> =>
    new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result as ArrayBuffer);
      reader.onerror = () => reject(reader.error);
      reader.readAsArrayBuffer(file);
    });

  const handleConvert = async () => {
    const f = pdfFile;
    if (!f) {
      message.warning('请先选择 PDF 文件');
      return;
    }
    setLoading(true);
    setLastBlob(null);
    try {
      const buf = await readFileAsArrayBuffer(f);
      const text = await extractTextFromPdf(buf);
      const base = f.name.replace(/\.pdf$/i, '') || 'export';
      const docTitle = `${base}（由 PDF 提取文本生成）`;
      const blob = await buildDocxBlobFromPlainText(text, docTitle);
      setLastBlob(blob);
      setOutName(`${base}.docx`);
      message.success('已生成 Word，可点击下载');
    } catch (e) {
      console.error(e);
      message.error('转换失败，请确认 PDF 未加密且浏览器支持 Web Worker');
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = () => {
    if (!lastBlob || !outName) return;
    const url = URL.createObjectURL(lastBlob);
    const a = document.createElement('a');
    a.href = url;
    a.download = outName;
    a.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div className="app-content-wrapper" style={{ padding: 24, maxWidth: 880, margin: '0 auto' }}>
      <Title level={2}>
        <FileWordOutlined style={{ marginRight: 8 }} />
        PDF 转 Word
      </Title>
      <Paragraph type="secondary">
        在浏览器本地解析 PDF 文本并生成 .docx，文件不会上传到服务器。纯扫描件、图片型 PDF
        可能几乎无文字，请使用带文字层的 PDF 或专业 OCR 工具。
      </Paragraph>

      <Alert
        type="info"
        showIcon
        style={{ marginBottom: 16 }}
        message="说明"
        description="本功能将 PDF 中的可选中文字导出为 Word；版式、图片、表格无法完整保留。首次使用需确保已执行 npm install（会自动同步 pdf.js worker 到 public）。"
      />

      <Card>
        <Space direction="vertical" size="middle" style={{ width: '100%' }}>
          <Upload
            accept=".pdf,application/pdf"
            fileList={
              pdfFile
                ? [
                    {
                      uid: '-1',
                      name: pdfFile.name,
                      status: 'done',
                    },
                  ]
                : []
            }
            beforeUpload={beforeUpload}
            onRemove={() => {
              setPdfFile(null);
              setLastBlob(null);
              setOutName('');
            }}
            maxCount={1}
          >
            <Button icon={<UploadOutlined />}>选择 PDF</Button>
          </Upload>

          <Space wrap>
            <Button type="primary" loading={loading} onClick={handleConvert}>
              转换为 Word
            </Button>
            <Button
              icon={<DownloadOutlined />}
              disabled={!lastBlob}
              onClick={handleDownload}
            >
              下载 .docx
            </Button>
          </Space>
        </Space>
      </Card>
    </div>
  );
};

export default PdfToWordPage;
