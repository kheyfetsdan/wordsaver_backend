package com.wordsaver.features.login

import com.wordsaver.features.auth.AuthConfig
import com.wordsaver.features.database.users.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class LoginController(private val call: ApplicationCall) {
    suspend fun performLogin() {
        val receive = call.receive<LoginReceiveRemote>()
        val userDto = Users.fetchUser(receive.email)

        if (userDto == null) {
            call.respond(HttpStatusCode.BadRequest, "User not found")
            return
        }

        if (!AuthConfig.verifyPassword(receive.password, userDto.password)) {
            call.respond(HttpStatusCode.BadRequest, "Invalid password")
            return
        }

        val token = AuthConfig.generateToken(receive.email)
        call.respond(LoginResponseRemote(token = token))
    }
}