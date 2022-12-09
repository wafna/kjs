package util

import kotlinx.browser.window
import react.FC
import react.PropsWithChildren
import react.PropsWithStyle
import react.dom.html.AnchorTarget
import react.dom.html.ReactHTML as h

/**
 * The router treats the hash fragment as a path with a query string.
 */
data class HashURL(val path: String, val params: Map<String, String> = mapOf()) {
    fun toHashRef() = buildString {
        append("#")
        append(path)
        if (params.isNotEmpty()) {
            append("?")
            for (param in params) {
                append(param.key)
                append("=")
                append(param.value)
            }
        }
    }
}

external interface LinkProps : PropsWithChildren, PropsWithStyle {
    var to: HashURL
    var target: AnchorTarget?
}

val Link = FC<LinkProps> { props ->
    h.a {
        href = props.to.toHashRef()
        target = props.target
        children = props.children
        style = props.style
    }
}


object Router {
    /**
     * Retrieves the current hash parsed as a HashURL.
     */
    fun currentHash(): HashURL = window.location.hash.let { hash ->
        val raw = if (hash.startsWith("#")) {
            hash.substring(1)
        } else hash
        val qSplit = raw.split("?")
        if (qSplit.isEmpty()) {
            HashURL(raw, mapOf())
        } else {
            check(2 >= qSplit.size)
            val path = qSplit[0]
            val params = if (2 > qSplit.size) {
                mapOf<String, String>()
            } else {
                qSplit[1].let { queryString ->
                    queryString.split("&").fold(mapOf()) { params, param ->
                        param.split("=").let { pair ->
                            check(2 == pair.size) { "Malformed query parameter $pair" }
                            val name = pair[0]
                            val value = pair[1]
                            check(!params.containsKey(name)) { "Duplicate param name $name" }
                            params + (name to value)
                        }
                    }
                }
            }
            HashURL(path, params)
        }
    }

}