import request from '../utils/request';
import { ApiResponse } from '../types';

export type PublishStatus = 'DRAFT' | 'PRODUCING' | 'PUBLISHED' | 'OFFLINE';
export type PromptTagType = 'CONFIG' | 'MANUAL';

export interface AiPromptItem {
  id?: number;
  idStr?: string;
  promptUuid?: string;
  title?: string;
  promptText?: string;
  configTags?: string;
  manualTags?: string;
  configTagList?: string[];
  manualTagList?: string[];
  /** 配置+人工合并，兼容展示 */
  tagList?: string[];
  summary?: string;
  publishStatus?: PublishStatus | string;
  status?: string;
  extInfo?: string;
  createTime?: string;
  modifyTime?: string;
}

export interface AiPromptTag {
  id?: number;
  idStr?: string;
  tagType?: PromptTagType | string;
  name?: string;
  color?: string;
  sortOrder?: number;
  status?: string;
}

export interface AiPromptPageResult {
  list?: AiPromptItem[];
  records?: AiPromptItem[];
  pageNum?: number;
  pageSize?: number;
  total?: number;
}

export interface AiPromptImportResult {
  created?: number;
  updated?: number;
  skipped?: number;
  message?: string;
}

export const promptApi = {
  page: (data: Record<string, unknown>): Promise<ApiResponse<AiPromptPageResult>> =>
    request.post('/user/prompt/page', data),

  list: (data: Record<string, unknown> = {}): Promise<ApiResponse<AiPromptItem[]>> =>
    request.post('/user/prompt/list', data),

  detail: (data: { id: number }): Promise<ApiResponse<AiPromptItem>> =>
    request.post('/user/prompt/detail', data),

  save: (data: AiPromptItem): Promise<ApiResponse<AiPromptItem>> =>
    request.post('/user/prompt/save', data),

  delete: (data: { id: number }): Promise<ApiResponse<null>> =>
    request.post('/user/prompt/delete', data),

  importBatch: (data: { items: AiPromptItem[] }): Promise<ApiResponse<AiPromptImportResult>> =>
    request.post('/user/prompt/import', data),

  exportList: (data: Record<string, unknown> = {}): Promise<ApiResponse<AiPromptItem[]>> =>
    request.post('/user/prompt/export', data),

  tagList: (data: Record<string, unknown> = {}): Promise<ApiResponse<AiPromptTag[]>> =>
    request.post('/user/prompt/tag/list', data),

  tagSave: (data: AiPromptTag): Promise<ApiResponse<AiPromptTag>> =>
    request.post('/user/prompt/tag/save', data),

  tagDelete: (data: { id: number }): Promise<ApiResponse<null>> =>
    request.post('/user/prompt/tag/delete', data),
};

/** 生成去横线 32 位 uuid */
export function newPromptUuid(): string {
  if (typeof crypto !== 'undefined' && crypto.randomUUID) {
    return crypto.randomUUID().replace(/-/g, '');
  }
  return 'xxxxxxxxxxxx4xxxyxxxxxxxxxxxxxxx'.replace(/[xy]/g, (c) => {
    const r = (Math.random() * 16) | 0;
    const v = c === 'x' ? r : (r & 0x3) | 0x8;
    return v.toString(16);
  });
}
