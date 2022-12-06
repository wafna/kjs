import csstype.ClassName
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.events.MouseEvent
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

fun preventDefault(op: () -> Unit): (react.dom.events.MouseEvent<*, *>) -> Unit = { e ->
    e.preventDefault()
    op()
}

val Loading = FC<Props> {
    ReactHTML.h1 { +"Loading..." }
}

typealias UUID = String

fun main() {
    document.getElementById("root")?.also {
        createRoot(it).render(App.create())
    } ?: error("Couldn't find root container!")
}
