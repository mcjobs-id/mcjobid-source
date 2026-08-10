package com.isankamil.mcjobid.ui.components.feedback

import com.isankamil.mcjobid.domain.model.Booking

/**
 * Explicit sealed class for Dialog State management across MCJOBID screens.
 * Avoids boolean state conflicts (showDialog, isLoading, isSuccess, etc.).
 */
sealed class DialogState {
    object Hidden : DialogState()
    
    data class Loading(val message: String = "Sedang menyimpan...") : DialogState()

    data class Confirm(
        val title: String,
        val description: String,
        val primaryCta: String = "Konfirmasi",
        val secondaryCta: String? = "Batal",
        val onConfirm: () -> Unit
    ) : DialogState()

    data class Warning(
        val title: String,
        val description: String,
        val primaryCta: String = "Mengerti",
        val secondaryCta: String? = null,
        val onPrimary: () -> Unit = {}
    ) : DialogState()

    data class Error(
        val title: String = "Belum berhasil disimpan",
        val description: String = "Data belum tersimpan. Periksa koneksi atau coba lagi.",
        val primaryCta: String = "Coba Lagi",
        val secondaryCta: String? = "Batal",
        val onRetry: () -> Unit,
        val onDismiss: () -> Unit = {}
    ) : DialogState()

    data class Success(
        val title: String,
        val description: String,
        val primaryCta: String = "Lihat Detail",
        val secondaryCta: String? = "Selesai",
        val onPrimary: () -> Unit = {},
        val onSecondary: () -> Unit = {}
    ) : DialogState()

    data class Info(
        val title: String,
        val description: String,
        val primaryCta: String = "Tutup",
        val onDismiss: () -> Unit = {}
    ) : DialogState()

    data class Destructive(
        val title: String,
        val description: String,
        val primaryCta: String, // E.g. "Hapus Job", "Batalkan Job"
        val secondaryCta: String = "Batal",
        val onConfirm: () -> Unit
    ) : DialogState()

    data class Conflict(
        val title: String = "Jadwal Bentrok",
        val description: String = "MCJOBID menemukan job lain pada waktu yang sama.",
        val conflictingBookings: List<Booking>,
        val primaryCta: String = "Tetap Simpan",
        val secondaryCta: String = "Kembali & Ubah Jadwal",
        val onOverride: () -> Unit,
        val onDismiss: () -> Unit
    ) : DialogState()

    data class PaymentConfirmation(
        val bookingTitle: String,
        val amount: Long,
        val paymentMethod: String,
        val paymentDate: String,
        val totalFee: Long,
        val currentPaid: Long,
        val remainingAfterPayment: Long,
        val isOverpayment: Boolean = false,
        val onConfirm: () -> Unit,
        val onEdit: () -> Unit
    ) : DialogState()

    data class UnsavedChanges(
        val title: String = "Perubahan belum disimpan.",
        val description: String = "Data yang sudah kamu masukkan akan hilang jika keluar sekarang.",
        val primaryCta: String = "Tetap Edit",
        val secondaryCta: String = "Keluar",
        val draftCta: String? = "Lanjutkan Nanti",
        val onStayEdit: () -> Unit,
        val onExit: () -> Unit,
        val onSaveDraft: (() -> Unit)? = null
    ) : DialogState()

    data class DeleteClient(
        val clientName: String,
        val jobCount: Int,
        val onArchive: () -> Unit,
        val onViewHistory: () -> Unit,
        val onDismiss: () -> Unit
    ) : DialogState()

    data class MCDayMode(
        val eventName: String,
        val onStart: () -> Unit,
        val onCancel: () -> Unit
    ) : DialogState()

    data class NotificationPermission(
        val onEnable: () -> Unit,
        val onLater: () -> Unit
    ) : DialogState()
}

/**
 * Snackbar Event model with Undo support.
 */
data class SnackbarEvent(
    val message: String,
    val actionLabel: String? = null,
    val type: SnackbarType = SnackbarType.SUCCESS,
    val onAction: (() -> Unit)? = null
)

enum class SnackbarType {
    SUCCESS, WARNING, ERROR, INFO
}

/**
 * Network / Offline Sync Status indicator.
 */
enum class SyncStatus {
    SYNCED,
    SYNCING,
    PENDING,
    FAILED
}

/**
 * User-friendly Error Message Mapping utility.
 */
object ErrorMapper {
    fun getMessage(throwable: Throwable?): String {
        if (throwable == null) return "Terjadi kesalahan. Silakan coba lagi."
        val msg = throwable.message ?: ""
        return when {
            msg.contains("network", ignoreCase = true) || msg.contains("ConnectException") || msg.contains("UnknownHostException") ->
                "Tidak ada koneksi internet."
            msg.contains("permission", ignoreCase = true) || msg.contains("PERMISSION_DENIED") ->
                "Kamu tidak memiliki izin untuk melakukan tindakan ini."
            msg.contains("timeout", ignoreCase = true) || msg.contains("SocketTimeoutException") ->
                "Server belum merespons."
            msg.contains("database", ignoreCase = true) || msg.contains("SQLite") || msg.contains("Room") ->
                "Data belum dapat disimpan."
            else -> "Terjadi kesalahan. Silakan coba lagi."
        }
    }
}
