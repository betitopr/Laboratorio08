package com.example.lab08

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class TaskViewModel(private val dao: TaskDao) : ViewModel() {


    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filterStatus = MutableStateFlow<Boolean?>(null)
    val filterStatus: StateFlow<Boolean?> = _filterStatus

    private val _sortOrder = MutableStateFlow("created_at")
    val sortOrder: StateFlow<String> = _sortOrder

    init {
        viewModelScope.launch {
            loadTasks()
        }
    }

    private suspend fun loadTasks() {
        _tasks.value = when {
            searchQuery.value.isNotEmpty() -> dao.searchTasks(searchQuery.value)
            filterStatus.value != null -> dao.getTasksByStatus(filterStatus.value!!)
            else -> dao.getTasksOrdered(sortOrder.value)
        }
    }

    fun addTask(description: String) {
        viewModelScope.launch {
            dao.insertTask(Task(description = description))
            loadTasks()
        }
    }

    fun updateTaskDescription(task: Task, newDescription: String) {
        viewModelScope.launch {
            dao.updateTask(task.copy(description = newDescription))
            loadTasks()
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            dao.updateTask(task.copy(isCompleted = !task.isCompleted))
            loadTasks()
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            dao.deleteTask(task)
            loadTasks()
        }
    }

    fun deleteAllTasks() {
        viewModelScope.launch {
            dao.deleteAllTasks()
            loadTasks()
        }
    }

    fun setSearchQuery(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query
            loadTasks()
        }
    }

    fun setFilterStatus(status: Boolean?) {
        viewModelScope.launch {
            _filterStatus.value = status
            loadTasks()
        }
    }

    fun setSortOrder(order: String) {
        viewModelScope.launch {
            _sortOrder.value = order
            loadTasks()
        }
    }
}
