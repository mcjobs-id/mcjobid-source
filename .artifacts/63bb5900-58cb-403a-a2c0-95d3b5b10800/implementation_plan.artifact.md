# Implementasi Custom Date Range Filter UI

Penyelesaian fitur filter tanggal kustom pada layar Analisis Performa untuk mendukung pemilihan bulan spesifik dan rentang tanggal (range) menggunakan Material 3 Pickers.

## User Review Required

> [!IMPORTANT]
> Filter bar akan diubah menjadi **Scrollable Row** agar 7 opsi filter (Bulan Ini, 3 Bulan, Tahun Ini, Bulan Lalu, Semua, Bulan Khusus, Rentang Hari) tidak bertumpuk dan tetap proporsional di layar ponsel kecil.

## Proposed Changes

### UI Layer

#### [MODIFY] [AnalyticsScreen.kt](file:///C:/Users/Muhari/Documents/MCJOBID/MCJOBID/app/src/main/java/com/isankamil/mcjobid/ui/screen/analytics/AnalyticsScreen.kt)
- **State Management**: Tambahkan `showMonthPicker` dan `showDateRangePicker` boolean state.
- **Filter Bar Redesign**:
    - Ganti `Row` statis dengan `LazyRow` (scrollable).
    - Tambahkan indikator visual (ikon kalender) pada opsi kustom.
    - Hubungkan klik opsi kustom ke pembukaan dialog.
- **Dialog Implementation**:
    - Implementasi `MonthPickerDialog`: Menggunakan `DatePicker` Material 3 (atau `DatePickerDialog` native jika lebih simpel untuk pemilihan bulan) untuk memilih bulan & tahun.
    - Implementasi `DateRangePickerDialog`: Menggunakan `DateRangePicker` Material 3.
- **Data Binding**:
    - Update `NetIncomeHeroCard` untuk menampilkan `data.displayLabel` (agar teks seperti "Januari 2026" muncul jika filter kustom aktif).
    - Update `TopAppBar` title/subtitle jika diperlukan.

## Verification Plan

### Automated Tests
- Tidak ada unit test baru untuk UI, fokus pada integrasi state.

### Manual Verification
1.  Buka layar **Analisis**.
2.  Klik opsi **Bulan Khusus**, pilih bulan tertentu, dan pastikan data ter-update dengan label bulan tersebut.
3.  Klik opsi **Rentang Hari**, pilih 2 tanggal berbeda, dan pastikan label "dd MMM - dd MMM yyyy" muncul dan data sesuai.
4.  Cek responsivitas filter bar saat di-scroll secara horizontal.
