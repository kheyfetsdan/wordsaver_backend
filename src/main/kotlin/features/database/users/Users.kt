package com.wordsaver.features.database.users

import com.wordsaver.features.auth.AuthConfig
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

object Users : Table("users_main") {
    private val id = integer("id").autoIncrement()
    private val email = varchar("email", 100)
    private val password = varchar("password", 255) // Увеличиваем размер для хешированного пароля
    private val username = varchar("username", 50)

    override val primaryKey = PrimaryKey(id)

    fun insert(userDto: UserDto) {
        transaction {
            Users.insert {
                it[email] = userDto.email
                it[password] = AuthConfig.hashPassword(userDto.password)
                it[username] = userDto.username
            }
        }
    }

    fun fetchUser(email: String): UserDto? {
        return try {
            transaction {
                val userModel = Users.selectAll().where { Users.email eq email }.single()
                UserDto(
                    email = userModel[Users.email],
                    password = userModel[password],
                    username = userModel[username]
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}