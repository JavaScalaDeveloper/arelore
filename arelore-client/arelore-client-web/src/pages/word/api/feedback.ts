import request, { ApiResponse } from '../utils/request';

export const feedbackApi = {
  page: (data?: any): Promise<ApiResponse<any>> =>
    request.post('/user/feedback/ticket/page', data || {}) as any,
  detail: (data: { id: number }): Promise<ApiResponse<any>> =>
    request.post('/user/feedback/ticket/detail', data) as any,
  create: (data: {
    productCode: string;
    contentText?: string;
    files?: { fileId: number; fileName?: string; contentType?: string }[];
  }): Promise<ApiResponse<any>> => request.post('/user/feedback/ticket/create', data) as any,
  reply: (data: {
    id: number;
    contentText?: string;
    files?: { fileId: number; fileName?: string; contentType?: string }[];
  }): Promise<ApiResponse<any>> => request.post('/user/feedback/ticket/reply', data) as any,
  close: (data: { id: number }): Promise<ApiResponse<any>> =>
    request.post('/user/feedback/ticket/close', data) as any,
  upload: (data: {
    contentBase64: string;
    fileName?: string;
    contentType?: string;
  }): Promise<ApiResponse<any>> =>
    request.post('/user/feedback/file/upload', data, { timeout: 120000 } as any) as any,
  fileContent: (data: { fileId: number }): Promise<ApiResponse<any>> =>
    request.post('/user/feedback/file/content', data, { timeout: 120000 } as any) as any,
};
