@echo off
setlocal
cd /d "%~dp0"
if not exist "dist\ColoresCreacionesT3.jar" call compilar.cmd
if errorlevel 1 exit /b 1
set "JAVA_T3=java"
if exist "%ProgramFiles%\Java\jdk-24\bin\java.exe" set "JAVA_T3=%ProgramFiles%\Java\jdk-24\bin\java.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_T3=%JAVA_HOME%\bin\java.exe"
"%JAVA_T3%" -jar "dist\ColoresCreacionesT3.jar"
if errorlevel 1 pause
