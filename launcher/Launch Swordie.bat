@echo off
cd /d "%~dp0"
set PYTHON=C:\Users\USER\AppData\Local\Programs\Python\Python311\python.exe
"%PYTHON%" -m pip install requests --quiet 2>nul
"%PYTHON%" launcher.py
