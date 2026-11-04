/** Clone word.en-us.ts into word.<locale>.ts for locales without a hand translation yet. */
const fs = require('fs');
const path = require('path');

const dir = path.join(__dirname, '../src/locale/messages');
const en = fs.readFileSync(path.join(dir, 'word.en-us.ts'), 'utf8');

const locales = [
  'ko-kr', 'es-es', 'fr-fr', 'de-de', 'pt-br', 'ru-ru', 'vi-vn', 'th-th', 'id-id',
  'ar-sa', 'hi-in', 'it-it', 'nl-nl', 'tr-tr', 'pl-pl', 'sv-se', 'uk-ua', 'ms-my',
];

function varName(locale) {
  const camel = locale.replace(/-([a-z])/g, (_, c) => c.toUpperCase());
  return 'word' + camel.charAt(0).toUpperCase() + camel.slice(1);
}

for (const locale of locales) {
  const name = varName(locale);
  const body = en
    .replace(/const wordEnUs:/, `const ${name}:`)
    .replace(/export default wordEnUs;/, `export default ${name};`);
  fs.writeFileSync(path.join(dir, `word.${locale}.ts`), body, 'utf8');
  console.log('wrote word.' + locale + '.ts');
}
