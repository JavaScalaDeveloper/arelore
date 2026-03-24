import React, { useState } from 'react';
import { Card, Checkbox, Button, Space, message, Typography, Upload, Progress } from 'antd';
import { UploadOutlined, EyeInvisibleOutlined, ClearOutlined } from '@ant-design/icons';
import request, { ApiResponse } from '../utils/request';

const { Title } = Typography;

// 敏感信息类型定义
interface SensitiveType {
  label: string;
  value: string;
}

// 敏感类型配置
const SENSITIVE_TYPES: SensitiveType[] = [
  { label: '中国手机号', value: 'china_phone' },
  { label: '国际手机号', value: 'intl_phone' },
  { label: '地址', value: 'address' },
  { label: '汉语姓名', value: 'chinese_name' },
  { label: '英文姓名', value: 'english_name' },
  { label: '邮箱', value: 'email' },
  { label: '身份证号', value: 'id_card' },
  { label: '银行卡号', value: 'bank_card' },
  { label: '护照号码', value: 'passport' },
  { label: 'IP 地址', value: 'ip_address' },
  { label: 'URL 链接', value: 'url' },
  { label: '车牌号', value: 'license_plate' },
];

// 支持的文件类型
const ACCEPTED_FILE_TYPES = {
  'image/*': ['.jpg', '.jpeg', '.png', '.gif', '.bmp'],
  'application/msword': ['.doc'],
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
};

// 敏感类型信息接口
interface SensitiveTypeInfo {
  code: string;
  label: string;
  example: string;
}

