package com.wordsaver.features.wordsOperations

import com.wordsaver.features.database.base.insertIntoTable
import com.wordsaver.features.database.words.Words
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.transactions.transaction
import com.auth0.jwt.exceptions.JWTVerificationException
import com.wordsaver.features.database.users.Users
import com.wordsaver.features.database.users.Users.getUserEmailFromToken
import com.wordsaver.features.database.words.WordDto
import com.wordsaver.features.database.words.Words.addedAt
import com.wordsaver.features.database.words.Words.failed
import com.wordsaver.features.database.words.Words.fetchData
import com.wordsaver.features.database.words.Words.fetchWord
import com.wordsaver.features.database.words.Words.id
import com.wordsaver.features.database.words.Words.success
import com.wordsaver.features.database.words.Words.translation
import com.wordsaver.features.database.words.Words.updateSingleParam
import com.wordsaver.features.database.words.Words.userId
import com.wordsaver.features.database.words.Words.word
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import kotlin.random.Random

class WordController(private val call: ApplicationCall) {

    private val userId = Users.fetchUserId(getUserEmailFromToken(call)).toString()
    private val wordOperation = WordOperation()

    suspend fun saveNewWord() {
        try {

            val wordReceiveRemote = call.receive<WordReceiveRemote>()

            val searchedWord = Words.fetchWord(
                userWord = wordReceiveRemote.word,
                userId = userId
            )

            if (searchedWord != null) {
                call.respond(HttpStatusCode.Conflict, "Word already exists")
            } else {
                try {
                    insertIntoTable(
                        Words, WordDto(
                            word = wordReceiveRemote.word,
                            translation = wordReceiveRemote.translation,
                            userId = userId,
                            failed = 0.0,
                            success = 0.0
                        )
                    )
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

    suspend fun getRandomSortedWord() {
        try {
            val sortedWordList = Words.fetchRandomSortedWord()

            if (sortedWordList.isEmpty()) {
                call.respond(HttpStatusCode.NotFound, "No words exist")
            }
            val randomIndex = Random.nextInt(sortedWordList.size)
            val rWord = sortedWordList[randomIndex][word]

            val wordModel = transaction { Words.fetchData(condition = (Words.userId eq userId) and (Words.word eq rWord)).singleOrNull() }

            if (wordModel != null) {
                call.respond(
                    WordResponseRemote(
                        id = wordModel[id],
                        word = wordModel[Words.word],
                        translation = wordModel[Words.translation],
                        failed = wordModel[Words.failed],
                        success = wordModel[Words.success],
                        addedAt = wordModel[Words.addedAt].toString() // Преобразуем LocalDateTime в строку
                    )
                )
            }

        } catch (e: Exception) {
            println(e)
        }
    }

    suspend fun getWords() {
        try {
            val request = call.receive<GetWordsRequest>()

            //SELECT * FROM your_glorious_table WHERE <glorious_filters> LIMIT <page_size> OFFSET <(page_number - 1) * page_size>
            val words = transaction {
                Words.fetchData(condition = Words.userId eq userId)
                    .orderBy(
                        when (request.sortingParam) {
                            "word" -> word
                            "failed" -> failed
                            "success" -> success
                            else -> word
                        },
                        when (request.sortingDirection) {
                            "asc" -> SortOrder.ASC
                            "desc" -> SortOrder.DESC
                            else -> SortOrder.ASC
                        }
                    )
                    .limit(request.pageSize)
                    .offset(((request.page - 1) * request.pageSize).toLong())
                    .map { row ->
                        WordResponseRemote(
                            id = row[Words.id],
                            word = row[word],
                            translation = row[translation],
                            failed = row[failed],
                            success = row[success],
                            addedAt = row[addedAt].toString() // Преобразуем LocalDateTime в строку
                        )
                    }
            }

            var wordsSize = 0
            try {
                wordsSize = transaction {
                    Words.fetchData(listOf(Words.word), Words.userId eq userId).count().toInt()
                }
            } catch (e: Exception) {
                println(e)
            }

            call.respond(
                WordList(
                    wordList = words,
                    total = wordsSize,
                    page = request.page
                )
            )
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

    suspend fun getWordById() {
        val id = call.parameters["id"] ?: "No ID"

        val word = transaction { Words.fetchData(condition = ((Words.userId eq userId) and (Words.id eq id.toInt()))).singleOrNull() }

        if (word != null) {
            call.respond(
                WordResponseRemote(
                    id = word[Words.id],
                    word = word[Words.word],
                    translation = word[translation],
                    failed = word[failed],
                    success = word[success],
                    addedAt = word[addedAt].toString()
                )
            )
        } else {
            call.respond(
                HttpStatusCode.InternalServerError,
                "An error occurred"
            )
        }
    }

    suspend fun updateWord() {
        val wordReceiveRemote = call.receive<WordIdReceiveRemote>()

        try {
            transaction {
                addLogger(StdOutSqlLogger)
                Words.update({ (Words.id eq wordReceiveRemote.id) and (Words.userId eq userId) }) {
                    it[Words.word] = wordReceiveRemote.word
                    it[Words.translation] = wordReceiveRemote.translation
                }
            }
            call.respond(
                HttpStatusCode.OK,
                "Word updated"
            )
        } catch (e: Exception) {
            println(e)
            call.respond(
                HttpStatusCode.NotFound,
                "Word not found"
            )
        }
    }

    suspend fun deleteWord() {
        val id = call.parameters["id"] ?: "No ID"

        try {
            transaction {
                addLogger(StdOutSqlLogger)
                Words.deleteWhere { (Words.id eq id.toInt()) and (Words.userId eq userId) }
            }
            call.respond(
                HttpStatusCode.OK,
                "Word deleted"
            )
        } catch (e: Exception) {
            println(e)
            call.respond(
                HttpStatusCode.NotFound,
                "Word not found"
            )
        }
    }

    suspend fun updateWordStatistic() {
        val id = call.parameters["id"] ?: "No ID"
        val wordReceiveRemote = call.receive<WordIdStatReceiveRemote>()

        try {
            if (wordReceiveRemote.success) {
                transaction {
                    addLogger(StdOutSqlLogger)
                    updateSingleParam(id, userId, Words.success)
                }
                call.respond(
                    HttpStatusCode.OK,
                    "Word success updated"
                )
            } else {
                transaction {
                    addLogger(StdOutSqlLogger)
                    updateSingleParam(id, userId, Words.failed)
                }
                call.respond(
                    HttpStatusCode.OK,
                    "Word fails updated"
                )
            }
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode.InternalServerError,
                "Update failed"
            )
        }
    }

    suspend fun quiz() {
        try {
            val quizReceiveRemote = call.receive<QuizRequest>()
            val checkWordsCount = transaction { fetchData(condition = (Words.userId eq userId)).toList() }
            println(checkWordsCount.size)

            if (checkWordsCount.size < 4) {
                call.respond(HttpStatusCode.NoContent, "Not enough words for quiz")
            } else {
                val wordModel = wordOperation.fetchRandomRow(quizReceiveRemote.previousWord, userId)!!
                println(wordModel[word])

                val translationList = transaction { wordOperation.fetchRandomThreeRowWithExclude(
                    quizReceiveRemote.previousWord,
                    wordModel[word], userId
                ) }

                println(translationList.size)

                val response = QuizResponse(
                    id = wordModel[Words.id],
                    word = wordModel[Words.word],
                    trueTranslation = wordModel[Words.translation],
                    translation1 = translationList[0],
                    translation2 = translationList[1],
                    translation3 = translationList[2],
                )
                call.respond(response)
            }
        } catch (e: Exception) {
            println(e)
        }
    }
}