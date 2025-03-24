package com.wordsaver.features.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object AuthConfig {
    // Выносим конфигурационные параметры в отдельный объект
    object SecurityConfig {
        val SECRET = System.getenv("JWT_SECRET") ?: "your-secret-key"
        const val ISSUER = "wordsaver-auth"
        const val TOKEN_LIFETIME = 30L * 24L * 60L * 60L * 1000L // 30 дней
        const val BCRYPT_STRENGTH = 12
    }

    private val algorithm = Algorithm.HMAC256(SecurityConfig.SECRET)

    fun generateToken(userId: String): String = JWT.create()
        .withSubject(userId)
        .withIssuer(SecurityConfig.ISSUER)
        .withExpiresAt(Date(System.currentTimeMillis() + SecurityConfig.TOKEN_LIFETIME))
        .sign(algorithm)

    fun hashPassword(password: String): String {
        return BCrypt.withDefaults().hashToString(SecurityConfig.BCRYPT_STRENGTH, password.toCharArray())
    }

    fun verifyPassword(password: String, hashedPassword: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hashedPassword).verified
    }

    val verifier = JWT.require(algorithm)
        .withIssuer(SecurityConfig.ISSUER)
        .build()
} 