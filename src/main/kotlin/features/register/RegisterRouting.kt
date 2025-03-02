package com.wordsaver.features.register

import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRegisterRouting() {
    routing {
        post("/registration") {
            val  registerController = RegisterController(call)
            registerController.registerNewUser()
        }
    }
}