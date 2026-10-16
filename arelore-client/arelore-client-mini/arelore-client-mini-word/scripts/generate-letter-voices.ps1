# Regenerate local A-Z letter packs via edge-tts (requires: pip install edge-tts)
# Usage: pwsh ./scripts/generate-letter-voices.ps1

$ErrorActionPreference = "Stop"
$base = Join-Path $PSScriptRoot "..\assets\letters"
$voices = @(
  @{ id = "jenny"; voice = "en-US-JennyNeural"; label = "Jenny 美式女声" },
  @{ id = "guy"; voice = "en-US-GuyNeural"; label = "Guy 美式男声" },
  @{ id = "sonia"; voice = "en-GB-SoniaNeural"; label = "Sonia 英式女声" },
  @{ id = "ryan"; voice = "en-GB-RyanNeural"; label = "Ryan 英式男声" }
)
$letters = 97..122 | ForEach-Object { [char]$_ }

New-Item -ItemType Directory -Force -Path $base | Out-Null
foreach ($v in $voices) {
  $dir = Join-Path $base $v.id
  New-Item -ItemType Directory -Force -Path $dir | Out-Null
  Write-Host "=== $($v.id) ($($v.voice)) ==="
  foreach ($ch in $letters) {
    $out = Join-Path $dir "$ch.mp3"
    $text = ([string]$ch).ToUpper()
    # 语速 0 使用这份原始语速；更高拼读语速由播放倍速加快
    & edge-tts --voice $v.voice --rate="+0%" --text $text --write-media $out
    if ($LASTEXITCODE -ne 0) { throw "edge-tts failed $($v.id)/$ch" }
    Write-Host "ok $($v.id)/$ch"
  }
}
Write-Host "Done. Packs under $base"
