package pages.gridley

import csstype.ClassName
import react.FC
import react.Props
import react.PropsWithClassName
import react.dom.html.ReactHTML as h

typealias DisplayLine = List<FC<Props>>

external interface GridleyDisplayProps : PropsWithClassName {
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
val GridleyDisplay = FC<GridleyDisplayProps> { props ->

    val records = props.records

    h.div {
        h.div {
            className = ClassName("flow-down")
            h.table {
                className = props.className
                h.thead {
                    h.tr {
                        for (header in props.headers) {
                            h.th { header {} }
                        }
                    }
                }
                h.tbody {
                    for (record in records) {
                        h.tr {
                            for (field in record) {
                                h.td { field {} }
                            }
                        }
                    }
                }
            }
        }
        if (records.isEmpty()) {
            props.empty {}
        }
    }
}


