import csstype.ClassName
import kotlinx.browser.window
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.useEffectOnce
import react.useState

val Other = FC<Props> {
    ReactHTML.h3 { +"Nav and permalink to here." }
}

/**
 * Get the current hash component of the window's address, stripping the superfluous leading '#'.
 */
private fun currentHash() = window.location.hash.let { hash ->
    if (hash.startsWith("#")) {
        hash.substring(1)
    } else hash
}


val App = FC<Props> {

    var hash: String by useState("")

    fun updateHash() = mainScope.launch {
        hash = currentHash()
    }

    useEffectOnce {
        window.onhashchange = {
            updateHash()
        }
        updateHash()
    }

    div {
        css(ClassName("container"))

        h1 {
            +"Kotlin Client Server (React)"
        }
        div {
            Chrome {}
            when (hash) {
                "other" -> Other {}
                "rest" -> RecordList {}
                else -> RecordList {}
            }
        }
    }
}