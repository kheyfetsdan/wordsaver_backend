package com.wordsaver

import com.wordsaver.features.database.users.Users
import com.wordsaver.features.login.configureLoginRouting
import com.wordsaver.features.register.configureRegisterRouting
import com.wordsaver.plugins.configureRouting
import com.wordsaver.plugins.configureSerialization
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    Database.connect(
        "jdbc:postgresql://localhost:5432/wordsaver",
        driver = "org.postgresql.Driver",
        user = "dkheyfets",
        password = "111111"
    )

    transaction {
        // Проверка существования таблицы
        if (!Users.exists()) {
            // Создание таблицы, если она не существует
            SchemaUtils.create(Users)
            println("Таблица '${Users.tableName}' создана.")
        } else {
            println("Таблица '${Users.tableName}' уже существует.")
        }
    }

    embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureLoginRouting()
        configureRegisterRouting()
        configureSerialization()
    }.start(wait = true)

}

