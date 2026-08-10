# Panduan Lengkap — GitHub, OTA Update & Android Studio
### MC Job ID | mcjobs-id | Dibuat: 2026-08-10

---

## DAFTAR REPOSITORY (Sudah Aktif)

| Repository | Fungsi | Visibilitas | URL |
|---|---|---|---|
| mcjobid-source | Backup kode sumber | PRIVATE | github.com/mcjobs-id/mcjobid-source |
| mcjobid-releases | Hosting file APK | PUBLIC* | github.com/mcjobs-id/mcjobid-releases |

*Public agar APK bisa diunduh langsung oleh aplikasi tanpa autentikasi.
Source code tetap PRIVATE - aman.

---

## ANDROID STUDIO - SINKRONISASI KE GITHUB

### Cara Commit & Push dari Android Studio (Tanpa Terminal)

Setelah kamu mengubah kode di Android Studio:

1. Tekan Ctrl+K  ->  muncul jendela Commit
2. Centang file yang berubah
3. Tulis pesan commit (misal: "fix: perbaikan tampilan hero card")
4. Klik "Commit and Push"
5. Klik "Push" -> kode langsung tersimpan ke github.com/mcjobs-id/mcjobid-source

### Cara Lihat Perubahan

- Menu VCS -> Git -> Show Git Log  (riwayat semua perubahan)
- Menu VCS -> Show Changes  (perubahan yang belum di-commit)

### Catatan Penting

- Sinkronisasi kode sumber ke GitHub BUKAN sinkronisasi yang menyebabkan banner update muncul
- Sinkronisasi kode = backup kode sumber saja
- Banner update muncul = hanya dari Firestore app_config/update_info

---

## NOTIFIKASI UPDATE - CARA KERJANYA

### Yang Sudah Ada (Otomatis)

Saat ini sistem bekerja seperti ini:
- User membuka aplikasi -> sistem cek Firestore -> jika ada versi baru -> banner muncul
- Tidak perlu tindakan developer apapun setelah isi Firestore

### Apakah Ada Push Notifikasi Khusus?

Belum ada push notifikasi (notifikasi saat aplikasi ditutup).
Saat ini: user hanya melihat banner ketika membuka aplikasi.
Ini sudah cukup untuk aplikasi premium eksklusif.

Jika kamu ingin tambahkan push notifikasi di masa depan:
- Gunakan Firebase Cloud Messaging (FCM) - gratis sampai 1 juta notif/bulan
- Kamu bisa kirim notifikasi dari Firebase Console tanpa kode tambahan

---

## TEMUAN KRITIS YANG SUDAH DIPERBAIKI

### Masalah: Private Repo = APK Tidak Bisa Diunduh (HTTP 404)
- Sebab: Release APK dibuat saat repo masih private
- Akibat: Sistem OTA tidak akan berfungsi sama sekali
- Solusi: mcjobid-releases diubah ke PUBLIC + release dibuat ulang
- Status: DIPERBAIKI - HTTP 200, file 42.3 MB bisa diunduh

### Masalah: Kode Sumber Tidak Ter-backup
- Sebab: Folder proyek belum punya Git repo
- Akibat: Jika laptop rusak/format, semua kode hilang
- Solusi: Dibuat mcjobid-source (private) + initial commit
- Status: DIPERBAIKI - 2 commit sudah ada di GitHub

---

## LANGKAH RILIS UPDATE (Lengkap, Setiap Ada Versi Baru)

### Langkah 1 - Update Kode & Naikkan Version

Buka app/build.gradle.kts:

    versionCode = 2        <- naikkan ini
    versionName = "1.1.0"  <- sesuaikan

Commit ke GitHub source (Ctrl+K di Android Studio):
Pesan: "release: MC Job ID v1.1.0"

---

### Langkah 2 - Build APK Release

    .\gradlew assembleRelease

APK ada di: app\build\outputs\apk\release\app-release.apk
Rename: mcjobid_v1.1.0.apk

---

### Langkah 3 - Upload ke GitHub Release (via Terminal)

    C:/Users/Muhari/.gemini/antigravity/bin;C:\Users\Muhari\AppData\Roaming\Antigravity\bin;C:\Program Files\Common Files\Oracle\Java\javapath;C:\Windows\system32;C:\Windows;C:\Windows\System32\Wbem;C:\Windows\System32\WindowsPowerShell\v1.0\;C:\Windows\System32\OpenSSH\;C:\Program Files\NVIDIA Corporation\NVIDIA App\NvDLISR;C:\Program Files (x86)\NVIDIA Corporation\PhysX\Common;C:\Program Files\nodejs\;C:\Program Files\Git\cmd;C:\Users\Muhari\AppData\Local\Microsoft\WindowsApps;C:\Users\Muhari\AppData\Local\Programs\Microsoft VS Code\bin;C:\Users\Muhari\AppData\Roaming\npm;C:\Users\Muhari\AppData\Local\Programs\Ollama;C:\Users\Muhari\AppData\Local\GitHubDesktop\bin;C:\adb;C:\Users\Muhari\AppData\Local\Microsoft\WindowsApps;C:\Users\Muhari\AppData\Local\Programs\Microsoft VS Code\bin;C:\Users\Muhari\AppData\Roaming\npm;C:\Users\Muhari\AppData\Local\Programs\Ollama;C:\Users\Muhari\AppData\Local\GitHubDesktop\bin;C:\Users\Muhari\AppData\Local\Programs\Antigravity IDE\bin;C:\Users\Muhari\AppData\Local\GitHubCopilotCLI\ += ";C:\Program Files\GitHub CLI\"
    
    gh release create v1.1.0 "mcjobid_v1.1.0.apk" `
        --repo mcjobs-id/mcjobid-releases `
        --title "MC Job ID v1.1.0" `
        --notes "Deskripsi fitur baru"

Link download otomatis tersedia:
https://github.com/mcjobs-id/mcjobid-releases/releases/download/v1.1.0/mcjobid_v1.1.0.apk

---

### Langkah 4 - Update Firestore

Buka: console.firebase.google.com -> Firestore -> app_config -> update_info

Update field:
- latestVersionCode  -> 2
- latestVersionName  -> 1.1.0
- apkDownloadUrl     -> [URL dari Langkah 3]
- releaseNotes       -> ringkasan perubahan
- isForceUpdate      -> false (atau true jika wajib)
- releaseDate        -> tanggal hari ini

Klik Save -> Banner muncul otomatis di semua user.

---

## CHECKLIST RILIS

    [ ] 1. Naikkan versionCode & versionName di build.gradle.kts
    [ ] 2. .\gradlew assembleRelease
    [ ] 3. gh release create vX.X.X "mcjobid_vX.X.X.apk" --repo mcjobs-id/mcjobid-releases --title "..." --notes "..."
    [ ] 4. Update Firestore app_config/update_info -> Save
    [ ] 5. (Opsional) Commit kode ke mcjobid-source via Android Studio
    [ ] SELESAI

---

## LINK PENTING

- Source code   : https://github.com/mcjobs-id/mcjobid-source (PRIVATE)
- APK releases  : https://github.com/mcjobs-id/mcjobid-releases (PUBLIC)
- Release v1.0.0: https://github.com/mcjobs-id/mcjobid-releases/releases/tag/v1.0.0
- Firebase      : https://console.firebase.google.com
- Firestore path: app_config/update_info

---

*Panduan ini disimpan di root proyek sebagai referensi permanen.*
*Terakhir diperbarui: 2026-08-10*
