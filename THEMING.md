# Panduan Tema Kustom JWSP

JWSP mendukung sistem tema berbasis JSON yang powerful. Pengguna dapat membuat, mengunggah, dan mengganti tema dengan mudah.

## Lokasi Tema

Tema disimpan di direktori `themes/` dalam folder aplikasi.

## Struktur JSON

File tema yang valid (contoh: `modern-blue.json`) harus mengikuti struktur ini:

```json
{
  "meta": {
    "name": "Tema Kustom Saya",
    "author": "Nama Anda",
    "version": "1.0"
  },
  "colors": {
    "textPrimary": "#FFFFFF",
    "textSecondary": "#CCCCCC",
    "background": "#1E1E1E",
    "accent": "#4CAF50"
  },
  "widget": {
    "backgroundOpacity": 0.75,
    "cornerRadius": 16,
    "padding": 12
  },
  "font": {
    "family": "Segoe UI",
    "titleSize": 18,
    "normalSize": 14
  }
}
```

### Penjelasan Properti:

- `textPrimary`: Warna heading dan jam utama.
- `textSecondary`: Warna teks sekunder.
- `background`: Warna latar belakang.
- `accent`: Warna aksen/highlight.
- `backgroundOpacity`: Mengontrol transparansi widget (0.0 hingga 1.0).
- `cornerRadius`: Kebulatan sudut widget.
- `padding`: Jarak padding dalam widget.
- `family`: Jenis font yang terinstal di sistem Anda (contoh: "Inter", "Segoe UI", "Roboto").
- `titleSize`: Ukuran font untuk judul.
- `normalSize`: Ukuran font untuk teks normal.

## Cara Menerapkan Tema

1. Buat file `.json` Anda.
2. Buka JWSP.
3. Klik **"Tema & Penampilan"** di jendela utama.
4. Klik **"Load JSON..."** dan pilih file Anda.
5. Klik **"Apply Selected"** (Terapkan yang Dipilih).

## Perilaku Tema Bawaan OS

- **Linux**: Default menggunakan tema modern gelap.
- **Windows**: Default menggunakan tema terang adaptif yang lembut jika tidak ada preferensi yang diatur.
- **Transparansi**: Widget secara otomatis mendukung efek glassmorphism berdasarkan nilai `backgroundOpacity` tema Anda.

## Tips Membuat Tema

- Gunakan kode warna hex (#RRGGBB) untuk konsistensi.
- Nilai `backgroundOpacity` yang rendah (0.3-0.5) memberikan efek transparan lebih kuat.
- Pastikan font yang dipilih terinstal di sistem target.
- Uji kontras warna untuk keterbacaan optimal.
