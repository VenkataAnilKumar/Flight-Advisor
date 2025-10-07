<#
run-compose.ps1
Runs docker compose up --build -d for the project, tails logs, and smoke-tests the info endpoint.
Usage (non-elevated is fine if Docker is running):
  Set-ExecutionPolicy Bypass -Scope Process -Force; .\scripts\run-compose.ps1
#>

param(
    [int]$WaitSeconds = 120
)

function Wait-For-ServiceLog {
    param(
        [string]$Service = 'app',
        [string]$Match = 'Started FlightAdvisorApplication',
        [int]$Timeout = 120
    )
    Write-Host "Waiting up to $Timeout seconds for service '$Service' to log: $Match"
    $end = (Get-Date).AddSeconds($Timeout)
    while ((Get-Date) -lt $end) {
        try {
            $logs = docker compose logs --no-color --since 1s $Service 2>$null
            if ($logs -match $Match) { Write-Host "Found match in logs."; return $true }
        } catch { }
        Start-Sleep -Seconds 2
    }
    Write-Host "Timeout waiting for log match."
    return $false
}

Write-Host "Ensure you're in the repository root where docker-compose.yml exists."

# Copy env if missing
if (-not (Test-Path .env)) {
    if (Test-Path .env.example) {
        Copy-Item .env.example .env
        Write-Host "Copied .env.example -> .env. Edit .env if you need to change DB credentials."
    }
}

Write-Host "Running docker compose up --build -d"
docker compose up --build -d

Write-Host "Tailing logs (press Ctrl+C to stop)..."
Start-Sleep -Seconds 3

# Wait for app to start by scanning logs for startup line
$ok = Wait-For-ServiceLog -Service app -Match 'Started FlightAdvisorApplication' -Timeout $WaitSeconds

Write-Host "Attempting to call Info endpoint (http://localhost:8090/flight/service/api/v1/info/version)"
try {
    $r = Invoke-RestMethod -Uri 'http://localhost:8090/flight/service/api/v1/info/version' -TimeoutSec 10 -ErrorAction Stop
    Write-Host "Info endpoint response:"; $r | ConvertTo-Json -Depth 4
} catch {
    Write-Host "Info call failed: $_"; Write-Host "Showing last 200 lines of app logs:"; docker compose logs --no-color --tail 200 app
}

Write-Host "Done. If services are unhealthy, run 'docker compose logs -f' to inspect."
