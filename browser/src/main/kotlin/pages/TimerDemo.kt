package pages

import csstype.Border
import csstype.Color
import csstype.LineStyle
import csstype.px
import emotion.react.css
import kotlinx.browser.document
import kotlinx.js.timers.Timeout
import kotlinx.js.timers.clearTimeout
import kotlinx.js.timers.setInterval
import org.w3c.dom.CanvasRenderingContext2D
import org.w3c.dom.HTMLCanvasElement
import react.FC
import react.Props
import react.rawUseEffect
import react.useEffect
import kotlin.random.Random
import react.dom.html.ReactHTML as h

external interface TimerDemoProps : Props {
    var height: Int
    var width: Int
}

val TimerDemo = FC<TimerDemoProps> { props ->

    val canvasId = "myCanvas"

    var timer: Timeout? = null

    fun createTimer() {
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

    fun deleteTimer() {
        if (null != timer) {
            clearTimeout(timer!!)
            timer = null
        }
    }

    rawUseEffect({
        createTimer()
        ::deleteTimer
    }, arrayOf())

    h.div {
        h.small { +"Timer..." }
    }
    h.div {
        h.canvas {
            id = canvasId
            width = props.width.toDouble()
            height = props.height.toDouble()
            css {
                border = Border(1.px, LineStyle.solid, Color("#000000"))
            }
        }
    }
}

