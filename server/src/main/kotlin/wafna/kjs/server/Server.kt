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
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import org.flywaydb.core.Flyway
import wafna.kjs.RecordWIP
import wafna.kjs.util.LazyLogger
import java.io.File
import java.lang.reflect.Type
import java.nio.file.Files
import java.util.*

private object Server

private val log = LazyLogger(Server::class)

fun main(): Unit = runBlocking {
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            log.warn { "Shutting down." }
        }
    })
    // The working directory will be different (by default) depending on whether it is run from Gradle or IDEA.
    val staticDir = "browser/build/distributions/".let { basePath ->
        val fromGradle = File("../$basePath")
        if (fromGradle.isDirectory) {
            fromGradle
        } else {
            val fromIDEA = File(basePath)
            if (fromIDEA.isDirectory) {
                fromIDEA
            } else {
                throw java.lang.RuntimeException("Cannot find static directory.")
            }
        }
    }.also {
        log.info { "Serving static directory: ${it.canonicalPath}"}
    }
    val config = HikariConfig().apply {
        jdbcUrl = "jdbc:h2:mem:kjs;DB_CLOSE_DELAY=-1"
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

        db.createRecord(RecordWIP("Huey").commit())
        db.createRecord(RecordWIP("Dewey").commit())
        db.createRecord(RecordWIP("Louie").commit())

        runServer(staticDir, db)
    }
}

private object Access
private val accessLog = LazyLogger(Access::class)

/**
 * Wrap this around a route to get a look at the activity on that route.
 */
fun Route.accessLog(callback: Route.() -> Unit): Route =
    createChild(object : RouteSelector() {
        override fun evaluate(context: RoutingResolveContext, segmentIndex: Int): RouteSelectorEvaluation =
            RouteSelectorEvaluation.Constant
    }).also { accessLogRoute ->
        accessLogRoute.intercept(ApplicationCallPipeline.Plugins) {
            accessLog.info { "${call.request.httpMethod.value} ${call.request.uri}" }
            proceed()
        }
        callback(accessLogRoute)
    }

fun runServer(staticDir: File, db: DB) {

    check(staticDir.isDirectory)
    val indexHtml = staticDir
        .resolve("index.html")
        .also { check(Files.isRegularFile(it.toPath())) }

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
                                    wafna.kjs.server.log.warn { ">>>>>>>>>>>>>>>>> UUID" }
                                    return json!!.asString
                                        .let { UUID.fromString(it) }
                                } catch (e: Throwable) {
                                    throw RuntimeException("Failed to serialize UUID from ${json?.asString}", e)
                                }
                            }
                        })
                }
            }

            routing {
                accessLog {
                    route("/api") {
                        api(db)
                    }
                    route("/") {
                        // https://ktor.io/docs/serving-static-content.html
                        static {
                            files(staticDir)
                            default(indexHtml)
                        }
                    }
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
