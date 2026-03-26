import React, { useState } from 'react';
import { Card, Checkbox, Radio, Button, Space, message, Typography, Select, Input } from 'antd';
import { CopyOutlined, ClearOutlined, EyeInvisibleOutlined } from '@ant-design/icons';
import request, { ApiResponse } from '../utils/request';

const { Title } = Typography;
const { TextArea } = Input;

// 敏感信息类型定义
interface SensitiveType {
  label: string;
  value: string;
  pattern?: RegExp;
  disabled?: boolean;
  example?: string; // 样例文本
}

// 敏感类型配置
const SENSITIVE_TYPES: SensitiveType[] = [
  { label: '中国手机号', value: 'china_phone', example: '13812345678' },
  { label: '国际手机号', value: 'intl_phone', example: '+86-138-1234-5678' },
  { label: '地址', value: 'address', example: '北京市朝阳区建国路 88 号' },
  { label: '汉语姓名', value: 'chinese_name', example: '张三' },
  { label: '英文姓名', value: 'english_name', example: 'John Smith' },
  { label: '邮箱', value: 'email', example: 'zhangsan@email.com' },
  { label: '身份证号', value: 'id_card', example: '110101199001011234' },
  { label: '银行卡号', value: 'bank_card', example: '6222 0218 0920 0000' },
  { label: '护照号码', value: 'passport', example: 'E12345678' },
  { label: 'IP 地址', value: 'ip_address', example: '192.168.1.1' },
  { label: 'URL 链接', value: 'url', example: 'https://www.example.com/path' },
  { label: '车牌号', value: 'license_plate', example: '京 A12345' },
  { label: '自定义正则表达式', value: 'custom_regex', disabled: true }, // 不可选中项
];

// 脱敏方式
const MASK_METHODS = [
  { label: '全部替换为*', value: 'full_mask' },
  { label: '保留部分', value: 'keep_partially', default: true },
];

const MAX_LENGTH = 50000; // 最大字符数

// 敏感类型信息接口
interface SensitiveTypeInfo {
  code: string;
  label: string;
  example: string;
}

