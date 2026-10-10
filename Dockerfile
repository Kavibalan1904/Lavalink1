# Build the upstream YouTube plugin at the commit that fixes YouTube's
# rejection of the TV client's Cobalt User-Agent (upstream commit b33460b).
FROM eclipse-temurin:21-jdk-alpine AS youtube-build

RUN apk add --no-cache git
WORKDIR /build

ARG YOUTUBE_SOURCE_COMMIT=b33460b38ad13b5cd07da75e46444397cf0ea2df
RUN git clone https://github.com/lavalink-devs/youtube-source.git . \
    && git checkout "${YOUTUBE_SOURCE_COMMIT}" \
    && chmod +x ./gradlew \
    && sed -i 's/id("org.ajoberstar.grgit") version "5.2.0"/id("org.ajoberstar.grgit") version "5.2.1"/' build.gradle.kts \
    && ./gradlew :plugin:jar --no-daemon --refresh-dependencies

FROM eclipse-temurin:21-jre-alpine

WORKDIR /opt/lavalink

ARG LAVALINK_VERSION=4.2.2

# libgcc provides libgcc_s.so.1, required by native libraries such as udpqueue.
RUN apk add --no-cache curl libgcc libstdc++ \
    && mkdir -p /opt/lavalink/plugins \
    && curl -fSL \
       "https://github.com/lavalink-devs/Lavalink/releases/download/${LAVALINK_VERSION}/Lavalink.jar" \
       -o /opt/lavalink/Lavalink.jar

COPY --from=youtube-build /build/plugin/build/libs/youtube-plugin-*.jar /opt/lavalink/plugins/youtube-plugin.jar
COPY application.yml /opt/lavalink/application.yml
COPY entrypoint.sh /opt/lavalink/entrypoint.sh
RUN chmod +x /opt/lavalink/entrypoint.sh

EXPOSE 2333

ENTRYPOINT ["/opt/lavalink/entrypoint.sh"]
