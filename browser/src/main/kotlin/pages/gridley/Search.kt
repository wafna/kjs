package pages.gridley

import csstype.ClassName
import react.FC
import react.Props
import react.dom.aria.ariaLabel
import react.dom.html.InputType
import react.useState
import util.withTargetValue
import react.dom.html.ReactHTML as h

external interface GridleySearchProps : Props {
    var onSearch: (String) -> Unit
}

val Search = FC<GridleySearchProps> { props ->
    var filter by useState("")
    h.input {
        className = ClassName("form-control")
        type = InputType.search
        placeholder = "Search..."
        ariaLabel = "Search"
        value = filter
        size = 48
        onChange = withTargetValue {
            filter = it
            props.onSearch(it)
        }
        autoFocus = true
    }
}

