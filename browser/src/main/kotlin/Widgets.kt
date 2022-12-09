import csstype.ClassName
import emotion.react.css
import react.FC
import react.Props
import util.HashRoute
import react.dom.html.ReactHTML as h

val Loading = FC<Props> {
    h.h1 { +"Loading..." }
}

external interface NavItemProps : Props {
    var name: String
    var to: HashRoute
}

val NavItem = FC<NavItemProps> { props ->
    h.li {
        css(ClassName("nav-item")) {}
        h.a {
            css(ClassName("nav-link")) {}
            +props.name
            href = props.to.href
        }
    }
}

external interface ErrorPageProps : Props {
    var message: String
}

val ErrorPage = FC<ErrorPageProps> { props ->
    h.div {
        css(ClassName("alert alert-warning")){}
        +props.message
    }
}

