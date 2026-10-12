@echo off
rem Double-click to start Badminton Club. Needs JDK 21+ ; the first run downloads JavaFX (internet required once).
rem Messages are in English on purpose: cmd.exe mis-parses Thai text inside .bat files.
setlocal
cd /d "%~dp0"

rem A broken JAVA_HOME (e.g. pointing at ...\bin) is common - ignore it and fall back to java on PATH
if defined JAVA_HOME if not exist "%JAVA_HOME%\bin\javac.exe" set "JAVA_HOME="

if not defined JAVA_HOME (
    for /f "tokens=2 delims==" %%h in ('java -XshowSettings:properties -version 2^>^&1 ^| findstr /c:"java.home"') do (
        for /f "tokens=*" %%t in ("%%h") do set "JAVA_HOME=%%t"
    )
)

if not exist "%JAVA_HOME%\bin\javac.exe" (
    echo [ERROR] JDK 21 or newer was not found.
    echo         Install it from https://adoptium.net  ^(choose JDK 21 LTS, tick "Set JAVA_HOME"^), then run this file again.
    pause
    exit /b 1
)

"%JAVA_HOME%\bin\java" -version 2>&1 | findstr /r /c:"version \"1[0-9]*\." /c:"version \"20\." >nul && (
    echo [ERROR] Java at "%JAVA_HOME%" is older than 21. Install JDK 21+ from https://adoptium.net
    pause
    exit /b 1
)

echo Starting Badminton Club with Java at "%JAVA_HOME%" ...
call mvnw.cmd -q javafx:run
if errorlevel 1 pause
