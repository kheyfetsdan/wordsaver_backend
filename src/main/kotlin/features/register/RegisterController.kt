package com.wordsaver.features.register

import com.wordsaver.features.auth.AuthConfig
import com.wordsaver.features.database.users.UserDto
import com.wordsaver.features.database.users.Users
import com.wordsaver.utils.isEmailBroken
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*

class RegisterController(private val call: ApplicationCall) {
    suspend fun registerNewUser() {
        val receive = call.receive<RegisterReceiveRemote>()
        
        if (receive.email.isEmailBroken()) {
            call.respond(HttpStatusCode.BadRequest, "Email is not valid")
            return
        }

        if (Users.fetchUser(receive.email) != null) {
            call.respond(HttpStatusCode.Conflict, "User already exists")
            return
        }

        try {
            Users.insert(
                UserDto(
                    email = receive.email,
                    password = receive.password,
                    username = receive.username
                )
            )

            val token = AuthConfig.generateToken(receive.email)
            call.respond(RegisterResponseRemote(token = token))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.InternalServerError, "Failed to register user")
        }
    }
}