@echo off
title Lavalink v4 Server
echo ========================================================
echo   Starting Lavalink v4 Audio Node
echo ========================================================
if not exist "Lavalink.jar" (
    echo [ERROR] Lavalink.jar not found!
    echo Please download Lavalink.jar from https://github.com/lavalink-devs/Lavalink/releases
    echo and place it in this directory.
    pause
    exit /b 1
)
java -Djava.net.preferIPv4Stack=true -Xms128m -Xmx768m -XX:+UseG1GC -XX:+ParallelRefProcEnabled -jar Lavalink.jar
pause
