package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.FileRepository
import com.example.data.PythonFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class IdeViewModel(private val repository: FileRepository) : ViewModel() {
    val allFiles: StateFlow<List<PythonFile>> = repository.allFiles
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _currentFile = MutableStateFlow<PythonFile?>(null)
    val currentFile: StateFlow<PythonFile?> = _currentFile

    fun selectFile(file: PythonFile) {
        _currentFile.value = file
    }

    fun createFile(name: String) {
        viewModelScope.launch {
            val file = PythonFile(name = name, content = "print(\"Hello Python!\")")
            val id = repository.insert(file)
            _currentFile.value = file.copy(id = id.toInt())
        }
    }

    fun updateContent(newContent: String) {
        _currentFile.value?.let { file ->
            val updated = file.copy(content = newContent)
            _currentFile.value = updated
        }
    }

    fun saveCurrentFile() {
        _currentFile.value?.let { file ->
            viewModelScope.launch {
                repository.update(file)
            }
        }
    }
}

class IdeViewModelFactory(private val repository: FileRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IdeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return IdeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
