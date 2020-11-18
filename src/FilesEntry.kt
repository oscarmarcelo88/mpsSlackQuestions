package com.example2

data class FilesEntry(val base64Files: String, val fileEntry_timestamp: String)

val filesEntries = mutableListOf(
    FilesEntry(
        "The drive to develop!",
        "test"
    )
)