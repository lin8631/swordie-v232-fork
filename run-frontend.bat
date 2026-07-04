@echo off
chcp 65001 >nul
set PATH=C:\Program Files\nodejs;%PATH%
cd /d %~dp0gms-ui
echo Installing dependencies...
call npm install --registry https://registry.npmmirror.com --no-audit --no-fund
echo Starting frontend dev server...
npm run dev
pause
