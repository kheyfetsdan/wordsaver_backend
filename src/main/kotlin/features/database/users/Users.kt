package com.wordsaver.features.database.users

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object Users : Table() {
    private val login = Users.varchar("login", 30)
    private val email = Users.varchar("email", 30)
    private val password = Users.varchar("password", 30)
    private val username = Users.varchar("username", 30)

    fun insert(userDto: UserDto) {
        transaction {
            Users.insert {
                it[login] = userDto.login
                it[password] = userDto.password
                it[username] = userDto.username
                it[email] = userDto.email ?: ""
            }
        }
    }

    fun fetchUser(flogin: String): UserDto? {
        return try {
            transaction {
                addLogger(StdOutSqlLogger)
                val userModel = Users.selectAll().where { Users.login eq(flogin) }.single()
                val userMode1l = Users.select(Users.login, password).where { Users.login eq(flogin) }.single()
                UserDto(
                    login = userModel[login],
                    password = userModel[password],
                    email = userModel[email],
                    username = userModel[username]
                )
            }
        } catch (e: Exception) {
            println(e)
            null
        }
    }
}