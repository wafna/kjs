import csstype.Border
import csstype.Color
import csstype.LineStyle
import csstype.px
import emotion.react.css
import kotlinx.browser.document
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import react.FC
import react.Props
import react.useEffectOnce
import kotlin.math.PI
import react.dom.html.ReactHTML as h

external interface CanvasProps : Props {
    var height: Double
    var width: Double
}
val Canvas = FC<CanvasProps> { props ->

    val canvasId = "myCanvas"

    useEffectOnce {
        val cx = props.width / 2.0
        val cy = props.height / 2.0
        val canvas = document.getElementById(canvasId) as HTMLCanvasElement
        (canvas.getContext("2d") as CanvasRenderingContext2D).apply {
            lineWidth = 1.0
            strokeStyle = "#00ffAA"
            arc(cx, cy, cx / 2.0, 0.0, 2 * PI)
            stroke()
            moveTo(cx / 2, cy / 2)
            lineTo(3 * cx / 2, 3 * cy / 2)
            stroke()
            moveTo(cx / 2, 3 * cy / 2)
            lineTo(3 * cx / 2, cy / 2)
            stroke()
            fillStyle = "#ff0088"
            fillRect(3 * cx / 4, 3 * cy / 4, cx / 2, cy / 2)
        }

    }

    h.div {
        h.small { +"Nav and permalink to here." }
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