const TextMaskingPage: React.FC = () => {
  const [inputType, setInputType] = useState<'text' | 'file'>('text');
  const [selectedTypes, setSelectedTypes] = useState<string[]>(['china_phone', 'address', 'chinese_name', 'email', 'id_card']);
  const [maskMethod, setMaskMethod] = useState<string>('keep_partially'); // 默认选中"保留部分"
  const [inputText, setInputText] = useState<string>('');
  const [outputText, setOutputText] = useState<string>('');
  const [isProcessing, setIsProcessing] = useState<boolean>(false);
  const [sensitiveTypeInfoList, setSensitiveTypeInfoList] = useState<SensitiveTypeInfo[]>([]);

  // 处理粘贴
  const handlePaste = async () => {
    try {
      const text = await navigator.clipboard.readText();
      if (text.length > MAX_LENGTH) {
        message.warning(`文本过长，已自动截取前${MAX_LENGTH}个字符`);
        setInputText(text.substring(0, MAX_LENGTH));
      } else {
        setInputText(text);
      }
      message.success('粘贴成功');
    } catch (error) {
      message.error('粘贴失败，请手动输入');
    }
  };

  // 清除文本
  const handleClear = () => {
    setInputText('');
    setOutputText('');
    setSensitiveTypeInfoList([]);
    message.success('已清除');
  };

  // 调用后端 API 进行脱敏处理
  const handleProcess = async () => {
    if (!inputText.trim()) {
      message.warning('请输入需要处理的文本');
      return;
    }

    if (selectedTypes.length === 0) {
      message.warning('请至少选择一种敏感类型');
      return;
    }

    setIsProcessing(true);

    try {
      const response: ApiResponse<any> = await request.post('/user/text-mask/mask', {
        text: inputText,
        sensitiveTypes: selectedTypes,
        maskMethod: maskMethod
      });

      // response 已经是后端返回的 ApiResponse 对象（拦截器处理过）
      if (response && response.code === 200) {
        setOutputText(response.data.maskedText);
        setSensitiveTypeInfoList(response.data.sensitiveTypeInfoList || []);
        message.success(`处理完成，共处理 ${response.data.processedCount} 个字符`);
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

  // 复制结果
  const handleCopyResult = async () => {
    if (!outputText) {
      message.warning('没有可复制的内容');
      return;
    }

    try {
      await navigator.clipboard.writeText(outputText);
      message.success('复制成功');
    } catch (error) {
      message.error('复制失败');
    }
  };

  return (
    <div style={{ maxWidth: '1200px', margin: '0 auto', padding: '24px' }}>
      <Card style={{ marginBottom: '24px' }}>
        <Title level={2} style={{ textAlign: 'center', marginBottom: '32px' }}>
          <EyeInvisibleOutlined style={{ marginRight: '8px', color: '#1890ff' }} />
          文本脱敏工具
        </Title>

        {/* 敏感类型选择 */}
        <div style={{ marginBottom: '24px' }}>
          <Title level={5} style={{ marginBottom: '12px' }}>
            敏感信息类型 <span style={{ color: '#999', fontSize: '12px' }}>（多选，自定义正则表达式暂不可用）</span>
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
                  disabled={type.disabled}
                  style={{ opacity: type.disabled ? 0.5 : 1 }}
                >
                  {type.label}{type.disabled && '（开发中）'}
                </Checkbox>
              ))}
            </div>
          </Checkbox.Group>
        </div>

        {/* 输入类型选择 */}
        <div style={{ marginBottom: '24px', display: 'flex', alignItems: 'center' }}>
          <Title level={5} style={{ margin: 0, marginRight: '16px' }}>输入方式</Title>
          <Radio.Group value={inputType} onChange={(e) => setInputType(e.target.value)}>
            <Radio.Button value="text">文本</Radio.Button>
            <Radio.Button value="file" disabled>文件（暂不可选）</Radio.Button>
          </Radio.Group>
        </div>

        {/* 输入区域 */}
        <div style={{ marginBottom: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
            <Title level={5} style={{ margin: 0 }}>输入文本</Title>
            <Space>
              <Button 
                icon={<CopyOutlined />} 
                size="small" 
                onClick={handlePaste}
              >
                粘贴
              </Button>
              <Button 
                icon={<ClearOutlined />} 
                size="small" 
                danger
                onClick={handleClear}
              >
                清除
              </Button>
            </Space>
          </div>
          <div style={{ position: 'relative' }}>
            <TextArea
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder="请输入需要脱敏的文本内容..."
              rows={8}
              maxLength={MAX_LENGTH}
              showCount
              style={{ fontFamily: 'monospace', fontSize: '14px' }}
            />
            <div style={{ 
              position: 'absolute', 
              right: '12px', 
              bottom: '32px',
              color: inputText.length > MAX_LENGTH * 0.9 ? '#ff4d4f' : '#999',
              fontSize: '12px'
            }}>
              {inputText.length} / {MAX_LENGTH.toLocaleString()}
            </div>
          </div>
        </div>

        {/* 脱敏方式选择 */}
        {/* <div style={{ marginBottom: '24px', display: 'flex', alignItems: 'center' }}>
          <Title level={5} style={{ margin: 0, marginRight: '16px' }}>脱敏方式</Title>
          <Radio.Group 
            value={maskMethod} 
            onChange={(e) => setMaskMethod(e.target.value)}
            buttonStyle="solid"
          >
            <Radio.Button value="full_mask" disabled>全部替换为*</Radio.Button>
            <Radio.Button value="keep_partially">保留部分</Radio.Button>
          </Radio.Group>
        </div> */}

        {/* 处理按钮 */}
        <div style={{ textAlign: 'center', marginBottom: '24px' }}>
          <Button
            type="primary"
            size="large"
            onClick={handleProcess}
            loading={isProcessing}
            icon={<EyeInvisibleOutlined />}
            style={{ minWidth: '200px' }}
          >
            立即处理
          </Button>
        </div>

        {/* 输出区域 */}
        {outputText && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <Title level={5} style={{ margin: 0 }}>处理结果</Title>
              <Button 
                icon={<CopyOutlined />} 
                size="small" 
                onClick={handleCopyResult}
              >
                复制结果
              </Button>
            </div>
            
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
                  敏感信息类型：
                </div>
                {sensitiveTypeInfoList.map((info, index) => (
                  <div key={index} style={{ marginBottom: '4px' }}>
                    {info.label}：{info.example}
                  </div>
                ))}
              </div>
            )}
            
            <TextArea
              value={outputText}
              readOnly
              rows={8}
              showCount
              style={{ 
                fontFamily: 'monospace', 
                fontSize: '14px',
                backgroundColor: '#fafafa'
              }}
            />
          </div>
        )}

        {/* 敏感类型样例展示 */}
        <div style={{ marginTop: '32px', paddingTop: '24px', borderTop: '1px solid #e8e8e8' }}>
          <Title level={5} style={{ marginBottom: '16px', color: '#666' }}>
            📋 敏感类型样例参考（点击可快速填充）
          </Title>
          <div style={{ 
            display: 'grid', 
            gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', 
            gap: '12px' 
          }}>
            {SENSITIVE_TYPES.filter(t => !t.disabled && t.example).map(type => (
              <Card 
                key={type.value}
                hoverable
                size="small"
                onClick={() => {
                  setInputText(prev => prev ? prev + '\n' + (type.example || '') : (type.example || ''));
                  message.success(`已添加${type.label}样例到输入框`);
                }}
                style={{ 
                  cursor: 'pointer',
                  border: '1px solid #e8e8e8',
                  transition: 'all 0.3s'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ fontWeight: 500, color: '#333' }}>{type.label}</span>
                  <span style={{ 
                    fontSize: '12px', 
                    color: '#1890ff',
                    fontFamily: 'monospace',
                    backgroundColor: '#e6f7ff',
                    padding: '2px 8px',
                    borderRadius: '4px'
                  }}>
                    {type.example}
                  </span>
                </div>
              </Card>
            ))}
          </div>
        </div>
      </Card>
    </div>
  );
};

export default TextMaskingPage;
