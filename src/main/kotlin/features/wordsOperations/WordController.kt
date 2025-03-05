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
import org.jetbrains.exposed.sql.transactions.transaction
import com.auth0.jwt.exceptions.JWTVerificationException
import com.wordsaver.features.database.users.Users
import com.wordsaver.features.database.words.WordDto
import com.wordsaver.features.database.words.Words.addedAt
import com.wordsaver.features.database.words.Words.failed
import com.wordsaver.features.database.words.Words.success
import com.wordsaver.features.database.words.Words.translation
import com.wordsaver.features.database.words.Words.word
import org.jetbrains.exposed.sql.*
import kotlin.random.Random

class WordController(private val call: ApplicationCall) {

    private fun getUserEmailFromToken(): String {
        val principal = call.principal<JWTPrincipal>()
        return principal?.payload?.subject
            ?: throw IllegalStateException("No user email in token")
    }

    suspend fun saveNewWord() {
        try {
            val userId = Users.fetchUserId(getUserEmailFromToken()).toString()
            val wordReceiveRemote =
                call.receive<WordReceiveRemote>()// Используем email из токена вместо userId из запроса

            val searchedWord = Words.fetchWord(
                userWord = wordReceiveRemote.word,
                _userId = userId
            )
            val wordDto = WordDto(
                word = wordReceiveRemote.word,
                translation = wordReceiveRemote.translation,
                userId = userId,
                failed = 0.0,
                success = 0.0
            )
            if (searchedWord != null) {
                call.respond(HttpStatusCode.Conflict, "Word already exists")
            } else {
                try {
                    insertIntoTable(Words, wordDto)
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
            val userId = Users.fetchUserId(getUserEmailFromToken()).toString()
            val request = call.receive<GetWordRequest>()
            //val token = call.request.header("Authorization")?.removePrefix("Bearer ")

            val randomWord = transaction {
                Words.select(word).where {
                    (Words.userId eq userId)
                }.toList()
            }
            println(randomWord)

            if (randomWord.isEmpty()) {
                call.respond(HttpStatusCode.NotFound, "No words exist")
            }
            val randomIndex = Random.nextInt(randomWord.size)
            val rWord = randomWord[randomIndex][word]

            // Выполняем запрос к базе данных
            val wordModel = transaction {
                addLogger(StdOutSqlLogger)
                Words.selectAll().where {
                    ((Words.userId eq userId) and (Words.word eq rWord))
                }.singleOrNull()
            }

            // Если слово найдено, возвращаем его
            if (wordModel != null) {
                val response = WordResponseRemote(
                    word = wordModel[Words.word],
                    translation = wordModel[Words.translation],
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
            val userId = Users.fetchUserId(getUserEmailFromToken()).toString()

            // Выполняем запрос к базе данных
            val words = transaction {
                Words.selectAll().where {
                    Words.userId eq userId // Ищем все слова по userId
                }.map { row ->
                    WordResponseRemote(
                        word = row[word],
                        translation = row[translation],
                        failed = row[failed],
                        success = row[success],
                        addedAt = row[addedAt].toString() // Преобразуем LocalDateTime в строку
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