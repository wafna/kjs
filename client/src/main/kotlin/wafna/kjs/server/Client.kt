package wafna.kjs.server

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.UUID

private fun HeadersBuilder.acceptJson() =
    append(HttpHeaders.Accept, "application/json")

private fun HeadersBuilder.sendJson() =
    append(HttpHeaders.ContentType, "application/json")

// For some reason, this is getting 404s on many calls (the ones with bodies) even though the calls are clearly working!
private fun HttpResponse.checkStatus(): HttpResponse = apply {
    check(status.isSuccess()) { "Request failed: ${request.method.value} ${request.url} $status" }
}

data class Record(val id: UUID, val data: String)

/**
 * Should be an integration test, but is just an ad hoc exercise of the API.
 */
fun main() = runBlocking(Dispatchers.IO) {

    HttpClient(CIO) {
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

        suspend fun create(record: Record) = client
            .put("$baseURL/record") {
                headers {
                    sendJson()
                    acceptJson()
                }
                setBody(record)
            }
            //.checkStatus()
            .body<Record>()

        suspend fun delete(id: String) = client
            .delete("$baseURL/record?id=$id")
            .checkStatus()

        // Do stuff...

        list().also { records ->
            require(3 == records.size)
            update(records[0].copy(data = "UPDATED-${UUID.randomUUID()}"))
        }
        list().also { records ->
            require(3 == records.size)
            delete(records[0].id.toString())
            require(2 == list().size)
        }
        create(Record(UUID.randomUUID(), "CREATED")).also {
            println("CREATE $it")
        }
        list().also { records ->
            require(3 == records.size)
        }

        Unit
    }
}