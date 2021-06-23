package com.quizserver

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Text
import com.sun.xml.internal.ws.client.ContentNegotiation
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import kotlinx.serialization.Serializable
import com.sun.org.apache.xalan.internal.xsltc.compiler.util.Type.Object
import io.ktor.server.netty.*
import io.ktor.util.Identity.encode
import jdk.jfr.ContentType
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class PlayerScore(val id: Int? = null, val name: String, val score: Int)

object Scores : Table() {
    private val id: Column<Int> = integer("id").autoIncrement()
    val name: Column<String> = varchar("name", 255)
    val score: Column<Int> = integer("score")

    override val primaryKey = PrimaryKey(id, name = "PK_PLAYER_ID")

    fun getPlayersScores(row: ResultRow): PlayerScore =
        PlayerScore(
            id = row[id],
            name = row[name],
            score = row[score]
        )
}

fun main(args: Array<String>): Unit = EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module(testing: Boolean = false) {

    // Connection to Database
    Database.connect("jdbc:postgresql://localhost/scores", driver = "org.postgresql.Driver",
        user = "gaetan", password = "gaetan")

    // Database Migration + Seeding
    transaction {
        SchemaUtils.create(Scores)

        Scores.insert {
            it[name] = "Evan"
            it[score] = 5
        }
    }

    routing {
        get("/") {
            call.respondText("This is an API made in Kotlin!")
        }

    install(Routing) {
        route("/scores") {

            get {
                val players = transaction {
                    Scores.selectAll().map { Scores.getPlayersScores(it) }
                }

                call.respondText(players.toString())
            }

            post {
                val newPlayerScore =  call.receive<PlayerScore>()

                transaction {
                    Scores.insert {
                        it[name] = newPlayerScore.name
                        it[score] = newPlayerScore.score
                    }
                }

                call.respond(newPlayerScore)
            }
        }
    }


//
//        post("/") {
//            val post = call.receive<String>()
//            call.respondText("Received $post from post request")
//        }

//        get("/scores") {
//            call.respondText("Pass scores here!")
//        }

//        post("/newscore") {
//            val newscore =
//        }

    }
}

