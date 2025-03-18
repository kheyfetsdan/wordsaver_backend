package com.wordsaver.features.database.words

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.case
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import kotlin.math.ceil
import kotlin.random.Random

object Words : Table("words") {
    val id = integer("id").autoIncrement()
    val word = varchar("word", 100)
    val translation = varchar("translation", 100)
    val userId = varchar("userId", 100)
    val failed = double("failed")
    val success = double("success")
    val addedAt = datetime("addedAt").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)

    fun fetchWord(userWord: String, userId: String): WordDto? {
        return try {
            transaction {
                addLogger(StdOutSqlLogger)
                val wordModel = Words.selectAll().where {
                    (word eq (userWord.lowercase())) and (this@Words.userId eq (userId))
                }
                    .single()
                WordDto(
                    word = wordModel[word],
                    translation = wordModel[translation],
                    userId = wordModel[this@Words.userId],
                    failed = wordModel[failed],
                    success = wordModel[success]
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    fun fetchData(
        columns: List<Column<*>> = this.columns,
        condition: Op<Boolean>,
        limit: Int = 1000
    ): Query {
        return transaction {
            addLogger(StdOutSqlLogger)
            Words.select(columns)
                .where {
                    condition
                }.limit(limit)
        }
    }

    fun updateSingleParam(id: String, userId: String, param: Column<Double>) =
        Words.update({ (Words.id eq id.toInt()) and (Words.userId eq userId) }) {
            with(SqlExpressionBuilder) {
                it.update(param, param + 1.0)
            }
        }

    fun fetchRandomSortedWord(): List<ResultRow> {
        val totalCount = transaction {
            fetchData(condition = Words.userId eq userId).count()
        }
        val limit = ceil(totalCount / 3.0).toInt()

        val sortedWordQuery = transaction {
            Words.fetchData(condition = Words.userId eq userId)
                .orderBy(
                    case()
                        .When((Words.success eq 0.0) and (Words.failed greater 0.0), intLiteral(1))
                        .When((Words.success eq 0.0) and (Words.failed eq 0.0), intLiteral(2))
                        .When(Words.success less Words.failed, intLiteral(3))
                        .When(Words.success eq Words.failed, intLiteral(4))
                        .When(Words.success greater Words.failed, intLiteral(5))
                        .Else(intLiteral(6))
                )
        }
        return transaction { sortedWordQuery.limit(limit).toList() }
    }


}