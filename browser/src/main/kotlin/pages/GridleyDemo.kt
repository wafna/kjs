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
 * @param id This will be sorted numerically.
 * @param name A string of random lower case letters (for sorting and searching).
 * @param number A string of random decimal digits (for sorting and searching).
 * @param stuff An excuse to do some pretty UI.
 */
data class GridRecord(val id: Int, val name: String, val number: String, val stuff: Pair<Boolean, Boolean>)

/**
 * Keeping track of the column on and direction in which sorting is to be applied.
 */
private data class SortKey(val index: Int, val sortDir: SortDir)

external interface GridleyProps<R> : Props {
    var records: List<R>
    var pageSize: Int
}

/**
 * The Gridley system decomposes the normal functions of a data grid (display, pagination, filtering, and sorting).
 *
 * This is one example of how to wire them all up.
 * The important point to note is that the contents of the display (in this example a table) are entirely defined here.
 */
@Suppress("LocalVariableName")
val GridleyDemo = FC<GridleyProps<GridRecord>> { props ->
    var selectedPage by useState(0)
    var searchTarget by useState("")
    var _sortKey: SortKey? by useState(null)

    // Below, we have to calculate many things, first and foremost being which records we'll display.
    // We also need to calculate

    val filteredRecords =
        if (searchTarget.isEmpty())
            props.records
        else {
            props.records.filter { record ->
                listOf(record.id.toString(), record.name, record.number).any { it.contains(searchTarget) }
            }
        }
    val totalRecords = filteredRecords.size
    val pageCount = ceil(totalRecords.toDouble() / props.pageSize).toInt()
    // Ensure we're on an actual page.
    val effectivePage = if (selectedPage >= pageCount) pageCount - 1 else selectedPage
    val sortedRecords =
        if (null == _sortKey) filteredRecords else {
            fun <S : Comparable<S>> directionalSort(sortingFunction: (GridRecord) -> S) =
                when (_sortKey!!.sortDir) {
                    SortDir.Ascending ->
                        filteredRecords.sortedBy { sortingFunction(it) }
                    SortDir.Descending ->
                        filteredRecords.sortedByDescending { sortingFunction(it) }
                }
            when (val sortIndex = _sortKey!!.index) {
                // Numerical sort on id.
                0 -> directionalSort { it.id }
                // Lexical sort on everything else.
                1 -> directionalSort { it.name }
                2 -> directionalSort { it.number }
                else -> {
                    console.warn("Invalid sort key index: $sortIndex")
                    filteredRecords
                }
            }
        }

    // The records we'll actually display.
    val recordSlice = let {
        val low = max(0, effectivePage * props.pageSize)
        val high = min((1 + effectivePage) * props.pageSize, totalRecords)
        sortedRecords.slice(low until high)
    }

    Row {
        Col {
            scale = ColumnScale.Large
            size = 12
            h.div {
                className = ClassName("float-left")
                GridleyPager {
                    totalPages = pageCount
                    currentPage = effectivePage
                    onPageSelect = { selectedPage = it }
                }
            }
            h.div {
                className = ClassName("float-right")
                GridleySearch {
                    onSearch = { searchTarget = it }
                }
            }
        }
    }
    h.br {}
    Row {
        Col {
            scale = ColumnScale.Large
            size = 12
            GridleyDisplay {
                className = ClassName("table table-sm")
                // Render the column headers to an array of components.
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
                // Render each record to an array of components.
                records = recordSlice.map { record ->
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
                    h.div {
                        className = ClassName("alert alert-warning")
                        h.h3 { +"No records." }
                    }
                }
            }
        }
    }
}

/**
 * This fronts for the demo by creating some records.
 * The data in the records are intended to make it easy to narrow down the page count.
 */
val GridleyDemoRecordSource = FC<Props> {
    val totalRecords = 1800
    val chars = ('a'..'z').toList()
    val digits = ('0'..'9').toList()

    GridleyDemo {
        pageSize = 15
        records = (0 until totalRecords).map { id ->
            GridRecord(
                id,
                randomString(chars, 32),
                randomString(digits, 32),
                Pair(Random.nextBoolean(), Random.nextBoolean())
            )
        }
    }
}
