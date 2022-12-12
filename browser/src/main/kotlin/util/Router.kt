package util

import kotlinx.browser.window
import react.ChildrenBuilder
import react.FC
import react.Props

typealias Params = Map<String, String>

fun Params.getString(param: String): String? = get(param)
fun Params.getInt(param: String): Int? = get(param)?.toIntOrNull()
fun Params.getDouble(param: String): Double? = get(param)?.toDoubleOrNull()

class ParamBuilder internal constructor() {
    private val params = mutableMapOf<String, String>()
    internal fun toMap(): Params = params.toMap()
    fun addParam(name: String, value: Any?) {
        if (params.containsKey(name))
            throw Exception("Duplicate parameter $name")
        params[name] = value?.toString() ?: ""
    }

    operator fun Pair<String, Any>.unaryPlus() {
        addParam(this.first, this.second)
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
 * Throws an error if no match is found.
 */
fun ChildrenBuilder.doRoute(
    routes: Collection<Route>,
    hash: HashRoute?,
    defaultComponent: FC<Props>,
    badHash: ChildrenBuilder.(HashRoute) -> Unit = {}
) {
    routes.map { it.routeId }.let {
        require(it.toSet().size == routes.size) {
            "Non-unique route ids detected in: ${it.joinToString(", ")}"
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
                null -> badHash(hash)
                else -> (page.component(hash.params)) {}
            }
        }
    }
}