import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML

external interface NavItemProps : Props {
    var name: String
    var href: String
    var active: Boolean
}

val NavItem = FC<NavItemProps> { props ->
    ReactHTML.li {
        css(classNames("nav-item", if (props.active) "active" else null))
        ReactHTML.a {
            css(ClassName("nav-link"))
            +props.name
            href = "#${props.href}"
        }
    }
}

val Chrome = FC<Props> {
    ReactHTML.nav {
        css(ClassName("navbar navbar-expand-lg navbar-light bg-light"))
        ReactHTML.ul {
            css(ClassName("navbar-nav mr-auto"))
            NavItem {
                name = "REST"
                href = "rest"
                active = false
            }
            NavItem {
                name = "Other"
                href = "other"
                active = true
            }
        }
    }
}