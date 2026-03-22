export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data?: T;
}

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
