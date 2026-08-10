# MASTER PANDUAN RILIS APLIKASI ANDROID
### Berlaku untuk semua proyek Android — Wajib dikerjakan bersama Agent AI

---

> !! PERATURAN WAJIB !!
> Semua tahapan di bawah ini HARUS dilakukan bersama Agent AI (Antigravity / Gemini).
> Jangan lakukan sendiri tanpa panduan Agent karena rawan salah konfigurasi.
> Cukup copy-paste perintah ke Agent dan minta dia mengeksekusi.

---

## DAFTAR PROYEK

| Proyek | Repo Source | Repo Releases | Firebase Project |
|---|---|---|---|
| MC Job ID | mcjobs-id/mcjobid-source | mcjobs-id/mcjobid-releases | MC Jobs |
| *(proyek baru)* | *(isi nanti)* | *(isi nanti)* | *(isi nanti)* |

---

## PERSIAPAN AWAL (Hanya Sekali per Proyek Baru)

Katakan ke Agent AI:
"Saya punya proyek Android baru bernama [NAMA PROYEK].
Tolong setup GitHub repository source code dan releases,
lalu hubungkan ke Android Studio dan buat sistem OTA update."

Yang akan dilakukan Agent:
1. Install GitHub CLI jika belum ada
2. Login GitHub
3. Buat repo [nama]-source (PRIVATE) untuk kode sumber
4. Buat repo [nama]-releases (PUBLIC) untuk hosting APK
5. Init Git di folder proyek
6. Push kode pertama ke GitHub
7. Setup sistem OTA update di kode aplikasi

---

## TAHAPAN RILIS UPDATE (Setiap Ada Fitur Baru atau Bug Fix)

### LANGKAH 1 - Kerjakan Perubahan Kode di Android Studio

Buat perubahan kode yang diperlukan di Android Studio.
Setelah selesai, commit ke GitHub:
- Tekan Ctrl+K
- Centang file yang berubah
- Tulis pesan: "feat: deskripsi fitur baru" atau "fix: deskripsi bug"
- Klik Commit and Push

---

### LANGKAH 2 - Minta Agent AI untuk Build dan Rilis

Salin perintah ini ke Agent AI:

"Tolong bantu saya rilis versi baru [NAMA APLIKASI].
versionCode baru: [angka]
versionName baru: [versi]
Catatan rilis: [deskripsi fitur/perbaikan]
Lakukan build, upload ke GitHub releases, dan update Firestore."

Yang akan dilakukan Agent:
1. Naikkan versionCode dan versionName di build.gradle.kts
2. Jalankan .\gradlew assembleRelease
3. Upload APK ke GitHub release
4. Update Firestore app_config/update_info
5. Verifikasi URL download bisa diakses
6. Konfirmasi banner sudah aktif

---

### LANGKAH 3 - Verifikasi (Agent yang Lakukan)

Agent akan memastikan:
- Build berhasil tanpa error
- APK bisa diunduh (HTTP 200)
- Firestore sudah terupdate
- Aplikasi bisa diinstall ke device

---

## DETAIL TEKNIS SISTEM OTA

### Cara Kerja
1. Developer update Firestore app_config/update_info
2. Aplikasi user mendeteksi otomatis secara real-time
3. Banner muncul di dashboard jika ada versi baru
4. User klik Perbarui -> APK terunduh -> terinstall otomatis

### Jenis Update
- isForceUpdate: false -> Banner muncul, user bisa abaikan
- isForceUpdate: true  -> Modal wajib muncul, user harus update

### Field Firestore yang Diupdate Setiap Rilis
- latestVersionCode     (int64)   -> naikkan setiap rilis
- latestVersionName     (string)  -> versi string misal 1.1.0
- minSupportedVersionCode (int64) -> versi minimum yang didukung
- apkDownloadUrl        (string)  -> URL APK dari GitHub releases
- apkSizeMb             (string)  -> ukuran file APK
- releaseNotes          (string)  -> deskripsi perubahan
- isForceUpdate         (boolean) -> wajib update atau tidak
- releaseDate           (string)  -> tanggal rilis

---

## CHECKLIST RILIS (Untuk Diberikan ke Agent AI)

Salin ini ke Agent:

"Tolong lakukan checklist rilis berikut:
[ ] Naikkan versionCode di build.gradle.kts
[ ] Build APK release
[ ] Upload ke GitHub releases
[ ] Update Firestore
[ ] Test download URL
[ ] Konfirmasi selesai"

---

## PERINTAH DARURAT

### Jika Update Wajib (Bug Kritis)
Katakan ke Agent:
"Ada bug kritis di [NAMA APLIKASI], tolong set isForceUpdate jadi true
di Firestore agar semua user wajib update."

### Jika Ingin Batalkan Banner Update
Katakan ke Agent:
"Tolong set latestVersionCode di Firestore kembali ke [angka lama]
untuk [NAMA APLIKASI] agar banner update tidak muncul."

### Jika APK Tidak Bisa Diunduh
Katakan ke Agent:
"APK [NAMA APLIKASI] tidak bisa diunduh, tolong cek dan perbaiki
URL di Firestore app_config/update_info."

---

## CATATAN PENTING

1. Repo SOURCE selalu PRIVATE (kode rahasia)
2. Repo RELEASES selalu PUBLIC (APK perlu diunduh tanpa login)
3. GitHub CLI sudah terinstall di: C:\Program Files\GitHub CLI\
4. GitHub account: mcjobs-id
5. Setiap proyek punya Firebase project sendiri
6. JANGAN pernah commit file google-services.json ke repo public

---

## LOG RILIS PER PROYEK

### MC Job ID
| Tanggal | Versi | versionCode | Keterangan |
|---|---|---|---|
| 2026-08-10 | 1.0.0 | 1 | Rilis perdana |
| *(rilis baru)* | | | |

---

*File ini adalah master panduan untuk semua proyek Android.*
*Selalu perbarui tabel Log Rilis setelah setiap rilis.*
*Terakhir diperbarui: 2026-08-10*
