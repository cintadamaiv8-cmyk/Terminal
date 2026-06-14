package com.example.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

fun highlightPython(code: String): AnnotatedString {
    val keywords = listOf(
        "def", "return", "print", "if", "else",
        "for", "while", "try", "except", "in", "class"
    )
    
    // We use regex to match whole words while preserving spaces and newlines
    val pattern = "\\b(${keywords.joinToString("|")})\\b".toRegex()
    
    return buildAnnotatedString {
        var currentIndex = 0
        val keywordColor = Color(0xFFFF5555)
        val defaultColor = Color.White

        pattern.findAll(code).forEach { result ->
            val start = result.range.first
            val end = result.range.last + 1
            
            // Append non-keyword text
            if (start > currentIndex) {
                withStyle(SpanStyle(color = defaultColor)) {
                    append(code.substring(currentIndex, start))
                }
            }
            
            // Append keyword
            withStyle(SpanStyle(color = keywordColor)) {
                append(code.substring(start, end))
            }
            currentIndex = end
        }
        
        // Append remaining text
        if (currentIndex < code.length) {
            withStyle(SpanStyle(color = defaultColor)) {
                append(code.substring(currentIndex))
            }
        }
    }
}
