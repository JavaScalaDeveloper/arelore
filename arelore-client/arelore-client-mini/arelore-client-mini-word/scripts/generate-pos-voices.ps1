# 词性中文整段音频（edge-tts）。避免在线 TTS 把「形容词」拆成逐字。
# Usage: pwsh ./scripts/generate-pos-voices.ps1

$ErrorActionPreference = "Stop"
$base = Join-Path $PSScriptRoot "..\assets\pos"
New-Item -ItemType Directory -Force -Path $base | Out-Null

$clips = @(
  @{ file = "n"; text = "名词" },
  @{ file = "v"; text = "动词" },
  @{ file = "vt"; text = "及物动词" },
  @{ file = "vi"; text = "不及物动词" },
  @{ file = "adj"; text = "形容词" },
  @{ file = "adv"; text = "副词" },
  @{ file = "prep"; text = "介词" },
  @{ file = "conj"; text = "连词" },
  @{ file = "pron"; text = "代词" },
  @{ file = "art"; text = "冠词" },
  @{ file = "num"; text = "数词" },
  @{ file = "int"; text = "感叹词" },
  @{ file = "aux"; text = "助动词" },
  @{ file = "modal"; text = "情态动词" },
  @{ file = "det"; text = "限定词" },
  @{ file = "abbr"; text = "缩写" },
  @{ file = "pl"; text = "复数" },
  @{ file = "phrase"; text = "短语" },
  @{ file = "pref"; text = "前缀" },
  @{ file = "suf"; text = "后缀" },
  @{ file = "uncount"; text = "不可数名词" },
  @{ file = "count"; text = "可数名词" },
  @{ file = "link"; text = "系动词" },
  @{ file = "ger"; text = "动名词" },
  @{ file = "part"; text = "分词" },
  @{ file = "inf"; text = "不定式" },
  @{ file = "ord"; text = "序数词" },
  @{ file = "quant"; text = "量词" },
  @{ file = "idiom"; text = "习语" }
)

foreach ($c in $clips) {
  $out = Join-Path $base "$($c.file).mp3"
  & edge-tts --voice "zh-CN-XiaoxiaoNeural" --rate="+0%" --text $c.text --write-media $out
  if ($LASTEXITCODE -ne 0) { throw "edge-tts failed $($c.file)" }
  Write-Host "ok $($c.file) $($c.text)"
}
Write-Host "Done. $base"
