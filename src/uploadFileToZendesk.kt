package com.example2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.auth.*
import io.ktor.client.features.auth.providers.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.*
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.Connection

public class DownloadFile{

    //clases for uploading the files to Zendesk
    @JsonIgnoreProperties(ignoreUnknown = true)  //to ignore the fields that are empty or null
    data class Response_file(val upload: Message_files)
    @JsonIgnoreProperties(ignoreUnknown = true)
    class Message_files(val token: String)

    val config = ConfigFile()
    suspend fun DownloadFromSlackAndUploadToZendesk(postID: EntityID<Int>, QuestionFile: Boolean): String {

        Database.connect("jdbc:sqlite:my.db", "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        var pathOfFile = ""
        transaction {
            if (QuestionFile){
                Questions.select { Questions.id eq postID }.forEach {
                    pathOfFile = it[Questions.path_file]
                }
            } else {
                Answers.select { Answers.id eq postID }.forEach {
                    pathOfFile = it[Answers.answer_path_file]
                }
            }
        }

        //we need to get the name of the file from the path
        val nameOfFile = pathOfFile.substring(pathOfFile.lastIndexOf("/") + 1)

        //works!
            val client = HttpClient()

            val myJwtToken = config.SlackToken
            val channel =
                client.get<ByteReadChannel>(pathOfFile) {
                    header(HttpHeaders.Authorization, "Bearer $myJwtToken")
                }
            val content = object : OutgoingContent.ReadChannelContent() {
                override fun readFrom(): ByteReadChannel {
                    return channel
                }
            }


            val clientZendesk = HttpClient(CIO) {
                expectSuccess = false
                install(Auth) {
                    basic {
                        username = "oscar.rodriguez@jetbrains.com/token"
                        password = config.ZendeskToken
                    }
                }
            }

            val resp = clientZendesk.post<String>("https://jbs1454063113.zendesk.com/api/v2/uploads.json?filename=${nameOfFile}") {
                header("application", "binary")
                body = content
                //body = LocalFileContent(file)
            }
            println(resp)

        //we have the image token inside the json, we need to deserialize it
        val gson = Gson()
        val tokenImage = gson.fromJson(resp, Response_file::class.java)
        return tokenImage.upload.token
    }
}



