package com.example2

import io.ktor.client.*
import io.ktor.client.content.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.request.*
import io.ktor.http.ContentDisposition.Companion.File
import java.io.File
import java.nio.file.Paths

suspend fun main() {
    val client = HttpClient(CIO) {
        expectSuccess = false
        install(Auth) {
            basic {
                username = "oscar.rodriguez@jetbrains.com/token"
                password = ""
            }
        }
    }
    val file = File("/Users/oscar_folder/IdeaProjects/ktor-firstProject/src/test4.png")
    println(file.exists())
    val resp = client.post<String>("https://jbs1454063113.zendesk.com/api/v2/uploads.json?filename=test4.png") {
        header("application", "binary")
        body = LocalFileContent(file)
    }
    println(resp)
}