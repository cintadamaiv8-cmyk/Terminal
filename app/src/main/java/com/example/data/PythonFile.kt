package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "python_files")
data class PythonFile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
