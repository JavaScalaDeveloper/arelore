import request, { ApiResponse } from '../utils/request';

export interface QrCodeData {
  qrCodeUrl: string;
  sceneId: string;
  expireSeconds?: number;
}

export interface QrStatusData {
  status?: string;
  userInfo?: any;
  code?: string;
  token?: string;
  user?: any;
}

export interface AuthLoginData {
  token: string;
  user: any;
}

export const authApi = {
  getWechatQrCode: (): Promise<ApiResponse<QrCodeData>> =>
    request.post('/user/auth/wechat/qrcode', {}) as any,

  checkWechatQrCodeStatus: (sceneId: string): Promise<ApiResponse<QrStatusData>> =>
    request.post('/user/auth/wechat/qrcode/check', { sceneId }) as any,

  wechatQuickLogin: (body: { code: string; userInfo: any }): Promise<ApiResponse<AuthLoginData>> =>
    request.post('/user/auth/wechat/quick', body) as any,

  applyEmailCode: (email: string): Promise<ApiResponse<string>> =>
    request.post('/user/auth/email/code/apply', { email }) as any,

  emailCodeLogin: (body: { email: string; code: string }): Promise<ApiResponse<AuthLoginData>> =>
    request.post('/user/auth/email/code/login', body) as any,

  applyEmailRegisterCode: (email: string): Promise<ApiResponse<string>> =>
    request.post('/user/auth/email/register/apply', { email }) as any,

  confirmEmailRegister: (body: {
    email: string;
    code: string;
    passwordHash: string;
  }): Promise<ApiResponse<AuthLoginData>> => request.post('/user/auth/email/register/confirm', body) as any,

  emailPasswordLogin: (body: { email: string; passwordHash: string }): Promise<ApiResponse<AuthLoginData>> =>
    request.post('/user/auth/email/login', body) as any,

  applyEmailResetPasswordCode: (email: string): Promise<ApiResponse<string>> =>
    request.post('/user/auth/email/password-reset/apply', { email }) as any,

  confirmEmailResetPassword: (body: {
    email: string;
    code: string;
    newPasswordHash: string;
  }): Promise<ApiResponse<string>> => request.post('/user/auth/email/password-reset/confirm', body) as any,

  logout: (): Promise<ApiResponse<void>> => request.post('/user/auth/logout', {}) as any,

  current: (): Promise<ApiResponse<any>> => request.post('/user/auth/current', {}) as any,
};
