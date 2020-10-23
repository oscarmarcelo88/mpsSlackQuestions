package com.example2

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.utils.io.*
import io.ktor.utils.io.copyTo
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.runBlocking
import kotlinx.css.header
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.sql.Connection


/*fun main (args: Array<String>){
    var total = 0
    val fileName = "src/text.txt"
    val file = File(fileName).readLines().indexOf()
    println("este es: $file")
}*/

public class DownloadFileFromSlackAPI {
    fun downloadFileLocally (timeStampPost: String)
    {

        Database.connect("jdbc:sqlite:my.db", "org.sqlite.JDBC")
        TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE
        var pathOfFile=""
        transaction{
            Questions.select { Questions.timestamp eq timeStampPost }.forEach {
                pathOfFile = it[Questions.path_file]
            }
        }

        //works!
        runBlocking {
            val client = HttpClient()
            val myJwtToken = ""
            val channel =
                client.get<ByteReadChannel>(pathOfFile) {
                    header(HttpHeaders.Authorization, "Bearer $myJwtToken")
                }
            val file = File("files")
            file.outputStream().use {
                channel.copyTo(it)
            }

        }
    }
}





/*
fun download(link: String, path: String) {
    val myJwtToken = ""
    val dstDir = File(path)
    dstDir.mkdirs()
    val url = URL(link)
    url.openStream().use { input ->
        FileOutputStream(dstDir.resolve(link.substringAfterLast("/"))).use { output ->
            input.copyTo(output)
        }
    }
}*/
