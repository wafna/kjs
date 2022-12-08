import csstype.ClassName
import kotlinx.browser.window
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML as h
import react.useEffectOnce
import react.useState

/**
 * Get the current hash component of the window's address, stripping the superfluous leading '#'.
 */
private fun currentHash() = window.location.hash.let { hash ->
    if (hash.startsWith("#")) {
        hash.substring(1)
    } else hash
}

external interface NavItemProps : Props {
    var name: String
    var href: String
    var active: Boolean
}

val NavItem = FC<NavItemProps> { props ->
    h.li {
        css(classNames("nav-item", if (props.active) "active" else null))
        h.a {
            css(ClassName("nav-link"))
            +props.name
            href = "#${props.href}"
        }
    }
}

val App = FC<Props> {

    var hash: String by useState("")

    fun updateHash() = mainScope.launch {
        hash = currentHash()
    }

    useEffectOnce {
        window.onhashchange = { updateHash() }
        updateHash()
    }

    h.div {
        css(ClassName("container"))

        h.h1 {
            +"Kotlin Client Server (React)"
        }
        h.div {
            h.nav {
                css(ClassName("navbar navbar-expand-lg navbar-light bg-light"))
                h.ul {
                    css(ClassName("navbar-nav mr-auto"))
                    NavItem {
                        name = "REST"
                        href = "rest"
                        active = false
                    }
                    NavItem {
                        name = "Timer"
                        href = "timer"
                        active = true
                    }
                }
            }
            when (hash) {
                "timer" -> Canvas {
                    width = 300.0
                    height = 300.0
                }

                "rest" -> RecordList {}
                else -> RecordList {}
            }
        }
    }
}