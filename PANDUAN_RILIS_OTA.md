# Panduan Rilis Pembaruan OTA — MC Job ID
### Langkah A sampai Z | Repository: github.com/mcjobs-id/mcjobid-releases

---

## Status Setup
- GitHub Account  : mcjobs-id (AKTIF)
- Repository      : mcjobs-id/mcjobid-releases (PRIVATE, AKTIF)
- Rilis Pertama   : v1.0.0 sudah ada
- Sistem OTA      : Terpasang di aplikasi (HomeScreen + HomeViewModel)

---

## PERSIAPAN AWAL - SELESAI (Tidak Perlu Diulang)

Repository sudah dibuat: https://github.com/mcjobs-id/mcjobid-releases
Rilis v1.0.0 sudah ada sebagai referensi.

---

## LANGKAH RILIS UPDATE (Lakukan Setiap Ada Versi Baru)

### Langkah 1 - Naikkan versionCode di Kode

Buka: app/build.gradle.kts

    defaultConfig {
        versionCode = 2       <- naikkan setiap rilis (sekarang: 1)
        versionName = "1.1.0" <- sesuaikan
    }

Simpan file.

---

### Langkah 2 - Build APK Release

Buka terminal di folder proyek, jalankan:

    .\gradlew assembleRelease

APK tersimpan di:

    app\build\outputs\apk\release\app-release.apk

Rename menjadi: mcjobid_v1.1.0.apk

---

### Langkah 3 - Upload APK ke GitHub Release (via Terminal)

Cara cepat via GitHub CLI (sudah terinstall di komputer ini):

    C:/Users/Muhari/.gemini/antigravity/bin;C:\Users\Muhari\AppData\Roaming\Antigravity\bin;C:\Program Files\Common Files\Oracle\Java\javapath;C:\Windows\system32;C:\Windows;C:\Windows\System32\Wbem;C:\Windows\System32\WindowsPowerShell\v1.0\;C:\Windows\System32\OpenSSH\;C:\Program Files\NVIDIA Corporation\NVIDIA App\NvDLISR;C:\Program Files (x86)\NVIDIA Corporation\PhysX\Common;C:\Program Files\nodejs\;C:\Program Files\Git\cmd;C:\Users\Muhari\AppData\Local\Microsoft\WindowsApps;C:\Users\Muhari\AppData\Local\Programs\Microsoft VS Code\bin;C:\Users\Muhari\AppData\Roaming\npm;C:\Users\Muhari\AppData\Local\Programs\Ollama;C:\Users\Muhari\AppData\Local\GitHubDesktop\bin;C:\adb;C:\Users\Muhari\AppData\Local\Microsoft\WindowsApps;C:\Users\Muhari\AppData\Local\Programs\Microsoft VS Code\bin;C:\Users\Muhari\AppData\Roaming\npm;C:\Users\Muhari\AppData\Local\Programs\Ollama;C:\Users\Muhari\AppData\Local\GitHubDesktop\bin;C:\Users\Muhari\AppData\Local\Programs\Antigravity IDE\bin;C:\Users\Muhari\AppData\Local\GitHubCopilotCLI\ += ";C:\Program Files\GitHub CLI\"

    gh release create v1.1.0 "mcjobid_v1.1.0.apk" `
        --repo mcjobs-id/mcjobid-releases `
        --title "MC Job ID v1.1.0" `
        --notes "Deskripsi perubahan di versi ini"

Setelah selesai, link download APK otomatis tersedia di:

    https://github.com/mcjobs-id/mcjobid-releases/releases/download/v1.1.0/mcjobid_v1.1.0.apk

(Ganti v1.1.0 dan nama file sesuai versi yang baru)

---

### Langkah 4 - Update Firestore

1. Buka https://console.firebase.google.com
2. Pilih project MC Jobs > Firestore Database
3. Navigasi: app_config > update_info
4. Perbarui field:

    latestVersionCode     -> 2  (sesuai versionCode baru)
    latestVersionName     -> 1.1.0
    minSupportedVersionCode -> 1
    apkDownloadUrl        -> https://github.com/mcjobs-id/mcjobid-releases/releases/download/v1.1.0/mcjobid_v1.1.0.apk
    apkSizeMb             -> 42 MB
    releaseNotes          -> Ringkasan fitur baru
    isForceUpdate         -> false
    releaseDate           -> 2026-08-10

5. Klik Save

---

### Langkah 5 - Selesai! Otomatis Terjadi di User

Detik itu juga setelah Save di Firestore:
- Semua user yang membuka aplikasi melihat banner pembaruan
- User klik "Perbarui" -> APK terunduh -> Installer Android terbuka -> Update selesai
- Kamu tidak perlu menyentuh kode lagi

---

## CHECKLIST RILIS (Copy setiap kali rilis)

    [ ] 1. Naikkan versionCode di build.gradle.kts
    [ ] 2. .\gradlew assembleRelease
    [ ] 3. Rename APK -> mcjobid_vX.X.X.apk
    [ ] 4. gh release create vX.X.X "mcjobid_vX.X.X.apk" --repo mcjobs-id/mcjobid-releases --title "MC Job ID vX.X.X" --notes "..."
    [ ] 5. Update Firestore app_config/update_info -> Save
    [ ] SELESAI

---

## UPDATE WAJIB (Forced)

Jika ada bug kritis, set di Firestore:
    isForceUpdate -> true

Modal muncul otomatis tanpa bisa ditutup user. Setelah stabil, kembalikan ke false.

---

## Link Penting

- Repository  : https://github.com/mcjobs-id/mcjobid-releases
- Releases    : https://github.com/mcjobs-id/mcjobid-releases/releases
- Firebase    : https://console.firebase.google.com
- Firestore   : app_config/update_info

---

*Panduan ini disimpan di root proyek. Dibuat: 2026-08-10*
