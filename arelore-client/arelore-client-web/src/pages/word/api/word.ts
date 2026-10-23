import request, { ApiResponse } from '../utils/request';

export interface StudyExample {
  en?: string;
  cn?: string;
}

export interface StudyPhrase {
  en?: string;
  cn?: string;
}

export interface StudySynonymGroup {
  pos?: string;
  tran?: string;
  words?: string[];
}

export interface StudyRelatedItem {
  word?: string;
  tran?: string;
}

export interface StudyRelatedGroup {
  pos?: string;
  words?: StudyRelatedItem[];
}

export interface StudyCard {
  mode?: string;
  wordCode: string;
  word: string;
  phonetic?: string;
  meaning?: string;
  picture?: string;
  pictures?: string[];
  pictureFileIds?: number[];
  /** 已落库释义整段语音 fileId；学习端优先播，失败再走在线 TTS */
  meaningAudioFileId?: number;
  /** 客户端加载后的 data:audio/... URL */
  meaningAudioUrl?: string;
  mnemonic?: string;
  exampleEn?: string;
  exampleCn?: string;
  examples?: StudyExample[];
  phrases?: StudyPhrase[];
  synonyms?: StudySynonymGroup[];
  relatedWords?: StudyRelatedGroup[];
  etym?: string;
}

export interface StudySession {
  bookCode?: string;
  voiceType?: number;
  cards?: StudyCard[];
  learnDone?: number;
  reviewDone?: number;
  learnTotal?: number;
  reviewTotal?: number;
}

export const wordApi = {
  languages: (): Promise<ApiResponse<any[]>> => request.post('/user/word/language/list', {}) as any,
  categories: (languageCode: string): Promise<ApiResponse<any[]>> =>
    request.post('/user/word/category/list', { languageCode }) as any,
  books: (languageCode: string, categoryCode: string): Promise<ApiResponse<any[]>> =>
    request.post('/user/word/book/list', { languageCode, categoryCode }) as any,
  currentBook: (): Promise<ApiResponse<any>> => request.post('/user/word/book/current', {}) as any,
  planGet: (bookCode: string): Promise<ApiResponse<any>> =>
    request.post('/user/word/plan/get', { bookCode }) as any,
  planConfirm: (data: {
    bookCode: string;
    newReviewRatio: string;
    dailyNewCount: number;
    extInfo?: string;
  }): Promise<ApiResponse<any>> => request.post('/user/word/plan/confirm', data) as any,
  session: (): Promise<ApiResponse<StudySession>> => request.post('/user/word/study/session', {}) as any,
  answer: (data: {
    bookCode?: string;
    wordCode: string;
    mode?: string;
    rememberFlag: boolean;
  }): Promise<ApiResponse<any>> => request.post('/user/word/study/answer', data) as any,
  stats: (): Promise<ApiResponse<any>> => request.post('/user/word/study/stats', {}) as any,
  pictureContent: (id: number): Promise<ApiResponse<{ contentType?: string; base64Data?: string }>> =>
    request.post('/user/word/picture-content', { id }) as any,
};
