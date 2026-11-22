@echo off
setlocal
cd /d "%~dp0"
if not exist "dist\ColoresCreaciones.jar" call compilar.cmd
if errorlevel 1 exit /b 1
set "JAVA_CMD=java"
if exist "%ProgramFiles%\Java\jdk-24\bin\java.exe" set "JAVA_CMD=%ProgramFiles%\Java\jdk-24\bin\java.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
"%JAVA_CMD%" -cp "dist\ColoresCreaciones.jar;lib\*" proyectofina.ProyectoFina
if errorlevel 1 pause
