import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.Promise
import kotlin.js.json

// Used throughout to wrap api calls and effects.
val mainScope = MainScope()

private suspend fun Promise<Response>.assertStatus() = await().apply {
    status.toInt().also {
        check(200 == it || 0 == it) {
            "Operation failed: $status  $url".also { msg ->
                console.log(msg)
                window.alert(msg)
            }
        }
    }
}

private suspend fun fetch(method: String, url: String, body: dynamic = null): Response =
    window.fetch(
        url, RequestInit(
            method = method,
            body = body,
            headers = json(
                "Content-Type" to "application/json",
                "Accept" to "application/json",
                "pragma" to "no-cache"
            )
        )
    ).assertStatus()

// Verbiage: expressing the semantics of each method.

private suspend fun get(url: String): Response =
    fetch("GET", url)

private suspend fun put(url: String, body: dynamic): Response =
    fetch("PUT", url, JSON.stringify(body))

private suspend fun post(url: String, body: dynamic): Response =
    fetch("POST", url, JSON.stringify(body))

private suspend fun delete(url: String): Response =
    fetch("DELETE", url)

/**
 * Serialize object from json in response.
 */
private suspend inline fun <reified T> json(response: Response): T =
    Json.decodeFromString(response.text().await())

/**
 * The API methods, mirroring the server.
 */
object API {
    private const val apiRoot = "http://localhost:8081/api"

    // Get all the records.
    suspend fun listRecords(): List<Record> =
        json(get("$apiRoot/record"))

    suspend fun deleteRecord(id: UUID) =
        delete("$apiRoot/record?id=$id")

    suspend fun updateRecord(record: Record) =
        post("$apiRoot/record", record)

    suspend fun createRecord(record: RecordWIP) =
        put("$apiRoot/record", record)

    // Intended to fail
    suspend fun nonesuch() = get("$apiRoot/nonesuch")
}
