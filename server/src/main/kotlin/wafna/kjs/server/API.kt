package wafna.kjs.server

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import wafna.kjs.Record
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

private val gson = GsonBuilder()
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
                val rawId = parameters["id"]
                log.error { "########### RAW ID $rawId"}
                val id = UUID.fromString(rawId)
                log.info { "DELETE $id" }
                if (db.deleteRecord(id))
                    call.ok()
                else
                    call.badRequest()
            }
        }
        post("/create") {
            log.info { "CREATE" }
            call.bracket {
//                val record = call.receive<Record>()
//                    .copy(id = UUID.randomUUID())
                val text = call.receiveText()
                log.info { "CREATE ${text::class} $text" }
                val record = gson.fromJson(text, Record::class.java)
                    .copy(id = UUID.randomUUID())
                log.info { "CREATE $record" }
                db.createRecord(record)
                respond(record)
            }
        }
        post("/update") {
            log.info { "UPDATE" }
            call.bracket {
//                val record = call.receive<Record>()
//                log.info { "UPDATE ${record.id}  \"${record.data}\"" }
                val text = call.receiveText()
                log.info { "UPDATE ${text::class} $text" }
                val record = gson.fromJson(text, Record::class.java)
                log.info { "UPDATE $record" }
                db.updateRecord(record)
                log.info { "UPDATE COMPLETE" }
            }
        }
    }
}