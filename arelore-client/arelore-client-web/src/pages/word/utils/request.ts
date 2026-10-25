import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { buildLocalePath, getLocaleFromLocation, stripLocalePrefix } from '../../../locale/path';
import { clearAuthSession, getAuthToken } from '../../../utils/authSession';

export interface ApiResponse<T = any> {
  code: number;
  message: string;
  data: T;
}

const request: AxiosInstance = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL || '',
  timeout: 30000,
});

function wordLoginHref() {
  return buildLocalePath(getLocaleFromLocation(), '/word/login');
}

function isOnWordLogin() {
  return stripLocalePrefix(window.location.pathname).startsWith('/word/login');
}

request.interceptors.request.use((config: any) => {
  const token = getAuthToken();
  if (token) {
    config.headers.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
  }
  config.headers['X-Locale'] = getLocaleFromLocation();
  return config;
});

request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data;
    if (res && typeof res === 'object' && 'code' in res && res.code !== 200) {
      if (res.code === 401 || res.code === 2001) {
        clearAuthSession();
        if (!isOnWordLogin()) {
          window.location.href = wordLoginHref();
        }
      }
      return Promise.reject(Object.assign(new Error(res.message || '请求失败'), { code: res.code, data: res }));
    }
    return res;
  },
  (error: any) => {
    if (error?.response?.status === 401) {
      clearAuthSession();
      if (!isOnWordLogin()) {
        window.location.href = wordLoginHref();
      }
    }
    const network = !error?.response;
    return Promise.reject(
      Object.assign(new Error(network ? '网络不通，请检查后端是否启动' : error?.message || '请求失败'), {
        isNetworkError: network,
        raw: error,
      })
    );
  }
);

export default request;

export function shortErrorMessage(err: any, fallback = '请求失败'): string {
  if (err?.isNetworkError && err.message) {
    return err.message;
  }
  if (err?.message) {
    return String(err.message);
  }
  return fallback;
}
