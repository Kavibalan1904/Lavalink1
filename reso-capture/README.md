# Reso PCM Capture Plugin (proof of concept)

This plugin is an opt-in PCM capture proof of concept for the Reso audio-reactive visualizer. It is built against the exact dev.arbjerg.lavalink:plugin-api:4.2.2 artifact and targets Java 17 bytecode for Lavalink 4.2.2 compatibility.

## Current scope

- Implements Lavalink's public AudioFilterExtension API with a pass-through filter named reso-capture.
- Copies decoded floating-point PCM into a bounded queue and forwards the same input buffers to the existing filter chain.
- Performs FFT and logarithmic frequency-band analysis on daemon worker threads, not in the audio callback.
- Exposes authenticated session creation, spectrum polling, and session deletion endpoints.
- Limits capture to four sessions, bounds queued PCM, bounds spectrum history, and expires sessions after two minutes.
- Capture is not enabled unless a player explicitly sends the reso-capture plugin filter and RESO_CAPTURE_API_KEY is configured.

This is not end-to-end integration yet. Resobott must create a capture session, apply the corresponding plugin filter to the active player, poll spectrum frames, and disable/delete the filter/session when done. That bot-side change is intentionally outside this plugin-only branch.

## Build

From this directory, with JDK 17 and Gradle 8.7 or compatible:

    gradle --no-daemon clean jar

The JAR is written to build/libs/reso-pcm-capture-0.1.0.jar.

## API

Set a strong random RESO_CAPTURE_API_KEY environment variable on the Lavalink service before enabling the endpoints. Never commit the key or put it in a public issue.

Create a session:

    curl -X POST -H "Authorization: $LAVALINK_PASSWORD" -H "X-Reso-Capture-Key: $RESO_CAPTURE_API_KEY" https://YOUR-LAVALINK-HOST/v1/reso-capture/sessions

The response includes sessionId. Resobott then needs to send this plugin filter through the Lavalink v4 player filters operation:

    {
      "op": "filters",
      "guildId": "DISCORD_GUILD_ID",
      "filters": {
        "pluginFilters": {
          "reso-capture": {
            "enabled": true,
            "sessionId": "SESSION_ID_FROM_CREATE_RESPONSE"
          }
        }
      }
    }

The exact WebSocket envelope should be checked against the installed Lavalink client version before bot integration; this example illustrates the plugin filter payload rather than a drop-in client call.

Poll the spectrum:

    curl -H "Authorization: $LAVALINK_PASSWORD" -H "X-Reso-Capture-Key: $RESO_CAPTURE_API_KEY" https://YOUR-LAVALINK-HOST/v1/reso-capture/sessions/SESSION_ID/spectrum

The response contains up to 36 measured frequency-band frames. Delete the session when capture ends:

    curl -X DELETE -H "Authorization: $LAVALINK_PASSWORD" -H "X-Reso-Capture-Key: $RESO_CAPTURE_API_KEY" https://YOUR-LAVALINK-HOST/v1/reso-capture/sessions/SESSION_ID

## Safety and validation status

- Capture sessions are not created automatically.
- The endpoints fail closed when the API key is missing or invalid.
- The audio callback never waits for queue capacity or performs FFT.
- Capture exceptions are isolated from the normal audio output path.
- Not yet runtime-tested against a running Lavalink 4.2.2 node. Build and playback pass-through tests are required before deployment.
