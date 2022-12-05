import csstype.ClassName
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.await
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.w3c.fetch.RequestInit
import org.w3c.fetch.Response
import react.FC
import react.Props
import react.PropsWithClassName
import react.create
import react.dom.client.createRoot
import react.dom.html.ReactHTML
import kotlin.js.Promise

// Odd that this overload was not provided.
inline fun PropsWithClassName.css(vararg classNames: ClassName?) {
    className = emotion.css.ClassName(classNames = classNames) {}
}

val Loading = FC<Props> {
    ReactHTML.h1 { +"Loading..." }
}

typealias UUID = String

// Our sole domain object.
@Serializable
data class Record(val id: UUID, val data: String)

fun main() {
    val container = document.getElementById("root") ?: error("Couldn't find root container!")
    createRoot(container).render(App.create())
}
