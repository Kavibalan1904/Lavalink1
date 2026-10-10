
FROM eclipse-temurin:21-jre-alpine

WORKDIR /opt/lavalink

ARG LAVALINK_VERSION=4.2.2
ARG YOUTUBE_PLUGIN_VERSION=1.18.2

RUN apk add --no-cache curl \
    && mkdir -p /opt/lavalink/plugins \
    && curl -fSL \
       "https://github.com/lavalink-devs/Lavalink/releases/download/${LAVALINK_VERSION}/Lavalink.jar" \
       -o /opt/lavalink/Lavalink.jar \
    && curl -fSL \
       "https://github.com/lavalink-devs/youtube-source/releases/download/${YOUTUBE_PLUGIN_VERSION}/youtube-plugin-${YOUTUBE_PLUGIN_VERSION}.jar" \
       -o /opt/lavalink/plugins/youtube-plugin-${YOUTUBE_PLUGIN_VERSION}.jar

COPY application.yml /opt/lavalink/application.yml

EXPOSE 2333

CMD ["sh", "-c", "exec java -Xms64m -Xmx320m -XX:MaxMetaspaceSize=96m -jar /opt/lavalink/Lavalink.jar"]
