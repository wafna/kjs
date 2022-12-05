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

    useEffectOnce {
        console.log("------------ TESTING")
        mainScope.launch {
            window.fetch(
                "http://localhost:8081/api/create",
                RequestInit(
                    method = "POST",
                    body = JSON.stringify(Record("34217821-21e5-4537-94cb-c57e92ee79ad", "TEST-POST")),
                    headers = kotlin.js.json(
                        "Content-Type" to "application/json",
                        "Accept" to "application/json",
                        "pragma" to "no-cache"
                    ),
                    cache = RequestCache.NO_CACHE,
                    mode = RequestMode.NO_CORS
                )
            ).then { response ->
                console.log("RESPONSE", response)
            }
        }
    }

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