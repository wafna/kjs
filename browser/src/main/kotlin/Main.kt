import csstype.ClassName
import kotlinx.browser.document
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

// By allowing nulls we can conditionally inline CSS classes.
fun classNames(vararg className: String?): ClassName =
    ClassName(className.filterNotNull().joinToString(" "))

val Loading = FC<Props> {
    ReactHTML.h1 { +"Loading..." }
}

fun main() {
    document.getElementById("root")?.also {
        createRoot(it).render(App.create())
    } ?: error("Couldn't find root container!")
}
