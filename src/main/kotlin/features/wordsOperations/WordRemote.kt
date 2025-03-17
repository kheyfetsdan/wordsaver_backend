package com.wordsaver.features.wordsOperations

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Expression

@Serializable
data class WordReceiveRemote(
    val word: String,
    val translation: String,
)

@Serializable
data class WordIdReceiveRemote(
    val id: Int,
    val word: String,
    val translation: String,
)

@Serializable
data class WordIdStatReceiveRemote(
    val success: Boolean
)

@Serializable
data class SuccessSaveResponse(
    val response: String
)

@Serializable
data class WordResponseRemote(
    val id: Int,
    val word: String,
    val translation: String,
    val failed: Double,
    val success: Double,
    val addedAt: String
)

@Serializable
data class WordList(
    val wordList: List<WordResponseRemote>,
    val total: Int,
    val page: Int
)

@Serializable
class GetWordsRequest(
    val sortingParam: String,
    val sortingDirection: String,
    val page: Int,
    val pageSize: Int
)

@Serializable
data class GetWordsByUserRequest(
    val userEmail: String
)

@Serializable
data class QuizRequest(
    val previousWord: String,
)

@Serializable
data class QuizResponse(
    val word: String,
    val trueTranslation: String,
    val translation1: String,
    val translation2: String,
    val translation3: String
)

