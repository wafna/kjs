package pages.gridley

import csstype.ClassName
import csstype.Color
import emotion.react.css
import pages.GridRecord
import react.FC
import react.Props
import react.useState
import util.Col
import util.ColumnScale
import util.Row
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import react.dom.html.ReactHTML as h

/**
 * Definition of a column in the table, providing the facilities of rendering, searching, and sorting.
 */
private abstract class DisplayColumn {
    /**
     * Indicates whether and how the column can be searched.
     * This allows for flexibility like case insensitive search or search on text implied by status icons.
     */
    abstract val searchFunction: ((GridRecord) -> String)?

    /**
     * Indicates whether and how the column can be sorted.
     * Allows for other sorting independent of representation, e.g. numbers and dates.
     */
    abstract val comparator: Comparator<GridRecord>?

    /**
     * The rendering of a column header.
     * Note that the sort key controls are applied later.
     */
    abstract val header: FC<Props>

    /**
     * The rendering of a record field.
     * Access to the entire record allows for derived fields.
     */
    abstract fun renderField(record: GridRecord): FC<Props>
}

// Specializations on DisplayColumn to avoid repetition.

private abstract class DisplayColumnStdHdr(headerText: String) : DisplayColumn() {
    override val header: FC<Props> = FC {
        h.span {
            className = ClassName("gridley-header")
            +headerText
        }
    }
}

private abstract class DisplayColumnInt(headerText: String) : DisplayColumnStdHdr(headerText) {
    abstract fun value(record: GridRecord): Int
    override val searchFunction: ((GridRecord) -> String) = { value(it).toString() }
    override val comparator: Comparator<GridRecord> = Comparator { a, b -> value(a).compareTo(value(b)) }
    override fun renderField(record: GridRecord): FC<Props> = FC { h.pre { +value(record).toString() } }
}

private abstract class DisplayColumnPre(headerText: String) : DisplayColumnStdHdr(headerText) {
    abstract fun value(record: GridRecord): String
    override val searchFunction: ((GridRecord) -> String) = { value(it) }
    override val comparator: Comparator<GridRecord> = Comparator { a, b -> value(a).compareTo(value(b)) }
    override fun renderField(record: GridRecord): FC<Props> = FC { h.pre { +value(record) } }
}

private val red = Color("#008000")
private val green = Color("#800000")

/**
 * By specifying the columns in an indexable collection we can correlate them with table events, like sorting.
 * For simple fields, all we have to do is teach the column how to get a value from the record.
 * This abstraction later sets up a very generic way to render the table and data
 * as well as implementation of searching and sorting.
 */
private val columns: List<DisplayColumn> = listOf(
    object : DisplayColumnInt("Id") {
        override fun value(record: GridRecord): Int = record.id
    },
    object : DisplayColumnPre("Name") {
        override fun value(record: GridRecord): String = record.name
    },
    object : DisplayColumnPre("Number") {
        override fun value(record: GridRecord): String = record.number
    },
    object : DisplayColumn() {
        override val searchFunction: ((GridRecord) -> String)? = null
        override val comparator: Comparator<GridRecord> =
            Comparator { a, b -> 10 * a.stuff.first.compareTo(b.stuff.first) + a.stuff.second.compareTo(b.stuff.second) }
        override val header: FC<Props> =
            FC { +"This"; h.br {}; +"That" }

        override fun renderField(record: GridRecord): FC<Props> =
            // You can do anything you want in here.
            FC {
                fun icon(s: Boolean) {
                    if (s) {
                        h.span {
                            css { color = red }
                            +"✓"
                        }
                    } else {
                        h.span {
                            css { color = green }
                            +"X"
                        }
                    }
                }
                icon(record.stuff.first)
                h.br {}
                icon(record.stuff.second)
            }
    }
)

/**
 * Keeping track of the column on and direction in which sorting is to be applied.
 */
private data class SortKey(val index: Int, val sortDir: SortDir)

/**
 * We can be be type safe on the records because none of the-components
 * knows what it's doing.
 */
external interface GridleyProps<R> : Props {
//    var columns: List<DisplayColumn>
    var pageSize: Int
    var recordSet: List<R>
}

/**
 * This is the nexus of the grid where all the bits are wired together.
 */
val Gridley = FC<GridleyProps<GridRecord>> { props ->
    var selectedPage by useState(0)
    var searchTarget by useState("")
    var sortKey: SortKey? by useState(null)

    // Below, we calculate a bunch of related things ultimately to collect the page of records we will display
    // and to get correct settings for the pagination control.

    val filteredRecords =
        if (searchTarget.isEmpty()) {
            props.recordSet
        } else {
            inline fun hit(s: String) = s.contains(searchTarget)
            props.recordSet.filter { record ->
                hit(record.id.toString()) || hit(record.name) || hit(record.number)
            }
        }
    val totalRecords = filteredRecords.size
    val pageCount = ceil(totalRecords.toDouble() / props.pageSize).toInt()
    // Ensure we're on an actual page.
    val effectivePage = min(pageCount - 1, selectedPage)
    val sortedRecords =
        if (null == sortKey) {
            filteredRecords
        } else {
            // We can be sure the comparator exists because we rendered a sort key for it.
            columns[sortKey!!.index].comparator!!.let { comparator ->
                filteredRecords.sortedWith(
                    when (sortKey!!.sortDir) {
                        SortDir.Ascending -> comparator
                        SortDir.Descending -> comparator.reversed()
                    }
                )
            }
        }
    // The page of records to display.
    val displayRecords = sortedRecords.run {
        val low = max(0,effectivePage * props.pageSize)
        val high = min((1 + effectivePage) * props.pageSize, totalRecords)
        slice(low until high)
    }

    Row {
        Col {
            scale = ColumnScale.Large
            size = 12
            h.div {
                className = ClassName("float-right")
                GridleyPager {
                    totalPages = pageCount
                    currentPage = effectivePage
                    onPageSelect = { selectedPage = it }
                }
            }
            h.div {
                className = ClassName("float-left")
                Search {
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
                // Render the column headers to an array of components.
                headers = columns.withIndex().map { p ->
                    val index = p.index
                    val column = p.value
                    FC {
                        // Sorting for all but the last column.
                        if (null != column.comparator) {
                            h.div {
                                className = ClassName("float-left ")
                                SortControl {
                                    sortDir = sortKey?.let { if (index == it.index) it.sortDir else null }
                                    action = { sortKey = SortKey(index, it) }
                                }
                            }
                        }
                        h.div {
                            className = ClassName("float-left")
                            column.header {}
                        }
                    }
                }
                records =
                    displayRecords.map { record ->
                        RecordLine(record.id.toString(), columns.map { it.renderField(record) })
                    }
                emptyMessage = FC {
                    h.div {
                        className = ClassName("alert alert-warning")
                        h.h3 { +"No records." }
                    }
                }
            }
        }
    }
}

