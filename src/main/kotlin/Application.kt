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
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.exists
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {

    /*Database.connect(
        "jdbc:postgresql://localhost:5432/wordsaver",
        driver = "org.postgresql.Driver",
        user = "dkheyfets",
        password = "111111"
    )*/

    val config = HikariConfig("hikari.properties")
    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)
    /*Database.connect(
        "cc0gj7hsrh0ht8.cluster-czrs8kj4isg7.us-east-1.rds.amazonaws.com:5432/dd4acp10r9dtr0",
        driver = "org.postgresql.Driver",
        user = "u16vehqf927hh2",
        password = "p78bcb534957efb3c87e4a33c745b331cefcb0a81d79536fbbd8f087ea2636595"
    )*/


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

    embeddedServer(Netty, port = System.getenv("PORT").toInt()) {
        configureStatusPages()
        configureSecurity()
        configureRouting()
        configureLoginRouting()
        configureRegisterRouting()
        configureSerialization()
        configureWordRouting()
    }.start(wait = true)

}

