FROM fredboat/lavalink:4.2.2

EXPOSE 8080

COPY application.yml /opt/Lavalink/application.yml

WORKDIR /opt/Lavalink
ENTRYPOINT ["java", "-Djdk.tls.client.protocols=TLSv1.2,TLSv1.3", "-jar", "Lavalink.jar"]
