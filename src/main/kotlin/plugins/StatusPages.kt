package com.wordsaver.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.plugins.statuspages.*
import com.auth0.jwt.exceptions.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<JWTVerificationException> { call, cause ->
            val message = when (cause) {
                is TokenExpiredException -> "Token has expired"
                is SignatureVerificationException -> "Invalid token signature"
                is InvalidClaimException -> "Invalid token claim"
                else -> "Invalid token"
            }
            call.respond(HttpStatusCode.Unauthorized, message)
        }
        
        exception<IllegalStateException> { call, cause ->
            when (cause.message) {
                "No user email in token" -> {
                    call.respond(HttpStatusCode.Unauthorized, "Invalid token format")
                }
                else -> {
                    call.respond(HttpStatusCode.InternalServerError, "An unexpected error occurred")
                }
            }
        }

        // Добавляем общий обработчик для всех остальных исключений
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                "An unexpected error occurred: ${cause.message}"
            )
        }
    }
} 