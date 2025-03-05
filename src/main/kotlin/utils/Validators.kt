package com.wordsaver.utils

import kotlin.random.Random
import kotlin.random.Random.Default.nextInt

fun String.isEmailBroken(): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    return !this.matches(emailRegex.toRegex())
}