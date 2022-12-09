package util

import kotlinx.browser.window
import react.*
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
            var sep = false
            for (param in params) {
                if (sep) append("&") else sep = true
                append(param.key)
                append("=")
                append(param.value)
            }
        }
    }
}

external interface LinkProps : PropsWithChildren, PropsWithStyle, PropsWithClassName {
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
                            check(2 == pair.size) { "Malformed query parameter $pair in $raw" }
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

    interface Route {
        val path: String

        /**
         * Each page must produce a component.  However, these components may require configuration (props).
         * Here, we get the params from the hash for use in component configuration.
         */
        fun component(params: Map<String, String> = mapOf()): FC<Props>

        /**
         * Returns the hash with no params.  Most routes will work this way.
         */
        fun defaultHash(): HashURL = HashURL(path)
    }

    fun ChildrenBuilder.doRoute(routes: Collection<Route>, hash: HashURL?, defaultPage: FC<Props>) {
        if (null == hash) {
            defaultPage {}
        } else {
            val hashPath = hash.path
            if (hashPath.isEmpty()) {
                defaultPage {}
            } else {
                when (val page = routes.find { it.path == hashPath }) {
                    null ->
                        throw RuntimeException("Unknown hash path: $hashPath")

                    else ->
                        (page.component(hash.params)) {}
                }
            }
        }
    }

}