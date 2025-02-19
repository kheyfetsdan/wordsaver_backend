package com.wordsaver.features.database.tokens

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

object Tokens: Table("tokens") {
    private val id = Tokens.varchar("id", 50)
    private val login = Tokens.varchar("login", 30)
    private val token = Tokens.varchar("token", 50)

    fun insert(tokensDto: TokensDto) {
        transaction {
            Tokens.insert {
                it[login] = tokensDto.login
                it[id] = tokensDto.id
                it[token] = tokensDto.token
            }
        }
    }
}