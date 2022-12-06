import csstype.ClassName
import kotlinx.browser.document
import kotlinx.serialization.Serializable
import react.FC
import react.Props
import react.PropsWithClassName
import react.create
import react.dom.client.createRoot
import react.dom.html.ReactHTML

// Odd that this overload was not provided.
inline fun PropsWithClassName.css(vararg classNames: ClassName?) {
    className = emotion.css.ClassName(classNames = classNames) {}
}

val Loading = FC<Props> {
    ReactHTML.h1 { +"Loading..." }
}

typealias UUID = String

fun main() {
    val container = document.getElementById("root") ?: error("Couldn't find root container!")
    createRoot(container).render(App.create())
}
