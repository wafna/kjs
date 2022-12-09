package util

import csstype.ClassName
import org.w3c.dom.HTMLInputElement
import react.PropsWithClassName
import react.dom.events.ChangeEvent
import react.dom.events.MouseEvent

// Odd that this overload was not provided.
inline fun PropsWithClassName.css(vararg classNames: ClassName?) {
    className = emotion.css.ClassName(classNames = classNames) {}
}

/**
 * Wraps an event handler by calling <code>preventDefault()</code> on the event before passing it on.
 */
fun preventDefault(op: (MouseEvent<*, *>) -> Unit): (MouseEvent<*, *>) -> Unit = { e ->
    e.preventDefault()
    op(e)
}

/**
 * Receives the value of an input.
 */
fun withTargetValue(block: (String) -> Unit): (ChangeEvent<HTMLInputElement>) -> Unit = { e ->
    block(e.target.value)
}

/**
 * Converts a collection to a map by applying a key generating function to each element.
 */
fun <K : Any, V : Any> Collection<K>.toMultiMap(key: (K) -> V): Map<V, List<K>> =
    fold(mutableMapOf()) { map, elem ->
        val k = key(elem)
        map[k] = when (val x = map[k]) {
            null -> listOf(elem)
            else -> x + elem
        }
        map
    }


// By allowing nulls we can conditionally inline CSS classes.
fun classNames(vararg className: String?): ClassName =
    ClassName(className.filterNotNull().joinToString(" "))

