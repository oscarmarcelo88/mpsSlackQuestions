package com.example2

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import freemarker.cache.ClassTemplateLoader
import freemarker.core.HTMLOutputFormat
import io.ktor.application.*
import io.ktor.routing.*
import io.ktor.http.*
import io.ktor.client.*
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.basic
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.freemarker.FreeMarker
import io.ktor.freemarker.FreeMarkerContent
import io.ktor.http.content.TextContent
import io.ktor.http.content.resources
import io.ktor.http.content.static
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import kotlinx.coroutines.*
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.lang.StringBuilder
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

                get("/approval-page") {
                    fetchQuestions()
                    call.respond(FreeMarkerContent("index.ftl", mapOf("questionEntries" to questionEntries, "answerEntries" to answerEntries), ""))
                }

                post ("/submit")
                {
                    val params = call.receiveParameters()
                    val question_timestamp = params["question_timestamp"] ?: return@post call.respond(HttpStatusCode.BadRequest)


                    val ZendeskToken = "ZbQxy4AoDsqbD8rmI53kpTjMjkjWp79uqWenz4F0"

                    val client = HttpClient(){
                        install(Auth){
                            basic {
                                username = "oscar.rodriguez@jetbrains.com/token"
                                password = ZendeskToken
                            }
                            install(JsonFeature) {
                                serializer = JacksonSerializer()
                            }
                        }
                    }
                    val userData = "{\"ticket\": {\"subject\": \"Help5\", \"comments\": [${textOfQuestion(question_timestamp)}]}}"
                    val text = client.post<String>("https://jbs1454063113.zendesk.com/api/v2/imports/tickets.json"){
                        body = TextContent(userData, contentType = ContentType.Application.Json)
                    }
                    //postQuestion(question_timestamp)
                    call.respond(FreeMarkerContent("submit.ftl", mapOf("questionErased" to question_timestamp), ""))
                }

                get("/") {

                    val client = HttpClient() {
                        install(JsonFeature) {
                            serializer = JacksonSerializer()
                        }
                    }

                    val myJwtToken = "xoxb-397574785314-1169530478945-s9qgkMw2i9GbxjbgetyqCC8A"

                    val response = client.get<Response>("https://slack.com/api/conversations.history?channel=CBQPEPSA2") {
                        header(HttpHeaders.Authorization, "Bearer $myJwtToken")
                    }
                    var path_file_question = ""
                    for (message in response.messages) {  //checking every message to get the replies

                        if (!message.files.isNullOrEmpty()){
                            for(file in message.files!!){
                                path_file_question = file.url_private_download
                            }
                        } else{
                            path_file_question = ""
                        }

                        if (!message.client_msg_id.isNullOrEmpty() || !message.files.isNullOrEmpty()) //We use the clientID to check that it's a user (not a bot) and file not empty to get the messages with files (Idk why when it has a file it has client id= null)
                        {
                            //The following methods are to feed the DB, we still need to check if the question exists or create time ranges.
                            //addQuestion(message.text, message.ts, message.user.toString(), path_file_question)
                            //addAnswers(message.ts, client, myJwtToken)
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
class Message(val text: String, val ts: String, val client_msg_id: String?, val user: String?, val files: List<Files>?) //we use the clientMessageID to know if it was sent by a user

//same as above but for the files
@JsonIgnoreProperties(ignoreUnknown = true)
class Files(val url_private_download: String)


//clases for uploading the files to Zendesk
@JsonIgnoreProperties(ignoreUnknown = true)  //to ignore the fields that are empty or null
data class Response_file(val upload: Message_files)
@JsonIgnoreProperties(ignoreUnknown = true)
class Message_files(val token: String)



//Create the Tables
object Questions: IntIdTable() {
    val text = text("text")
    val timestamp = varchar("timestamp", 25)
    val posted = bool("posted")
    val question_userID = varchar("question_userID", 50)
    val path_file = varchar("path_file", 250)
}
object Answers: IntIdTable() {
    val answer_text = text("answer_text")
    val question_id = text("question_id") //which is the timestamp of the Question
    val answer_userID = varchar("answer_userID", 50)
    val answer_path_file = varchar("answer_path_file", 250)
}

fun fetchQuestions(){
    accessingDB()
    transaction {
        // print sql to std-out
       // addLogger(StdOutSqlLogger)

        val queryQuestions = Questions.selectAll()
        val queryAnswers = Answers.selectAll()

       //Fetching the data from the DB and then add them to the objects of Questions and Answers
         queryQuestions.forEach loopQuestion@{
            val temp_QuestionText = it[Questions.text]
            val temp_QuestionTimestamp = it[Questions.timestamp]
             val temp_QuestionPosted= it[Questions.posted]
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
            var path_file_answer = ""
            if(!message_replies.files.isNullOrEmpty()){
                for (file in message_replies.files!!){
                    path_file_answer = file.url_private_download
                }
            }else{
                path_file_answer = ""
            }


            accessingDB()
            transaction {
                // print sql to std-out
              //  addLogger(StdOutSqlLogger)
                //create table if doesn't exist.
                SchemaUtils.create (Answers)

                Answers.insert {
                    it[answer_text] = message_replies.text
                    it[question_id] = timestamp
                    it[answer_userID] = message_replies.user.toString()
                    it[answer_path_file] = path_file_answer
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
        //addLogger(StdOutSqlLogger)

        Questions.update ({Questions.timestamp eq timestamp_question}) {
          //  Is commented for testing pruposes
              it[Questions.posted] = true

        }
    }
}

fun addQuestion (question: String, timestamp_question: String, askerID: String, path_file_question: String){

    accessingDB()
    transaction {
/*        Questions.deleteAll()
        Answers.deleteAll()*/

        // print sql to std-out
        //addLogger(StdOutSqlLogger)
        //create table if doesn't exist.
        SchemaUtils.create (Questions)

        Questions.insert {
            it[text] = question
            it[timestamp] = timestamp_question
            it[posted] = false
            it[question_userID] = askerID
            it[path_file] = path_file_question //hardcoded to test adding an image to Zendesk
        } get Questions.id
    }
}

fun accessingDB(){
    // In file
    Database.connect("jdbc:sqlite:my.db", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
}

suspend fun textOfQuestion (timestamp_question: String): String {
    accessingDB()
    val commentBuilder = StringBuilder()
    //We need to download the file first before uploading it to Zendesk
    val downloadFiles = DownloadFile()
    transaction { //Change the posted field to true, which means that it was already posted
        // print sql to std-out
        //addLogger(StdOutSqlLogger)

        val queryAnswers = Answers.selectAll()

            Questions.select{Questions.timestamp eq timestamp_question}.forEach {
                if (it[Questions.path_file].isBlank())
                    {
                        commentBuilder.append("{ \"author_id\": 4018454609, \"value\": \"${it[Questions.text]}\"}")
                    } else{
                    var imageTokenZendesk = runBlocking {downloadFiles.DownloadFromSlackAndUploadToZendesk(it[Questions.id], true)}

                   // var imageTokenZendesk = runBlocking {downloadFiles.DownloadFromSlackAndUploadToZendesk(it[Questions.id], true)}
                        commentBuilder.append("{ \"author_id\": 4018454609, \"value\": \"${it[Questions.text]}\", \"uploads\": [\"${imageTokenZendesk}\"]}")
                }
            }

            queryAnswers.forEach {

                if(timestamp_question == it[Answers.question_id])  //Adding the question that have answers and not being posted
                {
                    if (it[Answers.answer_path_file].isBlank())
                    {
                        commentBuilder.append(", { \"author_id\": 4018454609, \"value\": \"${it[Answers.answer_text]}\"}")
                    }else{
                        var imageTokenZendeskForAnswer = runBlocking {downloadFiles.DownloadFromSlackAndUploadToZendesk(it[Answers.id], false)} //We send the answer ID to
                        commentBuilder.append(", { \"author_id\": 4018454609, \"value\": \"${it[Answers.answer_text]}\", \"uploads\": [\"${imageTokenZendeskForAnswer}\"]}")
                    }
                }
            }
    }
    return commentBuilder.toString()
}






