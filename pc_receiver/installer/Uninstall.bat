@echo off
REM Double-click me to remove AirMouse3D Receiver.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0install.ps1" -Uninstall
echo.
pause
