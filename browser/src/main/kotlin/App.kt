import csstype.ClassName
import kotlinx.browser.window
import kotlinx.coroutines.launch
import pages.RecordList
import pages.TimerDemo
import react.FC
import react.Props
import react.dom.html.ReactHTML as h
import react.useEffectOnce
import react.useState
import util.*
import util.Router.currentHash
import util.Router.doRoute

external interface NavItemProps : Props {
    var name: String
    var to: HashURL
}

val NavItem = FC<NavItemProps> { props ->
    h.li {
        css(ClassName("nav-item"))
        Link {
            css(ClassName("nav-link"))
            +props.name
            to = props.to
        }
    }
}

object APIDemoPage : Router.Route {
    override val path: String = "api-demo"
    override fun component(params: Map<String, String>): FC<Props> = RecordList
}

object TimerDemoPage : Router.Route {
    override val path: String = "timer-demo"

    // If the page component requires configuration, just wrap it in a component that does not.
    override fun component(params: Map<String, String>): FC<Props> = FC {
        TimerDemo {
            height = params["height"]?.toDoubleOrNull() ?: 400.0
            width = params["width"]?.toDoubleOrNull() ?: 400.0
        }
    }

    fun makeHash(height: Double, width: Double): HashURL =
        HashURL(path, mapOf("height" to height.toString(), "width" to width.toString()))
};


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
                        to = APIDemoPage.defaultHash()
                    }
                    NavItem {
                        name = "Timer Demo"
                        to = TimerDemoPage.makeHash(500.0, 500.0)
                    }
                }
            }
            try {
                doRoute(listOf(APIDemoPage, TimerDemoPage), hash, RecordList)
            } catch (e: Throwable) {
                console.error(e)
                ErrorPage { message = e.message ?: e::class.toString() }
            }
        }
    }
}