package com.wordsaver.features.wordsOperations

import com.wordsaver.features.database.base.insertIntoTable
import com.wordsaver.features.database.words.Words
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class WordController(private val call: ApplicationCall) {

    suspend fun saveNewWord() {
        val wordReceiveRemote = call.receive<WordReceiveRemote>()

        val wordDto = Words.fetchWord(
            userWord = wordReceiveRemote.word,
            _userId = wordReceiveRemote.userId
        )
        if (wordDto != null) {
            call.respond(HttpStatusCode.Conflict, "Word already Exist")
        } else {
            try {
                insertIntoTable(Words, wordReceiveRemote)
            } catch (e: ExposedSQLException) {
                call.respond(HttpStatusCode.Conflict, "Word already Exist")
            }
            call.respond(SuccessSaveResponse("Saved"))
        }
    }

    suspend fun getWord() {
        return try {
            // Получаем тело запроса
            val request = call.receive<GetWordRequest>()

            // Выполняем запрос к базе данных
            val wordModel = transaction {
                Words.selectAll().where {
                    (Words.word eq request.word) and (Words.userId eq request.userId)
                }.singleOrNull()
            }

            // Если слово найдено, возвращаем его
            if (wordModel != null) {
                val response = WordResponseRemote(
                    word = wordModel[Words.word],
                    translation = wordModel[Words.translation],
                    userId = wordModel[Words.userId],
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
            // Обрабатываем ошибки
            call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.message}")
        }
    }

    suspend fun getWords() {
        try {
            // Получаем тело запроса
            val request = call.receive<GetWordsByUserRequest>()

            // Выполняем запрос к базе данных
            val words = transaction {
                Words.selectAll().where {
                    Words.userId eq request.userId // Ищем все слова по userId
                }.map { row ->
                    WordResponseRemote(
                        word = row[Words.word],
                        translation = row[Words.translation],
                        userId = row[Words.userId],
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
            // Обрабатываем ошибки
            call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.message}")
        }
    }
}