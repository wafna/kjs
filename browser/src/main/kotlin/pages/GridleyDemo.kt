package pages

import Col
import ColumnScale
import Row
import csstype.ClassName
import react.FC
import react.Props
import react.PropsWithClassName
import react.dom.aria.ariaLabel
import react.dom.html.ButtonType
import react.dom.html.InputType
import react.useState
import util.classNames
import util.preventDefault
import util.withTargetValue
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random
import react.dom.html.ReactHTML as h

// The nested list allows us to specify lines within table cells.
typealias Record = List<List<String>>
// ibid.
typealias FieldName = List<String>

private data class SortKey(val index: Int, val ascending: Boolean)

private external interface GridleyProps : PropsWithClassName {
    var fields: List<FieldName>
    var records: List<Record>
    var sortKey: SortKey?
}

/**
 * Data grid with pagination.
 */
private val Gridley = FC<GridleyProps> { props ->

    val records = props.records

    if (records.isEmpty()) {
        h.div {
            className = ClassName("alert alert-warning")
            h.h3 { +"No records." }
        }
    } else {

        h.table {
            className = props.className
            h.thead {
                h.tr {
                    for (field in props.fields) {
                        h.th {
                            var sep = false
                            for (line in field) {
                                if (sep) h.br {} else sep = true
                                +line
                            }
                        }
                    }
                }
            }
            h.tbody {
                for (record in records) {
                    h.tr {
                        for (field in record) {
                            h.td {
                                var sep = false
                                for (line in field) {
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
}

external interface FiltrationProps : Props {
    var onFilter: (String) -> Unit
}

val Filtration = FC<FiltrationProps> { props ->
    var filter by useState("")
    h.form {
        className = ClassName("form-inline")
        h.div {
            className = ClassName("form-group")
            h.input {
                className = ClassName("form-control")
                type = InputType.search
                placeholder = "Search..."
                ariaLabel = "Search"
                value = filter
                onChange = withTargetValue { filter = it }
            }
            h.button {
                className = ClassName("btn btn-outline-primary")
                type = ButtonType.submit
                onClick = preventDefault { props.onFilter(filter) }
                +"Search"
            }
        }
    }
}

external interface PaginatorProps : Props {
    var totalPages: Int
    var currentPage: Int
    var onPageSelect: (Int) -> Unit
}

val Paginator = FC<PaginatorProps> { props ->
    val preceding = props.currentPage
    val following = props.totalPages - props.currentPage - 1

    h.nav {
        h.ul {
            className = ClassName("pagination")
            h.li {
                className = ClassName("page-item")
                h.span {
                    className = classNames("page-link", if (0 < preceding) null else "disabled")
                    +"Prev"
                    onClick = { props.onPageSelect(preceding - 1) }
                }
            }
            h.li {
                className = ClassName("page-item")
                h.span {
                    className = ClassName("page-link disabled")
                    +(1 + props.currentPage).toString()
                }
            }
            h.li {
                className = ClassName("page-item")
                h.span {
                    className = classNames("page-link", if (0 < following) null else "disabled")
                    +"Next"
                    onClick = { props.onPageSelect(preceding + 1) }
                }
            }
        }
    }
}

/**
 * The normal functions of a data grid (display, pagination, filtering, and sorting) are decomposed.
 * We get flexibility, but must hook a lot of stuff up.
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
            Paginator {
                totalPages = _totalPages
                currentPage = effectivePage
                onPageSelect = { _currentPage = it }
            }
        }
    }
    Row {
        Col {
            scale = ColumnScale.Large
            size = 12
            Filtration {
                onFilter = { _filter = it }
            }
        }
    }
    Row {
        Col {
            scale = ColumnScale.Large
            size = 12
            Gridley {
                className = ClassName("table table-sm")
                fields = listOf(listOf("ID"), listOf("Name"), listOf("Random", "Number"))
                records = filtered.slice(pageBounds)

            }
        }
    }
}
