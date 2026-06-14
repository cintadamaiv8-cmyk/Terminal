package com.example

import com.chaquo.python.Python

object PythonRunner {
    fun runPythonCode(code: String): String {
        return try {
            val py = Python.getInstance()
            val engine = py.getModule("engine")
            engine.callAttr("run", code).toString()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }
}
