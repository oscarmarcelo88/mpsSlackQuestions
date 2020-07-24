package com.example2

data class QuestionEntry(val text: String, val timestamp: String, val answers: MutableList<AnswerEntry>?)

val questionEntries = mutableListOf(
    QuestionEntry(
        "The drive to develop!",
        "...it's what keeps me going.",
        null
    )
)