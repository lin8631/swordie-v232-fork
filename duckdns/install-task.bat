@echo off
echo Registering DuckDNS auto-update task (every 5 minutes)...
schtasks /create /tn "SwordieDuckDNS" /tr "powershell -ExecutionPolicy Bypass -File \"C:\Swordie\duckdns\update-ip.ps1\"" /sc minute /mo 5 /ru SYSTEM /f
if %ERRORLEVEL% EQU 0 (
    echo Success! DuckDNS will auto-update every 5 minutes.
    echo Running first update now...
    powershell -ExecutionPolicy Bypass -File "C:\Swordie\duckdns\update-ip.ps1"
) else (
    echo Failed to register task. Try running this as Administrator.
)
pause
