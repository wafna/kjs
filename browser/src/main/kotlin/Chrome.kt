import react.FC
import react.Props

external interface ChromeProps : Props {
    var page: FC<Props>
}

val Chrome = FC<ChromeProps> { props ->
    props.page {}
}