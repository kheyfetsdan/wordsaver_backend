package com.wordsaver.features.database.users

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Users : Table("users_main") {
    private val id = integer("id").autoIncrement()
    private val username = varchar("username", 50)
    private val email = varchar("email", 100)
    private val password = varchar("password", 255)
    private val createdAt = datetime("created_at").default(LocalDateTime.now())

    override val primaryKey = PrimaryKey(id)

    fun insert(userDto: UserDto) {
        transaction {
            Users.insert {
                it[username] = userDto.username
                it[password] = userDto.password
                it[email] = userDto.email
            }
        }
    }

    fun fetchUser(userEmail: String): UserDto? {
        return try {
            transaction {
                addLogger(StdOutSqlLogger)
                val userModel = Users.selectAll().where { Users.email eq(userEmail) }.single()
                UserDto(
                    username = userModel[username],
                    email = userModel[email],
                    password = userModel[password]
                )
            }
        } catch (e: Exception) {
            null
        }
    }
}