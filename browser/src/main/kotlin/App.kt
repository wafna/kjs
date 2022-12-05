import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1

val App = FC<Props> {
    div {
        css(ClassName("container"))

        h1 {
            +"Kotlin Client Server (React)"
        }
        div {
            RecordList { }
        }
    }
}