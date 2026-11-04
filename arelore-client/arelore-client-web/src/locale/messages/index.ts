import { AppLocale, DEFAULT_LOCALE, SUPPORTED_LOCALES } from '../constants';
import arSa from './ar-sa';
import deDe from './de-de';
import enUs from './en-us';
import esEs from './es-es';
import frFr from './fr-fr';
import hiIn from './hi-in';
import idId from './id-id';
import itIt from './it-it';
import jaJp from './ja-jp';
import koKr from './ko-kr';
import msMy from './ms-my';
import nlNl from './nl-nl';
import plPl from './pl-pl';
import ptBr from './pt-br';
import { getPromptMessages } from './promptPacks';
import ruRu from './ru-ru';
import svSe from './sv-se';
import thTh from './th-th';
import trTr from './tr-tr';
import { MessageTree, SiteMessageTree } from './types';
import ukUa from './uk-ua';
import viVn from './vi-vn';
import { getWordMessages } from './wordPacks';
import zhCn from './zh-cn';

/** 官网壳文案包（子应用 word/prompt 另行合并） */
const SITE_PACKS: Record<AppLocale, SiteMessageTree> = {
  'zh-cn': zhCn,
  'en-us': enUs,
  'ja-jp': jaJp,
  'ko-kr': koKr,
  'es-es': esEs,
  'fr-fr': frFr,
  'de-de': deDe,
  'pt-br': ptBr,
  'ru-ru': ruRu,
  'vi-vn': viVn,
  'th-th': thTh,
  'id-id': idId,
  'ar-sa': arSa,
  'hi-in': hiIn,
  'it-it': itIt,
  'nl-nl': nlNl,
  'tr-tr': trTr,
  'pl-pl': plPl,
  'sv-se': svSe,
  'uk-ua': ukUa,
  'ms-my': msMy,
};

for (const locale of SUPPORTED_LOCALES) {
  if (!SITE_PACKS[locale]) {
    throw new Error(`Missing site message pack for locale: ${locale}`);
  }
}

export function getMessages(locale: AppLocale): MessageTree {
  const site = SITE_PACKS[locale] || SITE_PACKS[DEFAULT_LOCALE] || zhCn;
  return {
    ...site,
    word: getWordMessages(locale),
    prompt: getPromptMessages(locale),
  };
}

export type { MessageTree, SiteMessageTree };
