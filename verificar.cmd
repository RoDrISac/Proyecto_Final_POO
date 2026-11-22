@echo off
setlocal
cd /d "%~dp0"
call compilar.cmd
if errorlevel 1 exit /b 1
set "JAVAC_CMD=javac"
set "JAVA_CMD=java"
if exist "%ProgramFiles%\Java\jdk-24\bin\javac.exe" set "JAVAC_CMD=%ProgramFiles%\Java\jdk-24\bin\javac.exe"
if exist "%ProgramFiles%\Java\jdk-24\bin\java.exe" set "JAVA_CMD=%ProgramFiles%\Java\jdk-24\bin\java.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
"%JAVAC_CMD%" --release 17 -encoding UTF-8 -cp "build\release;lib\*" -d build\tests test\proyectofina\*.java
if errorlevel 1 exit /b 1
"%JAVA_CMD%" -cp "build\release;build\tests;lib\*" proyectofina.PruebasAceptacion
if errorlevel 1 exit /b 1
"%JAVA_CMD%" -cp "build\release;build\tests;lib\*" proyectofina.PruebaImportacion
if errorlevel 1 exit /b 1
"%JAVA_CMD%" -cp "build\release;build\tests;lib\*" proyectofina.PruebasEF
if errorlevel 1 exit /b 1
"%JAVA_CMD%" -cp "build\release;build\tests;lib\*" proyectofina.PruebasVisuales
if errorlevel 1 exit /b 1
"%JAVA_CMD%" -cp "build\release;build\tests;lib\*" proyectofina.PruebasVisualesEF
if errorlevel 1 exit /b 1
rem PruebaBD trabaja sobre la BD real: agrega una produccion, un pedido y una venta de prueba.
"%JAVA_CMD%" -cp "build\release;build\tests;lib\*" proyectofina.PruebaBD
if errorlevel 1 exit /b 1
echo Pruebas completas. Consulta docs\evidencias.
