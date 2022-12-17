package pages.gridley

import csstype.ClassName
import react.FC
import react.Props
import util.preventDefault
import react.dom.html.ReactHTML as h

enum class SortDir {
    Ascending, Descending
}

/**
 * A clickable control for a single sort direction.
 */
fun sortIcon(text: String) = FC<Props> {
    h.span {
        className = ClassName("float-down clickable spinner")
        +text
    }
}

external interface SortControlProps : Props {
    /**
     * Which if either of the arrows to highlight.
     * This indicates the current sort condition on the row.
     */
    var sortDir: SortDir?
    /**
     * When one of the directions is selected the direction is transmitted to the controller,
     * which already knows which column this is.
     */
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
            when (sort) {
                SortDir.Ascending -> sortIcon("▲")()
                else -> sortIcon("△")()
            }
        }
        h.div {
            className = ClassName("float-down")
            onClick = preventDefault { props.action(SortDir.Descending) }
            when (sort) {
                SortDir.Descending -> sortIcon("▼")()
                else -> sortIcon("▽")()
            }
        }
    }
}
