# JWSP – Jadwal Waktu Sholat & Puasa

JWSP adalah **aplikasi desktop Java** untuk menampilkan jadwal waktu sholat dan informasi puasa, dengan dukungan widget desktop dan system tray.

## Fitur Utama

- 🕌 Jadwal 5 waktu sholat + fase waktu (wajib & sunnah)
- 🌙 Informasi puasa Ramadhan & sunnah
- 🖥️ Desktop widget (opsional, transparan)
- 📌 System tray / taskbar info sholat berikutnya
- 🔊 Notifikasi audio (adzan, tarkhim, sirine)
- 🎨 Tema kustom (JSON, user-editable)
- 📍 Provinsi → Kota/Kabupaten (Indonesia)
- 📆 Kalender Hijriah + countdown menuju Ramadhan
- 🔁 Fallback offline (cache → perhitungan manual)

---

## Persyaratan

### Minimum

- **Java JDK / JRE 17+**

### Linux (Audio)

Beberapa distro membutuhkan external player:

```bash
sudo apt install mpv
```

---

## Cara Menjalankan (Recommended)

### ▶️ 1 Klik Run (Tanpa Terminal)

#### Windows

```
Double click: run.bat
```

#### Linux

```bash
chmod +x run.sh
./run.sh
```

Script akan:

- Mengecek Java
- Mengunduh Java (user mode) jika belum ada
- Compile source (jika belum)
- Menjalankan JWSP

---

## Build & Packaging

### Build Script

#### Linux

```bash
chmod +x build.sh
./build.sh compile     # Compile
./build.sh jar         # Build JAR
./build.sh portable    # Portable folder
./build.sh all         # Full build
```

#### Windows

```batch
build.bat compile
build.bat jar
build.bat portable
build.bat all
```

---

## Struktur Folder

```
JWSP/
├── src/
│   └── jwsp/
│       ├── app/        # Entry point & lifecycle
│       ├── ui/         # MainFrame, Widget, Tray
│       ├── api/        # API service
│       ├── domain/     # Logic & model
│       ├── util/       # Helper & utils
│       ├── audio/      # Audio logic
│       ├── theme/      # Theme manager
│       └── data/       # CSV wilayah
├── audio/
│   ├── adzan/
│   ├── sirine/
│   └── tarkhim/
├── themes/             # Tema JSON
├── cache/              # Offline cache (runtime)
├── lib/                # External libraries (optional)
├── bin/                # Compiled classes
├── build.sh
├── build.bat
├── run.sh
├── run.bat
├── jwsp.png
├── jwsp.ico
└── README.md
```

---

## Tema Kustom

Tema berbasis JSON, dapat dibuat user tanpa recompile.

### Contoh:

```json
{
  "name": "Dark Night",
  "background": "#121212",
  "backgroundAlpha": 180,
  "textPrimary": "#FFFFFF",
  "textSecondary": "#BBBBBB",
  "accent": "#4CAF50",
  "fontFamily": "SansSerif",
  "fontSize": 14,
  "padding": 14
}
```

Tema dapat di-load ulang saat runtime.

---

## Desktop Widget

- Transparan (opsional)
- Tidak muncul di taskbar
- Fixed di desktop (tidak overlay aplikasi)
- Kalender Hijriah + countdown Ramadhan
- Mode widget / tray / keduanya

---

## System Tray / Taskbar

- Menampilkan jadwal sholat berikutnya
- Update otomatis menjelang pergantian waktu
- Menu kontrol aplikasi
- Aplikasi tetap berjalan di background

---

## Audio Notifikasi

- ⏰ 10 menit sebelum Shubuh & Maghrib → Tarkhim
- 🌅 Maghrib (Ramadhan) → Tarkhim buka puasa → Sirine → Adzan
- 🕓 Imsak → Sirine
- 🕌 Waktu sholat → Adzan (pilihan user)

### Format audio:

- WAV (recommended)
- MP3 (Linux perlu player eksternal)

---

## Runtime Path Handling

Aplikasi akan mencari resource di urutan:

1. Working directory
2. Folder runtime (audio/, themes/, data/)
3. Classpath (JAR)

---

## Limitasi Teknis

- Widget Java bukan true desktop layer (batasan window manager)
- System tray tetap membutuhkan icon (API OS)
- Waktu sholat berbasis perhitungan astronomis + API (bukan jadwal resmi Kemenag)

---

## Lisensi & Kredit

Dikembangkan untuk pembelajaran dan implementasi Java Desktop Application.

© 2025 – JWSP Project
