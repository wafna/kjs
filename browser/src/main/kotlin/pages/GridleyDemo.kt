package pages

import csstype.ClassName
import react.*
import util.*
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import react.dom.html.ReactHTML as h

val columnHeaders = listOf<FC<Props>>(
    FC {
        h.div {
            className = ClassName("floater")
            +"Id"
        }
    },
    FC {
        h.div {
            className = ClassName("floater")
            +"Name"
        }
    },
    FC {
        h.div {
            className = ClassName("floater")
            +"Random"; h.br {}; +"Number"
        }
    })

external interface SortIconProps : PropsWithChildren, PropsWithClassName {
    var action: () -> Unit
}

/**
 * A clickable control for a single sort direction.
 */
val SortIcon = FC<SortIconProps> { props ->
    h.span {
        className = ClassName("clickable spinner")
        children = props.children
    }
}

val DownOff = FC<SortIconProps> { SortIcon { className = ClassName("float-down spinner"); +"▽" } }
val DownOn = FC<SortIconProps> { SortIcon { className = ClassName("float-down spinner"); +"▼" } }
val UpOff = FC<SortIconProps> { SortIcon { className = ClassName("float-down spinner"); +"△" } }
val UpOn = FC<SortIconProps> { SortIcon { className = ClassName("float-down spinner"); +"▲" } }

external interface SortControlProps : Props {
    var sort: Boolean?
    var action: (Boolean) -> Unit
}

/**
 * A clickable control for two way sort direction.
 */
val SortControl = FC<SortControlProps> { props ->
    val sort = props.sort
    h.div {
        className = ClassName("float-left ")
        h.div {
            onClick = preventDefault {
                console.log("SORT CONTROL")
                props.action(true) }
            if (null == sort || !sort)
                UpOff {}
            else
                UpOn {}
        }
        h.div {
            onClick = preventDefault { props.action(false) }
            if (null == sort || sort)
                DownOff {}
            else
                DownOn {}
        }
    }
}

/**
 * The normal functions of a data grid (display, pagination, filtering, and sorting) are decomposed.
 * We get flexibility and separation of concerns, but must hook a lot of stuff up.
 */
@Suppress("LocalVariableName")
val GridleyDemo = FC<Props> {
    val _totalRecords = 100
    val _pageSize = 15

    var _currentPage by useState(0)
    var _filter by useState("")

    val allRecords = (0 until _totalRecords).map { i ->
        listOf(listOf(i.toString()), listOf("Thing", "$i"), listOf(Random.nextInt().toString()))
    }
    val filtered = if (_filter.isEmpty()) allRecords else {
        allRecords.filter { record -> record.any { lines -> lines.any { it.contains(_filter) } } }
    }
    val _totalPages = ceil(filtered.size.toDouble() / _pageSize).toInt()

    // Ensure we're on an actual page.
    val effectivePage = if (_currentPage >= _totalPages) _totalPages - 1 else _currentPage

    val pageBounds = filtered.size.let { totalRecords ->
        val low = max(0, effectivePage * _pageSize)
        val high = min((1 + effectivePage) * _pageSize, totalRecords)
        low until high
    }

    Row {
        Col {
            scale = ColumnScale.Large
            size = 12
            GridleySearch {
                onFilter = { _filter = it }
            }
        }
    }
    h.br {}
    Row {
        Col {
            scale = ColumnScale.Large
            size = 12
            GridleyPager {
                totalPages = _totalPages
                currentPage = effectivePage
                onPageSelect = { _currentPage = it }
            }
        }
    }
    h.br {}
    Row {
        Col {
            scale = ColumnScale.Large
            size = 12
            GridleyTable {
                className = ClassName("table table-sm")
                fields = columnHeaders.map { hdr ->
                    FC {
                        SortControl {
                            action = { dir -> console.log("ACTION!", dir) }
                        }
                        h.div {
                            className = ClassName("float-left")
                            Entities.nbsp {}
                        }
                        h.div {
                            className = ClassName("float-left header")
                            hdr {}
                        }
                    }
                }
                records = filtered.slice(pageBounds).map { record ->
                    record.map { lines ->
                        FC {
                            var sep = false
                            for (line in lines) {
                                if (sep) h.br {} else sep = true
                                +line
                            }
                        }
                    }
                }

            }
        }
    }
}
