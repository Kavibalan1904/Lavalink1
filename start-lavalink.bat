@echo off
title Lavalink v4 Server
echo ========================================================
echo   Starting Lavalink v4 Audio Node
echo ========================================================
if not exist "Lavalink.jar" (
    if exist "app.jar" (
        copy /y app.jar Lavalink.jar >nul
    ) else (
        echo [ERROR] Neither Lavalink.jar nor app.jar found!
        echo Please ensure app.jar or Lavalink.jar is present in this directory.
        pause
        exit /b 1
    )
)
java -Djava.net.preferIPv4Stack=true -Xms128m -Xmx768m -XX:+UseG1GC -XX:+ParallelRefProcEnabled -jar Lavalink.jar
pause
