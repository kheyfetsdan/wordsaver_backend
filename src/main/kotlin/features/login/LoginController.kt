package com.wordsaver.features.login

import com.wordsaver.features.auth.AuthConfig
import com.wordsaver.features.database.users.Users
import com.wordsaver.features.base.BaseController
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*

class LoginController(call: ApplicationCall) : BaseController(call) {
    suspend fun performLogin() {
        val receive = try {
            call.receive<LoginReceiveRemote>()
        } catch (e: Exception) {
            respondError(HttpStatusCode.BadRequest, "Invalid request format")
            return
        }

        val userDto = Users.fetchUserDto(receive.email)
            ?: run {
                respondError(HttpStatusCode.BadRequest, "User not found")
                return
            }

        if (!AuthConfig.verifyPassword(receive.password, userDto.password)) {
            respondError(HttpStatusCode.BadRequest, "Invalid password")
            return
        }

        val token = AuthConfig.generateToken(receive.email)
        respond(LoginResponseRemote(token = token))
    }

    suspend fun refreshToken() {
        try {
            val email = Users.getUserEmailFromToken(call)
            val newToken = AuthConfig.generateToken(email)
            respond(LoginResponseRemote(token = newToken))
        } catch (e: Exception) {
            respondError(HttpStatusCode.Unauthorized, "Invalid token")
        }
    }
}