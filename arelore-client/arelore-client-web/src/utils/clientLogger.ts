import request from './request';

type ClientLogLevel = 'INFO' | 'WARN' | 'ERROR';

export const reportClientLog = async (
  level: ClientLogLevel,
  message: string,
  detail?: string
): Promise<void> => {
  // 同步打印，便于微信内嵌页用 vConsole 排查（异步上报失败时仍能看到）
  const line = `[CLIENT_LOG ${level}] ${message}${detail ? ` | ${detail}` : ''}`;
  if (level === 'ERROR') {
    // eslint-disable-next-line no-console
    console.error(line);
  } else if (level === 'WARN') {
    // eslint-disable-next-line no-console
    console.warn(line);
  } else {
    // eslint-disable-next-line no-console
    console.info(line);
  }
  try {
    await request.post('/user/client-log/report', {
      level,
      message,
      detail: detail || '',
      pageUrl: window.location.href,
      userAgent: navigator.userAgent
    });
  } catch (error) {
    // 避免日志上报影响业务流程
  }
};
