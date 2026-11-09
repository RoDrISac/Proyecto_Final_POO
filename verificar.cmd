@echo off
setlocal
cd /d "%~dp0"
call compilar.cmd
if errorlevel 1 exit /b 1
set "JAVAC_T3=javac"
set "JAVA_T3=java"
if exist "%ProgramFiles%\Java\jdk-24\bin\javac.exe" set "JAVAC_T3=%ProgramFiles%\Java\jdk-24\bin\javac.exe"
if exist "%ProgramFiles%\Java\jdk-24\bin\java.exe" set "JAVA_T3=%ProgramFiles%\Java\jdk-24\bin\java.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC_T3=%JAVA_HOME%\bin\javac.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_T3=%JAVA_HOME%\bin\java.exe"
"%JAVAC_T3%" --release 17 -encoding UTF-8 -cp build\release -d build\tests test\proyectofina\*.java
if errorlevel 1 exit /b 1
"%JAVA_T3%" -cp "build\release;build\tests" proyectofina.PruebasAceptacion
if errorlevel 1 exit /b 1
"%JAVA_T3%" -cp "build\release;build\tests" proyectofina.PruebaImportacion
if errorlevel 1 exit /b 1
"%JAVA_T3%" -cp "build\release;build\tests" proyectofina.PruebasVisuales
if errorlevel 1 exit /b 1
echo Pruebas completas. Consulta docs\evidencias.
