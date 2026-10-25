/**
 * 全站共用登录态：墨鱼背词与官网其它模块共用同一套 token。
 * - token / userToken 互为别名（历史兼容）
 * - word_current_user / currentUser 互为别名
 */

const KEY_TOKEN = 'token';
const KEY_USER_TOKEN = 'userToken';
const KEY_WORD_USER = 'word_current_user';
const KEY_CURRENT_USER = 'currentUser';

export function getAuthToken(): string {
  return localStorage.getItem(KEY_TOKEN) || localStorage.getItem(KEY_USER_TOKEN) || '';
}

export function setAuthToken(token: string | null | undefined): void {
  if (token) {
    localStorage.setItem(KEY_TOKEN, token);
    localStorage.setItem(KEY_USER_TOKEN, token);
  } else {
    localStorage.removeItem(KEY_TOKEN);
    localStorage.removeItem(KEY_USER_TOKEN);
  }
}

export function getAuthUserRaw(): string | null {
  return localStorage.getItem(KEY_WORD_USER) || localStorage.getItem(KEY_CURRENT_USER);
}

export function setAuthUser(user: unknown): void {
  if (user == null) {
    localStorage.removeItem(KEY_WORD_USER);
    localStorage.removeItem(KEY_CURRENT_USER);
    return;
  }
  const raw = JSON.stringify(user);
  localStorage.setItem(KEY_WORD_USER, raw);
  localStorage.setItem(KEY_CURRENT_USER, raw);
}

export function clearAuthSession(): void {
  localStorage.removeItem(KEY_TOKEN);
  localStorage.removeItem(KEY_USER_TOKEN);
  localStorage.removeItem(KEY_WORD_USER);
  localStorage.removeItem(KEY_CURRENT_USER);
}

/** 启动时把历史单 key 登录态补齐到双 key，便于跨模块复用 */
export function syncAuthSession(): void {
  const token = getAuthToken();
  if (token) {
    setAuthToken(token);
  }
  const userRaw = getAuthUserRaw();
  if (userRaw) {
    localStorage.setItem(KEY_WORD_USER, userRaw);
    localStorage.setItem(KEY_CURRENT_USER, userRaw);
  }
}
