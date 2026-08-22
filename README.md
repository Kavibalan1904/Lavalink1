# 🎵 24/7 Lavalink v4 Server Deployment Kit for Render

Production-ready Lavalink v4 server configuration designed for Render.com deployment with zero-drop audio buffering, YouTube Music studio audio plugin, and LavaSrc integrations.

---

## 🚀 How to Deploy on Render.com (2 Minutes)

1. Go to **[dashboard.render.com](https://dashboard.render.com)**.
2. Click **New +** → select **Web Service**.
3. Connect your repository: **`Kavibalan1904/Lavalink1`**.
4. Render will automatically detect `render.yaml` & `Dockerfile`:
   - **Name:** `reso-lavalink`
   - **Region:** `Singapore` (closest to India for low latency)
   - **Environment:** `Docker`
   - **Instance Type:** `Free` ($0/month)
5. Click **Deploy Web Service**.

---

## 🔌 Connecting to Your Bot

Once Render finishes deploying, your service URL will be shown at the top of the dashboard:
`https://reso-lavalink-xxxx.onrender.com`

Update your bot's `.env` file:
```env
LAVALINK_HOST=reso-lavalink-xxxx.onrender.com
LAVALINK_PORT=443
LAVALINK_PASSWORD=youshallnotpass
LAVALINK_SECURE=true
```

---

## 🛠 Features Included
- **YouTube Plugin (`youtube-plugin:1.11.4`):** Direct YouTube Music & Topic studio audio support.
- **LavaSrc Plugin (`lavasrc-plugin:4.4.1`):** Spotify & SoundCloud metadata parsing.
- **Zero-Stutter Buffering:** `bufferDurationMs: 1000` & `frameBufferDurationMs: 5000`.
- **Default Password:** `youshallnotpass`
