package pages.gridley

import csstype.ClassName
import react.FC
import react.Props
import react.dom.aria.ariaLabel
import react.dom.html.InputType
import react.dom.html.ReactHTML
import react.useState
import util.withTargetValue

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
        size = 48
        onChange = withTargetValue {
            filter = it
            props.onFilter(it)
        }
        autoFocus = true
    }
}

