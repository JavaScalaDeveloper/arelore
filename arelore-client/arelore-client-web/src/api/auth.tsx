import request from '../utils/request';
import { ApiResponse, QrCodeResponse, QrCodeStatusResponse, LoginResponse } from '../types';

/**
 * 用户认证相关 API
 */
export const authApi = {
  /**
   * 微信一键登录
   * @param code - 微信授权码
   */
  wechatQuickLogin: (data: { code: string; userInfo?: any }): Promise<ApiResponse<LoginResponse>> => {
    return request.post('/user/auth/wechat/quick', data);
  },

  /**
   * 获取微信扫码二维码
   */
  getWechatQrCode: (): Promise<ApiResponse<QrCodeResponse>> => {
    return request.post('/user/auth/wechat/qrcode', {});
  },

  /**
   * 轮询检查微信扫码状态
   * @param sceneId - 场景 ID
   */
  checkWechatQrCodeStatus: (data: { sceneId: string }): Promise<ApiResponse<QrCodeStatusResponse>> => {
    return request.post('/user/auth/wechat/qrcode/check', data);
  },

  /**
   * 退出登录
   */
  logout: (): Promise<ApiResponse<void>> => {
    return request.post('/user/auth/logout', {});
  },

  /**
   * 获取当前用户信息
   */
  getCurrentUser: (): Promise<ApiResponse<any>> => {
    return request.post('/user/current', {});
  },
};
