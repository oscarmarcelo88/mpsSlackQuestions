package com.example2

import org.jetbrains.exposed.dao.id.EntityID

data class AnswerEntry(val text: String, val timestamp: String, val id_answer: EntityID<Int>)

val answerEntries = mutableListOf(
    AnswerEntry(
        "The drive to develop!",
        "...it's what keeps me going.",
        EntityID(0, Answers)

    )
)