import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.fetch.*
import kotlin.js.Promise
import kotlin.js.json

// Used throughout to wrap api calls.
val mainScope = MainScope()

private suspend fun Promise<Response>.assertStatus() = await().apply {
    console.log("STATUS", status, statusText)
    check(200.toShort() == status) {
        "Operation failed: $status  $url".also {
            console.log(it)
            window.alert(it)
        }
    }
}

private suspend fun fetch(method: String, url: String, body: dynamic = null) {

}

// Verbiage

private suspend fun get(url: String): Response =
    window.fetch(
        url, RequestInit(
            method = "GET", body = null,
            headers = json(
                "Content-Type" to "application/json",
                "Accept" to "application/json",
                "pragma" to "no-cache"
            )
        )
    )
        .also {
            console.log("GET $url")
        }.assertStatus()

private suspend fun post(url: String, body: dynamic): Response {
    console.log("POST BODY ", body, JSON.stringify(body))
//    return window.fetch(url, RequestInit(method = "POST", body = body)).also {
    return window.fetch(
        url, RequestInit(
            method = "POST", body = JSON.stringify(body), headers = json(
                "Content-Type" to "application/json",
                "Accept" to "application/json",
                "pragma" to "no-cache"
            ), cache = RequestCache.NO_CACHE, mode = RequestMode.NO_CORS
        )
    ).assertStatus()
}

private suspend fun put(url: String, body: dynamic): Response =
    window.fetch(
        url, RequestInit(
            method = "PUT", body = JSON.stringify(body), headers = json(
                "Content-Type" to "application/json",
                "Accept" to "application/json"
            )
        )
    ).also {
        console.log("PUT $url")
    }.assertStatus()

private suspend fun delete(url: String): Response =
    window.fetch(
        url, RequestInit(
            method = "DELETE", headers = json(
                "Content-Type" to "application/json",
                "Accept" to "application/json"
            )
        )
    ).assertStatus()

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
        json(get("$apiRoot/list"))

    suspend fun deleteRecord(id: UUID) =
        delete("$apiRoot/delete?id=$id")

    suspend fun updateRecord(record: Record) =
        post("$apiRoot/update", record)

    suspend fun createRecord(record: RecordWIP) =
        put("$apiRoot/create", record)
}
