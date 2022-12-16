package pages.gridley

import csstype.ClassName
import react.FC
import react.Props
import react.dom.html.ReactHTML

external interface GridleyPagerProps : Props {
    var totalPages: Int
    var currentPage: Int
    var onPageSelect: (Int) -> Unit
}

private val PageLink = ClassName("page-link")
private val PageLinkDisabled = ClassName("page-link disabled")
private val PageItem = ClassName("page-item")

/**
 * Implementation of GridleyPagerProps using Bootstrap.
 */
val GridleyPager = FC<GridleyPagerProps> { props ->
    val preceding = props.currentPage
    val following = props.totalPages - props.currentPage - 1

    ReactHTML.nav {
        ReactHTML.ul {
            className = ClassName("pagination")
            ReactHTML.li {
                className = PageItem
                ReactHTML.span {
                    className = if (1 < preceding) PageLink else PageLinkDisabled
                    +"⟪"
                    onClick = { props.onPageSelect(0) }
                }
            }
            ReactHTML.li {
                className = PageItem
                ReactHTML.span {
                    className = if (0 < preceding) PageLink else PageLinkDisabled
                    +"⟨"
                    onClick = { props.onPageSelect(preceding - 1) }
                }
            }
            ReactHTML.li {
                className = PageItem
                ReactHTML.span {
                    className = PageLinkDisabled
                    +(1 + preceding).toString()
                }
            }
            ReactHTML.li {
                className = PageItem
                ReactHTML.span {
                    className = if (0 < following) PageLink else PageLinkDisabled
                    +"⟩"
                    onClick = { props.onPageSelect(preceding + 1) }
                }
            }
            ReactHTML.li {
                className = PageItem
                ReactHTML.span {
                    className = if (1 < following) PageLink else PageLinkDisabled
                    +"》"
                    onClick = { props.onPageSelect(props.totalPages - 1) }
                }
            }
        }
    }
}
