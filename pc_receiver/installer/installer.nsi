; AirMouse3D Receiver — NSIS installer script (optional).
;
; This produces a single downloadable "AirMouse3D-Setup.exe" like a normal app installer.
; It is OPTIONAL: install.ps1 / Install.bat already install the app with no extra tooling.
; Use this only if you specifically want a one-file Setup.exe to hand out.
;
; Build it (after `cargo build --release`) with the NSIS toolset installed:
;   makensis installer.nsi
;
; Expects the built exe at ..\target\release\AirMouse3D-Receiver.exe

!define APPNAME    "AirMouse3D Receiver"
!define EXENAME    "AirMouse3D-Receiver.exe"
!define COMPANY    "AirMouse3D"
!define VERSION    "1.0.0"

Name "${APPNAME}"
OutFile "AirMouse3D-Setup.exe"
; Per-user install — no admin prompt.
RequestExecutionLevel user
InstallDir "$LOCALAPPDATA\Programs\AirMouse3D"
SetCompressor /SOLID lzma

Page directory
Page instfiles
UninstPage uninstConfirm
UninstPage instfiles

Section "Install"
    SetOutPath "$INSTDIR"
    File "..\target\release\${EXENAME}"

    CreateShortcut "$SMPROGRAMS\${APPNAME}.lnk" "$INSTDIR\${EXENAME}" "" "$INSTDIR\${EXENAME}" 0
    CreateShortcut "$DESKTOP\${APPNAME}.lnk"    "$INSTDIR\${EXENAME}" "" "$INSTDIR\${EXENAME}" 0

    WriteUninstaller "$INSTDIR\Uninstall.exe"

    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\AirMouse3D" "DisplayName" "${APPNAME}"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\AirMouse3D" "DisplayIcon" "$INSTDIR\${EXENAME}"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\AirMouse3D" "DisplayVersion" "${VERSION}"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\AirMouse3D" "Publisher" "${COMPANY}"
    WriteRegStr HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\AirMouse3D" "UninstallString" "$INSTDIR\Uninstall.exe"
    WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\AirMouse3D" "NoModify" 1
    WriteRegDWORD HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\AirMouse3D" "NoRepair" 1
SectionEnd

Section "Uninstall"
    Delete "$INSTDIR\${EXENAME}"
    Delete "$INSTDIR\Uninstall.exe"
    Delete "$SMPROGRAMS\${APPNAME}.lnk"
    Delete "$DESKTOP\${APPNAME}.lnk"
    RMDir "$INSTDIR"
    DeleteRegKey HKCU "Software\Microsoft\Windows\CurrentVersion\Uninstall\AirMouse3D"
SectionEnd
