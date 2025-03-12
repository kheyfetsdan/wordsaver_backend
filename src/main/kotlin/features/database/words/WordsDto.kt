package com.wordsaver.features.database.words

import java.time.LocalDateTime

data class WordDto(
    val word: String,
    val translation: String,
    val userId: String,
    val failed: Double,
    val success: Double,
    val addedAt: LocalDateTime = LocalDateTime.now()
)

data class WordStat(
    val failed: Double,
    val success: Double,
    val addedAt: LocalDateTime = LocalDateTime.now()
)