package com.wordsaver.features.login

import com.wordsaver.features.cache.InMemoryCache
import com.wordsaver.features.cache.TokenCache
import com.wordsaver.plugins.Test
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureLoginRouting() {

    routing {
        post("/login") {
            val loginController = LoginController(call)
            loginController.performLogin()
        }
    }
}