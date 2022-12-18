package pages.gridley

import csstype.ClassName
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
 * By specifying the columns in an indexable collection we can correlate them with table events, like sorting.
 * This abstraction later sets up a very generic way to render the table and data
 * as well as implementation of searching and sorting.
 */
abstract class DisplayColumn {
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

/**
 * Keeping track of the column on and direction in which sorting is to be applied.
 */
private data class SortKey(val index: Int, val sortDir: SortDir)

/**
 * We can be be type safe on the records because none of the-components
 * knows what it's doing.
 */
external interface GridleyProps<R> : Props {
    var columns: List<DisplayColumn>
    var pageSize: Int
    var recordSet: List<R>
    var emptyMessage: FC<Props>
}

/**
 * This is the nexus of the grid where all the bits are wired together.
 */
val Gridley = FC<GridleyProps<GridRecord>> { props ->

    // We keep separate lists of filtered and sorted records.
    // This improves performance as well as making the effect of successive sorts cumulative.
    var filteredRecords by useState(props.recordSet)
    var sortedRecords by useState(props.recordSet)

    var selectedPage by useState(0)
    var sortKey: SortKey? by useState(null)

    fun updateFilteredRecords(searchTarget: String) {
        filteredRecords = if (searchTarget.isEmpty()) {
            props.recordSet
        } else {
            inline fun hit(s: String) = s.contains(searchTarget)
            props.recordSet.filter { record ->
                hit(record.id.toString()) || hit(record.name) || hit(record.number)
            }
        }
    }

    fun updateSortedRecords(sortKey: SortKey?) {
        sortedRecords = when (sortKey) {
            null -> filteredRecords
            else ->
                // We can be sure the comparator exists because we rendered a sort key for it.
                props.columns[sortKey.index].comparator!!.let { comparator ->
                    filteredRecords.sortedWith(
                        when (sortKey.sortDir) {
                            SortDir.Ascending -> comparator
                            SortDir.Descending -> comparator.reversed()
                        }
                    )
                }
        }
    }

    // Data for pagination and display.

    val totalRecords = filteredRecords.size
    val pageCount = ceil(totalRecords.toDouble() / props.pageSize).toInt()
    // Ensure we're on an actual page.
    val effectivePage = max(0, min(pageCount - 1, selectedPage))
    // The page of records to display.
    val displayRecords = sortedRecords.run {
        val low = max(0, effectivePage * props.pageSize)
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
                    onSearch = ::updateFilteredRecords
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
                headers = props.columns.withIndex().map { p ->
                    val columnIndex = p.index
                    val column = p.value
                    FC {
                        // The presence of a comparator means the column is sortable.
                        if (null != column.comparator) {
                            h.div {
                                className = ClassName("float-left ")
                                SortControl {
                                    sortDir = sortKey?.let { if (columnIndex == it.index) it.sortDir else null }
                                    action = { sortDir ->
                                        // We cannot rely on state to return the new sort key when updateSortedRecords
                                        // executes, below, so we pass it in.
                                        val newSK = SortKey(columnIndex, sortDir)
                                        sortKey = newSK
                                        updateSortedRecords(newSK)
                                    }
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
                        RecordLine(record.id.toString(), props.columns.map { it.renderField(record) })
                    }
                emptyMessage = props.emptyMessage
            }
        }
    }
}

