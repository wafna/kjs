import kotlinx.browser.document
import kotlinx.coroutines.MainScope
import react.create
import react.dom.client.createRoot

// Used throughout to wrap api calls and effects.
val mainScope = MainScope()

fun main() {
    document.getElementById("root")?.also {
        createRoot(it).render(App.create())
    } ?: error("Couldn't find root container!")
}
