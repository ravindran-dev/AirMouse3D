<#
    Assembles a ready-to-share "dist" folder: the built exe plus the double-click installer.
    Zip the resulting folder and hand it to anyone — they run Install.bat and they're done.

    Run after `cargo build --release`:
      powershell -ExecutionPolicy Bypass -File installer/package.ps1
#>
$ErrorActionPreference = 'Stop'
$root = Split-Path $PSScriptRoot -Parent           # pc_receiver/
$exe  = Join-Path $root 'target\release\AirMouse3D-Receiver.exe'
$dist = Join-Path $root 'dist'

if (-not (Test-Path $exe)) {
    Write-Error "Build the exe first:  cargo build --release"
    return
}

New-Item -ItemType Directory -Force -Path $dist | Out-Null
Copy-Item $exe                                   $dist -Force
Copy-Item (Join-Path $PSScriptRoot 'Install.bat')   $dist -Force
Copy-Item (Join-Path $PSScriptRoot 'Uninstall.bat') $dist -Force
Copy-Item (Join-Path $PSScriptRoot 'install.ps1')   $dist -Force

@"
AirMouse3D Receiver
===================

To install:   double-click Install.bat
To uninstall: double-click Uninstall.bat  (or Settings > Apps)

Then open "AirMouse3D Receiver" from the Start Menu or Desktop, and scan its
QR code from the AirMouse3D phone app (same Wi-Fi network, no internet needed).
"@ | Set-Content -Path (Join-Path $dist 'README.txt') -Encoding utf8

Write-Host "Packaged to $dist" -ForegroundColor Green
Get-ChildItem $dist | Select-Object Name, Length
