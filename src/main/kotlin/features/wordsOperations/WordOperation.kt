package com.wordsaver.features.wordsOperations

import com.wordsaver.features.database.words.Words
import com.wordsaver.features.database.words.Words.userId
import com.wordsaver.features.database.words.Words.word
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.and
import kotlin.random.Random

class WordOperation {

    fun fetchRandomRow(previousWord: String): ResultRow? {
        val randomWord =
            Words.fetchData(
                listOf(Words.word),
                ((Words.userId eq userId) and (Words.word neq previousWord))
            ).toList()

        val randomIndex = Random.nextInt(randomWord.size)
        val rWord = randomWord[randomIndex][word]

        return Words.fetchData(condition = ((Words.userId eq userId) and (Words.word eq rWord))).singleOrNull()
    }


}