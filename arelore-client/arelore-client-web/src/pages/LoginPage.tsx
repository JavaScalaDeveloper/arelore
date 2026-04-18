import React, { useState, useEffect, useCallback } from 'react';
import { Modal, Tabs, QRCode, Button, message, Space, Result } from 'antd';
import { WechatOutlined, QrcodeOutlined, LoginOutlined } from '@ant-design/icons';
import { authApi } from '../api/auth';
import { UserInfo } from '../types';

interface WechatLoginModalProps {
  visible: boolean;
  onCancel: () => void;
  onLoginSuccess: (user: UserInfo) => void;
}

interface QrCodeData {
  qrCodeUrl: string;
  sceneId: string;
  expireSeconds: number;
}

const { TabPane } = Tabs;

/**
 * 微信登录弹窗组件
 */
const WechatLoginModal: React.FC<WechatLoginModalProps> = ({ visible, onCancel, onLoginSuccess }) => {
  const [activeTab, setActiveTab] = useState<string>('qrcode');
  const [qrCodeData, setQrCodeData] = useState<QrCodeData | null>(null);
  const [loading, setLoading] = useState<boolean>(false);
  const [scanStatus, setScanStatus] = useState<string>('waiting'); // waiting, scanned, expired

  // 获取微信扫码二维码
  useEffect(() => {
    if (visible && activeTab === 'qrcode') {
      loadQrCode();
    }
  }, [visible, activeTab]);

  // 轮询检查扫码状态
  // 加载二维码
  const loadQrCode = async () => {
    try {
      setLoading(true);
      const response = await authApi.getWechatQrCode();
      if (response.code === 200 && response.data) {
        setQrCodeData(response.data);
        setScanStatus('waiting');
      }
    } catch (error) {
      console.error('获取二维码失败:', error);
      message.error('获取二维码失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  // 处理登录成功
  const handleLoginSuccess = useCallback(async (userInfo: UserInfo, code: string) => {
    try {
      // 调用后端登录接口
      const response = await authApi.wechatQuickLogin({
        code: code,
        userInfo: userInfo
      });

      if (response.code === 200 && response.data) {
        const { token, user } = response.data;
        
        // 保存 token 和用户信息
        localStorage.setItem('userToken', token);
        localStorage.setItem('currentUser', JSON.stringify(user));
        
        message.success('登录成功！');
        onLoginSuccess(user);
        onCancel();
      }
    } catch (error) {
      console.error('登录失败:', error);
      message.error('登录失败，请重试');
    }
  }, [onCancel, onLoginSuccess]);

  // 检查二维码状态
  const checkQrCodeStatus = useCallback(async () => {
    if (!qrCodeData) {
      return;
    }
    try {
      const response = await authApi.checkWechatQrCodeStatus({
        sceneId: qrCodeData.sceneId
      });
      
      if (response.code === 200 && response.data) {
        const { status, userInfo, code } = response.data;
        
        if (status === 'SCANED' || status === 'CONFIRMED') {
          // 用户已扫码并确认
          handleLoginSuccess(userInfo!, code!);
          setScanStatus('scanned');
        } else if (status === 'EXPIRED') {
          // 二维码已过期
          setScanStatus('expired');
          message.warning('二维码已过期，请刷新重试');
        }
      }
    } catch (error) {
      console.error('检查二维码状态失败:', error);
    }
  }, [handleLoginSuccess, qrCodeData]);

  useEffect(() => {
    let interval: NodeJS.Timeout | null = null;
    if (visible && qrCodeData && scanStatus !== 'expired') {
      interval = setInterval(() => {
        checkQrCodeStatus();
      }, 2000); // 每 2 秒检查一次
    }
    return () => {
      if (interval) clearInterval(interval);
    };
  }, [visible, qrCodeData, scanStatus, checkQrCodeStatus]);

  // 一键登录（模拟）
  const handleQuickLogin = async () => {
    try {
      setLoading(true);
      // 这里应该调用微信 JS-SDK 获取授权码
      // 现在模拟一个登录流程
      const mockUserInfo: UserInfo = {
        id: '1',
        username: '微信用户',
        avatar: 'https://wx.qlogo.cn/mmopen/vi_32/DEFAULT',
        nickname: '微信用户'
      };

      const mockToken = 'mock_jwt_token_' + Date.now();

      // 保存 token 和用户信息
      localStorage.setItem('userToken', mockToken);
      localStorage.setItem('currentUser', JSON.stringify(mockUserInfo));

      message.success('登录成功！');
      onLoginSuccess(mockUserInfo);
      onCancel();
    } catch (error) {
      console.error('一键登录失败:', error);
      message.error('登录失败，请重试');
    } finally {
      setLoading(false);
    }
  };

  // 刷新二维码
  const handleRefreshQrCode = () => {
    loadQrCode();
  };

  return (
    <Modal
      title={
        <Space>
          <WechatOutlined style={{ color: '#07c160', fontSize: '24px' }} />
          <span style={{ fontSize: '18px' }}>微信登录</span>
        </Space>
      }
      open={visible}
      onCancel={onCancel}
      footer={null}
      width={400}
      centered
    >
      <Tabs activeKey={activeTab} onChange={setActiveTab}>
        <TabPane 
          tab={
            <span>
              <QrcodeOutlined />
              扫码登录
            </span>
          } 
          key="qrcode"
        >
          <div style={{ textAlign: 'center', padding: '20px' }}>
            {loading ? (
              <div style={{ padding: '40px 0' }}>
                <p>加载中...</p>
              </div>
            ) : scanStatus === 'expired' ? (
              <Result
                status="warning"
                title="二维码已过期"
                extra={
                  <Button type="primary" onClick={handleRefreshQrCode}>
                    刷新二维码
                  </Button>
                }
              />
            ) : qrCodeData ? (
              <div>
                <QRCode
                  value={qrCodeData.qrCodeUrl}
                  size={256}
                  icon="https://wx.qlogo.cn/mmopen/vi_32/DEFAULT"
                  iconSize={40}
                />
                <div style={{ marginTop: '16px', color: '#666' }}>
                  <p>请使用微信扫一扫登录</p>
                  {scanStatus === 'scanned' && (
                    <p style={{ color: '#52c41a' }}>已扫码，请确认登录</p>
                  )}
                </div>
              </div>
            ) : (
              <div style={{ padding: '40px 0' }}>
                <p>无法加载二维码</p>
                <Button type="primary" onClick={handleRefreshQrCode}>
                  重新加载
                </Button>
              </div>
            )}
          </div>
        </TabPane>

        <TabPane 
          tab={
            <span>
              <LoginOutlined />
              一键登录
            </span>
          } 
          key="quick"
        >
          <div style={{ textAlign: 'center', padding: '40px 20px' }}>
            <Result
              icon={<WechatOutlined style={{ color: '#07c160', fontSize: '64px' }} />}
              title="微信一键登录"
              subTitle={
                <div>
                  <p style={{ marginBottom: '24px' }}>
                    点击下方按钮，使用微信快速登录
                  </p>
                  <Button
                    type="primary"
                    size="large"
                    icon={<WechatOutlined />}
                    loading={loading}
                    onClick={handleQuickLogin}
                    style={{ 
                      backgroundColor: '#07c160',
                      borderColor: '#07c160',
                      width: '200px'
                    }}
                  >
                    微信一键登录
                  </Button>
                </div>
              }
            />
          </div>
        </TabPane>
      </Tabs>

      <div style={{ 
        marginTop: '16px', 
        paddingTop: '16px', 
        borderTop: '1px solid #f0f0f0',
        textAlign: 'center',
        color: '#999',
        fontSize: '12px'
      }}>
        登录即表示您同意我们的服务条款和隐私政策
      </div>
    </Modal>
  );
};

export default WechatLoginModal;
