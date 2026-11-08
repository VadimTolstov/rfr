$ErrorActionPreference = 'Stop'
$event = [Console]::In.ReadToEnd() | ConvertFrom-Json
$command = [string]$event.tool_input.command

if ($event.hook_event_name -eq 'PreToolUse' -and $event.tool_name -eq 'Bash') {
  $pattern = '(?im)(?:^|[;&|]\s*|\r?\n\s*)(?:rtk\s+(?:proxy\s+)?)?(?:git|git\.exe)\s+(?:(?:-C(?:\s+(?:"[^"]+"|''[^'']+''|\S+)|\S+))\s+)*push(?:\s|$)'
  if ($command -match $pattern) {
    [ordered]@{
      hookSpecificOutput = [ordered]@{
        hookEventName = 'PreToolUse'
        permissionDecision = 'deny'
        permissionDecisionReason = 'Git push is forbidden, including git -C invocations'
      }
    } | ConvertTo-Json -Compress
  }
  exit 0
}

if ($event.hook_event_name -eq 'PostToolUse' -and $event.tool_name -eq 'apply_patch' -and $command -match '(?im)^\*\*\*\s+(?:(?:Update|Add|Delete) File|Move to):\s+.*\.(?:java|tsx)\s*$') {
  $root = [string]$event.cwd
  $checker = Join-Path $root 'check-code.bat'
  if (-not (Test-Path -LiteralPath $checker)) {
    [Console]::Error.WriteLine('check-code.bat was not found in the project root')
    exit 2
  }
  $preference = $ErrorActionPreference
  $ErrorActionPreference = 'Continue'
  $output = & $env:ComSpec /c "`"$checker`"" 2>&1
  $code = $LASTEXITCODE
  $ErrorActionPreference = $preference
  if ($code -ne 0) {
    [Console]::Error.WriteLine(($output -join [Environment]::NewLine))
    [Console]::Error.WriteLine("check-code.bat failed with exit code $code")
    exit 2
  }
  if ($output.Count -gt 0) {
    [ordered]@{
      hookSpecificOutput = [ordered]@{
        hookEventName = 'PostToolUse'
        additionalContext = $output -join [Environment]::NewLine
      }
    } | ConvertTo-Json -Compress
  }
}

exit 0
