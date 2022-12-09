import kotlinx.browser.document
import react.create
import react.dom.client.createRoot

fun main() {
    document.getElementById("root")?.also {
        createRoot(it).render(App.create())
    } ?: error("Couldn't find root container!")
}
