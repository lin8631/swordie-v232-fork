@echo off
chcp 65001 >nul
echo ========================================
echo   GMS UI - Frontend Dev Server
echo ========================================
echo.
echo Prerequisite: start-server.bat must be running.
echo.

start "Swordie-Frontend" cmd /c "%~dp0run-frontend.bat"
echo Frontend starting on http://localhost:5173
echo.
pause
