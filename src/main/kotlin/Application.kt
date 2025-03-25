package com.wordsaver

import com.wordsaver.features.database.users.Users
import com.wordsaver.features.database.words.Words
import com.wordsaver.features.login.configureLoginRouting
import com.wordsaver.features.register.configureRegisterRouting
import com.wordsaver.features.wordsOperations.configureWordRouting
import com.wordsaver.plugins.configureRouting
import com.wordsaver.plugins.configureSerialization
import com.wordsaver.plugins.configureSecurity
import com.wordsaver.plugins.configureStatusPages
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
        if (!Words.exists()) {
            // Создание таблицы, если она не существует
            SchemaUtils.create(Words)
            println("Таблица '${Words.tableName}' создана.")
        } else {
            println("Таблица '${Words.tableName}' уже существует.")
        }
    }

    embeddedServer(CIO, port = System.getenv("PORT").toInt()) {
        configureStatusPages()
        configureSecurity()
        configureRouting()
        configureLoginRouting()
        configureRegisterRouting()
        configureSerialization()
        configureWordRouting()
    }.start(wait = true)

}

