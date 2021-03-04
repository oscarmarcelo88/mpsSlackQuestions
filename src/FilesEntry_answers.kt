package com.example2

import org.jetbrains.exposed.dao.id.EntityID

data class FilesEntry_answers(val base64Files: String, val fileEntry_timestamp_answers: String,  val id_answer: EntityID<Int>)

val filesEntries_answers = mutableListOf(
    FilesEntry_answers(
        "The drive to develop!",
        "test",
        EntityID(0, Answers)
    )
)