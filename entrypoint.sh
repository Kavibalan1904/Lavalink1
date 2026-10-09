#!/bin/bash
set -e

# Render passes dynamic port via PORT environment variable
BIND_PORT="${PORT:-2333}"

echo "=========================================================="
echo " Starting Lavalink Server v4.2.2 (Render Free Tier)       "
echo " Binding Port: ${BIND_PORT}                               "
echo " Memory Optimization: Capped for 512MB RAM environment    "
echo " Included Sources: YouTube, Spotify, SoundCloud, HTTP     "
echo " Configured for: Resobott                                 "
echo "=========================================================="

# Launch Lavalink with Spring Boot CLI overrides to guarantee binding
exec java ${JAVA_OPTS} -jar /opt/lavalink/Lavalink.jar \
    --server.port="${BIND_PORT}" \
    --server.address="0.0.0.0"
