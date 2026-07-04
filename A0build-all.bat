@echo off
chcp 65001 >nul
setlocal enabledelayedexpansion

set ROOT=%~dp0
set JAVA_HOME=%ROOT%JDK 21_WIN\jdk-21.0.11+10
set MAVEN_HOME=%ROOT%tools\apache-maven-3.9.9
set PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%PATH%

echo ========================================
echo   Building Swordie Backend + Frontend
echo ========================================
echo.

echo [1/2] Building backend (mvn package)...
cd /d %ROOT%
call mvn package -DskipTests -q
if %errorlevel% neq 0 (
    echo Backend build FAILED!
    exit /b 1
)
echo Backend build OK - bin\maplestory-1.77.3.jar
echo.

echo [2/2] Building frontend (npm run build)...
set PATH=C:\Program Files\nodejs;%PATH%
cd /d %ROOT%gms-ui
call npm install --registry https://registry.npmmirror.com --no-audit --no-fund >nul
call npm run build
if %errorlevel% neq 0 (
    echo Frontend build FAILED!
    exit /b 1
)
echo Frontend build OK - gms-ui\dist\
echo.

echo ========================================
echo   All builds successful!
echo   Backend:  bin\maplestory-1.77.3.jar
echo   Frontend: gms-ui\dist\
echo ========================================
pause
