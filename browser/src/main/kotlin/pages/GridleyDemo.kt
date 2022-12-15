package pages

import csstype.ClassName
import csstype.Color
import emotion.react.css
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
            className = ClassName("float-right")
            +"Id"
        }
    },
    FC {
        h.div {
            className = ClassName("float-right")
            +"Name"
        }
    },
    FC {
        h.div {
            className = ClassName("float-right")
            +"Number"
        }
    },
    FC {
        h.div {
            className = ClassName("float-right")
            +"This"; h.br {}; +"That"
        }
    })

fun randomString(chars: List<Char>, length: Int): String {
    require(0 <= length)
    return buildString {
        repeat(32) {
            append(
                chars[floor(Random.nextDouble() * chars.size).toInt()]
            )
        }
    }
}

data class GridRecord(val id: Int, val name: String, val number: String, val stuff: Pair<Boolean, Boolean>)

external interface GridleyProps : Props {
    var records: List<GridRecord>
    var pageSize: Int
}

/**
 * The normal functions of a data grid (display, pagination, filtering, and sorting) are decomposed.
 * We get flexibility and separation of concerns, but must do a lot for ourselves..
 */
@Suppress("LocalVariableName")
val GridleyDemo = FC<GridleyProps> { props ->
    var _currentPage by useState(0)
    var _filter by useState("")
    var _sortKey: SortKey? by useState(null)

    // First, filter, then sort.
    val processedRecords =
        if (_filter.isEmpty()) props.records else {
            props.records.filter { record ->
                listOf(record.id.toString(), record.name, record.number).any { it.contains(_filter) }
            }
        }.let { filtered ->
            // Here, we're free to represent the fields in the record in any way we want for the purposes of sorting.
            if (null == _sortKey) filtered else {
                fun <S : Comparable<S>> directionalSort(sortingFunction: (GridRecord) -> S) =
                    when (_sortKey!!.sortDir) {
                        SortDir.Ascending ->
                            filtered.sortedBy { sortingFunction(it) }
                        SortDir.Descending ->
                            filtered.sortedByDescending { sortingFunction(it) }
                    }
                when (val sortIndex = _sortKey!!.index) {
                    // Numerical sort on id.
                    0 -> directionalSort { it.id }
                    // Textual sort on everything else.
                    1 -> directionalSort { it.name }
                    3 -> directionalSort { it.number }
                    else -> {
                        console.warn("Invalid sort key index: $sortIndex")
                        directionalSort { true }
                    }
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
            h.div {
                className = ClassName("float-left")
                GridleyPager {
                    totalPages = _totalPages
                    currentPage = effectivePage
                    onPageSelect = { _currentPage = it }
                }
            }
            h.div {
                className = ClassName("float-right")
                GridleySearch {
                    onFilter = { _filter = it }
                }
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
                    listOf(
                        FC { +record.id.toString() },
                        FC { h.pre { +record.name } },
                        FC { h.pre { +record.number } },
                        FC {
                            fun stuff(s: Boolean) {
                                if (s) {
                                    h.span {
                                        css {
                                            color = Color("#008000")
                                        }
                                        +"✓"
                                    }
                                } else {
                                    h.span {
                                        css {
                                            color = Color("#800000")
                                        }
                                        +"X"
                                    }
                                }
                            }
                            stuff(record.stuff.first)
                            h.br {}
                            stuff(record.stuff.second)
                        }
                    )
                }
                empty = FC {
                    react.dom.html.ReactHTML.div {
                        className = ClassName("alert alert-warning")
                        react.dom.html.ReactHTML.h3 { +"No records." }
                    }
                }

            }
        }
    }
}

/**
 * This fronts for the demo by creating some records.
 */
val GridleyDemoRecordSource = FC<Props> {
    val totalRecords = 1800
    val chars = ('A'..'Z').toList()
    val digits = ('0'..'9').toList()

    GridleyDemo {
        records = (0 until totalRecords).map { id ->
            GridRecord(
                id,
                randomString(chars, 32),
                randomString(digits, 32),
                Pair(Random.nextBoolean(), Random.nextBoolean())
            )
        }
        pageSize = 15
    }
}
