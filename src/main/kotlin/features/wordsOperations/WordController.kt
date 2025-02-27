package com.wordsaver.features.wordsOperations

import com.wordsaver.features.database.base.insertIntoTable
import com.wordsaver.features.database.words.Words
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import com.auth0.jwt.exceptions.JWTVerificationException

class WordController(private val call: ApplicationCall) {

    private fun getUserEmailFromToken(): String {
        val principal = call.principal<JWTPrincipal>()
        return principal?.payload?.subject 
            ?: throw IllegalStateException("No user email in token")
    }

    suspend fun saveNewWord() {
        try {
            val userEmail = getUserEmailFromToken()
            val wordReceiveRemote = call.receive<WordReceiveRemote>()// Используем email из токена вместо userId из запроса

            val wordDto = Words.fetchWord(
                userWord = wordReceiveRemote.word,
                _userEmail = userEmail
            )
            if (wordDto != null) {
                call.respond(HttpStatusCode.Conflict, "Word already exists")
            } else {
                try {
                    insertIntoTable(Words, wordReceiveRemote)
                    call.respond(SuccessSaveResponse("Saved"))
                } catch (e: ExposedSQLException) {
                    call.respond(HttpStatusCode.Conflict, "Word already exists")
                }
            }
        } catch (e: Exception) {
            when (e) {
                is IllegalStateException, 
                is JWTVerificationException -> throw e // Пробрасываем дальше для обработки в StatusPages
                else -> call.respond(
                    HttpStatusCode.InternalServerError, 
                    "An error occurred: ${e.message}"
                )
            }
        }
    }

    suspend fun getWord() {
        try {
            val userEmail = getUserEmailFromToken()
            val request = call.receive<GetWordRequest>()
            
            if (request.userEmail != userEmail) {
                call.respond(HttpStatusCode.Forbidden, "Access denied")
                return
            }

            // Выполняем запрос к базе данных
            val wordModel = transaction {
                Words.selectAll().where {
                    (Words.word eq request.word) and (Words.userEmail eq request.userEmail)
                }.singleOrNull()
            }

            // Если слово найдено, возвращаем его
            if (wordModel != null) {
                val response = WordResponseRemote(
                    word = wordModel[Words.word],
                    translation = wordModel[Words.translation],
                    userEmail = wordModel[Words.userEmail],
                    failed = wordModel[Words.failed],
                    success = wordModel[Words.success],
                    addedAt = wordModel[Words.addedAt].toString() // Преобразуем LocalDateTime в строку
                )
                call.respond(response)
            } else {
                // Если слово не найдено, возвращаем 404
                call.respond(HttpStatusCode.NotFound, "Word not found")
            }
        } catch (e: Exception) {
            when (e) {
                is IllegalStateException, 
                is JWTVerificationException -> throw e
                else -> call.respond(
                    HttpStatusCode.InternalServerError, 
                    "An error occurred: ${e.message}"
                )
            }
        }
    }

    suspend fun getWords() {
        try {
            val userEmail = getUserEmailFromToken()
            val request = call.receive<GetWordsByUserRequest>()
            
            if (request.userEmail != userEmail) {
                call.respond(HttpStatusCode.Forbidden, "Access denied")
                return
            }

            // Выполняем запрос к базе данных
            val words = transaction {
                Words.selectAll().where {
                    Words.userEmail eq request.userEmail // Ищем все слова по userId
                }.map { row ->
                    WordResponseRemote(
                        word = row[Words.word],
                        translation = row[Words.translation],
                        userEmail = row[Words.userEmail],
                        failed = row[Words.failed],
                        success = row[Words.success],
                        addedAt = row[Words.addedAt].toString() // Преобразуем LocalDateTime в строку
                    )
                }
            }

            // Возвращаем список слов
            if (words.isNotEmpty()) {
                call.respond(words)
            } else {
                // Если слова не найдены, возвращаем 404
                call.respond(HttpStatusCode.NotFound, "No words found for this user")
            }
        } catch (e: Exception) {
            when (e) {
                is IllegalStateException, 
                is JWTVerificationException -> throw e
                else -> call.respond(
                    HttpStatusCode.InternalServerError, 
                    "An error occurred: ${e.message}"
                )
            }
        }
    }
}