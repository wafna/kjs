package util

import csstype.ClassName
import react.FC
import react.Props
import react.PropsWithClassName
import react.dom.aria.ariaLabel
import react.dom.html.InputType
import react.dom.html.ReactHTML
import react.useState

/*
 * A collection of components for presenting records in a table with optional pagination and searching (filtering).
 */

typealias DisplayLine = List<FC<Props>>

external interface GridleyTableProps : PropsWithClassName {
    var fields: DisplayLine
    var records: List<DisplayLine>
    var empty: FC<Props>
}

val GridleyTable = FC<GridleyTableProps> { props ->

    val records = props.records

    if (records.isEmpty()) {
        props.empty {}
    } else {

        ReactHTML.table {
            className = props.className
            ReactHTML.thead {
                ReactHTML.tr {
                    for (field in props.fields) {
                        ReactHTML.th {
                            field {}
                        }
                    }
                }
            }
            ReactHTML.tbody {
                for (record in records) {
                    ReactHTML.tr {
                        for (field in record) {
                            ReactHTML.td {
                                field {}
                            }
                        }
                    }
                }
            }
        }
    }
}

external interface GridleySearchProps : Props {
    var onFilter: (String) -> Unit
}

val GridleySearch = FC<GridleySearchProps> { props ->
    var filter by useState("")
    ReactHTML.input {
        className = ClassName("form-control")
        type = InputType.search
        placeholder = "Search..."
        ariaLabel = "Search"
        value = filter
        onChange = withTargetValue {
            filter = it
            props.onFilter(it)
        }
        autoFocus = true
    }
}

external interface GridleyPagerProps : Props {
    var totalPages: Int
    var currentPage: Int
    var onPageSelect: (Int) -> Unit
}

val GridleyPager = FC<GridleyPagerProps> { props ->
    val preceding = props.currentPage
    val following = props.totalPages - props.currentPage - 1

    ReactHTML.nav {
        ReactHTML.ul {
            className = ClassName("pagination")
            ReactHTML.li {
                className = ClassName("page-item")
                ReactHTML.span {
                    className = classNames("page-link", if (1 < preceding) null else "disabled")
                    +"⟪"
                    onClick = { props.onPageSelect(0) }
                }
            }
            ReactHTML.li {
                className = ClassName("page-item")
                ReactHTML.span {
                    className = classNames("page-link", if (0 < preceding) null else "disabled")
                    +"⟨"
                    onClick = { props.onPageSelect(preceding - 1) }
                }
            }
            ReactHTML.li {
                className = ClassName("page-item")
                ReactHTML.span {
                    className = ClassName("page-link disabled")
                    +(1 + props.currentPage).toString()
                }
            }
            ReactHTML.li {
                className = ClassName("page-item")
                ReactHTML.span {
                    className = classNames("page-link", if (0 < following) null else "disabled")
                    +"⟩"
                    onClick = { props.onPageSelect(preceding + 1) }
                }
            }
            ReactHTML.li {
                className = ClassName("page-item")
                ReactHTML.span {
                    className = classNames("page-link", if (1 < following) null else "disabled")
                    +"》"
                    onClick = { props.onPageSelect(props.totalPages - 1) }
                }
            }
        }
    }
}

