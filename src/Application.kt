package com.example2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import freemarker.cache.ClassTemplateLoader
import freemarker.core.HTMLOutputFormat
import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.features.DefaultHeaders
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.css.html
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

    fun Application.module() {
        install(FreeMarker) {
            templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
            outputFormat = HTMLOutputFormat.INSTANCE
        }
            routing {

                static("/static") {
                    resources("files")
                }

                get("/ss") {
                    fetchQuestions()
                    call.respond(FreeMarkerContent("index.ftl", mapOf("questionEntries" to questionEntries, "answerEntries" to answerEntries), ""))
                }

                post ("/submit")
                {
                    val params = call.receiveParameters()
                    val question_timestamp = params["question_timestamp"] ?: return@post call.respond(HttpStatusCode.BadRequest)
                    postQuestion(question_timestamp)
                    call.respond(FreeMarkerContent("submit.ftl", mapOf("questionErased" to question_timestamp), ""))
                }

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

                    for (message in response.messages) {  //checking every message to get the replies
                        if (!message.client_msg_id.isNullOrEmpty())
                        {
                            addQuestion(message.text, message.ts, message.user.toString())
                            addAnswers(message.ts, client, myJwtToken)
                        }
                    }
                }
            }
        //}
        //server.start(wait = true)
    }



@JsonIgnoreProperties(ignoreUnknown = true)  //to ignore the fields that are empty or null
data class Response(val messages: List<Message>)
@JsonIgnoreProperties(ignoreUnknown = true)
class Message(val text: String, val ts: String, val client_msg_id: String?, val user: String?) //we use the clientMessageID to know if it was sent by a user

//Create the Tables
object Questions: IntIdTable() {
    val text = text("text")
    val timestamp = varchar("timestamp", 25)
    val posted = bool("posted")
    val question_userID = varchar("question_userID", 50)
}
object Answers: IntIdTable() {
    val answer_text = text("answer_text")
    val question_id = text("question_id") //which is the timestamp of the Question
    val answer_userID = varchar("answer_userID", 50)
}

fun fetchQuestions(){
    accessingDB()
    transaction {
        // print sql to std-out
        addLogger(StdOutSqlLogger)

        var queryQuestions = Questions.selectAll()
        var queryAnswers = Answers.selectAll()

       //Fetching the data from the DB and then add them to the objects of Questions and Answers
         queryQuestions.forEach loopQuestion@{
            var temp_QuestionText = it[Questions.text]
            var temp_QuestionTimestamp = it[Questions.timestamp]
             var temp_QuestionPosted= it[Questions.posted]
            queryAnswers.forEach {
                if(temp_QuestionTimestamp == it[Answers.question_id] && !temp_QuestionPosted)  //Adding the question that have answers and not being posted
                {
                    questionEntries.add(0, QuestionEntry(temp_QuestionText, temp_QuestionTimestamp))
                    return@loopQuestion //jump back to the previous loop to avoid duplication of the question.
                }
            }
        }
        queryAnswers.forEach {
            answerEntries.add(0, AnswerEntry(it[Answers.answer_text], it[Answers.question_id]))
        }
    }
}

suspend fun addAnswers(timestamp: String, client: HttpClient, token: String) {
    val response_replies = client.get<Response>("https://slack.com/api/conversations.replies?channel=CBQPEPSA2&ts=$timestamp"){
        header(HttpHeaders.Authorization, "Bearer $token")
    }

    for ((index, message_replies) in response_replies.messages.withIndex()) //go through all the answers of that question (using the timestamp of the question)
    {
        if (index > 0){
            accessingDB()
            transaction {
                // print sql to std-out
                addLogger(StdOutSqlLogger)
                //create table if doesn't exist.
                SchemaUtils.create (Answers)

                Answers.insert {
                    it[answer_text] = message_replies.text
                    it[question_id] = timestamp
                    it[answer_userID] = message_replies.user.toString()
                } get Answers.id
                //Answers.deleteAll()
            }
        }
    }
}

fun postQuestion(timestamp_question: String){

    //to-do(): Post it on the Forum

    accessingDB()
    transaction { //Change the posted field to true, which means that it was already posted
        // print sql to std-out
        addLogger(StdOutSqlLogger)
        Questions.update ({Questions.timestamp eq timestamp_question}) {
            it[Questions.posted] = true
        }
    }
}

fun addQuestion (question: String, timestamp_question: String, askerID: String){

    accessingDB()
    transaction {
        // print sql to std-out
        addLogger(StdOutSqlLogger)
        //create table if doesn't exist.
        SchemaUtils.create (Questions)

        Questions.insert {
            it[text] = question
            it[timestamp] = timestamp_question
            it[posted] = false
            it[question_userID] = askerID
        } get Questions.id

       //Questions.deleteAll()
    }
}

fun accessingDB(){
    // In file
    Database.connect("jdbc:sqlite:my.db", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
}



