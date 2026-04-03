@echo off
cd /d "%~dp0"

:: Try to find Python in PATH first
python --version >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    set PYTHON=python
    goto RUN
)

:: Search common install locations
for %%P in (
    "%LocalAppData%\Programs\Python\Python311\python.exe"
    "%LocalAppData%\Programs\Python\Python312\python.exe"
    "%LocalAppData%\Programs\Python\Python310\python.exe"
    "C:\Python311\python.exe"
    "C:\Python312\python.exe"
    "C:\Program Files\Python311\python.exe"
    "C:\Program Files\Python312\python.exe"
) do (
    if exist %%P (
        set PYTHON=%%P
        goto RUN
    )
)

echo Python not found! Please install Python from https://www.python.org/downloads/
echo Make sure to tick "Add Python to PATH" during installation.
pause
exit /b 1

:RUN
"%PYTHON%" -m pip install requests --quiet 2>nul
"%PYTHON%" launcher.py
