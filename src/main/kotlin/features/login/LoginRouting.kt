package com.wordsaver.features.login

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureLoginRouting() {
    routing {
        post("/login") {
            LoginController(call).performLogin()
        }
        
        authenticate {
            post("/refresh-token") {
                LoginController(call).refreshToken()
            }
        }
    }
}