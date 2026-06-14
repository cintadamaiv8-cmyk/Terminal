package com.example.data

import kotlinx.coroutines.flow.Flow

class FileRepository(private val dao: PythonFileDao) {
    val allFiles: Flow<List<PythonFile>> = dao.getAllFiles()

    suspend fun insert(file: PythonFile): Long = dao.insertFile(file)

    suspend fun update(file: PythonFile) = dao.updateFile(file)

    suspend fun deleteById(id: Int) = dao.deleteFileById(id)
}
