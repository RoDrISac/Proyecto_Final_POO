@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"
set "JAVAC_T3=javac"
set "JAR_T3=jar"
if exist "%ProgramFiles%\Java\jdk-24\bin\javac.exe" set "JAVAC_T3=%ProgramFiles%\Java\jdk-24\bin\javac.exe"
if exist "%ProgramFiles%\Java\jdk-24\bin\jar.exe" set "JAR_T3=%ProgramFiles%\Java\jdk-24\bin\jar.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC_T3=%JAVA_HOME%\bin\javac.exe"
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jar.exe" set "JAR_T3=%JAVA_HOME%\bin\jar.exe"
if not exist build mkdir build
if not exist dist mkdir dist
(for /r src %%f in (*.java) do (
    set "FUENTE_T3=%%f"
    echo "!FUENTE_T3:\=/!"
)) > build\fuentes.txt
"%JAVAC_T3%" --release 17 -encoding UTF-8 -d build\release @build\fuentes.txt
if errorlevel 1 exit /b 1
"%JAR_T3%" --create --file dist\ColoresCreacionesT3.jar --main-class proyectofina.ProyectoFina -C build\release .
if errorlevel 1 exit /b 1
echo Compilacion completada.
