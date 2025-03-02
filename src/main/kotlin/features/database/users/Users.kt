package com.wordsaver.features.database.users

import com.wordsaver.features.auth.AuthConfig
import com.wordsaver.features.database.words.Words.default
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object Users : Table("users_main") {
    private val id = integer("id").autoIncrement()
    private val email = varchar("email", 100)
    private val password = varchar("password", 255)
    private val addedAt = datetime("addedAt").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)

    fun insert(userDto: UserDto) {
        transaction {
            Users.insert {
                it[email] = userDto.email
                it[password] = AuthConfig.hashPassword(userDto.password)
            }
        }
    }

    fun fetchUser(email: String): UserDto? {
        return try {
            transaction {
                val userModel = Users.selectAll().where { Users.email eq email }.single()
                UserDto(
                    email = userModel[Users.email],
                    password = userModel[password]
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}