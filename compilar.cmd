@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
set "JAVAC_CMD=javac"
set "JAR_CMD=jar"
if exist "%ProgramFiles%\Java\jdk-24\bin\javac.exe" set "JAVAC_CMD=%ProgramFiles%\Java\jdk-24\bin\javac.exe"
if exist "%ProgramFiles%\Java\jdk-24\bin\jar.exe" set "JAR_CMD=%ProgramFiles%\Java\jdk-24\bin\jar.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jar.exe" set "JAR_CMD=%JAVA_HOME%\bin\jar.exe"
if not exist build mkdir build
if not exist dist mkdir dist
(for /r src %%f in (*.java) do (
    set "FUENTE=%%f"
    echo "!FUENTE:\=/!"
)) > build\fuentes.txt
"%JAVAC_CMD%" --release 17 -encoding UTF-8 -cp "lib\*" -d build\release @build\fuentes.txt
if errorlevel 1 exit /b 1
"%JAR_CMD%" --create --file dist\ColoresCreaciones.jar --main-class proyectofina.ProyectoFina -C build\release .
if errorlevel 1 exit /b 1
echo Compilacion completada.
