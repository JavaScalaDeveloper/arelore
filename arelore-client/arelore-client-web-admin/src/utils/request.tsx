import axios, { AxiosInstance, AxiosResponse } from 'axios';
import { message } from 'antd';

/** 已在拦截器中提示过的错误，避免 CRA 开发态全屏 Uncaught overlay */
export class ApiRequestError extends Error {
  readonly notified = true;

  constructor(msg: string) {
    super(msg);
    this.name = 'ApiRequestError';
  }
}

function extractErrorMessage(error: any): string {
  if (!error) {
    return '请求失败';
  }
  if (typeof error === 'string') {
    return error;
  }
  if (error instanceof ApiRequestError) {
    return error.message;
  }
  const fromData = error?.response?.data?.message
    || error?.response?.data?.msg
    || error?.message;
  if (fromData && typeof fromData === 'string' && fromData !== 'Network Error') {
    return fromData;
  }
  if (error?.code === 'ECONNABORTED') {
    return '请求超时，请稍后重试';
  }
  if (error?.message === 'Network Error' || error?.code === 'ERR_NETWORK') {
    return '无法连接后端服务，请检查服务是否已启动';
  }
  if (typeof error?.message === 'string' && error.message) {
    return error.message;
  }
  return '请求失败';
}

function rejectWithToast(error: any): Promise<never> {
  if (error instanceof ApiRequestError && error.notified) {
    return Promise.reject(error);
  }
  const msg = extractErrorMessage(error);
  message.error(msg);
  return Promise.reject(new ApiRequestError(msg));
}

const request: AxiosInstance = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL,
  timeout: 60000,
});

request.interceptors.request.use(
  (config: any) => {
    const token = localStorage.getItem('adminToken');
    const adminUser = localStorage.getItem('adminUser');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    if (adminUser) {
      config.headers['X-Current-User'] = adminUser;
    }
    return config;
  },
  (error: any) => rejectWithToast(error)
);

request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data;

    if (res && typeof res === 'object' && 'code' in res && res.code !== 200) {
      if (res.code === 401 || res.code === 2001 || res.code === 2004 || res.code === 2005) {
        localStorage.removeItem('adminToken');
        window.location.href = '/login';
      }
      return rejectWithToast(new Error(res.message || '请求失败'));
    }

    return res;
  },
  (error: any) => {
    if (error?.response?.status === 401) {
      localStorage.removeItem('adminToken');
      window.location.href = '/login';
    }
    return rejectWithToast(error);
  }
);

export default request;
