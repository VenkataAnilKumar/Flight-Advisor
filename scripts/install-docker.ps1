<#
install-docker.ps1
Automates installation of Docker Desktop on Windows using winget or chocolatey if available,
otherwise downloads the official installer and runs it.

USAGE (run as Administrator PowerShell):
  Set-ExecutionPolicy Bypass -Scope Process -Force; \n  .\scripts\install-docker.ps1
#>

function Ensure-Admin {
    $isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
    if (-not $isAdmin) {
        Write-Host "Not running as Administrator. Relaunching with elevation..."
        Start-Process -FilePath pwsh -ArgumentList "-NoProfile -ExecutionPolicy Bypass -File \"$PSCommandPath\"" -Verb RunAs
        Exit 0
    }
}

function Test-CommandExists([string]$cmd) {
    return (Get-Command $cmd -ErrorAction SilentlyContinue) -ne $null
}

function Enable-WSL2IfNeeded {
    Write-Host "Checking WSL/Virtual Machine Platform features..."
    $wslFeature = (Get-WindowsOptionalFeature -Online -FeatureName Microsoft-Windows-Subsystem-Linux -ErrorAction SilentlyContinue)
    $vmFeature = (Get-WindowsOptionalFeature -Online -FeatureName VirtualMachinePlatform -ErrorAction SilentlyContinue)

    $needReboot = $false
    if ($wslFeature -and $wslFeature.State -ne 'Enabled') {
        Write-Host "Enabling WSL feature..."
        dism.exe /online /enable-feature /featurename:Microsoft-Windows-Subsystem-Linux /all /norestart | Out-Null
        $needReboot = $true
    }
    if ($vmFeature -and $vmFeature.State -ne 'Enabled') {
        Write-Host "Enabling VirtualMachinePlatform..."
        dism.exe /online /enable-feature /featurename:VirtualMachinePlatform /all /norestart | Out-Null
        $needReboot = $true
    }

    if ($needReboot) {
        Write-Host "A reboot is required to finish enabling WSL/VM features. Please reboot and re-run this script after login."
        Exit 0
    }

    try {
        wsl --set-default-version 2 2>$null
    } catch {
        Write-Host "Unable to set WSL default version to 2 (wsl CLI may be missing). If you intend to use WSL2, follow Microsoft docs to install WSL2." 
    }
}

function Install-DockerWithWinget {
    Write-Host "Installing Docker Desktop using winget..."
    winget install --id Docker.DockerDesktop -e --source winget --accept-package-agreements --accept-source-agreements
}

function Install-DockerWithChoco {
    Write-Host "Installing Docker Desktop using Chocolatey..."
    choco install docker-desktop -y
}

function Download-And-Run-Installer {
    $tmp = Join-Path $env:TEMP "DockerDesktopInstaller.exe"
    $url = 'https://desktop.docker.com/win/stable/amd64/Docker%20Desktop%20Installer.exe'
    Write-Host "Downloading Docker Desktop installer to $tmp"
    try {
        Invoke-WebRequest -Uri $url -OutFile $tmp -UseBasicParsing -TimeoutSec 600
    } catch {
        Write-Error "Failed to download installer from $url. Please download it manually from https://www.docker.com/get-started"
        Exit 1
    }

    Write-Host "Running installer (may show UI) - please follow prompts."
    Start-Process -FilePath $tmp -ArgumentList '/quiet' -Wait
}

function Wait-For-Docker {
    Write-Host "Waiting for Docker daemon to become available (this may take a minute)..."
    $max = 180
    $i = 0
    while ($i -lt $max) {
        try {
            $v = docker version --format '{{.Server.Version}}' 2>$null
            if ($LASTEXITCODE -eq 0) {
                Write-Host "Docker is available. Version: $v"
                return $true
            }
        } catch { }
        Start-Sleep -Seconds 2
        $i++
    }
    Write-Error "Docker did not become available within timeout. Open Docker Desktop UI and wait until it reports 'Docker is running'."
    return $false
}

# Main
Ensure-Admin
Write-Host "Starting Docker Desktop installer helper..."

if (Test-CommandExists 'docker') {
    Write-Host "Docker CLI already present:"; docker --version
    Exit 0
}

# Ensure WSL2 features for best experience
Enable-WSL2IfNeeded

if (Test-CommandExists 'winget') {
    Install-DockerWithWinget
} elseif (Test-CommandExists 'choco') {
    Install-DockerWithChoco
} else {
    Write-Host "No winget/choco found - falling back to direct download installer."
    Download-And-Run-Installer
}

# Start Docker Desktop if present in default location
$possiblePaths = @(
    "$Env:ProgramFiles\Docker\Docker\Docker Desktop.exe",
    "$Env:ProgramFiles(x86)\Docker\Docker\Docker Desktop.exe"
)
foreach ($p in $possiblePaths) {
    if (Test-Path $p) {
        Write-Host "Starting Docker Desktop from $p"
        Start-Process -FilePath $p -NoNewWindow
        break
    }
}

# Wait for Docker
$ok = Wait-For-Docker
if ($ok) {
    Write-Host "Docker installation appears successful. You can now run 'docker compose up --build -d' in the project root."
} else {
    Write-Host "Please open Docker Desktop and wait until it reports 'Docker is running'. Then re-run the smoke tests."
}
