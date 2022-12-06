package wafna.kjs.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * Avoids creation of the error message unless the
 */
@Suppress("unused")
class LazyLogger(val log: Logger) {
    constructor(kClass: KClass<*>) : this(LoggerFactory.getLogger(kClass.java))
    constructor(name: String) : this(LoggerFactory.getLogger(name))

    inline fun error(msg: () -> String) {
        if (log.isErrorEnabled) log.error(msg())
    }

    inline fun error(e: Throwable, msg: () -> String) {
        if (log.isErrorEnabled) log.error(msg(), e)
    }

    inline fun warn(msg: () -> String) {
        if (log.isWarnEnabled) log.warn(msg())
    }

    suspend fun warnT(msg: suspend () -> String) {
        if (log.isWarnEnabled) msg().also { log.warn(it) }
    }

    inline fun warn(e: Throwable, msg: () -> String) {
        if (log.isWarnEnabled) log.warn(msg(), e)
    }

    inline fun info(msg: () -> String) {
        if (log.isInfoEnabled) log.info(msg())
    }

    suspend fun infoT(msg: suspend () -> String) {
        if (log.isInfoEnabled) msg().also { log.info(it) }
    }

    inline fun info(e: Throwable, msg: () -> String) {
        if (log.isInfoEnabled) log.info(msg(), e)
    }

    inline fun debug(msg: () -> String) {
        if (log.isDebugEnabled) log.debug(msg())
    }

    suspend fun debugT(msg: suspend () -> String) {
        if (log.isDebugEnabled) msg().also { log.debug(it) }
    }

    inline fun debug(e: Throwable, msg: () -> String) {
        if (log.isDebugEnabled) log.debug(msg(), e)
    }

    inline fun trace(msg: () -> String) {
        if (log.isTraceEnabled) log.trace(msg())
    }

    inline fun trace(e: Throwable, msg: () -> String) {
        if (log.isTraceEnabled) log.trace(msg(), e)
    }
}

