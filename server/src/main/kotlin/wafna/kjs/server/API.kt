package wafna.kjs.server

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wafna.kjs.util.LazyLogger
import java.lang.reflect.Type
import java.util.*

fun ApplicationCall.ok() = response.status(HttpStatusCode.OK)
fun ApplicationCall.internalServerError() = response.status(HttpStatusCode.InternalServerError)
fun ApplicationCall.badRequest() = response.status(HttpStatusCode.BadRequest)

private object API

private val log = LazyLogger(API::class)

suspend fun ApplicationCall.bracket(block: suspend ApplicationCall.() -> Unit) {
    try {
        block()
    } catch (e: Throwable) {
        log.error(e) { "Call bracket." }
        internalServerError()
    }
}

/**
 * The browser API.
 */
internal fun Routing.api(db: DB) {
    route("/api") {
        get("/list") {
            call.bracket {
                db.readRecords().let { records ->
                    log.info { "LIST ${records.size}" }
                    respond(records)
                }
            }
        }
        delete("/delete") {
            call.bracket {
                val id = UUID.fromString(parameters["id"])
                log.info { "DELETE $id" }
                if (db.deleteRecord(id))
                    call.ok()
                else
                    call.badRequest()
            }
        }
        put("/create") {
            call.bracket {
                val record = call.receive<Record>()
                    .copy(id = UUID.randomUUID())
                db.createRecord(record)
            }
        }
        post("/update") {
            call.bracket {
//                val record = call.receive<Record>()
//                log.info { "UPDATE ${record.id}  \"${record.data}\"" }
                val text = call.receiveText()
                log.info { "UPDATE ${text::class} $text" }
                val gson = GsonBuilder()
                    .registerTypeAdapter(
                        UUID::class.java,
                        object : JsonDeserializer<UUID> {
                            override fun deserialize(
                                json: JsonElement?,
                                typeOfT: Type?,
                                context: JsonDeserializationContext?
                            ): UUID {
                                try {
                                    return json!!.asString.let {
                                        UUID.fromString(it)
                                    }
                                } catch (e: Throwable) {
                                    log.error(e) { "Bummer, dude." }
                                    throw e
                                }
                            }
                        })
                    .create()
                val record = gson.fromJson(text, Record::class.java)
                db.updateRecord(record)
            }
        }
    }
}