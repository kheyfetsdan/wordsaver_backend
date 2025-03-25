package com.wordsaver.features.base

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.Serializable

abstract class BaseController(protected val call: ApplicationCall) {
    protected suspend fun respond(data: Any) {
        call.respond(data)
    }

    protected suspend inline fun <reified T> receiveRequest(): T {
        return call.receive()
    }

    protected suspend fun respondError(
        status: HttpStatusCode,
        message: String
    ) {
        call.respond(status, mapOf("error" to message))
    }

    protected suspend fun respondSuccess(message: String = "Success") {
        call.respond(HttpStatusCode.OK, mapOf("message" to message))
    }
} 