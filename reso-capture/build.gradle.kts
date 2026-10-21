plugins {
    java
}

group = "com.kavibalan.reso"
version = "0.1.0"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.lavalink.dev/releases")
    }
}

dependencies {
    // Provided by Lavalink at runtime. Do not bundle a second copy of the API.
    compileOnly("dev.arbjerg.lavalink:plugin-api:4.2.2")
    testImplementation("dev.arbjerg.lavalink:plugin-api:4.2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(17)
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    archiveBaseName.set("reso-pcm-capture")
    manifest {
        attributes["Implementation-Title"] = "Reso PCM Capture"
        attributes["Implementation-Version"] = project.version
    }
}
