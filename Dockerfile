```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /opt/lavalink

ARG LAVALINK_VERSION=4.2.2
ARG YOUTUBE_PLUGIN_VERSION=1.18.2

ENV PORT=2333
ENV JAVA_OPTS="-Xms96m -Xmx320m -XX:MaxMetaspaceSize=96m -XX:+UseSerialGC"

RUN apk add --no-cache curl

RUN mkdir -p plugins logs && \
    curl -fSL \
      "https://github.com/lavalink-devs/Lavalink/releases/download/${LAVALINK_VERSION}/Lavalink.jar" \
      -o Lavalink.jar && \
    curl -fSL \
      "https://github.com/lavalink-devs/youtube-source/releases/download/${YOUTUBE_PLUGIN_VERSION}/youtube-plugin-${YOUTUBE_PLUGIN_VERSION}.jar" \
      -o "plugins/youtube-plugin-${YOUTUBE_PLUGIN_VERSION}.jar"

COPY application.yml ./application.yml

EXPOSE 2333

CMD ["sh", "-c", "exec java $JAVA_OPTS -jar Lavalink.jar"]
```
