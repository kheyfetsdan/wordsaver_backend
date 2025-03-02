package com.wordsaver.features.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object AuthConfig {
    private const val SECRET = "your-secret-key" // В продакшене должен храниться в защищенном месте
    private const val ISSUER = "wordsaver-auth"
    private val algorithm = Algorithm.HMAC256(SECRET)
    private const val TOKEN_LIFETIME = 24L * 60L * 60L * 1000L // 24 часа

    fun generateToken(userId: String): String = JWT.create()
        .withSubject(userId)
        .withIssuer(ISSUER)
        .withExpiresAt(Date(System.currentTimeMillis() + TOKEN_LIFETIME))
        .sign(algorithm)

    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified
    }

    val verifier = JWT.require(algorithm)
        .withIssuer(ISSUER)
        .build()
} 