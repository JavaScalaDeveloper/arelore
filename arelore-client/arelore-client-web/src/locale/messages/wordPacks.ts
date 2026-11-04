import { AppLocale } from '../constants';
import wordArSa from './word.ar-sa';
import wordDeDe from './word.de-de';
import wordEnUs from './word.en-us';
import wordEsEs from './word.es-es';
import wordFrFr from './word.fr-fr';
import wordHiIn from './word.hi-in';
import wordIdId from './word.id-id';
import wordItIt from './word.it-it';
import wordJaJp from './word.ja-jp';
import wordKoKr from './word.ko-kr';
import wordMsMy from './word.ms-my';
import wordNlNl from './word.nl-nl';
import wordPlPl from './word.pl-pl';
import wordPtBr from './word.pt-br';
import wordRuRu from './word.ru-ru';
import wordSvSe from './word.sv-se';
import wordThTh from './word.th-th';
import wordTrTr from './word.tr-tr';
import wordUkUa from './word.uk-ua';
import wordViVn from './word.vi-vn';
import wordZhCn from './word.zh-cn';
import { WordMessages } from './wordTypes';

const PACKS: Record<AppLocale, WordMessages> = {
  'zh-cn': wordZhCn,
  'en-us': wordEnUs,
  'ja-jp': wordJaJp,
  'ko-kr': wordKoKr,
  'es-es': wordEsEs,
  'fr-fr': wordFrFr,
  'de-de': wordDeDe,
  'pt-br': wordPtBr,
  'ru-ru': wordRuRu,
  'vi-vn': wordViVn,
  'th-th': wordThTh,
  'id-id': wordIdId,
  'ar-sa': wordArSa,
  'hi-in': wordHiIn,
  'it-it': wordItIt,
  'nl-nl': wordNlNl,
  'tr-tr': wordTrTr,
  'pl-pl': wordPlPl,
  'sv-se': wordSvSe,
  'uk-ua': wordUkUa,
  'ms-my': wordMsMy,
};

export function getWordMessages(locale: AppLocale): WordMessages {
  if (locale.startsWith('zh')) {
    return PACKS['zh-cn'];
  }
  return PACKS[locale] || PACKS['en-us'];
}
