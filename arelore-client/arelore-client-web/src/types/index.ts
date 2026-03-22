import { MenuProps } from 'antd';

export interface UserInfo {
  id: string;
  username?: string;
  nickname?: string;
  avatar?: string;
  email?: string;
  status?: number;
}

export interface LoginResponse {
  token: string;
  user: UserInfo;
}

export interface QrCodeResponse {
  qrCodeUrl: string;
  sceneId: string;
  expireSeconds: number;
}

export interface QrCodeStatusResponse {
  status: 'WAIT' | 'SCANED' | 'CONFIRMED' | 'EXPIRED';
  userInfo?: UserInfo;
  code?: string;
}

export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data?: T;
}

export type MenuItem = Required<MenuProps>['items'][number];
