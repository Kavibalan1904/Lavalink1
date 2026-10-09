# ====================================================================
# Render-Optimized Lavalink Server v4.2.2
# Tailored for Render Free Plan (512MB RAM, dynamic $PORT binding)
# Pre-bundles YouTube (dev.lavalink.youtube) & Spotify/SoundCloud (LavaSrc)
# ====================================================================
FROM eclipse-temurin:21-jre-alpine

# Set environment defaults
ENV PORT=2333 \
    LAVALINK_PASSWORD=youshallnotpass \
    JAVA_OPTS="-Xms96m -Xmx320m -XX:MaxMetaspaceSize=96m -Xss256k -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError -Djdk.tls.client.protocols=TLSv1.2,TLSv1.3 -Dfile.encoding=UTF-8"

# Install curl, dumb-init, bash, and native C runtime libraries for JNI (libudpqueue)
RUN apk add --no-cache curl bash dumb-init libgcc libstdc++ gcompat

# Create non-root application user and directory structure
RUN addgroup -S lavalink && adduser -S lavalink -G lavalink
WORKDIR /opt/lavalink
RUN mkdir -p /opt/lavalink/plugins /opt/lavalink/logs && chown -R lavalink:lavalink /opt/lavalink

# Pre-download Lavalink.jar and plugins during image build so startup is instant (< 5s)
# This prevents Render 502/timeouts and saves memory on container boot
ARG LAVALINK_VERSION=4.2.2
ARG YOUTUBE_PLUGIN_VERSION=1.18.2
ARG LAVASRC_PLUGIN_VERSION=4.8.3

RUN curl -L -s -o /opt/lavalink/Lavalink.jar \
    "https://github.com/lavalink-devs/Lavalink/releases/download/${LAVALINK_VERSION}/Lavalink.jar" && \
    curl -L -s -o /opt/lavalink/plugins/youtube-plugin-${YOUTUBE_PLUGIN_VERSION}.jar \
    "https://github.com/lavalink-devs/youtube-source/releases/download/${YOUTUBE_PLUGIN_VERSION}/youtube-plugin-${YOUTUBE_PLUGIN_VERSION}.jar" && \
    curl -L -s -o /opt/lavalink/plugins/lavasrc-plugin-${LAVASRC_PLUGIN_VERSION}.jar \
    "https://github.com/topi314/LavaSrc/releases/download/${LAVASRC_PLUGIN_VERSION}/lavasrc-plugin-${LAVASRC_PLUGIN_VERSION}.jar"

# Copy configuration and entrypoint
COPY application.yml /opt/lavalink/application.yml
COPY entrypoint.sh /opt/lavalink/entrypoint.sh

RUN chmod +x /opt/lavalink/entrypoint.sh && chown -R lavalink:lavalink /opt/lavalink

# Switch to non-root user
USER lavalink

# Expose default port
EXPOSE 2333

# HTTP Health check endpoint (Lavalink /version returns 200 without auth)
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://127.0.0.1:${PORT:-2333}/version || exit 1

ENTRYPOINT ["/usr/bin/dumb-init", "--"]
CMD ["/opt/lavalink/entrypoint.sh"]
