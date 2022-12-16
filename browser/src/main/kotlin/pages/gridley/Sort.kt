package pages.gridley

import csstype.ClassName
import react.FC
import react.Props
import react.PropsWithChildren
import react.PropsWithClassName
import react.dom.html.ReactHTML
import util.preventDefault

data class SortKey(val index: Int, val sortDir: SortDir)

external interface SortIconProps : PropsWithChildren, PropsWithClassName

/**
 * A clickable control for a single sort direction.
 */
val SortIcon = FC<SortIconProps> { props ->
    ReactHTML.span {
        className = ClassName("clickable spinner")
        children = props.children
    }
}

val DownOff = FC<SortIconProps> { SortIcon { className = ClassName("float-down spinner"); +"▽" } }
val DownOn = FC<SortIconProps> { SortIcon { className = ClassName("float-down spinner"); +"▼" } }
val UpOff = FC<SortIconProps> { SortIcon { className = ClassName("float-down spinner"); +"△" } }
val UpOn = FC<SortIconProps> { SortIcon { className = ClassName("float-down spinner"); +"▲" } }

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
    ReactHTML.div {
        className = ClassName("float-left ")
        ReactHTML.div {
            onClick = preventDefault { props.action(SortDir.Ascending) }
            if (sort == SortDir.Ascending)
                UpOn {}
            else
                UpOff {}
        }
        ReactHTML.div {
            onClick = preventDefault { props.action(SortDir.Descending) }
            if (sort == SortDir.Descending)
                DownOn {}
            else
                DownOff {}
        }
    }
}
