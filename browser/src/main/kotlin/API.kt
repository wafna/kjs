import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import kotlin.js.Promise

// Used throughout to wrap api calls.
val mainScope = MainScope()

private suspend fun Promise<Response>.assertStatus() = await().apply {
    check(200.toShort() == status) {
        "Operation failed: $url".also {
            window.alert(it)
        }
    }
}

// Verbiage

private suspend fun get(url: String): Response =
    window.fetch(url).also {
        console.log("GET $url")
    }.assertStatus()

private suspend fun post(url: String, body: Any): Response =
    window.fetch(url, RequestInit(method = "POST", body = Json.encodeToJsonElement(body))).also {
        console.log("POST $url")
    }.assertStatus()

private suspend fun put(url: String, body: Any): Response =
    window.fetch(url, RequestInit(method = "PUT", body = Json.encodeToJsonElement(body))).also {
        console.log("PUT $url")
    }.assertStatus()

private suspend fun delete(url: String): Response =
    window.fetch(url, RequestInit(method = "DELETE")).assertStatus()

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

    suspend fun createRecord(data: String) =
        put("$apiRoot/create", Record("51682216-4758-4a5a-aa30-9bf9cd225765", data))
}
