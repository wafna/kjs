import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML

external interface ChromeProps : Props {
    var page: FC<Props>
}

val Chrome = FC<ChromeProps> { props ->
    ReactHTML.nav {
        css(ClassName("navbar navbar-expand-lg navbar-light bg-light"))
        ReactHTML.span {
            css(ClassName("navbar-brand"))
//            href = "#"
            +"KJS"
        }
    }
    props.page()
}