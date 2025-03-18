package com.wordsaver.features.wordsOperations

import com.wordsaver.features.database.words.Words
import com.wordsaver.features.database.words.Words.translation
import com.wordsaver.features.database.words.Words.userId
import com.wordsaver.features.database.words.Words.word
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.random.Random

class WordOperation {

    fun fetchRandomRow(previousWord: String, userId: String): ResultRow? {
        val randomWord = transaction {
            Words.fetchData(
                listOf(Words.word, Words.id),
                ((Words.userId eq userId) and (Words.word neq previousWord))
            ).toList()
        }

        val randomIndex = Random.nextInt(randomWord.size)
        val rWord = randomWord[randomIndex][word]

        return transaction { Words.fetchData(condition = ((Words.userId eq userId) and (Words.word eq rWord))).singleOrNull() }
    }

    fun fetchRandomThreeRowWithExclude(previousWord: String, currentWord: String, userId: String): List<String> {
        val randomWord = transaction {
            Words.fetchData(
                listOf(translation),
                ((Words.userId eq userId) and (Words.word neq previousWord) and (Words.word neq currentWord))
            ).toList()
        }

        val shuffledList = randomWord.shuffled()

        return listOf(shuffledList[0][translation], shuffledList[1][translation], shuffledList[2][translation])

    }


}