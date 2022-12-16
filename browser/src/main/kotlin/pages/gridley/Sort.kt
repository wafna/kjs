package pages.gridley

import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML as h
import util.preventDefault

/**
 * A clickable control for a single sort direction.
 */
fun sortIcon(text: String) = FC<Props> {
    h.span {
        className = ClassName("float-down clickable spinner")
        +text
    }
}

// The various indications.

val DownOff = sortIcon("▽")
val DownOn = sortIcon("▼")
val UpOff = sortIcon("△")
val UpOn = sortIcon("▲")

enum class SortDir {
    Ascending, Descending
}

external interface SortControlProps : Props {
    var sortDir: SortDir?
    var action: (SortDir) -> Unit
}

/**
 * A clickable control for two way sort direction.
 */
val SortControl = FC<SortControlProps> { props ->
    val sort = props.sortDir
    h.div {
        className = ClassName("sort-control-box")
        h.div {
            onClick = preventDefault { props.action(SortDir.Ascending) }
            if (sort == SortDir.Ascending)
                UpOn {}
            else
                UpOff {}
        }
        h.div {
            className = ClassName("float-down")
            onClick = preventDefault { props.action(SortDir.Descending) }
            if (sort == SortDir.Descending)
                DownOn {}
            else
                DownOff {}
        }
    }
}
