package com.isankamil.mcjobid.ui.screen.pricelist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isankamil.mcjobid.data.repository.RateCardRepository
import com.isankamil.mcjobid.domain.model.RateCard
import com.isankamil.mcjobid.util.Formatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PriceListViewModel @Inject constructor(
    private val repository: RateCardRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Semua")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _allRateCards = MutableStateFlow<List<RateCard>>(emptyList())

    val filteredRateCards: StateFlow<List<RateCard>> = combine(
        _allRateCards,
        _searchQuery,
        _selectedCategory
    ) { cards, query, cat ->
        cards.filter { card ->
            val matchesCategory = if (cat == "Semua") true else card.category.equals(cat, ignoreCase = true)
            val matchesQuery = query.isBlank() || 
                    card.title.contains(query, ignoreCase = true) ||
                    card.description.contains(query, ignoreCase = true) ||
                    card.inclusions.any { it.contains(query, ignoreCase = true) } ||
                    card.addOns.any { it.contains(query, ignoreCase = true) }
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val categories = listOf("Semua", "Wedding", "Corporate", "Private Event", "Government", "Other")

    init {
        viewModelScope.launch {
            repository.getRateCards().collect { list ->
                _allRateCards.value = list
                _isLoading.value = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelect(category: String) {
        _selectedCategory.value = category
    }

    fun duplicateRateCard(card: RateCard) {
        viewModelScope.launch {
            val duplicated = card.copy(
                id = UUID.randomUUID().toString(),
                title = "${card.title} (Salinan)",
                isDefault = false,
                createdAt = LocalDateTime.now().toString()
            )
            repository.saveRateCard(duplicated)
        }
    }

    fun deleteRateCard(id: String) {
        viewModelScope.launch {
            repository.deleteRateCard(id)
        }
    }

    fun generateWhatsAppQuote(
        card: RateCard,
        clientName: String = "",
        eventDate: String = "",
        customNote: String = ""
    ): String {
        val formattedPrice = Formatter.formatCurrency(card.price)
        val durationText = if (card.durationHours > 0) "${card.durationHours} Jam" else "Fleksibel"
        
        val greeting = if (clientName.isNotBlank()) {
            "Halo Yth. *${clientName.trim()}*,\nTerima kasih telah menghubungi kami. Berikut adalah rincian penawaran resmi paket pemandu acara (MC):"
        } else {
            "Halo,\nBerikut adalah rincian penawaran resmi paket pemandu acara (MC):"
        }

        val dateLine = if (eventDate.isNotBlank()) "📅 Estimasi Tanggal: *${eventDate.trim()}*\n" else ""

        val inclusionsText = if (card.inclusions.isNotEmpty()) {
            card.inclusions.joinToString("\n") { "  ✓ $it" }
        } else {
            "  ✓ Layanan MC Profesional"
        }

        val addOnsText = if (card.addOns.isNotEmpty()) {
            "\n\n💡 *OPSI TAMBAHAN (ADD-ONS):*\n" + card.addOns.joinToString("\n") { "  • $it" }
        } else ""

        val termsText = if (card.terms.isNotBlank()) {
            "\n\n📜 *KETENTUAN BOOKING (S&K):*\n${card.terms}"
        } else ""

        val noteText = if (customNote.isNotBlank()) {
            "\n\n💬 *CATATAN KHUSUS:*\n${customNote.trim()}"
        } else ""

        return """
$greeting

✨ *${card.title.uppercase()}* ✨
🏷️ Kategori: ${card.category}
💰 Honorarium: *${formattedPrice}* (Estimasi ${durationText})
$dateLine
📝 *RINGKASAN LAYANAN:*
${card.description.ifBlank { "Pemandu acara profesional berdedikasi dengan alur acara tertata rapi." }}

✅ *FASILITAS SUDAH TERMASUK:*
$inclusionsText$addOnsText$termsText$noteText

---
Silakan hubungi kami kembali untuk konfirmasi ketersediaan jadwal & penguncian tanggal. Terima kasih! 🙏
*Dikelola via MCJOB.ID*
        """.trimIndent()
    }
}
