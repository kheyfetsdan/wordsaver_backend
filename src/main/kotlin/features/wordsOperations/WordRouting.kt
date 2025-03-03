package com.wordsaver.features.wordsOperations

import com.wordsaver.features.register.RegisterController
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.configureWordRouting() {
    routing {
        authenticate {
            post("/save-word") {
                val wordController = WordController(call)
                wordController.saveNewWord()
            }

            post("/get-word") {
                val wordController = WordController(call)
                wordController.getWord()
            }

            post("/get-words-by-user") {
                val wordController = WordController(call)
                wordController.getWords()
            }
        }
    }
}