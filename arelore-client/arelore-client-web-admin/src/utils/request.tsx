import axios, { AxiosInstance, AxiosResponse } from 'axios';

// 创建 axios 实例
const request: AxiosInstance = axios.create({
  baseURL: process.env.REACT_APP_API_BASE_URL,
  timeout: 10000, // 请求超时时间
});

// 请求拦截器
request.interceptors.request.use(
  (config: any) => {
    // 从 localStorage 获取 token
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
  (error: any) => {
    console.error('请求错误:', error);
    return Promise.reject(error);
  }
);

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data;
    
    // 如果返回的状态码不是 200，说明接口有错误
    if (res.code !== 200) {
      console.error('接口错误:', res.message);
      
      // 如果是未授权，跳转到登录页
      if (res.code === 401 || res.code === 2001 || res.code === 2004 || res.code === 2005) {
        localStorage.removeItem('adminToken');
        window.location.href = '/login';
      }
      
      return Promise.reject(new Error(res.message || '请求失败'));
    }
    
    return res;
  },
  (error: any) => {
    console.error('网络错误:', error);
    
    if (error.response) {
      // 服务器返回错误响应
      const { status, data } = error.response;
      
      if (status === 401) {
        localStorage.removeItem('adminToken');
        window.location.href = '/login';
      }
      
      return Promise.reject(data || error);
    } else if (error.request) {
      // 请求已发送但没有收到响应
      console.error('未收到响应，请检查网络连接');
    }
    
    return Promise.reject(error);
  }
);

export default request;
