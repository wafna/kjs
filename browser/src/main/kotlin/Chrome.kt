import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML as h

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

val Chrome = FC<Props> {
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
                name = "Canvas"
                href = "canvas"
                active = true
            }
        }
    }
}