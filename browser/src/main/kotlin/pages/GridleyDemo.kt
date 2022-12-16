package pages

import csstype.ClassName
import csstype.Color
import emotion.react.css
import pages.gridley.*
import react.*
import util.*
import util.Entities.nbsp
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import react.dom.html.ReactHTML as h

/**
 * Table cell contents for each of the column headers.
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

/**
 * Our custom record data type.
 */
data class GridRecord(val id: Int, val name: String, val number: String, val stuff: Pair<Boolean, Boolean>)

external interface GridleyProps<R> : Props {
    var records: List<R>
    var pageSize: Int
}

/**
 * The Gridley system decomposes the normal functions of a data grid (display, pagination, filtering, and sorting).
 *
 * This is one example of how to wire them all up.
 */
@Suppress("LocalVariableName")
val GridleyDemo = FC<GridleyProps<GridRecord>> { props ->
    var _currentPage by useState(0)
    var _filter by useState("")
    var _sortKey: SortKey? by useState(null)

    // We have some options here, depending on whether we want our sorts to be stable relative to previous sorts.
    // Here, we resort from the original list.
    // We could, instead, apply the sort to the entire record set and save it in state,
    val processedRecords =
        // First, filter, then sort.
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
                    2 -> directionalSort { it.number }
                    else -> {
                        console.warn("Invalid sort key index: $sortIndex")
                        directionalSort { false }
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
                headers = columnHeaders.withIndex().map { p ->
                    val index = p.index
                    FC {
                        if (index != 3) {
                            SortControl {
                                sortDir = _sortKey?.let { if (index == it.index) it.sortDir else null }
                                action = { dir ->
                                    _sortKey = SortKey(index, dir)
                                }
                            }
                            h.div {
                                className = ClassName("float-left")
                                nbsp()
                            }
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
