package com.wordsaver.features.register

import kotlinx.serialization.Serializable

@Serializable
data class RegisterReceiveRemote(
    val username: String,
    val email: String,
    val password: String
)

@Serializable
data class RegisterResponseRemote(
    val token: String
)