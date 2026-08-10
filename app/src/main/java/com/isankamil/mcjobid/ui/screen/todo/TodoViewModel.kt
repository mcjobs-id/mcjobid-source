package com.isankamil.mcjobid.ui.screen.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.isankamil.mcjobid.data.repository.TodoRepository
import com.isankamil.mcjobid.domain.model.TodoCategory
import com.isankamil.mcjobid.domain.model.TodoItem
import com.isankamil.mcjobid.domain.model.TodoPriority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TodoTab(val title: String) {
    SEMUA("Semua"),
    BELUM_SELESAI("Tertunda"),
    SELESAI("Selesai")
}

data class TodoStats(
    val total: Int = 0,
    val pending: Int = 0,
    val completed: Int = 0,
    val percentage: Float = 0f,
    val highPriorityPending: Int = 0
)

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    private val _rawTodos = MutableStateFlow<List<TodoItem>>(emptyList())
    
    private val _selectedTab = MutableStateFlow(TodoTab.SEMUA)
    val selectedTab: StateFlow<TodoTab> = _selectedTab.asStateFlow()

    private val _selectedCategory = MutableStateFlow<TodoCategory?>(null)
    val selectedCategory: StateFlow<TodoCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeTodos().collect { list ->
                _rawTodos.value = list
            }
        }
    }

    val stats: StateFlow<TodoStats> = _rawTodos.map { list ->
        val total = list.size
        val completed = list.count { it.isCompleted }
        val pending = total - completed
        val percentage = if (total > 0) completed.toFloat() / total.toFloat() else 0f
        val highPriorityPending = list.count { !it.isCompleted && it.priority == TodoPriority.TINGGI }
        TodoStats(
            total = total,
            pending = pending,
            completed = completed,
            percentage = percentage,
            highPriorityPending = highPriorityPending
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TodoStats())

    val filteredTodos: StateFlow<List<TodoItem>> = combine(
        _rawTodos,
        _selectedTab,
        _selectedCategory,
        _searchQuery
    ) { todos, tab, category, query ->
        todos.filter { item ->
            val matchTab = when (tab) {
                TodoTab.SEMUA -> true
                TodoTab.BELUM_SELESAI -> !item.isCompleted
                TodoTab.SELESAI -> item.isCompleted
            }
            val matchCategory = category == null || item.category == category
            val matchQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.notes.contains(query, ignoreCase = true)

            matchTab && matchCategory && matchQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setTab(tab: TodoTab) {
        _selectedTab.value = tab
    }

    fun setCategory(category: TodoCategory?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun addTodo(
        title: String,
        notes: String,
        category: TodoCategory,
        priority: TodoPriority,
        dueDate: Long
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val result = repository.addTodo(
                title = title,
                notes = notes,
                category = category,
                priority = priority,
                dueDate = dueDate
            )
            if (result.isSuccess) {
                _snackbarMessage.value = "Tugas baru berhasil ditambahkan! 🚀"
            }
        }
    }

    fun updateTodo(todo: TodoItem) {
        viewModelScope.launch {
            val result = repository.updateTodo(todo)
            if (result.isSuccess) {
                _snackbarMessage.value = "Tugas berhasil diperbarui! ✍️"
            }
        }
    }

    fun toggleTodo(todo: TodoItem) {
        viewModelScope.launch {
            repository.toggleCompletion(todo)
        }
    }

    fun deleteTodo(id: String) {
        viewModelScope.launch {
            val result = repository.deleteTodo(id)
            if (result.isSuccess) {
                _snackbarMessage.value = "Tugas telah dihapus 🗑️"
            }
        }
    }

    fun deleteCompletedTodos() {
        viewModelScope.launch {
            val result = repository.deleteCompletedTodos()
            if (result.isSuccess) {
                _snackbarMessage.value = "Semua tugas selesai telah dibersihkan ✨"
            }
        }
    }

    fun applyMcTemplates() {
        viewModelScope.launch {
            val result = repository.applyPredefinedMcTemplates()
            if (result.isSuccess) {
                _snackbarMessage.value = "Paket checklist persiapan MC berhasil dimuat! 🎉"
            }
        }
    }

    fun clearSnackbar() {
        _snackbarMessage.value = null
    }
}
