package com.example2

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection



/*
fun main(args: Array<String>) {
    //an example connection to H2 DB
    // In file
  Database.connect("jdbc:sqlite:my.db", "org.sqlite.JDBC")
// In memory
  //  Database.connect("jdbc:sqlite:file:test?mode=memory&cache=shared", "org.sqlite.JDBC")
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE // Or Connection.TRANSACTION_READ_UNCOMMITTED

    var prueba: String
    transaction {
        // print sql to std-out
        addLogger(StdOutSqlLogger)
        //create table if doesn't exist.
        SchemaUtils.create (Questions)

        // insert new city. SQL: INSERT INTO Cities (name) VALUES ('St. Petersburg')
        Questions.insert {
            it[text] = "question"
        } get Questions.id

        //example to update
/*        Cities.update({Cities.id eq 1}){
            it[Cities.name] = "Moscow 2"
        }*/


        var query = Questions.selectAll()
        query.forEach {
            println(it[Questions.text])
        }
    }
}

object Questions: IntIdTable() {
    val text = text("text")
}

object Answers: IntIdTable() {
    val answer_text = text("answer_text")
    val question_id = integer("question_id")
}

*/