# 🎵 24/7 Lavalink v4 Server Deployment Kit

Production-ready Lavalink v4 server configuration designed for Discord music bots with zero-drop audio buffering, YouTube Music studio audio plugin, and LavaSrc integrations.

---

## ⚡ Quick Deployment Options

### 1. ☁️ Koyeb / Render / Hugging Face (1-Click Docker Deployment)
1. Fork or connect this repository to **Koyeb** or **Render** or **Hugging Face Spaces**.
2. Deploy using `Dockerfile` (Port `8080` / `443`).
3. Set environment variables if needed:
   - Password: `youshallnotpass`

---

### 2. 📦 Discloud Deployment
1. Download or zip the contents of this repository.
2. Upload the zip to **Discloud Dashboard**.
3. It will automatically initialize Node.js, fetch `Lavalink.jar v4.2.2`, and boot the server.

---

### 3. 💻 Local PC / VPS (Manual Run)
Run locally on your PC or VPS:
```bash
node index.js
```
*(Or run `java -jar Lavalink.jar` directly)*

---

## 🛠 Configuration Features
- **YouTube Plugin (`youtube-plugin:1.11.4`):** Support for YouTube Music & official Topic tracks.
- **LavaSrc Plugin (`lavasrc-plugin:4.4.1`):** Spotify & SoundCloud metadata parsing.
- **Zero-Stutter Buffering:** `bufferDurationMs: 1000` & `frameBufferDurationMs: 5000`.
- **Default Password:** `youshallnotpass`
