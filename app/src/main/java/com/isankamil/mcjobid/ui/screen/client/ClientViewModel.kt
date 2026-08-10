package com.isankamil.mcjobid.ui.screen.client

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isankamil.mcjobid.data.repository.BookingRepository
import com.isankamil.mcjobid.data.repository.ClientRepository
import com.isankamil.mcjobid.domain.model.Booking
import com.isankamil.mcjobid.domain.model.Client
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class ClientMetrics(
    val totalJobs: Int = 0,
    val totalRevenue: Long = 0,
    val totalPaid: Long = 0,
    val totalOutstanding: Long = 0,
    val lastEventDate: String? = null
)

enum class ClientFilterTab {
    ALL,
    FAVORITE,
    ARCHIVED
}

@HiltViewModel
class ClientViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedTab = MutableStateFlow(ClientFilterTab.ALL)
    val selectedTab: StateFlow<ClientFilterTab> = _selectedTab.asStateFlow()

    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()

    private val _selectedClient = MutableStateFlow<Client?>(null)
    val selectedClient: StateFlow<Client?> = _selectedClient.asStateFlow()

    private val _selectedClientBookings = MutableStateFlow<List<Booking>>(emptyList())
    val selectedClientBookings: StateFlow<List<Booking>> = _selectedClientBookings.asStateFlow()

    private val _allBookings = MutableStateFlow<List<Booking>>(emptyList())
    val allBookings: StateFlow<List<Booking>> = _allBookings.asStateFlow()

    private val _deleteError = MutableStateFlow<String?>(null)
    val deleteError: StateFlow<String?> = _deleteError.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            clientRepository.getAllClientsIncludingArchived().collect { list ->
                _clients.value = list
            }
        }
        viewModelScope.launch {
            bookingRepository.getAllBookings().collect { list ->
                _allBookings.value = list
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterTab(tab: ClientFilterTab) {
        _selectedTab.value = tab
    }

    fun selectClient(client: Client?) {
        _selectedClient.value = client
        if (client != null) {
            viewModelScope.launch {
                bookingRepository.getBookingsByClient(client.name, client.id).collect { list ->
                    _selectedClientBookings.value = list
                }
            }
        } else {
            _selectedClientBookings.value = emptyList()
        }
    }

    fun saveClient(name: String, phone: String?, email: String?, company: String?, pic: String?, notes: String?) {
        viewModelScope.launch {
            val now = LocalDateTime.now()
            val newClient = Client(
                id = "c_${System.currentTimeMillis()}",
                name = name.trim(),
                phone = phone?.trim()?.ifBlank { null },
                email = email?.trim()?.ifBlank { null },
                company = company?.trim()?.ifBlank { null },
                pic = pic?.trim()?.ifBlank { null },
                notes = notes?.trim()?.ifBlank { null },
                isFavorite = false,
                isArchived = false,
                createdAt = now,
                updatedAt = now
            )
            clientRepository.saveClient(newClient)
            selectClient(newClient)
        }
    }

    fun updateClient(id: String, name: String, phone: String?, email: String?, company: String?, pic: String?, notes: String?) {
        viewModelScope.launch {
            val existing = _clients.value.firstOrNull { it.id == id } ?: return@launch
            val now = LocalDateTime.now()
            val updated = existing.copy(
                name = name.trim(),
                phone = phone?.trim()?.ifBlank { null },
                email = email?.trim()?.ifBlank { null },
                company = company?.trim()?.ifBlank { null },
                pic = pic?.trim()?.ifBlank { null },
                notes = notes?.trim()?.ifBlank { null },
                updatedAt = now
            )
            clientRepository.updateClient(updated)
            if (_selectedClient.value?.id == id) {
                _selectedClient.value = updated
            }
        }
    }

    fun toggleFavorite(clientId: String) {
        viewModelScope.launch {
            clientRepository.toggleFavoriteClient(clientId)
            if (_selectedClient.value?.id == clientId) {
                val current = _selectedClient.value
                if (current != null) {
                    _selectedClient.value = current.copy(isFavorite = !current.isFavorite)
                }
            }
        }
    }

    fun getMetricsForClient(client: Client): ClientMetrics {
        // Exclude CANCELLED bookings for accurate financial metrics
        val clientJobs = _allBookings.value.filter {
            (it.client == client.name || it.clientId == client.id) && it.status != Booking.BookingStatus.CANCELLED
        }
        val totalRevenue = clientJobs.sumOf { it.fee }
        val totalPaid = clientJobs.sumOf { it.dp }
        val totalOutstanding = clientJobs.sumOf { it.outstanding }
        val lastEvent = clientJobs.maxByOrNull { it.date }?.date?.toString()

        return ClientMetrics(
            totalJobs = clientJobs.size,
            totalRevenue = totalRevenue,
            totalPaid = totalPaid,
            totalOutstanding = totalOutstanding,
            lastEventDate = lastEvent
        )
    }

    fun archiveClient(clientId: String) {
        viewModelScope.launch {
            clientRepository.archiveClient(clientId)
            if (_selectedClient.value?.id == clientId) {
                _selectedClient.value = null
            }
        }
    }

    fun unarchiveClient(clientId: String) {
        viewModelScope.launch {
            clientRepository.unarchiveClient(clientId)
            if (_selectedClient.value?.id == clientId) {
                _selectedClient.value = _selectedClient.value?.copy(isArchived = false)
            }
        }
    }

    fun deleteClient(client: Client) {
        viewModelScope.launch {
            // Protect: do not delete client if they have active bookings
            val activeBookings = _allBookings.value.filter {
                (it.client == client.name || it.clientId == client.id) &&
                it.status !in listOf(Booking.BookingStatus.CANCELLED, Booking.BookingStatus.COMPLETED)
            }
            if (activeBookings.isNotEmpty()) {
                _deleteError.value = "Client '${client.name}' masih memiliki ${activeBookings.size} job aktif. Selesaikan atau batalkan job terlebih dahulu."
                return@launch
            }
            clientRepository.deleteClient(client)
            if (_selectedClient.value?.id == client.id) {
                _selectedClient.value = null
            }
        }
    }

    fun clearDeleteError() {
        _deleteError.value = null
    }

    val filteredClients: StateFlow<List<Client>> = combine(
        _clients,
        _searchQuery,
        _selectedTab
    ) { list, query, tab ->
        val tabFiltered = when (tab) {
            ClientFilterTab.ALL -> list.filter { !it.isArchived }
            ClientFilterTab.FAVORITE -> list.filter { !it.isArchived && it.isFavorite }
            ClientFilterTab.ARCHIVED -> list.filter { it.isArchived }
        }

        if (query.trim().isBlank()) {
            tabFiltered
        } else {
            val q = query.trim()
            tabFiltered.filter {
                it.name.contains(q, ignoreCase = true) ||
                (it.company?.contains(q, ignoreCase = true) == true) ||
                (it.phone?.contains(q) == true) ||
                (it.email?.contains(q, ignoreCase = true) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}
