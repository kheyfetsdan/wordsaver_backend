package com.wordsaver.features.wordsOperations

import com.wordsaver.features.register.RegisterController
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureWordRouting() {
    routing {
        post("/saveWord") {
            val  wordController = WordController(call)
            wordController.saveNewWord()
        }

        post("/get_word") {
            val  wordController = WordController(call)
            wordController.getWord()
        }

        post("/get_words_by_user") {
            val  wordController = WordController(call)
            wordController.getWords()
        }
    }
}