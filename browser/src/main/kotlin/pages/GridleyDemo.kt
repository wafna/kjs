package pages

import csstype.ClassName
import react.*
import util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import react.dom.html.ReactHTML as h

data class SortKey(val index: Int, val sortDir: SortDir)

external interface SortIconProps : PropsWithChildren, PropsWithClassName

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
        className = ClassName("float-left ")
        h.div {
            onClick = preventDefault { props.action(SortDir.Ascending) }
            if (sort == SortDir.Ascending)
                UpOn {}
            else
                UpOff {}
        }
        h.div {
            onClick = preventDefault { props.action(SortDir.Descending) }
            if (sort == SortDir.Descending)
                DownOn {}
            else
                DownOff {}
        }
    }
}

/**
 * The style and method of displaying the column headers.
 * Sort controls are added at render, below, when we know the sort key state.
 */
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
            +"This"; h.br {}; +"That"
        }
    })

val GridleyDemo = FC<Props> {
    val totalRecords = 100
    val chars = ('A'..'Z').toList()
    val allRecords = (0 until totalRecords).map { i ->
        val name = buildString {
            repeat(32) {
                append(
                    chars[floor(Random.nextDouble() * chars.size).toInt()]
                )
            }
        }
        listOf(
            listOf(i.toString()),
            listOf(name),
            listOf(Random.nextBoolean().toString(), Random.nextBoolean().toString())
        )
    }

    Gridley {
        records = allRecords
        pageSize = 15
    }
}

external interface GridleyProps : Props {
    var records: List<List<List<String>>>
    var pageSize: Int
}

/**
 * The normal functions of a data grid (display, pagination, filtering, and sorting) are decomposed.
 * We get flexibility and separation of concerns, but must do a lot for ourselves..
 */
@Suppress("LocalVariableName")
val Gridley = FC<GridleyProps> { props ->
    var _currentPage by useState(0)
    var _filter by useState("")
    var _sortKey: SortKey? by useState(null)

    // First, filter, then sort.
    val processedRecords: List<List<List<String>>> =
        if (_filter.isEmpty()) props.records else {
            props.records.filter { record -> record.any { lines -> lines.any { it.contains(_filter) } } }
        }.let { filtered ->
            // Here, we're free to represent the fields in the record in any way we want for the purposes of sorting.
            if (null == _sortKey) filtered else {
                fun <S : Comparable<S>> directionalSort(sortingFunction: (List<List<String>>) -> S) =
                    when (_sortKey!!.sortDir) {
                        SortDir.Ascending ->
                            filtered.sortedBy { sortingFunction(it) }
                        SortDir.Descending ->
                            filtered.sortedByDescending { sortingFunction(it) }
                    }
                when (val sortIndex = _sortKey!!.index) {
                    0 -> directionalSort { it[sortIndex][0].toInt() }
                    else -> directionalSort { it[sortIndex].joinToString("") }
                }
            }
        }
    val _totalPages = ceil(processedRecords.size.toDouble() / props.pageSize).toInt()

    // Ensure we're on an actual page.
    val effectivePage = if (_currentPage >= _totalPages) _totalPages - 1 else _currentPage

    val pageBounds = processedRecords.size.let { totalRecords ->
        val low = max(0, effectivePage * props.pageSize)
        val high = min((1 + effectivePage) * props.pageSize, totalRecords)
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
                fields = columnHeaders.withIndex().map { p ->
                    val index = p.index
                    FC {
                        SortControl {
                            sortDir = _sortKey?.let { if (index == it.index) it.sortDir else null }
                            action = { dir ->
                                console.log("ACTION!", dir)
                                _sortKey = SortKey(index, dir)
                            }
                        }
                        h.div {
                            className = ClassName("float-left")
                            Entities.nbsp {}
                        }
                        h.div {
                            className = ClassName("float-left header")
                            p.value {}
                        }
                    }
                }
                records = processedRecords.slice(pageBounds).map { record ->
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

