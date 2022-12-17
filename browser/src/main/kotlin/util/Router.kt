package util

import kotlinx.browser.window
import react.ChildrenBuilder
import react.FC
import react.Props

typealias Params = Map<String, String>

fun Params.getInt(param: String): Int? = get(param)?.toIntOrNull()

class ParamBuilder internal constructor() {
    private val params = mutableMapOf<String, String>()
    internal fun toMap(): Params = params.toMap()

    operator fun Pair<String, Any>.unaryPlus() {
        if (params.containsKey(this.first))
            throw Exception("Duplicate parameter ${this.first}")
        params[this.first] = this.second.toString() ?: ""
    }
}

fun paramBuilder(block: ParamBuilder.() -> Unit): Params =
    ParamBuilder().also { it.block() }.toMap()


/**
 * The router treats the hash fragment as an id followed by an optional query string.
 */
data class HashRoute(val path: String, val params: Params = mapOf()) {
    /**
     * For anchors.
     */
    val href by lazy {
        buildString {
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

    @Suppress("unused")
    fun goto() {
        window.location.hash = href
    }

    companion object {
        fun build(routeId: String, params: ParamBuilder.() -> Unit): HashRoute =
            HashRoute(routeId, paramBuilder(params))

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

/**
 * Encapsulates mappings between hash routes and components.
 */
interface Route {
    /**
     * Uniquely indicates a route.
     */
    val routeId: String

    /**
     * Each page must produce a component.  However, these components may require configuration (props).
     * Here, the params from the hash are available for component configuration.
     */
    fun component(params: Params = mapOf()): FC<Props>

    /**
     * Returns the hash with no params.  Most routes will work this way.
     */
    fun defaultHash(): HashRoute = HashRoute(routeId)
}

/**
 * Searches the routes for a match for the hash and emits its component.
 * Emits the defaultComponent when the hash is empty or missing.
 * Uses the first route as a default if no hash is present or no match is found.
 */
fun ChildrenBuilder.doRoute(
    hash: HashRoute?,
    routes: List<Route>
) {
    require(routes.isNotEmpty()) {
        "No routes provided."
    }
    routes.map { it.routeId }.let {
        require(it.toSet().size == routes.size) {
            "Non-unique route ids detected in: ${it.joinToString(", ")}"
        }
    }
    val defPage by lazy { routes[0].component() }
    if (null == hash) {
        defPage{}
    } else {
        val hashPath = hash.path
        if (hashPath.isEmpty()) {
            defPage{}
        } else {
            when (val page = routes.find { it.routeId == hashPath }) {
                null -> {
                    console.warn("Bad route hash", hash)
                    defPage{}
                }
                else -> (page.component(hash.params)){}
            }
        }
    }
}