const FileDetectionPage: React.FC = () => {
  const [selectedTypes, setSelectedTypes] = useState<string[]>(['china_phone', 'address', 'chinese_name', 'email', 'id_card']);
  const [fileList, setFileList] = useState<any[]>([]);
  const [isProcessing, setIsProcessing] = useState<boolean>(false);
  const [progress, setProgress] = useState<number>(0);
  const [sensitiveTypeInfoList, setSensitiveTypeInfoList] = useState<SensitiveTypeInfo[]>([]);
  const [detectionResult, setDetectionResult] = useState<string>('');

  // 文件上传前校验
  const beforeUpload = (file: File) => {
    const isImage = file.type.startsWith('image/');
    const isWord = file.type === 'application/msword' || file.type === 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
    
    if (!isImage && !isWord) {
      message.error('仅支持图片和Word文档格式');
      return Upload.LIST_IGNORE;
    }
    
    const isLt10M = file.size / 1024 / 1024 < 10;
    if (!isLt10M) {
      message.error('文件大小不能超过10MB');
      return Upload.LIST_IGNORE;
    }
    
    return false; // 阻止自动上传
  };

  // 文件选择
  const handleFileChange = (info: any) => {
    const { fileList } = info;
    setFileList(fileList);
  };

  // 清除文件
  const handleClearFile = () => {
    setFileList([]);
    setDetectionResult('');
    setSensitiveTypeInfoList([]);
    message.success('已清除文件');
  };

  // 调用后端 API 进行文件处理
  const handleProcess = async () => {
    if (fileList.length === 0) {
      message.warning('请先上传文件');
      return;
    }

    if (selectedTypes.length === 0) {
      message.warning('请至少选择一种敏感类型');
      return;
    }

    setIsProcessing(true);
    setProgress(0);

    try {
      const formData = new FormData();
      formData.append('file', fileList[0].originFileObj);
      formData.append('sensitiveTypes', JSON.stringify(selectedTypes));

      const response: ApiResponse<any> = await request.post('/user/file-detection/detect', formData, {
        onUploadProgress: (progressEvent) => {
          const percentCompleted = Math.round((progressEvent.loaded * 100) / (progressEvent.total || 1));
          setProgress(percentCompleted);
        }
      });

      if (response && response.code === 200) {
        setDetectionResult(response.data.detectionResult || '');
        setSensitiveTypeInfoList(response.data.sensitiveTypeInfoList || []);
        message.success('文件处理完成');
      } else {
        message.error(response?.message || '处理失败');
      }
    } catch (error: any) {
      message.error(error.response?.data?.message || error.message || '处理失败，请重试');
      console.error('处理错误:', error);
    } finally {
      setIsProcessing(false);
    }
  };

  // 上传组件配置
  const uploadProps = {
    name: 'file',
    multiple: false,
    fileList,
    beforeUpload,
    onChange: handleFileChange,
    accept: '.jpg,.jpeg,.png,.gif,.bmp,.doc,.docx',
    showUploadList: {
      showPreviewIcon: true,
      showRemoveIcon: true,
    },
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '24px' }}>
      <Card style={{ marginBottom: '24px' }}>
        <Title level={2} style={{ textAlign: 'center', marginBottom: '32px' }}>
          <EyeInvisibleOutlined style={{ marginRight: '8px', color: '#1890ff' }} />
          文件敏感内容检测
        </Title>

        {/* 敏感类型选择 */}
        <div style={{ marginBottom: '24px' }}>
          <Title level={5} style={{ marginBottom: '12px' }}>
            敏感信息类型 <span style={{ color: '#999', fontSize: '12px' }}>（多选）</span>
          </Title>
          <Checkbox.Group
            value={selectedTypes}
            onChange={(values) => setSelectedTypes(values as string[])}
            style={{ width: '100%' }}
          >
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '16px' }}>
              {SENSITIVE_TYPES.map(type => (
                <Checkbox 
                  key={type.value} 
                  value={type.value}
                >
                  {type.label}
                </Checkbox>
              ))}
            </div>
          </Checkbox.Group>
        </div>

        {/* 文件上传 */}
        <div style={{ marginBottom: '24px' }}>
          <Title level={5} style={{ marginBottom: '12px' }}>上传文件</Title>
          <Space style={{ width: '100%' }}>
            <Upload {...uploadProps}>
              <Button icon={<UploadOutlined />} size="large">
                选择文件（支持图片、Word文档）
              </Button>
            </Upload>
            <Button 
              icon={<ClearOutlined />} 
              size="large" 
              danger
              onClick={handleClearFile}
              disabled={fileList.length === 0}
            >
              清除文件
            </Button>
          </Space>
          <div style={{ marginTop: '12px', fontSize: '12px', color: '#999' }}>
            支持格式：JPG、PNG、GIF、BMP、DOC、DOCX（最大10MB）
          </div>
        </div>

        {/* 处理按钮 */}
        <div style={{ textAlign: 'center', marginBottom: '24px' }}>
          <Button
            type="primary"
            size="large"
            onClick={handleProcess}
            loading={isProcessing}
            icon={<EyeInvisibleOutlined />}
            style={{ minWidth: '200px' }}
            disabled={fileList.length === 0 || selectedTypes.length === 0}
          >
            立即处理
          </Button>
        </div>

        {/* 进度条 */}
        {isProcessing && (
          <div style={{ marginBottom: '24px' }}>
            <Progress percent={progress} status="active" />
          </div>
        )}

        {/* 检测结果 */}
        {(detectionResult || sensitiveTypeInfoList.length > 0) && (
          <div>
            <Title level={5} style={{ marginBottom: '16px' }}>检测结果</Title>
            
            {/* 敏感类型信息展示 */}
            {sensitiveTypeInfoList.length > 0 && (
              <div style={{ 
                marginBottom: '16px', 
                padding: '12px', 
                backgroundColor: '#f0f5ff', 
                borderRadius: '4px',
                fontSize: '13px',
                lineHeight: '1.8'
              }}>
                <div style={{ fontWeight: 'bold', marginBottom: '8px', color: '#1890ff' }}>
                  检测到的敏感信息：
                </div>
                {sensitiveTypeInfoList.map((info, index) => (
                  <div key={index} style={{ marginBottom: '4px' }}>
                    {info.label}：{info.example}
                  </div>
                ))}
              </div>
            )}
            
            {/* 检测结果文本 */}
            {detectionResult && (
              <div style={{ 
                padding: '16px', 
                backgroundColor: '#fafafa', 
                borderRadius: '4px',
                fontSize: '14px',
                lineHeight: '1.8',
                whiteSpace: 'pre-wrap',
                fontFamily: 'monospace'
              }}>
                {detectionResult}
              </div>
            )}
          </div>
        )}
      </Card>
    </div>
  );
};

export default FileDetectionPage;