# 🎵 Render-Optimized Lavalink Server v4

A high-performance, containerized **Lavalink v4** audio node specially tuned for the **Render Free Tier** (512 MB RAM) and pre-configured for seamless integration with **[Resobott](https://github.com/Kavibalan1904/Resobott)**.

---

## ✨ Features & Optimizations

- **⚡ Instant Container Startup (< 5s)**: Both `Lavalink.jar` (v4.2.2) and required plugins are baked directly into the Docker image during build. No runtime downloads or Maven delays.
- **🛡️ 512MB RAM Capped for Render Free Plan**:
  - Memory tuned via `-Xms96m -Xmx320m -XX:MaxMetaspaceSize=96m -Xss256k -XX:+UseSerialGC`.
  - Serial Garbage Collector eliminates multi-threaded GC metadata overhead, keeping the entire container well below the strict 512 MB memory threshold.
  - Off-heap audio frame buffers optimized to prevent memory leak spikes while maintaining 0% audio stutter.
- **🔌 Pre-Integrated Music Sources**:
  - **YouTube**: Powered by `dev.lavalink.youtube:youtube-plugin:1.18.2` (supporting `MUSIC`, `ANDROID_VR`, `WEB`, `WEBEMBEDDED`, `MWEB`, and `TV` clients, plus optional Proof-of-Origin `poToken` and OAuth).
  - **Spotify**: Powered by `com.github.topi314.lavasrc:lavasrc-plugin:4.8.3` (supports Spotify track, playlist, album, and artist search and direct URLs mirrored to YouTube audio streams).
  - **SoundCloud**: Native high-speed SoundCloud playback enabled.
  - **HTTP & Attachments**: Audio file uploads and stream links supported.
- **🎛️ Audio Filters**:
  - Full filter pipeline enabled: Equalizer, Bassboost, Timescale (Speed/Pitch/Nightcore), Volume, 8D / Rotation, Vibrato, Tremolo, Distortion, and Channel Mix matching all Resobott `/filter` commands.
- **🌐 Dynamic `$PORT` Binding**:
  - Automatic detection and binding to Render's dynamically assigned `$PORT` on `0.0.0.0`.
- **🔐 Bot-Matched Credentials**:
  - Configured with password `youshallnotpass` (overridable via `LAVALINK_PASSWORD`), directly aligning with Resobott's primary node defaults.

---

## 🚀 How to Deploy on Render (Free Plan)

### Method 1: Blueprint Deployment (Recommended — 1-Click)

1. Fork or push this repository to GitHub: `https://github.com/Kavibalan1904/Lavalink1.git`.
2. Go to the **[Render Dashboard](https://dashboard.render.com/)**.
3. Click **New +** in the top navigation bar and select **Blueprint**.
4. Connect your **Lavalink1** repository.
5. Render will automatically parse [`render.yaml`](./render.yaml) and configure the service as:
   - **Runtime**: Docker
   - **Plan**: Free
   - **Health Check Path**: `/version`
6. Click **Apply**. Render will build the container and deploy your Lavalink server!

---

### Method 2: Manual Web Service Setup

1. Go to the **[Render Dashboard](https://dashboard.render.com/)**.
2. Click **New +** > **Web Service**.
3. Choose **Build and deploy from a Git repository** and select your `Lavalink1` repository.
4. Fill in the following details:
   - **Name**: `lavalink1` (or your preferred name, e.g. `lavalink-resobott`)
   - **Region**: Choose the region closest to your Discord bot (e.g. *Frankfurt*, *Singapore*, or *Oregon*)
   - **Language**: `Docker`
   - **Instance Type**: `Free`
5. Under **Environment Variables**, add the following (optional if using defaults):
   | Key | Default Value | Description |
   |---|---|---|
   | `LAVALINK_PASSWORD` | `youshallnotpass` | Password for your bot to authenticate |
   | `JAVA_OPTS` | `-Xms96m -Xmx320m -XX:MaxMetaspaceSize=96m -Xss256k -XX:+UseSerialGC -XX:+ExitOnOutOfMemoryError -Djdk.tls.client.protocols=TLSv1.2,TLSv1.3 -Dfile.encoding=UTF-8` | Low-RAM JVM arguments |
   | `SPOTIFY_CLIENT_ID` | *(Optional)* | Your Spotify Developer App Client ID |
   | `SPOTIFY_CLIENT_SECRET` | *(Optional)* | Your Spotify Developer App Client Secret |
   | `YOUTUBE_PO_TOKEN` | *(Optional)* | Proof-of-Origin Token for YouTube |
   | `YOUTUBE_VISITOR_DATA` | *(Optional)* | YouTube Visitor Data Token |
6. Click **Create Web Service**.

Once the deploy completes, Render will provide your public URL:
```
https://<your-service-name>.onrender.com
```

---

## 🤖 Connecting to Your Discord Bot (`Resobott`)

In your **Resobott** directory, open your `.env` file and configure your Lavalink credentials:

```env
# Lavalink Node Configuration for Render
LAVALINK_HOST=<your-service-name>.onrender.com
LAVALINK_PORT=443
LAVALINK_PASSWORD=youshallnotpass
LAVALINK_SECURE=true
```

> 💡 **Important Notes for Render:**
> - `LAVALINK_PORT` is always **`443`** on Render's public domain.
> - `LAVALINK_SECURE` must be set to **`true`** because Render terminates SSL/TLS (HTTPS & WSS).
> - Do not include `https://` or `wss://` in `LAVALINK_HOST` — just the hostname (e.g. `lavalink1.onrender.com`).

When Resobott starts, it will log:
```
[Reso] 🔒 Loading DEDICATED private Lavalink node: <your-service-name>.onrender.com:443
[Reso] Lavalink Node primary-main connected!
```

---

## ⏰ Keeping Render Awake 24/7 (Preventing Sleep)

On Render's Free tier, services spin down into sleep mode after **15 minutes of inactivity**. If sleeping, waking the container takes 30–45 seconds when a user runs `/play`.

To keep your Lavalink server warm and responsive 24/7 without paying:

1. Create a free account at **[UptimeRobot](https://uptimerobot.com/)** or **[cron-job.org](https://cron-job.org/)**.
2. Create an **HTTP(s) Monitor**:
   - **URL**: `https://<your-service-name>.onrender.com/version`
   - **Monitoring Interval**: Every `5 minutes` or `10 minutes`
3. This sends a lightweight heartbeat request that resets Render's 15-minute inactivity timer, keeping your Lavalink server hot and ready for Discord commands 24/7!

---

## 🎵 Source Setup & Customization

### 1. Spotify
LavaSrc resolves Spotify tracks, playlists, and albums and mirrors them to high-fidelity audio streams.
- Although public Spotify scraping works out of the box, setting your own free Spotify API credentials avoids rate-limiting:
  1. Go to [Spotify Developer Dashboard](https://developer.spotify.com/dashboard).
  2. Create an application and copy your **Client ID** and **Client Secret**.
  3. Add `SPOTIFY_CLIENT_ID` and `SPOTIFY_CLIENT_SECRET` to your Render Environment Variables.

### 2. YouTube & Anti-Bot Protection (Fixing "This video requires login")
The `dev.lavalink.youtube` plugin handles YouTube playback. YouTube regularly challenges cloud datacenter IPs (like Render) with "Sign in to confirm you're not a bot" or "This video requires login".

#### Option A: YouTube OAuth Flow (Recommended)
1. In `application.yml`, OAuth is enabled by default (`oauth.enabled: true`).
2. Deploy or restart your Lavalink service on Render.
3. Open the **Logs** tab in your Render Dashboard.
4. You will see a log message prompting:
   ```
   Go to https://www.google.com/device and enter code XXXX-XXXX
   ```
5. Open `https://www.google.com/device` in your browser, enter the code, and sign in with a **burner / secondary Google account** (do not use your primary personal account).
6. Once approved, the Render logs will print your `refreshToken`:
   ```
   OAuth refresh token: <YOUR_TOKEN>
   ```
7. Copy this token and go to Render Dashboard -> **Environment Variables** -> add:
   - `YOUTUBE_OAUTH_REFRESH_TOKEN` = `<YOUR_TOKEN>`
   - `YOUTUBE_OAUTH_SKIP_INIT` = `true`
8. Save changes and redeploy. All YouTube tracks will now play seamlessly without login errors!

#### Option B: Proof of Origin Token (`poToken`)
- Alternatively, you can supply a Proof-of-Origin token:
  1. Generate tokens using [youtube-trusted-session-generator](https://github.com/iv-org/youtube-trusted-session-generator).
  2. Add `YOUTUBE_PO_TOKEN` and `YOUTUBE_VISITOR_DATA` in your Render Environment Variables.

---

## 📁 Repository Structure

```
.
├── application.yml     # Lavalink configuration with memory, source, and filter optimizations
├── Dockerfile          # Pre-bundled, lightweight Alpine Temurin 21 container image
├── entrypoint.sh       # Dynamic port binding & safe JVM executor
├── render.yaml         # Render Blueprint for zero-configuration 1-click deploy
├── .dockerignore       # Docker build exclusions
├── .gitignore          # Git exclusions
├── .env.example        # Environment variable reference
└── README.md           # Documentation
```

---

## 📄 License

This repository is licensed under the [MIT License](LICENSE).
