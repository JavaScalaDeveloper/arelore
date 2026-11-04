const fs = require('fs');
const p = require('path').join(__dirname, '../src/pages/word/pages/LoginPage.tsx');
let s = fs.readFileSync(p, 'utf8');

const reps = [
  ["message.success('登录成功')", "message.success(t('word.login.loginOk'))"],
  ['message.warning(`请 ${left} 秒后再获取验证码`)', "message.warning(t('word.login.codeWait', { n: left }))"],
  ["message.success('验证码已发送，请查收邮件（含垃圾箱）')", "message.success(t('word.login.codeSent'))"],
  ["message.error(shortErrorMessage(e, '发送失败'))", "message.error(shortErrorMessage(e, t('word.login.sendFailed')))"],
  ["message.error('当前环境无法完成密码摘要，请更换浏览器后重试')", "message.error(t('word.login.hashFailed'))"],
  ["message.error('登录失败：未返回 token')", "message.error(t('word.login.noTokenLogin'))"],
  ["message.error('注册失败：未返回 token')", "message.error(t('word.login.noTokenRegister'))"],
  ["message.success('注册成功')", "message.success(t('word.login.registerOk'))"],
  [
    "message.success(getToken() ? '密码已更新' : '密码已更新，请使用新密码登录')",
    "message.success(getToken() ? t('word.login.passwordUpdated') : t('word.login.passwordUpdatedLogin'))",
  ],
  [
    "message.error(shortErrorMessage(e, mode === 'register' ? '注册失败' : mode === 'reset' ? '改密失败' : '登录失败'))",
    "message.error(shortErrorMessage(e, mode === 'register' ? t('word.login.registerFailed') : mode === 'reset' ? t('word.login.resetFailed') : t('word.login.loginFailed')))",
  ],
];

for (const [a, b] of reps) {
  if (!s.includes(a)) console.log('MISS', a.slice(0, 80));
  else s = s.split(a).join(b);
}

const uiOld = `  const titleMap: Record<Mode, string> = {
    password: '邮箱密码登录',
    code: '验证码登录',
    register: '邮箱注册',
    reset: '找回 / 设置密码',
  };

  const tipMap: Record<Mode, string> = {
    password: '使用邮箱作为账号；未设密码请先找回/设置密码',
    code: '验证码登录可自动注册；无密码账号请之后设置密码',
    register: '验证邮箱后设置密码，注册成功即登录',
    reset: '已注册用户（含无密码）可通过验证码设置新密码',
  };`;

const uiNew = `  const titleMap: Record<Mode, string> = {
    password: t('word.login.titlePassword'),
    code: t('word.login.titleCode'),
    register: t('word.login.titleRegister'),
    reset: t('word.login.titleReset'),
  };

  const tipMap: Record<Mode, string> = {
    password: t('word.login.tipPassword'),
    code: t('word.login.tipCode'),
    register: t('word.login.tipRegister'),
    reset: t('word.login.tipReset'),
  };`;

if (!s.includes(uiOld)) console.log('MISS titleMap block');
else s = s.replace(uiOld, uiNew);

const formReps = [
  ["{ key: 'password', label: '密码登录' }", "{ key: 'password', label: t('word.login.tabPassword') }"],
  ["{ key: 'code', label: '验证码登录' }", "{ key: 'code', label: t('word.login.tabCode') }"],
  ['label="邮箱"', 'label={t(\'word.login.email\')}'],
  ["{ required: true, message: '请输入邮箱' }", "{ required: true, message: t('word.login.emailRequired') }"],
  ["{ type: 'email', message: '邮箱格式不正确' }", "{ type: 'email', message: t('word.login.emailInvalid') }"],
  ['label="密码"', 'label={t(\'word.login.password\')}'],
  ["{ required: true, message: '请输入密码' }", "{ required: true, message: t('word.login.passwordRequired') }"],
  ["{ min: 8, message: '密码至少 8 位' }", "{ min: 8, message: t('word.login.passwordMin') }"],
  ['placeholder="至少 8 位"', 'placeholder={t(\'word.login.passwordPlaceholder\')}'],
  ['label="验证码" required>', 'label={t(\'word.login.code\')} required>'],
  ["{ required: true, message: '请输入验证码' }", "{ required: true, message: t('word.login.codeRequired') }"],
  ["{ pattern: /^\\d{4,8}$/, message: '验证码为 4～8 位数字' }", "{ pattern: /^\\d{4,8}$/, message: t('word.login.codePattern') }"],
  ['placeholder="邮件中的验证码"', 'placeholder={t(\'word.login.codePlaceholder\')}'],
  ['{countdown > 0 ? `${countdown}s 后重试` : \'获取验证码\'}', "{countdown > 0 ? t('word.login.retryIn', { n: countdown }) : t('word.login.getCode')}"],
  ['label="设置密码"', 'label={t(\'word.login.setPassword\')}'],
  ["{ required: true, message: '请设置密码' }", "{ required: true, message: t('word.login.passwordRequired') }"],
  ['label="新密码"', 'label={t(\'word.login.newPassword\')}'],
  ["{ required: true, message: '请输入新密码' }", "{ required: true, message: t('word.login.passwordRequired') }"],
  ['label="确认新密码"', 'label={t(\'word.login.confirmPassword\')}'],
  ["{ required: true, message: '请再次输入新密码' }", "{ required: true, message: t('word.login.confirmRequired') }"],
  ["return Promise.reject(new Error('两次输入的密码不一致'));", "return Promise.reject(new Error(t('word.login.confirmMismatch')));"],
  ['placeholder="再次输入"', 'placeholder={t(\'word.login.confirmPlaceholder\')}'],
  [
    "{mode === 'register' ? '注册并登录' : mode === 'reset' ? '更新密码' : '登录'}",
    "{mode === 'register' ? t('word.login.submitRegister') : mode === 'reset' ? t('word.login.submitReset') : t('word.login.submitLogin')}",
  ],
  ['<Link onClick={() => switchMode(\'register\')}>注册账号</Link>', '<Link onClick={() => switchMode(\'register\')}>{t(\'word.login.linkRegister\')}</Link>'],
  ['<Link onClick={() => switchMode(\'reset\')}>忘记密码 / 设置密码</Link>', '<Link onClick={() => switchMode(\'reset\')}>{t(\'word.login.linkForgot\')}</Link>'],
  ['<Link onClick={() => navigate(\'/word/profile\')}>返回我的</Link>', '<Link onClick={() => navigate(\'/word/profile\')}>{t(\'word.login.backProfile\')}</Link>'],
  ['<Link onClick={() => switchMode(\'password\')}>返回登录</Link>', '<Link onClick={() => switchMode(\'password\')}>{t(\'word.login.backLogin\')}</Link>'],
];

for (const [a, b] of formReps) {
  if (!s.includes(a)) console.log('MISS form', a.slice(0, 70));
  else s = s.split(a).join(b);
}

fs.writeFileSync(p, s);
console.log('patched LoginPage');
