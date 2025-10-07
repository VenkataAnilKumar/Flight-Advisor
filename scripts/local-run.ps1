<#
Local helper script to run tests, build, and start the Flight-Advisor app on Windows

Usage (run from PowerShell in repo root or from this file path):
  Set-ExecutionPolicy Bypass -Scope Process -Force; .\scripts\local-run.ps1

What it does:
- stops any running java processes
- removes local H2 DB files that commonly cause file-locks
- runs mvnw test (batch mode)
- if tests pass, packages the jar (skip tests) and starts it in background
- polls the info endpoint and prints the JSON response
- tails logs/ files when something fails

Logs are written to ./logs/app.out and ./logs/app.err
#>

$ErrorActionPreference = 'Stop'

function Write-Log($msg) {
    $t = Get-Date -Format 'yyyy-MM-dd HH:mm:ss'
    Write-Host "[$t] $msg"
}

try {
    $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
    Set-Location (Join-Path $scriptDir '..')
    $root = Get-Location
    Write-Log "Working directory: $root"

    if (!(Test-Path .\logs)) { New-Item -ItemType Directory -Path .\logs | Out-Null }

    Write-Log 'Stopping java processes (if any)...'
    $jps = Get-Process -Name java -ErrorAction SilentlyContinue
    if ($jps) {
        $jps | ForEach-Object { try { Stop-Process -Id $_.Id -Force -ErrorAction Stop; Write-Log "Stopped java pid: $($_.Id)" } catch { Write-Log "Could not stop java pid: $($_.Id): $($_.Exception.Message)" } }
    } else { Write-Log 'No java processes found.' }

    Write-Log 'Removing local H2 DB files to avoid file locks...'
    $dbFiles = @('.\db\flightDB.mv.db', '.\db\flightDB.trace.db')
    foreach ($f in $dbFiles) {
        if (Test-Path $f) { Remove-Item $f -Force -ErrorAction SilentlyContinue; Write-Log "Removed $f" } else { Write-Log "$f not found" }
    }

    Write-Log 'Running mvnw test (batch mode)'
    $testCmd = '.\mvnw.cmd -B test'
    $testExit = & .\mvnw.cmd -B test
    if ($LASTEXITCODE -ne 0) {
        Write-Log "Tests failed with exit code $LASTEXITCODE. Collecting last lines of surefire reports and logs..."
        if (Test-Path .\target\surefire-reports) {
            Get-ChildItem -Path .\target\surefire-reports -Filter '*.txt' -Recurse | Select-Object -Last 5 | ForEach-Object { Write-Log "--- $_.FullName ---"; Get-Content $_.FullName -Tail 200 }
        }
        if (Test-Path .\logs\app.err) { Write-Log 'Last app.err:'; Get-Content .\logs\app.err -Tail 200 }
        exit 1
    }
    Write-Log 'Tests passed.'

    Write-Log 'Packaging jar (skip tests)'
    & .\mvnw.cmd -B -DskipTests package | Out-Null
    if ($LASTEXITCODE -ne 0) { Write-Log "Package failed (exit $LASTEXITCODE)"; exit 2 }

    $jar = Get-ChildItem -Path .\target\*.jar -ErrorAction SilentlyContinue | Where-Object { $_.Name -notmatch 'original' } | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    if ($null -eq $jar) { Write-Log 'Packaged jar not found in target/. Aborting.'; exit 3 }
    Write-Log "Found jar: $($jar.Name)"

    Write-Log 'Starting jar in background (logs -> ./logs/)'
    $proc = Start-Process -FilePath 'java' -ArgumentList '-jar', $jar.FullName, '--spring.profiles.active=prod' -RedirectStandardOutput .\logs\app.out -RedirectStandardError .\logs\app.err -PassThru
    Start-Sleep -Seconds 2
    Write-Log "Started java pid: $($proc.Id)"

    $url = 'http://localhost:8090/flight/service/api/v1/info/version'
    Write-Log "Polling $url (timeout 60s)"
    $success = $false
    for ($i=0;$i -lt 30;$i++) {
        try {
            $r = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 5
            Write-Log 'App responded:'
            $r | ConvertTo-Json
            $success = $true
            break
        } catch {
            Start-Sleep -Seconds 2
        }
    }

    if (-not $success) {
        Write-Log 'App did not respond in time. Showing last 200 lines of logs (app.out and app.err)'
        if (Test-Path .\logs\app.out) { Write-Log '--- app.out ---'; Get-Content .\logs\app.out -Tail 200 }
        if (Test-Path .\logs\app.err) { Write-Log '--- app.err ---'; Get-Content .\logs\app.err -Tail 200 }
        Write-Log 'You can inspect logs in ./logs and rerun this script after fixing issues.'
        exit 4
    }

    Write-Log 'All steps completed successfully.'
    exit 0

} catch {
    Write-Host "ERROR: $($_.Exception.Message)"
    exit 10
}
