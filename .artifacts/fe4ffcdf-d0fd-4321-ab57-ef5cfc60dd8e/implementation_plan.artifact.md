# Perbaikan Tampilan Tombol Login Google

User melaporkan bahwa tombol login Google tidak berada di tengah (center) secara visual dibandingkan dengan teks "Selamat Datang", dan tidak memiliki ikon Google. Rencana ini akan memperbaiki tata letak (layout) dan menambahkan ikon Google yang hilang.

## User Review Required

> [!NOTE]
> Perubahan tata letak akan membuat logo McJobId tetap di atas, namun memusatkan (centering) konten "Selamat Datang" dan tombol Login di tengah layar agar lebih seimbang secara visual.

## Proposed Changes

### [UI Components]

#### [NEW] [ic_google.xml](file:///C:/Users/Muhari/Documents/MCJOBID/MCJOBID/app/src/main/res/drawable/ic_google.xml)
Membuat ikon vektor Google "G" standar untuk digunakan pada tombol login.

#### [MODIFY] [LoginScreen.kt](file:///C:/Users/Muhari/Documents/MCJOBID/MCJOBID/app/src/main/java/com/isankamil/mcjobid/ui/screen/auth/LoginScreen.kt)
- Menambahkan `Icon` Google ke dalam `Button` Google Login.
- Mengubah `verticalArrangement` pada `Column` utama dari `Arrangement.SpaceBetween` menjadi tata letak yang lebih terpusat menggunakan `Spacer` dengan `weight(1f)`.
- Memperbaiki `Row` di dalam `Button` agar ikon dan teks sejajar dengan benar di tengah.

## Verification Plan

### Automated Tests
- Menjalankan build Gradle untuk memastikan tidak ada error pada resource XML baru.
- `gradle_build(":app:assembleDebug")`

### Manual Verification
- Menjalankan aplikasi dan memeriksa layar Login.
- Memastikan ikon Google muncul di tombol.
- Memastikan teks "Selamat Datang" dan tombol Login terlihat seimbang di tengah layar.
- Mengambil screenshot untuk verifikasi akhir.
