package com.isankamil.mcjobid.ui.screen.pricelist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isankamil.mcjobid.data.repository.RateCardRepository
import com.isankamil.mcjobid.domain.model.RateCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditRateCardViewModel @Inject constructor(
    private val rateCardRepository: RateCardRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val rateCardId: String? = savedStateHandle.get<String>("rateCardId")

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _category = MutableStateFlow("Wedding")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _price = MutableStateFlow("")
    val price: StateFlow<String> = _price.asStateFlow()

    private val _durationHours = MutableStateFlow("3.0")
    val durationHours: StateFlow<String> = _durationHours.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _inclusions = MutableStateFlow<List<String>>(emptyList())
    val inclusions: StateFlow<List<String>> = _inclusions.asStateFlow()

    private val _addOns = MutableStateFlow<List<String>>(emptyList())
    val addOns: StateFlow<List<String>> = _addOns.asStateFlow()

    private val _terms = MutableStateFlow("")
    val terms: StateFlow<String> = _terms.asStateFlow()

    private val _fieldErrors = MutableStateFlow<Map<String, String>>(emptyMap())
    val fieldErrors: StateFlow<Map<String, String>> = _fieldErrors.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess: SharedFlow<Boolean> = _saveSuccess.asSharedFlow()

    val isEditMode: Boolean = !rateCardId.isNullOrBlank()

    init {
        if (!rateCardId.isNullOrBlank()) {
            viewModelScope.launch {
                val loaded = rateCardRepository.getRateCardById(rateCardId)
                loaded?.let { rc ->
                    _title.value = rc.title
                    _category.value = rc.category
                    _price.value = if (rc.price > 0) rc.price.toString() else ""
                    _durationHours.value = rc.durationHours.toString()
                    _description.value = rc.description
                    _inclusions.value = rc.inclusions
                    _addOns.value = rc.addOns
                    _terms.value = rc.terms
                }
            }
        }
    }

    fun updateTitle(value: String) {
        _title.value = value
        clearFieldError("title")
    }

    fun updateCategory(value: String) {
        _category.value = value
    }

    fun updatePrice(value: String) {
        _price.value = value
        clearFieldError("price")
    }

    fun updateDurationHours(value: String) {
        _durationHours.value = value
    }

    fun updateDescription(value: String) {
        _description.value = value
    }

    fun updateTerms(value: String) {
        _terms.value = value
    }

    fun addInclusion(item: String) {
        val trimmed = item.trim()
        if (trimmed.isNotBlank() && !_inclusions.value.contains(trimmed)) {
            _inclusions.value = _inclusions.value + trimmed
        }
    }

    fun removeInclusion(index: Int) {
        if (index in _inclusions.value.indices) {
            _inclusions.value = _inclusions.value.toMutableList().apply { removeAt(index) }
        }
    }

    fun addAddOn(item: String) {
        val trimmed = item.trim()
        if (trimmed.isNotBlank() && !_addOns.value.contains(trimmed)) {
            _addOns.value = _addOns.value + trimmed
        }
    }

    fun removeAddOn(index: Int) {
        if (index in _addOns.value.indices) {
            _addOns.value = _addOns.value.toMutableList().apply { removeAt(index) }
        }
    }

    private fun clearFieldError(field: String) {
        if (_fieldErrors.value.containsKey(field)) {
            _fieldErrors.value = _fieldErrors.value - field
        }
    }

    fun saveRateCard() {
        val errors = mutableMapOf<String, String>()
        val titleVal = _title.value.trim()
        val priceVal = _price.value.toLongOrNull() ?: 0L
        val durationVal = _durationHours.value.toDoubleOrNull() ?: 3.0

        if (titleVal.isBlank()) {
            errors["title"] = "Nama paket wajib diisi"
        }
        if (priceVal <= 0) {
            errors["price"] = "Tarif harga paket harus lebih dari Rp 0"
        }

        if (errors.isNotEmpty()) {
            _fieldErrors.value = errors
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            val rc = RateCard(
                id = rateCardId ?: UUID.randomUUID().toString(),
                category = _category.value,
                title = titleVal,
                price = priceVal,
                durationHours = durationVal,
                description = _description.value.trim(),
                inclusions = _inclusions.value.filter { it.isNotBlank() },
                addOns = _addOns.value.filter { it.isNotBlank() },
                terms = _terms.value.trim(),
                isDefault = false,
                createdAt = LocalDateTime.now().toString()
            )
            rateCardRepository.saveRateCard(rc)
            _isSaving.value = false
            _saveSuccess.emit(true)
        }
    }
}
