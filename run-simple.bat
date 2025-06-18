@echo off
echo Setting up Java environment...

REM Try to find Java installation
for /f "tokens=2*" %%i in ('reg query "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\JDK" /s /v JavaHome 2^>nul ^| find "JavaHome"') do set JAVA_HOME=%%j

if "%JAVA_HOME%"=="" (
    echo Java installation not found in registry. Trying common locations...
    if exist "C:\Program Files\Java\jdk-20" set JAVA_HOME=C:\Program Files\Java\jdk-20
    if exist "C:\Program Files\Java\jdk-17" set JAVA_HOME=C:\Program Files\Java\jdk-17
    if exist "C:\Program Files\Java\jdk-11" set JAVA_HOME=C:\Program Files\Java\jdk-11
    if exist "C:\Program Files\Oracle\Java\javapath" set JAVA_HOME=C:\Program Files\Oracle\Java\javapath
)

if "%JAVA_HOME%"=="" (
    echo ERROR: Could not find Java installation.
    echo Please install Java JDK 17 or higher and set JAVA_HOME environment variable.
    pause
    exit /b 1
)

echo Found Java at: %JAVA_HOME%
echo.

echo Building and running Split App Backend...
call mvnw.cmd clean install -DskipTests
if errorlevel 1 (
    echo Build failed!
    pause
    exit /b 1
)

echo.
echo Starting the application...
call mvnw.cmd spring-boot:run