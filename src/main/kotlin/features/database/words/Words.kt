package com.wordsaver.features.database.words



import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object Words : Table("words") {
    private val id = integer("id").autoIncrement()
    val word = varchar("word", 100)
    val translation = varchar("translation", 100)
    val userId = varchar("userId", 100)
    val failed = double("failed")
    val success = double("success")
    val addedAt = datetime("addedAt").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)

    fun fetchWord(userWord: String, _userId: String): WordDto? {
        return try {
            transaction {
                addLogger(StdOutSqlLogger)
                val wordModel = Words.selectAll().where {
                    (Words.word eq(userWord)) and (userId eq(_userId)) }
                    .single()
                WordDto(
                    word = wordModel[word],
                    translation = wordModel[translation],
                    userId = wordModel[userId],
                    failed = wordModel[failed],
                    success = wordModel[success]
                )
            }
        } catch (e: Exception) {
            null
        }
    }

}