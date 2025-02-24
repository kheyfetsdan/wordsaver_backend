package com.wordsaver.features.login

import com.wordsaver.features.cache.InMemoryCache
import com.wordsaver.features.cache.TokenCache
import com.wordsaver.features.database.tokens.Tokens
import com.wordsaver.features.database.tokens.TokensDto
import com.wordsaver.features.database.users.Users
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import java.util.*

class LoginController(private val call: ApplicationCall) {

    suspend fun performLogin() {
        val receive = call.receive<LoginReceiveRemote>()
        val userDto = Users.fetchUser(receive.email)

        if (userDto == null) {
            call.respond(HttpStatusCode.BadRequest, "User not found")
        } else {
            if (userDto.password == receive.password) {
                val token = UUID.randomUUID().toString()
                Tokens.insert(
                    TokensDto(
                        id = UUID.randomUUID().toString(),
                        login = receive.email,
                        token = token
                    )
                )
                call.respond(LoginResponseRemote(token = token))
            } else {
                call.respond(HttpStatusCode.BadRequest, "Invalid password")
            }
        }
    }
}