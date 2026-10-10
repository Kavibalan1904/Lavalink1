#!/bin/sh
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

# Honor Render JVM settings, with memory-safe defaults for local deployments.
JAVA_OPTS="${JAVA_OPTS:--Xms96m -Xmx320m -XX:MaxMetaspaceSize=96m -Xss256k -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError -Djdk.tls.client.protocols=TLSv1.2,TLSv1.3 -Dfile.encoding=UTF-8}"

# Launch Lavalink with Spring Boot CLI overrides to guarantee binding.
exec java ${JAVA_OPTS} -jar /opt/lavalink/Lavalink.jar \
    --server.port="${BIND_PORT}" \
    --server.address="0.0.0.0"
