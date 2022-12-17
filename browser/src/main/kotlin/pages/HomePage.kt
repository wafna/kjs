package pages

import APIDemoRoute
import GridleyDemoRoute
import TimerDemoRoute
import csstype.px
import emotion.react.css
import react.FC
import react.Props
import react.PropsWithChildren
import util.Entities
import react.dom.html.ReactHTML as h

private external interface LineItemProps : PropsWithChildren {
    var desc: String
}

private val LineItem = FC<LineItemProps> { props ->
    h.li {
        +props.children
        +Entities.nbsp; +Entities.ndash; +Entities.nbsp
        +props.desc
    }
    h.div {
        css {
            lineHeight = 10.px
        }
        h.br()
    }
}

private external interface ParagraphProps : Props {
    var sentences: List<String>
}

private val Paragraph = FC<ParagraphProps> { props ->
    h.p {
        var sep = false
        for (s in props.sentences) {
            if (sep) +Entities.nspace else sep = true
            +s
        }
    }
}

val HomePage = FC<Props> {
    Paragraph {
        sentences = listOf("This project demonstrates several things of interest in React and Kotlin/JS:")
    }
    h.ul {
        LineItem {
            desc = "Hash router with type safe parameters."
            +"Router"
        }
        Paragraph {
            sentences = listOf(
                "Create hrefs with type safe functions and map params to component properties.",
                "The Route encapsulation means that target components do not know they are route targets.",
                "Instead, they receive props just as any other embedded component would."
            )
        }
        LineItem {
            desc = "Fetch with JSON."
            h.a { href = APIDemoRoute.defaultHash().href; +"API Demo" }
        }
        Paragraph {
            sentences = listOf("Demonstrates how to consume a REST API with full CRUD.")
        }
        LineItem {
            desc = "Effects, router params, and canvas."
            h.a { href = TimerDemoRoute.defaultHash().href; +"Timer Demo" }
        }
        Paragraph {
            sentences = listOf(
                "Demonstrates how use a cleanup effect to manage a timer.",
                "Uses router params to size the canvas."
            )
        }
        LineItem {
            desc = "Full featured tabular data display."
            h.a { href = GridleyDemoRoute.defaultHash().href; +"Gridley Demo" }
        }
        Paragraph {
            sentences = listOf("Demonstrates a highly decomposed system for displaying tabular data.")
        }
        h.ul {
            h.li { +"Customizable rendering of data." }
            h.li { +"Derived and compositional fields." }
            h.li { +"Customizable rendering of controls and components." }
            h.li { +"Representation independent sorting and searching." }
            h.li { +"Type safe handling of data records." }
        }
        h.br()
        Paragraph {
            sentences = listOf(
                "The problem with most approaches to this problem is that they violate the principle of single responsibility, binding together styling, layout," +
                        " and implementations into a fairly opaque blob that limits one's options to those foreseen by the implementor.",
                "This approach breaks all the functions apart at the price of turning a library into a framework, at best, but really a design pattern.",
                "In return, we get an enormous amount of flexibility."
            )
        }
        Paragraph {
            sentences = listOf(
                "The major components of the system (display, sorting, searching, and pagination) know nothing about each other.",
                "Neither do they know anything about the data that they are displaying (yes, even the display component).",
                "This is achieved by abstracting most of the logic into columns that define how to render, search, and sort the data.",
                "The logic for implementing searching, sorting, and pagination is bound in a single controller component.",
                "Thus, the controller component is the only place where the structure of the data being displayed is known."
            )
        }
    }
}