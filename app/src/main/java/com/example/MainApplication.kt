package com.example

import android.app.Application
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.data.AppDatabase
import com.example.data.FileRepository

class MainApplication : Application() {
    lateinit var repository: FileRepository

    override fun onCreate() {
        super.onCreate()
        if (!Python.isStarted()) {
            Python.start(AndroidPlatform(this))
        }
        val database = AppDatabase.getDatabase(this)
        repository = FileRepository(database.pythonFileDao())
    }
}
