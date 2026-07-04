@echo off
chcp 65001 >nul
echo Delegate to start-server.bat...
cd /d %~dp0
call start-server.bat
