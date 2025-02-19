package com.wordsaver.features.register

import com.wordsaver.features.cache.InMemoryCache
import com.wordsaver.features.cache.TokenCache
import com.wordsaver.features.login.LoginReceiveRemote
import com.wordsaver.features.login.LoginResponseRemote
import com.wordsaver.utils.isValidEmail
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

fun Application.configureRegisterRouting() {
    routing {
        post("/register") {
            val  registerController = RegisterController(call)
            registerController.registerNewUser()
        }
    }
}