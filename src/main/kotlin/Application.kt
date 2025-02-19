package com.wordsaver

import com.wordsaver.features.login.configureLoginRouting
import com.wordsaver.features.register.configureRegisterRouting
import com.wordsaver.plugins.configureRouting
import com.wordsaver.plugins.configureSerialization
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import org.jetbrains.exposed.sql.Database

fun main() {

    Database.connect(
        "jdbc:postgresql://localhost:5432/wordsaver",
        driver = "org.postgresql.Driver",
        user = "dkheyfets",
        password = "111111"
    )

    embeddedServer(CIO, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureLoginRouting()
        configureRegisterRouting()
        configureSerialization()
    }.start(wait = true)

}

