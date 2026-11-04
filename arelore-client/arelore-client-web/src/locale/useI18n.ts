import { useCallback, useMemo } from 'react';
import { AppLocale } from './constants';
import { useLocaleOptional } from './LocaleContext';
import { getMessages, MessageTree } from './messages';

type Vars = Record<string, string | number>;

function format(template: string, vars?: Vars): string {
  if (!vars) {
    return template;
  }
  return template.replace(/\{(\w+)\}/g, (_, key: string) =>
    vars[key] == null ? '' : String(vars[key])
  );
}

function readPath(tree: MessageTree, path: string): string | undefined {
  const parts = path.split('.');
  let cur: any = tree;
  for (const p of parts) {
    if (cur == null || typeof cur !== 'object') {
      return undefined;
    }
    cur = cur[p];
  }
  return typeof cur === 'string' ? cur : undefined;
}

export function useI18n() {
  const { locale } = useLocaleOptional();
  const messages = useMemo(() => getMessages(locale), [locale]);

  const t = useCallback(
    (path: string, vars?: Vars) => {
      const raw = readPath(messages, path);
      if (raw == null) {
        return path;
      }
      return format(raw, vars);
    },
    [messages]
  );

  return { locale, t, messages };
}

/** 非 Hook：按 locale 取文案（catalog / 工具函数用） */
export function translate(locale: AppLocale, path: string, vars?: Vars): string {
  const raw = readPath(getMessages(locale), path);
  if (raw == null) {
    return path;
  }
  return format(raw, vars);
}
