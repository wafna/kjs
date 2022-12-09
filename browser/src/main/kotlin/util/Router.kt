package util

import kotlinx.browser.window
import react.*
import react.dom.html.AnchorTarget

/**
 * The router treats the hash fragment as an id followed by an optional query string.
 */
data class HashRoute(val path: String, val params: Map<String, String> = mapOf()) {
    /**
     * For anchors.
     */
    val href = buildString {
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

    fun goto() {
        window.location.hash = href
    }

    companion object {
        /**
         * Retrieves the current hash parsed as a HashRoute.
         */
        fun currentHash(): HashRoute = window.location.hash.let { hash ->
            val raw = if (hash.startsWith("#")) {
                hash.substring(1)
            } else hash
            val qSplit = raw.split("?")
            if (qSplit.isEmpty()) {
                HashRoute(raw, mapOf())
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
                HashRoute(path, params)
            }
        }
    }
}

interface Route {
    /**
     * Uniquely indicates a route.
     */
    val routeId: String

    /**
     * Each page must produce a component.  However, these components may require configuration (props).
     * Here, the params from the hash are available for component configuration.
     */
    fun component(params: Map<String, String> = mapOf()): FC<Props>

    /**
     * Returns the hash with no params.  Most routes will work this way.
     */
    fun defaultHash(): HashRoute = HashRoute(routeId)
}

/**
 * Searches the routes for a match for the hash and emits its component.
 * Emits the defaultComponent when the hash is empty or missing.
 * Throws an error if no match is found.
 */
fun ChildrenBuilder.doRoute(routes: Collection<Route>, hash: HashRoute?, defaultComponent: FC<Props>) {
    routes.map { it.routeId }.let {
        require(it.toSet().size == routes.size) {
            throw RuntimeException("Non-unique route ids detected in: ${it.joinToString(", ")}")
        }
    }
    if (null == hash) {
        defaultComponent {}
    } else {
        val hashPath = hash.path
        if (hashPath.isEmpty()) {
            defaultComponent {}
        } else {
            when (val page = routes.find { it.routeId == hashPath }) {
                null -> throw RuntimeException("Unknown hash path: $hashPath")
                else -> (page.component(hash.params)) {}
            }
        }
    }
}