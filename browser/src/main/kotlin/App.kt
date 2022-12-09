import csstype.ClassName
import kotlinx.browser.window
import kotlinx.coroutines.launch
import react.FC
import react.Props
import react.dom.html.ReactHTML as h
import react.useEffectOnce
import react.useState
import util.HashURL
import util.Router
import util.Router.currentHash
import util.Router.doRoute
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

/**
 * Defining the pages in an enum makes them easier to enumerate (IKR?!).
 */
enum class Page : Router.Route {
    APIDemoPage() {
        override val path: String = "api-demo"
        override fun component(params: Map<String, String>): FC<Props> = RecordList
    },
    TimerDemoPage() {
        override val path: String = "timer-demo"

        // If the page component requires configuration, just wrap it in a component that does not.
        override fun component(params: Map<String, String>): FC<Props> = FC {
            TimerDemo {
                height = params["height"]?.toDoubleOrNull() ?: 400.0
                width = params["width"]?.toDoubleOrNull() ?: 400.0
            }
        }
    };

    companion object {
        fun pages(): Collection<Router.Route> = Page.values().toList()
    }
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
                        name = "API Demo"
                        href = Page.APIDemoPage.path
                        active = false
                    }
                    NavItem {
                        name = "Timer Demo"
                        href = Page.TimerDemoPage.path
                        active = true
                    }
                }
            }
            try {
                doRoute(Page.pages(), hash, RecordList)
            } catch (e: Throwable) {
                console.error(e)
                ErrorPage { message = e.message ?: e::class.toString() }
            }
        }
    }
}