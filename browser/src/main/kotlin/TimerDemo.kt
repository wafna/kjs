import csstype.Border
import csstype.Color
import csstype.LineStyle
import csstype.px
import emotion.react.css
import kotlinx.browser.document
import kotlinx.js.timers.Timeout
import kotlinx.js.timers.setInterval
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import react.FC
import react.Props
import react.useEffect
import kotlin.random.Random
import react.dom.html.ReactHTML as h

external interface TimerDemoProps : Props {
    var height: Double
    var width: Double
}

val TimerDemo = FC<TimerDemoProps> { props ->

    val canvasId = "myCanvas"

    var timer: Timeout? = null

    useEffect {
        timer = setInterval({
            val c1 = document.getElementById(canvasId) as HTMLCanvasElement
            (c1.getContext("2d") as CanvasRenderingContext2D).apply {
                val x = Random.nextDouble() * props.width
                val y = Random.nextDouble() * props.height
                lineWidth = 1.0
                strokeStyle = "#00ffAA"
                lineTo(x, y)
                stroke()
            }
        }, 1000)
    }

    h.div {
        h.small { +"Timer..." }
    }
    h.div {
        h.canvas {
            id = canvasId
            width = props.width
            height = props.height
            css {
                border = Border(1.px, LineStyle.solid, Color("#000000"))
            }
        }
    }
}

