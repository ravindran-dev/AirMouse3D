<#
    AirMouse3D Receiver — installer / uninstaller.

    Installs the app the way a normal Windows program installs (per-user, no admin needed):
      * copies the exe to %LOCALAPPDATA%\Programs\AirMouse3D
      * creates Start Menu and Desktop shortcuts (with the app icon)
      * registers an entry in Settings ▸ Apps ("Add/Remove Programs") so it can be uninstalled

    Usage:
      Install:    powershell -ExecutionPolicy Bypass -File install.ps1
      + launch:   powershell -ExecutionPolicy Bypass -File install.ps1 -Launch
      Uninstall:  powershell -ExecutionPolicy Bypass -File install.ps1 -Uninstall

    (Most people just double-click Install.bat, which runs this for you.)
#>
param(
    [switch]$Uninstall,
    [switch]$Launch
)

$ErrorActionPreference = 'Stop'

$AppName    = 'AirMouse3D Receiver'
$ExeName    = 'AirMouse3D-Receiver.exe'
$ProcName   = 'AirMouse3D-Receiver'
$InstallDir = Join-Path $env:LOCALAPPDATA 'Programs\AirMouse3D'
$ExePath    = Join-Path $InstallDir $ExeName
$StartMenu  = Join-Path $env:APPDATA "Microsoft\Windows\Start Menu\Programs\$AppName.lnk"
$Desktop    = Join-Path ([Environment]::GetFolderPath('Desktop')) "$AppName.lnk"
$UninstKey  = 'HKCU:\Software\Microsoft\Windows\CurrentVersion\Uninstall\AirMouse3D'

function New-Shortcut($LinkPath, $Target) {
    $shell = New-Object -ComObject WScript.Shell
    $sc = $shell.CreateShortcut($LinkPath)
    $sc.TargetPath = $Target
    $sc.WorkingDirectory = Split-Path $Target
    $sc.IconLocation = "$Target,0"
    $sc.Description = $AppName
    $sc.Save()
}

function Stop-Running {
    Get-Process -Name $ProcName -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Milliseconds 300
}

if ($Uninstall) {
    Stop-Running
    Remove-Item $StartMenu -ErrorAction SilentlyContinue
    Remove-Item $Desktop -ErrorAction SilentlyContinue
    Remove-Item $UninstKey -Recurse -ErrorAction SilentlyContinue
    Remove-Item $InstallDir -Recurse -Force -ErrorAction SilentlyContinue
    Write-Host "$AppName has been uninstalled." -ForegroundColor Green
    return
}

# Locate the built exe: next to this script (packaged dist), or in the cargo target dir.
$candidates = @(
    (Join-Path $PSScriptRoot $ExeName),
    (Join-Path $PSScriptRoot "..\target\release\$ExeName"),
    (Join-Path $PSScriptRoot "..\..\target\release\$ExeName")
)
$src = $candidates | Where-Object { Test-Path $_ } | Select-Object -First 1
if (-not $src) {
    Write-Error "Could not find $ExeName. Build it first with:  cargo build --release"
    return
}

Stop-Running
New-Item -ItemType Directory -Force -Path $InstallDir | Out-Null
Copy-Item $src $ExePath -Force
Copy-Item $PSCommandPath (Join-Path $InstallDir 'install.ps1') -Force

New-Shortcut $StartMenu $ExePath
New-Shortcut $Desktop $ExePath

New-Item -Path $UninstKey -Force | Out-Null
Set-ItemProperty $UninstKey DisplayName     $AppName
Set-ItemProperty $UninstKey DisplayIcon     $ExePath
Set-ItemProperty $UninstKey DisplayVersion  '1.0.0'
Set-ItemProperty $UninstKey Publisher       'AirMouse3D'
Set-ItemProperty $UninstKey InstallLocation $InstallDir
Set-ItemProperty $UninstKey UninstallString "powershell -ExecutionPolicy Bypass -File `"$InstallDir\install.ps1`" -Uninstall"
Set-ItemProperty $UninstKey NoModify 1 -Type DWord
Set-ItemProperty $UninstKey NoRepair 1 -Type DWord

Write-Host ""
Write-Host "  $AppName installed." -ForegroundColor Green
Write-Host "  Location : $InstallDir"
Write-Host "  Shortcuts: Start Menu + Desktop"
Write-Host "  Uninstall: Settings > Apps, or run install.ps1 -Uninstall"
Write-Host ""

if ($Launch) {
    Start-Process $ExePath
}
