package com.isankamil.mcjobid.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.isankamil.mcjobid.R
import com.isankamil.mcjobid.data.remote.firebase.FirestoreSyncService
import com.isankamil.mcjobid.domain.model.Testimonial
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Calendar

@Singleton
class TestimonialRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firestoreSyncService: FirestoreSyncService
) {
    private val collection = firestore.collection("testimonials")

    private fun dateOf(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, day, 10, 30, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getSeedTestimonials(): List<Testimonial> {
        return listOf(

            // ════ AGUSTUS 2026 (Bulan Saat Ini) ════

            // 8 Agt 2026 (Profesional, foto PT)
            Testimonial(
                id = "seed_p1",
                userName = "PT. Sinar Pangan Semesta",
                avatarResId = R.drawable.pt_sinar_pangan_semesta,
                rating = 5,
                review = "Fitur pembuatan invoice serba cepat. Klien langsung percaya karena kwitansinya profesional.",
                suggestion = "Desain invoice lebih beragam.",
                createdAt = dateOf(2026, 8, 8)
            ),
            // 5 Agt 2026 (Profesional, foto)
            Testimonial(
                id = "seed_p2",
                userName = "Rina Wulandari",
                avatarResId = R.drawable.perempuan_3,
                rating = 5,
                review = "Penyimpanan riwayat acara dan dokumen kerja sangat tertata rapi. Sangat merekomendasikan aplikasi ini.",
                suggestion = "Pertahankan kualitasnya.",
                createdAt = dateOf(2026, 8, 5)
            ),
            // 2 Agt 2026 (Profesional, foto)
            Testimonial(
                id = "seed_p3",
                userName = "Mega Utami",
                avatarResId = R.drawable.mega_utami,
                rating = 5,
                review = "Semua rekap honor MC mingguan terhitung otomatis tanpa ada yang terlewat. Luar biasa fiturnya.",
                suggestion = "Fitur export PDF lebih lengkap.",
                createdAt = dateOf(2026, 8, 2)
            ),

            // ════ JULI 2026 ════

            // 23 Jul 2026 — Hari Anak Nasional (Random)
            Testimonial(
                id = "seed_g1",
                userName = "indahpermata_mc",
                avatarResId = null,
                rating = 5,
                review = "Aplikasi ini benar-benar mengubah cara saya mengelola job. Dari yang manual jadi serba digital dan rapi.",
                suggestion = "Tambahkan fitur export ke Google Sheets.",
                createdAt = dateOf(2026, 7, 23)
            ),
            // 17 Jul 2026 (Profesional, foto PT)
            Testimonial(
                id = "seed_p4",
                userName = "PT. Media Inspirasi Bangsa",
                avatarResId = R.drawable.pt_media_inspirasi_bangsa,
                rating = 5,
                review = "Sistem manajemen job MC kami jadi lebih terorganisir. Tim MC kami sangat terbantu.",
                suggestion = "Luar biasa fiturnya.",
                createdAt = dateOf(2026, 7, 17)
            ),
            // 10 Jul 2026 (Profesional, foto)
            Testimonial(
                id = "seed_p5",
                userName = "Ahmad Fauzi",
                avatarResId = R.drawable.ahmad_fauzi,
                rating = 5,
                review = "Pencatatan sisa pembayaran dari klien sangat terstruktur. Tidak ada lagi drama lupa bayar.",
                suggestion = "Semakin sukses aplikasinya.",
                createdAt = dateOf(2026, 7, 10)
            ),

            // ════ JUNI 2026 ════

            // 21 Jun 2026 — Hari Musik Dunia (Profesional, foto)
            Testimonial(
                id = "seed_p6",
                userName = "Dian Sastro",
                avatarResId = R.drawable.dian_sastro,
                rating = 5,
                review = "Fitur pencatatan DP dan pelunasan akurat. Klien juga senang dapat kwitansi rapi.",
                suggestion = "Pertahankan performa aplikasi.",
                createdAt = dateOf(2026, 6, 21)
            ),
            // 10 Jun 2026 (Random)
            Testimonial(
                id = "seed_g2",
                userName = "BambangW_pro",
                avatarResId = null,
                rating = 4,
                review = "Sangat membantu rekap job event perusahaan. Tidak ada lagi catatan yang hilang atau tercecer.",
                suggestion = "Bisa tambahkan fitur laporan tahunan.",
                createdAt = dateOf(2026, 6, 10)
            ),
            // 1 Jun 2026 — Hari Lahir Pancasila (Profesional, foto)
            Testimonial(
                id = "seed_p7",
                userName = "Fitri Handayani",
                avatarResId = R.drawable.fitri,
                rating = 5,
                review = "Tidak perlu pusing bawa buku catatan fisik lagi. Semua jadwal nge-MC tersusun rapi.",
                suggestion = "Tambahkan fitur backup manual.",
                createdAt = dateOf(2026, 6, 1)
            ),

            // ════ MEI 2026 ════

            // 23 Mei 2026 — Hari Raya Waisak (Profesional, foto PT)
            Testimonial(
                id = "seed_p8",
                userName = "PT. Graha Land",
                avatarResId = R.drawable.pt_graha_land,
                rating = 5,
                review = "Catatan khusus rundown dan proteksi data job sangat aman dan mudah diakses kapan saja.",
                suggestion = "Sangat membantu sehari-hari.",
                createdAt = dateOf(2026, 5, 23)
            ),
            // 14 Mei 2026 — Kenaikan Yesus Kristus (Profesional, foto)
            Testimonial(
                id = "seed_p9",
                userName = "Pak Yanto",
                avatarResId = R.drawable.pak_yanto,
                rating = 4,
                review = "Aplikasi ringkas untuk rekap fee dan DP dari klien. Sangat praktis digunakan sehabis nge-MC.",
                suggestion = "Sudah bagus dan responsif.",
                createdAt = dateOf(2026, 5, 14)
            ),
            // 1 Mei 2026 — Hari Buruh / May Day (Random)
            Testimonial(
                id = "seed_g3",
                userName = "triSusanto_event",
                avatarResId = null,
                rating = 5,
                review = "Sistem jadwalnya joss! Mencegah job bentrok di hari yang sama.",
                suggestion = "Tambahkan filter bulan.",
                createdAt = dateOf(2026, 5, 1)
            ),

            // ════ APRIL 2026 ════

            // 21 Apr 2026 — Hari Kartini (Profesional, foto)
            Testimonial(
                id = "seed_p10",
                userName = "Dewi Anggraini",
                avatarResId = R.drawable.dewi_anggraini,
                rating = 5,
                review = "Sangat membantu buat atur jadwal MC wedding weekend yang padat. Nggak pernah bentrok lagi waktu dan lokasi acara.",
                suggestion = "Semoga makin banyak fitur pengingat otomatis.",
                createdAt = dateOf(2026, 4, 21)
            ),
            // 5 Apr 2026 — Hari Raya Idul Fitri (Profesional, foto)
            Testimonial(
                id = "seed_p11",
                userName = "Maya Putri Maharani",
                avatarResId = R.drawable.maya,
                rating = 5,
                review = "Input data klien baru jadi super cepat. Bahkan bisa dilakukan sambil standby di venue acara.",
                suggestion = "Pertahankan kecepatan aplikasinya.",
                createdAt = dateOf(2026, 4, 5)
            ),

            // ════ MARET 2026 ════

            // 19 Mar 2026 — Hari Raya Nyepi (Random)
            Testimonial(
                id = "seed_g4",
                userName = "nisaRahmawati92",
                avatarResId = null,
                rating = 5,
                review = "Fitur reminder H-1 sebelum acara sangat menolong. Tidak pernah lagi lupa persiapan mic dan busana.",
                suggestion = "Tambah notifikasi WhatsApp otomatis.",
                createdAt = dateOf(2026, 3, 19)
            ),
            // 8 Mar 2026 — Hari Perempuan Internasional (Profesional, foto)
            Testimonial(
                id = "seed_p12",
                userName = "Nisa Rahmawati",
                avatarResId = R.drawable.nisa,
                rating = 5,
                review = "Fitur checklist peralatan MC (mic, busana, cue card) membantu banget pas mau berangkat ke venue.",
                suggestion = "Tambahkan opsi cetak PDF untuk invoice.",
                createdAt = dateOf(2026, 3, 8)
            ),

            // ════ FEBRUARI 2026 ════

            // 17 Feb 2026 — Tahun Baru Imlek (Profesional, foto PT)
            Testimonial(
                id = "seed_p13",
                userName = "PT. Dana Mandiri",
                avatarResId = R.drawable.pt_dana_mandiri,
                rating = 5,
                review = "Aplikasi wajib buat MC event maupun wedding. Manajemen waktu dan invoice jadi profesional.",
                suggestion = "Tingkatkan respon sync.",
                createdAt = dateOf(2026, 2, 17)
            ),
            // 14 Feb 2026 — Peak Wedding Season (Random)
            Testimonial(
                id = "seed_g5",
                userName = "FajarRmdn_mc",
                avatarResId = null,
                rating = 4,
                review = "Dashboard keuangan MC-nya lengkap banget. Pemasukan dan pengeluaran langsung keliatan per bulan.",
                suggestion = "Kalau bisa ada grafik bulanannya.",
                createdAt = dateOf(2026, 2, 14)
            ),

            // ════ JANUARI 2026 (Peluncuran Awal Aplikasi) ════

            // 27 Jan 2026 — Isra Mi'raj (Profesional, foto)
            Testimonial(
                id = "seed_p14",
                userName = "Tri Susanto",
                avatarResId = R.drawable.tri_susanto,
                rating = 4,
                review = "Sangat membantu koordinasi jadwal job luar kota. Lokasi venue dan kontak PIC bisa dicatat lengkap.",
                suggestion = "Notifikasi pengingat diperbanyak.",
                createdAt = dateOf(2026, 1, 27)
            ),
            // 20 Jan 2026 (Profesional, foto)
            Testimonial(
                id = "seed_p15",
                userName = "Rian Hidayat",
                avatarResId = R.drawable.rian_hidayat,
                rating = 5,
                review = "Sangat membantu untuk kelola invoice dan kwitansi ke WO tanpa perlu bikin manual di laptop.",
                suggestion = "Tingkatkan integrasi ke WhatsApp.",
                createdAt = dateOf(2026, 1, 20)
            ),
            // 15 Jan 2026 (Random)
            Testimonial(
                id = "seed_g6",
                userName = "DoniKusuma_EO",
                avatarResId = null,
                rating = 5,
                review = "Sebagai MC pemula, aplikasi ini membuat saya terlihat sangat profesional di mata klien.",
                suggestion = "Makin maju MCJOBID.",
                createdAt = dateOf(2026, 1, 15)
            ),
            // 10 Jan 2026 (Profesional, foto PT)
            Testimonial(
                id = "seed_p16",
                userName = "PT. Cipta Kreasi Event",
                avatarResId = R.drawable.pt_cipta_kreasi_event,
                rating = 4,
                review = "Bagus untuk lacak jadwal acara corporate dan gala dinner. Catatan detail acara cukup lengkap.",
                suggestion = "Tampilan kalender dibuat lebih luas.",
                createdAt = dateOf(2026, 1, 10)
            ),
            // 5 Jan 2026 (Profesional, foto)
            Testimonial(
                id = "seed_p17",
                userName = "Bambang Wijaya",
                avatarResId = R.drawable.bambang_wijaya,
                rating = 5,
                review = "Buat MC yang aktif seperti saya, fitur draf invoice dan template reminder sangat menolong.",
                suggestion = "Bisa tambah kategori jenis event.",
                createdAt = dateOf(2026, 1, 5)
            ),
            // 3 Jan 2026 (Random)
            Testimonial(
                id = "seed_g7",
                userName = "user_mc2026",
                avatarResId = null,
                rating = 4,
                review = "Pengingat H-3 dan H-1 sangat membantu persiapan gaun dan sepatu MC.",
                suggestion = "Pengingat via SMS/WA.",
                createdAt = dateOf(2026, 1, 3)
            ),
            // 2 Jan 2026 (Profesional, foto PT)
            Testimonial(
                id = "seed_p18",
                userName = "PT. Nusantara Tekno",
                avatarResId = R.drawable.pt_nusantara_tekno,
                rating = 5,
                review = "Fitur pengeluaran dan pemasukan MC membantu hitung laba bersih bulanan secara transparan.",
                suggestion = "Sudah sangat lengkap.",
                createdAt = dateOf(2026, 1, 2)
            ),
            // 1 Jan 2026 — Peluncuran Tahun Baru 2026 (Random)
            Testimonial(
                id = "seed_g8",
                userName = "EkaRahmawati_MC",
                avatarResId = null,
                rating = 5,
                review = "Manajemen kontak klien dan katering terorganisir dengan rapi dalam satu aplikasi.",
                suggestion = "Ekspor laporan bulanan.",
                createdAt = dateOf(2026, 1, 1)
            ),
            Testimonial(
                id = "seed_g9",
                userName = "agusSetia_mc",
                avatarResId = null,
                rating = 5,
                review = "Saya MC freelance dan aplikasi ini benar-benar membantu track semua job tanpa ribet.",
                suggestion = "Semoga ada fitur kolaborasi dengan sesama MC.",
                createdAt = dateOf(2026, 1, 1)
            ),
            Testimonial(
                id = "seed_g10",
                userName = "AnisaFitri_wedding",
                avatarResId = null,
                rating = 5,
                review = "Cocok banget buat MC wedding yang jadwalnya padat. Satu aplikasi untuk semua kebutuhan administrasi.",
                suggestion = "Bisa tambah fitur template rundown.",
                createdAt = dateOf(2026, 1, 1)
            ),
            Testimonial(
                id = "seed_g11",
                userName = "SitiNurhaliza_mc",
                avatarResId = null,
                rating = 5,
                review = "Aplikasi yang benar-benar diciptakan untuk kebutuhan MC profesional. Lengkap dan mudah dipakai.",
                suggestion = "Minta fitur mode offline.",
                createdAt = dateOf(2026, 1, 1)
            ),
            Testimonial(
                id = "seed_g12",
                userName = "AgusSetia88",
                avatarResId = null,
                rating = 4,
                review = "Pengingat jadwal H-1 acara bekerja dengan baik. Sangat berguna waktu job padat di akhir bulan.",
                suggestion = "Pilihan warna tema bisa ditambah.",
                createdAt = dateOf(2026, 1, 1)
            ),
            Testimonial(
                id = "seed_g13",
                userName = "PakYanto_EO",
                avatarResId = null,
                rating = 4,
                review = "Sangat berguna untuk catat riwayat job dan honorarium dari berbagai Event Organizer.",
                suggestion = "Mode gelap dipercantik lagi.",
                createdAt = dateOf(2026, 1, 1)
            ),
            Testimonial(
                id = "seed_g14",
                userName = "rudi_soundman",
                avatarResId = null,
                rating = 4,
                review = "Rekomendasi dari teman MC, dan ternyata memang worth it. Semua data job tersimpan rapi sejak pertama pakai.",
                suggestion = "Semoga terus dikembangkan.",
                createdAt = dateOf(2026, 1, 1)
            ),
            Testimonial(
                id = "seed_g15",
                userName = "FajarRmdn_id",
                avatarResId = null,
                rating = 4,
                review = "Awal tahun langsung coba aplikasi ini. Langsung jatuh cinta karena desainnya clean dan fiturnya lengkap.",
                suggestion = "Tambah fitur statistik tahunan.",
                createdAt = dateOf(2026, 1, 1)
            )
        )
    }

    /**
     * One-shot fetch — used as an emergency fallback.
     * For live updates, use observeTestimonials() instead.
     */
    suspend fun getTestimonials(): Result<List<Testimonial>> {
        val seed = getSeedTestimonials()
        return try {
            val snapshot = collection.get().await()
            val remoteList = snapshot.documents.mapNotNull { doc ->
                try {
                    val item = doc.toObject(Testimonial::class.java)
                    item?.copy(id = doc.id)
                } catch (e: Exception) {
                    null
                }
            }
            // Real user reviews (remoteList) ALWAYS placed first (newest to oldest)
            val sortedRemote = remoteList.sortedByDescending { it.createdAt }
            val sortedSeed = seed.sortedByDescending { it.createdAt }
            val combined = (sortedRemote + sortedSeed).distinctBy { it.id }
            Result.success(combined)
        } catch (e: Exception) {
            Result.success(seed.sortedByDescending { it.createdAt })
        }
    }

    /**
     * Real-time Flow of testimonials from Firestore + seed fallback.
     * - Ulasan asli dari user (remoteList) SELALU berada di posisi teratas (urutan pertama).
     * - Ulasan milik akun saat ini otomatis disematkan di posisi puncak.
     * - Otomatis ter-update secara real-time di seluruh perangkat pengguna.
     */
    fun observeTestimonials(currentUserId: String = ""): Flow<List<Testimonial>> {
        val seed = getSeedTestimonials()
        return firestoreSyncService.observeTestimonials()
            .map { remoteList ->
                // Sort remote real reviews newest-first
                val sortedRemote = remoteList.sortedByDescending { it.createdAt }
                // Sort seed showcase reviews newest-first
                val sortedSeed = seed.sortedByDescending { it.createdAt }
                
                // Real user testimonials ALWAYS take precedence at the very top
                val combined = (sortedRemote + sortedSeed).distinctBy { it.id }
                combined.sortedWith(
                    compareByDescending<Testimonial> { it.userId == currentUserId && currentUserId.isNotBlank() }
                        .thenByDescending { !it.id.startsWith("seed_") }
                        .thenByDescending { it.createdAt }
                )
            }
            .catch {
                emit(seed.sortedByDescending { it.createdAt })
            }
    }

    suspend fun addTestimonial(testimonial: Testimonial): Result<Unit> {
        val docId = if (testimonial.id.isNotBlank()) {
            testimonial.id
        } else if (testimonial.userId.isNotBlank()) {
            testimonial.userId
        } else {
            java.util.UUID.randomUUID().toString()
        }
        val now = System.currentTimeMillis()
        val finalTestimonial = testimonial.copy(
            id = docId,
            createdAt = if (testimonial.createdAt <= 0) now else testimonial.createdAt
        )
        return firestoreSyncService.saveTestimonialToFirestore(finalTestimonial)
    }

    /**
     * Hapus testimoni dari Firestore secara permanen (Developer Mode only).
     * Seed testimoni (id diawali "seed_") tidak dapat dihapus karena
     * hanya ada di kode, bukan di Firestore collection.
     */
    suspend fun deleteTestimonial(id: String): Result<Unit> {
        if (id.startsWith("seed_")) return Result.failure(Exception("Seed testimonials cannot be deleted"))
        return try {
            firestoreSyncService.deleteTestimonial(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
