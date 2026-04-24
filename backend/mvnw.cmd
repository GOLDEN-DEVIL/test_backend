@REM ----------------------------------------------------------------------------
@echo off

set WRAPPER_DIR=%~dp0
set MVNW_DIR=%WRAPPER_DIR%.mvn\wrapper

set MAVEN_HOME=%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9
set MAVEN_CMD=%MAVEN_HOME%\bin\mvn.cmd

set DOWNLOAD_URL=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.9/apache-maven-3.9.9-bin.zip
set MAVEN_ZIP=%TEMP%\apache-maven-3.9.9-bin.zip

if not exist "%MAVEN_CMD%" (
    echo Maven not found. Downloading Maven 3.9.9...
    
    if not exist "%MAVEN_HOME%" mkdir "%MAVEN_HOME%"
    
     powershell -Command "Invoke-WebRequest -Uri '%DOWNLOAD_URL%' -OutFile '%MAVEN_ZIP%' -UseBasicParsing"
    
    powershell -Command "Expand-Archive -Path '%MAVEN_ZIP%' -DestinationPath '%USERPROFILE%\.m2\wrapper\dists' -Force"
    
    if exist "%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9\bin\mvn.cmd" (
        echo Maven 3.9.9 installed successfully.
    ) else (
        for /d %%i in ("%USERPROFILE%\.m2\wrapper\dists\apache-maven-3.9.9\apache-maven-*") do (
            xcopy /s /e /y "%%i\*" "%MAVEN_HOME%\" >nul 2>&1
        )
    )
    del "%MAVEN_ZIP%" 2>nul
)

if "%JAVA_HOME%"=="" (
    for /f "tokens=*" %%i in ('where java') do (
        set JAVA_EXE=%%i
        goto :found_java
    )
    :found_java
    for %%i in ("%JAVA_EXE%") do set JAVA_BIN_DIR=%%~dpi
    for %%i in ("%JAVA_BIN_DIR%..") do set JAVA_HOME=%%~fi
)

"%MAVEN_CMD%" %*
