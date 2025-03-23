package com.wordsaver.features.login

import com.wordsaver.features.auth.AuthConfig
import com.wordsaver.features.database.users.Users
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.auth.*

fun Application.configureLoginRouting() {

    routing {
        post("/login") {
            val loginController = LoginController(call)
            loginController.performLogin()
        }
        
        authenticate {
            post("/refresh-token") {
                val email = Users.getUserEmailFromToken(call)
                val newToken = AuthConfig.generateToken(email)
                call.respond(LoginResponseRemote(token = newToken))
            }
        }
    }
}