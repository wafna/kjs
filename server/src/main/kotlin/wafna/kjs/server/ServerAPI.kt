package wafna.kjs.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.utils.io.*
import wafna.kjs.Record
import wafna.kjs.RecordWIP
import wafna.kjs.util.LazyLogger
import java.util.*

fun ApplicationCall.ok() = response.status(HttpStatusCode.OK)
fun ApplicationCall.internalServerError() = response.status(HttpStatusCode.InternalServerError)
fun ApplicationCall.badRequest() = response.status(HttpStatusCode.BadRequest)

private object API

private val log = LazyLogger(API::class)

/**
 * Sets the given status code into the response if the block returns normally.
 */
suspend fun ApplicationCall.bracket(
    status: HttpStatusCode = HttpStatusCode.OK,
    block: suspend ApplicationCall.() -> Unit
) {
    try {
        block()
        response.status(status)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        log.error(e) { "Call bracket." }
        internalServerError()
    }
}

/**
 * The browser API.
 */
internal fun Route.api(db: DB) {
    route("/record") {
        get("") {
            call.bracket {
                db.readRecords().let { records ->
                    log.info { "LIST ${records.size}" }
                    respond(records)
                }
            }
        }
        delete("") {
            call.bracket {
                val id = UUID.fromString(parameters["id"])
                log.info { "DELETE $id" }
                if (db.deleteRecord(id))
                    ok()
                else
                    badRequest()
            }
        }
        put("") {
            call.bracket {
                val record = receive<RecordWIP>().commit()
                log.info { "CREATE $record" }
                db.createRecord(record)
                respond(record)
            }
        }
        post("") {
            call.bracket {
                val record = receive<Record>()
                log.info { "UPDATE $record" }
                db.updateRecord(record)
            }
        }
    }
}