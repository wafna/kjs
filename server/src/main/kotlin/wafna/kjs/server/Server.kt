package wafna.kjs.server

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import wafna.kjs.util.LazyLogger
import java.lang.reflect.Type
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import kotlin.io.path.absolutePathString

private object Server

private val log = LazyLogger(Server::class)

data class Record(val id: UUID, val data: String)

fun main(): Unit = runBlocking {
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            log.warn { "Shutting down." }
        }
    })
    // NB this directory will not be found if you run the server from IDEA because the working directory will be
    // the root of the top level project.
    val staticDir = Paths.get("../browser/build/distributions").also {
        check(Files.isDirectory(it)) { "Static directory not found: ${it.absolutePathString()}"}
    }
    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:h2:mem:kjs"
        username = "sa"
        password = ""
        maximumPoolSize = 1
    }

    HikariDataSource(config).use { dataSource ->

        // Because it's h2:mem, we must do this here as the database won't exist until now.
        Flyway
            .configure()
            .dataSource(config.jdbcUrl, config.username, config.password)
            .locations("flyway")
            .load()
            .migrate()

        val db = DB(dataSource)

        db.createRecord(Record(UUID.randomUUID(), "Huey"))
        db.createRecord(Record(UUID.randomUUID(), "Dewey"))
        db.createRecord(Record(UUID.randomUUID(), "Louie"))

        runServer(staticDir, db)
    }
}

fun runServer(staticDir: Path, db: DB) {
    val indexHtml = staticDir.resolve("index.html").also {
        check(Files.isRegularFile(it))
    }
    val _log = log
    applicationEngineEnvironment {
        connector {
            port = 8081
        }
        module {
            // See https://ktor.io/docs/cors.html
            install(CORS) {
                anyHost()
                allowHeaders { true }
                allowNonSimpleContentTypes = true
                // Only need GET!
                methods.addAll(listOf(HttpMethod.Get, HttpMethod.Delete, HttpMethod.Post, HttpMethod.Put))
            }
            // See https://ktor.io/docs/gson.html
            install(ContentNegotiation) {
                gson {
                    disableHtmlEscaping()
                    serializeNulls()
                    registerTypeAdapter(
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
                                    _log.error(e) { "Bummer, dude."}
                                    throw e
                                }
                            }
                        })
                }
            }

            routing {
                api(db)
                route("/") {
                    // https://ktor.io/docs/serving-static-content.html
                    static {
                        files(staticDir.toFile())
                        default(indexHtml.toFile())
                    }
                }
                // This is necessary to show the UI in cases where we get URLs that are not understood on the server side,
                // e.g. redirects from auth servers and path based routing (as opposed to hash based routing).
                get("*") {
                    call.respondFile(indexHtml.toFile())
                }
            }
        }
    }.let { environment ->
        embeddedServer(Netty, environment).also { engine ->
            log.info { "Starting server..." }
            engine.start(true)
        }
    }
}
