package com.wordsaver.features.wordsOperations

import kotlinx.serialization.Serializable

@Serializable
data class WordReceiveRemote(
    val word: String,
    val translation: String,
    val failed: Double,
    val success: Double
)

@Serializable
data class SuccessSaveResponse(
    val response: String
)

@Serializable
data class WordResponseRemote(
    val word: String,
    val translation: String,
    val failed: Double,
    val success: Double,
    val addedAt: String
)

@Serializable
data class GetWordRequest(
    val word: String,
)

@Serializable
data class GetWordsByUserRequest(
    val userEmail: String
)

