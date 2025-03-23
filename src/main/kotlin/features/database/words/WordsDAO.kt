package com.wordsaver.features.database.words

import org.jetbrains.exposed.dao.id.IntIdTable

object WordsDAO: IntIdTable("words") {
    val word = Words.varchar("word", 100)
    val translation = Words.varchar("translation", 100)
    val userId = Words.varchar("userId", 100)
    val failed = Words.double("failed")
    val success = Words.double("success")

}