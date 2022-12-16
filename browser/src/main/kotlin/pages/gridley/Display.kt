package pages.gridley

import react.FC
import react.Props
import react.PropsWithClassName
import react.dom.html.ReactHTML

typealias DisplayLine = List<FC<Props>>

external interface GridleyTableProps : PropsWithClassName {
    /**
     * List of components for the table header.
     */
    var headers: DisplayLine

    /**
     * A list of lists of components for the table data.
     */
    var records: List<DisplayLine>

    /**
     * Component to display when there are no records.
     */
    var empty: FC<Props>
}

/**
 * Creates a table with the supplied className and renders the header and row data into it.
 * This renders ALL the records given to it; pagination is handled elsewhere.
 */
val GridleyTable = FC<GridleyTableProps> { props ->

    val records = props.records

    if (records.isEmpty()) {
        props.empty {}
    } else {

        ReactHTML.table {
            className = props.className
            ReactHTML.thead {
                ReactHTML.tr {
                    for (header in props.headers) {
                        ReactHTML.th { header {} }
                    }
                }
            }
            ReactHTML.tbody {
                for (record in records) {
                    ReactHTML.tr {
                        for (field in record) {
                            ReactHTML.td { field {} }
                        }
                    }
                }
            }
        }
    }
}


