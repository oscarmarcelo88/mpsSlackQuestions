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
import java.lang.StringBuilder

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

                 //To retrieve all the json
               /* val response = client.get<Response>("https://slack.com/api/conversations.replies?channel=CBQPEPSA2&ts=1593597724.000200") {
                    header(HttpHeaders.Authorization, "Bearer $myJwtToken")
                }
                call.respond(response)*/

               val response = client.get<Response>("https://slack.com/api/conversations.history?channel=CBQPEPSA2") {
                    header(HttpHeaders.Authorization, "Bearer $myJwtToken")
                }
                var messageTs: String
                var messageQuestion: String
                val sb = StringBuilder()

                for (message in response.messages) {
                    messageTs = message.ts
                    messageQuestion = message.text
                   val response_replies = client.get<Response>("https://slack.com/api/conversations.replies?channel=CBQPEPSA2&ts=$messageTs"){
                    header(HttpHeaders.Authorization, "Bearer $myJwtToken")
                   }


                    for ((index, message_replies) in response_replies.messages.withIndex())
                    {
                       if (index > 0) {
                           if (index == 1) println("\n The question is: " + messageQuestion)   //printing once the question that have answers
                           println(message_replies.text)
                       }
                        //sb.append("\n").append(message_replies.text)
                    }
                }
                call.respond(sb.toString())


            }
        }
    }
    server.start(wait = true)
}

@JsonIgnoreProperties(ignoreUnknown = true)  //to ignore the fields that are empty or null
data class Response(val messages: List<Message>)
@JsonIgnoreProperties(ignoreUnknown = true)
class Message(val text: String, val ts: String)







//creo que no lo necesito
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    val client = HttpClient(Apache) {

    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        get("/html-dsl") {
            call.respondHtml {
                body {
                    h1 { +"HTML" }
                    ul {
                        for (n in 1..10) {
                            li { +"$n" }
                        }
                    }
                }
            }
        }

        get("/styles.css") {
            call.respondCss {
                body {
                    backgroundColor = Color.red
                }
                p {
                    fontSize = 2.em
                }
                rule("p.myclass") {
                    color = Color.blue
                }
            }
        }
    }
}

fun FlowOrMetaDataContent.styleCss(builder: CSSBuilder.() -> Unit) {
    style(type = ContentType.Text.CSS.toString()) {
        +CSSBuilder().apply(builder).toString()
    }
}

fun CommonAttributeGroupFacade.style(builder: CSSBuilder.() -> Unit) {
    this.style = CSSBuilder().apply(builder).toString().trim()
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}
