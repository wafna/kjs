package wafna.kjs.server

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import wafna.kjs.Record
import wafna.kjs.RecordWIP
import wafna.kjs.util.LazyLogger
import java.util.*

private object Client

private val log = LazyLogger(Client::class)

private fun HeadersBuilder.acceptJson() =
    append(HttpHeaders.Accept, "application/json")

private fun HeadersBuilder.sendJson() =
    append(HttpHeaders.ContentType, "application/json")

// For some reason, this is getting 404s on many calls (the ones with bodies) even though the calls are clearly working!
private fun HttpResponse.checkStatus(): HttpResponse = apply {
    log.info { "CHECK STATUS: ${request.method.value} ${request.url} $status" }
    check(status.isSuccess()) { "Request failed: ${request.method.value} ${request.url} $status" }
}

/**
 * Should be an integration test, but is just an ad hoc exercise of the API.
 */
fun main() = runBlocking(Dispatchers.IO) {

    HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.INFO
        }
        install(ContentNegotiation) {
            gson {
                serializeNulls()
            }
        }
    }.use { client ->

        val baseURL = "http://localhost:8081/api"

        // Define the API.

        suspend fun list(): List<Record> = client
            .get("$baseURL/record") {
                headers {
                    acceptJson()
                }
            }
            .checkStatus()
            .body<List<Record>>()
            .also { records ->
                println(
                    """RECORDS
                      |${records.joinToString("\n") { "   $it" }}""".trimMargin()
                )
            }

        suspend fun update(record: Record) = client
            .post("$baseURL/record") {
                headers {
                    sendJson()
                }
                setBody(record)
            }
            .checkStatus()

        suspend fun create(record: RecordWIP) = client
            .put("$baseURL/record") {
                headers {
                    sendJson()
                    acceptJson()
                }
                setBody(record)
            }
            //.checkStatus()
            .body<Record>()

        suspend fun delete(id: UUID) = client
            .delete("$baseURL/record?id=$id")
            .checkStatus()

        // Do stuff...

        suspend fun lawyerUp() {
            create(RecordWIP("Huey"))
            create(RecordWIP("Dewey"))
            create(RecordWIP("Louie"))
        }

        list().forEach { record ->
            delete(record.id)
        }

        lawyerUp()

        list().also { records ->
            require(3 == records.size)
            update(records[0].let { it.copy(data = "UPDATED-${it.data}") })
        }
        list().also { records ->
            require(3 == records.size)
            delete(records[0].id)
            require(2 == list().size)
        }
        create(RecordWIP("CREATED")).also {
            println("CREATE $it")
        }
        list().also { records ->
            require(3 == records.size)
        }

        Unit
    }
}