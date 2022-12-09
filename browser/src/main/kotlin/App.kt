import csstype.ClassName
import kotlinx.browser.window
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML as h
import react.useEffectOnce
import react.useState
import util.HashURL
import util.Router.currentHash
import util.classNames
import util.css

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

enum class Page(val path: String) {
    APIDemoPage("api-demo") {
        override fun component(params: Map<String, String>): FC<Props> = RecordList
    },
    TimerDemoPage("timer-demo") {
        override fun component(params: Map<String, String>): FC<Props> = FC {
            TimerDemo {
                height = 400.0
                width = 400.0
            }
        }
    };

    abstract fun component(params: Map<String, String> = mapOf()): FC<Props>
}

val App = FC<Props> {

    var hash: HashURL? by useState(null)

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
            try {
                if (null == hash) {
                    ErrorPage {
                        message = "Invalid path: null"
                    }
                } else {
                    when (val page = Page.values().find { it.path == hash!!.path }) {
                        null ->
                            RecordList {}
                        else ->
                            (page.component(hash!!.params)) {}
                    }
                }
            } catch (e: Throwable) {
                console.error(e)
                ErrorPage { message = e.message ?: e::class.toString() }
            }
        }
    }
}