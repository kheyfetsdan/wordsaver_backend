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

            get("/sorted-random-word") {
                val wordController = WordController(call)
                wordController.getRandomSortedWord()
            }

            post("/get-words-by-user") {
                val wordController = WordController(call)
                wordController.getWords()
            }

            get("/word/{id}") {
                val wordController = WordController(call)
                wordController.getWordById()
            }

            put("/word") {
                val wordController = WordController(call)
                wordController.updateWord()
            }

            delete("/delete-word/{id}") {
                val wordController = WordController(call)
                wordController.deleteWord()
            }

            put("/word-stat/{id}") {
                val wordController = WordController(call)
                wordController.updateWordStatistic()
            }

            post("/quiz") {
                val wordController = WordController(call)
                wordController.quiz()
            }

        }
    }
}