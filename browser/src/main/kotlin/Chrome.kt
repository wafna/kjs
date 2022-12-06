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
        css(ClassName(buildList {
            add("nav-item")
            if (props.active)
                add("active")
        }.joinToString(" ")))

        ReactHTML.a {
            css(ClassName("nav-link"))
            +props.name
            href = "#${props.href}"
        }
    }
}

external interface ChromeProps : Props {
    var page: FC<Props>
}

val Chrome = FC<ChromeProps> { props ->
    ReactHTML.nav {
        css(ClassName("navbar navbar-expand-lg navbar-light bg-light"))
        ReactHTML.ul {
            css(ClassName("navbar-nav mr-auto"))
            NavItem {
                name = "Records"
                href = ""
                active = false
            }
            NavItem {
                name = "Other"
                href = "other"
                active = true
            }
//            ReactHTML.li {
//                css(ClassName("nav-item"))
//                ReactHTML.a {
//                    css(ClassName("nav-link"))
//                    +"Records"
//                    href = "#"
//                }
//            }
//            ReactHTML.li {
//                css(ClassName("nav-item"))
//                ReactHTML.a {
//                    css(ClassName("nav-link"))
//                    +"Other"
//                    href = "#other"
//                }
//            }
        }
    }
    props.page()
}