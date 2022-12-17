import csstype.ClassName
import kotlinx.browser.window
import kotlinx.coroutines.launch
import pages.GridleyDemo
import pages.HomePage
import pages.RecordList
import pages.TimerDemo
import react.FC
import react.Props
import react.useEffectOnce
import react.useState
import util.*
import react.dom.html.ReactHTML as h

// Routes...

object HomeRoute : Route {
    override val routeId: String = "home"
    override fun component(params: Params): FC<Props> = HomePage
}

object APIDemoRoute : Route {
    override val routeId: String = "api-demo"
    override fun component(params: Params): FC<Props> = RecordList
}

object TimerDemoRoute : Route {
    override val routeId: String = "timer-demo"

    // Even the names of the hash params are fully encapsulated.
    private const val Height = "height"
    private const val Width = "width"

    // If the page component requires configuration, just wrap it in a component that does not!
    override fun component(params: Params): FC<Props> = FC {
        TimerDemo {
            height = params.getInt(Height) ?: 400
            width = params.getInt(Width) ?: 400
        }
    }

    /**
     * A custom URL generator for this route.
     */
    fun makeHash(height: Double, width: Double): HashRoute {
        return HashRoute.build(routeId) {
            +(Height to height)
            +(Width to width)
        }
    }
}

object GridleyDemoRoute : Route {
    override val routeId: String = "gridley-demo"
    override fun component(params: Params): FC<Props> = GridleyDemo
}

val Routes = listOf(HomeRoute, APIDemoRoute, TimerDemoRoute, GridleyDemoRoute)

// Main component.

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
                    h.a {
                        className = ClassName("navbar-brand")
                        href = HomeRoute.defaultHash().href
                        +"KJS"
                    }
                    NavItem {
                        name = "API Demo"
                        to = APIDemoRoute.defaultHash()
                    }
                    NavItem {
                        name = "Timer Demo"
                        to = TimerDemoRoute.makeHash(500.0, 500.0)
                    }
                    NavItem {
                        name = "Gridley Demo"
                        to = GridleyDemoRoute.defaultHash()
                    }
                }
            }
        }
        h.br()
        Row {
            Col {
                scale = ColumnScale.Large
                size = 12
                doRoute(route, Routes)
            }
        }
    }
}
