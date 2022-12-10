import csstype.ClassName
import emotion.react.css
import react.FC
import react.Props
import react.PropsWithChildren
import util.HashRoute
import util.PropsSplat
import react.dom.html.ReactHTML as h

val Loading = FC<Props> {
    h.h1 { +"Loading..." }
}

external interface NavItemProps : Props {
    var name: String
    var to: HashRoute
}

val Container = FC<PropsSplat> { props ->
    h.div {
        className = ClassName("container")
        style = props.style
        children = props.children
    }
}

val Row = FC<PropsSplat> { props ->
    h.div {
        className = ClassName("row")
        style = props.style
        children = props.children
    }
}

enum class ColumnScale(val scale: String) {
    Small("sm"),
    Medium("md"),
    Large("lg");

    override fun toString(): String = scale
}

external interface ColProps : PropsSplat {
    var scale: ColumnScale
    var size: Int
}

val Col = FC<ColProps> { props ->
    h.div {
        className = ClassName("col-${props.scale}-${props.size}")
        style = props.style
        children = props.children
    }
}

val NavBar = FC<PropsWithChildren> { props ->
    h.nav {
        css(ClassName("navbar navbar-expand-lg navbar-light bg-light")) {}
        h.ul {
            css(ClassName("navbar-nav mr-auto")) {}
            children = props.children
        }
    }
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
        css(ClassName("alert alert-warning")) {}
        +props.message
    }
}

