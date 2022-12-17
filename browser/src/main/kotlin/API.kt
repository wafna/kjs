import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.json

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
    ).await().apply {
        status.toInt().also {
            check(200 == it || 0 == it) {
                "Operation failed: $status $url".also { msg ->
                    console.log(msg)
                    window.alert(msg)
                }
            }
        }
    }

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

    private fun makeURL(path: String, vararg params: Pair<String, String>): String = buildString {
        append(apiRoot)
        append("/")
        append(path)
        if (params.isNotEmpty()) {
            append("?")
            var sep = false
            for (param in params) {
                if (sep) append("&") else sep = true
                append(param.first)
                append("=")
                append(param.second)
            }
        }
    }

    // Get all the records.
    suspend fun listRecords(): List<Record> =
        json(get(makeURL("record")))

    suspend fun deleteRecord(id: UUID) =
        delete(makeURL("record", "id" to id))

    suspend fun updateRecord(record: Record) =
        post(makeURL("record"), record)

    suspend fun createRecord(record: RecordWIP) =
        put(makeURL("record"), record)
}
