import csstype.ClassName
import kotlinx.browser.window
import kotlinx.coroutines.launch
import org.w3c.fetch.*
import react.FC
import react.Props
import react.dom.html.ReactHTML.div
import react.dom.html.ReactHTML.h1
import react.useEffectOnce

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