package com.example2

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm.HMAC256
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.*
import io.ktor.auth.Authentication
import io.ktor.auth.Credential
import io.ktor.auth.Principal
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.html.*
import kotlinx.html.*
import kotlinx.css.*
import io.ktor.client.*
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.GsonSerializer
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.cio.CIOHeaders
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import jdk.nashorn.internal.runtime.regexp.RegExpFactory.validate
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.StringBuilder
import java.sql.Connection

fun main(args: Array<String>) {

    val server = embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {

                val client = HttpClient() {
                    install(JsonFeature) {
                        serializer = JacksonSerializer()
                    }
                }
                val myJwtToken = null

               val response = client.get<Response>("https://slack.com/api/conversations.history?channel=CBQPEPSA2") {
                    header(HttpHeaders.Authorization, "Bearer $myJwtToken")
                }
                var messageTs: String
                var clientMessageId: String? //to know if the message is from a user

                for (message in response.messages) {  //checking every message to get the replies
                    messageTs = message.ts
                   if (!message.client_msg_id.isNullOrEmpty())
                    {
                        addQuestion(message.text)
                    }

                    /* Code to go through the responses with the ts of the Question
                    val response_replies = client.get<Response>("https://slack.com/api/conversations.replies?channel=CBQPEPSA2&ts=$messageTs"){
                        header(HttpHeaders.Authorization, "Bearer $myJwtToken")
                   }

                    for ((index, message_replies) in response_replies.messages.withIndex())
                    {
                       if (index > 0) {
                           if (index == 1) println("\n The question is: " + message.text)   //printing once the question that have answers
                           println(message_replies.text)

                       }
                    }*/
                }


            }
        }
    }
    server.start(wait = true)
}

@JsonIgnoreProperties(ignoreUnknown = true)  //to ignore the fields that are empty or null
data class Response(val messages: List<Message>)
@JsonIgnoreProperties(ignoreUnknown = true)
class Message(val text: String, val ts: String, val client_msg_id: String?) //we use the clientMessageID to know if it was sent by a user


object Questions: IntIdTable() {
    val text = text("text")
}

object Answers: IntIdTable() {
    val answer_text = text("answer_text")
    val question_id = integer("question_id")
}

fun addQuestion (question: String){
    // In file
    Database.connect("jdbc:sqlite:my.db", "org.sqlite.JDBC")
// In memory
    //  Database.connect("jdbc:sqlite:file:test?mode=memory&cache=shared", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    transaction {
        // print sql to std-out
        addLogger(StdOutSqlLogger)
        //create table if doesn't exist.
        SchemaUtils.create (Questions)

        // insert new city. SQL: INSERT INTO Cities (name) VALUES ('St. Petersburg')
        Questions.insert {
            it[text] = question
        } get Questions.id

        //Questions.deleteAll()

        var query = Questions.selectAll()
        query.forEach {
            println(it[Questions.text])
        }
    }
}




