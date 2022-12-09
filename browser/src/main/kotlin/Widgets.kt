import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML
import util.css

val Loading = FC<Props> {
    ReactHTML.h1 { +"Loading..." }
}

external interface ErrorPageProps : Props {
    var message: String
}

val ErrorPage = FC<ErrorPageProps> { props ->
    ReactHTML.div {
        css(ClassName("alert alert-warning"))
        +props.message
    }
}

