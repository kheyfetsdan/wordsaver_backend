package com.wordsaver.plugins

import com.wordsaver.features.auth.AuthConfig
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity() {
    authentication {
        jwt {
            verifier(AuthConfig.verifier)
            validate { credential ->
                if (credential.payload.getClaim("sub").asString().isNotEmpty()) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }
} 