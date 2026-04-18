import request from './request';

type ClientLogLevel = 'INFO' | 'WARN' | 'ERROR';

export const reportClientLog = async (
  level: ClientLogLevel,
  message: string,
  detail?: string
): Promise<void> => {
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
