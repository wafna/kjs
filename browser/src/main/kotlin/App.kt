import kotlinx.browser.window
import kotlinx.coroutines.launch
import pages.RecordList
import pages.TimerDemo
import react.FC
import react.Props
import react.useEffectOnce
import react.useState
import util.HashRoute
import util.Route
import util.doRoute
import react.dom.html.ReactHTML as h

// Routes...

object APIDemoPage : Route {
    override val routeId: String = "api-demo"
    override fun component(params: Map<String, String>): FC<Props> = RecordList
}

object TimerDemoPage : Route {
    override val routeId: String = "timer-demo"

    // If the page component requires configuration, just wrap it in a component that does not!
    override fun component(params: Map<String, String>): FC<Props> = FC {
        TimerDemo {
            height = params["height"]?.toDoubleOrNull() ?: 400.0
            width = params["width"]?.toDoubleOrNull() ?: 400.0
        }
    }

    /**
     * A custom URL generator for this route.
     */
    fun makeHash(height: Double, width: Double): HashRoute =
        HashRoute(routeId, mapOf("height" to height.toString(), "width" to width.toString()))
}

/**
 * Containing the chrome and the routing.
 */
val App = FC<Props> {

    var route: HashRoute? by useState(null)

    fun updateRoute() = mainScope.launch {
        route = HashRoute.currentHash()
    }

    useEffectOnce {
        window.onhashchange = { updateRoute() }
        updateRoute()
    }

    Container {
        Row {
            Col {
                scale = ColumnScale.Large
                size = 12
                h.h1 { +"Kotlin Client Server (React)" }
            }
        }

        Row {
            Col {
                scale = ColumnScale.Large
                size = 12
                NavBar {
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
        }
        doRoute(listOf(APIDemoPage, TimerDemoPage), route, RecordList) { hash ->
            ErrorPage { message = "Bad route: ${hash.href}" }
        }
    }
}