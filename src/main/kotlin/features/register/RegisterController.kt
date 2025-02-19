package com.wordsaver.features.register

import com.wordsaver.features.database.tokens.Tokens
import com.wordsaver.features.database.tokens.TokensDto
import com.wordsaver.features.database.users.UserDto
import com.wordsaver.features.database.users.Users
import com.wordsaver.utils.isValidEmail
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.jetbrains.exposed.exceptions.ExposedSQLException
import java.util.*

class RegisterController(private val call: ApplicationCall) {

    suspend fun registerNewUser() {
        val registerReceiveRemote = call.receive<RegisterReceiveRemote>()
        if (!registerReceiveRemote.email.isValidEmail()) {
            call.respond(HttpStatusCode.BadRequest, "Email is not valid")
        }
        val userDto = Users.fetchUser(registerReceiveRemote.login)

        if (userDto != null) {
            call.respond(HttpStatusCode.Conflict, "User already Exist")
        } else {
            val token = UUID.randomUUID().toString()

            try {
                Users.insert(
                    UserDto(
                        login = registerReceiveRemote.login,
                        email = registerReceiveRemote.email,
                        password = registerReceiveRemote.password,
                        username = ""
                    )
                )
            } catch (e: ExposedSQLException) {
                call.respond(HttpStatusCode.Conflict, "User already Exist")
            }


            Tokens.insert(
                TokensDto(
                    id = UUID.randomUUID().toString(),
                    login = registerReceiveRemote.login,
                    token = token
                )
            )

            call.respond(RegisterResponseRemote(token = token))
        }
    }


}