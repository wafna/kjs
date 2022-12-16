package util

import csstype.ClassName
import react.ChildrenBuilder
import react.FC
import react.Props
import react.PropsWithChildren
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
        className = props.className
        className = ClassName("container")
        style = props.style
        children = props.children
    }
}

val Row = FC<PropsSplat> { props ->
    h.div {
        className = props.className
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
        className = props.className
        className = ClassName("col-${props.scale}-${props.size}")
        style = props.style
        children = props.children
    }
}

val NavBar = FC<PropsWithChildren> { props ->
    h.nav {
        className = ClassName("navbar navbar-expand-lg navbar-light bg-light")
        h.ul {
            className = ClassName("navbar-nav mr-auto")
            children = props.children
        }
    }
}

val NavItem = FC<NavItemProps> { props ->
    h.li {
        className = ClassName("nav-item")
        h.a {
            className = ClassName("nav-link")
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
        className = ClassName("alert alert-warning")
        +props.message
    }
}

object Entities {
    fun ChildrenBuilder.nbsp() = +"\u00A0"
}

