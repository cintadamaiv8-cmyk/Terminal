package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PythonFileDao {
    @Query("SELECT * FROM python_files ORDER BY timestamp DESC")
    fun getAllFiles(): Flow<List<PythonFile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: PythonFile): Long

    @Update
    suspend fun updateFile(file: PythonFile)

    @Query("DELETE FROM python_files WHERE id = :id")
    suspend fun deleteFileById(id: Int)
}
