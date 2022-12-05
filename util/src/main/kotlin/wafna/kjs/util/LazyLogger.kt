package wafna.kjs.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

class LazyLogger(val log: Logger) {
    constructor(kClass: KClass<*>) : this(LoggerFactory.getLogger(kClass.java))
    constructor(name: String) : this(LoggerFactory.getLogger(name))

    fun error(msg: () -> String) {
        if (log.isErrorEnabled) log.error(msg())
    }

    fun error(e: Throwable, msg: () -> String) {
        if (log.isErrorEnabled) log.error(msg(), e)
    }

    fun warn(msg: () -> String) {
        if (log.isWarnEnabled) log.warn(msg())
    }

    suspend fun warnT(msg: suspend () -> String) {
        if (log.isWarnEnabled) msg().also { log.warn(it) }
    }

    @Suppress("unused")
    fun warn(e: Throwable, msg: () -> String) {
        if (log.isWarnEnabled) log.warn(msg(), e)
    }

    fun info(msg: () -> String) {
        if (log.isInfoEnabled) log.info(msg())
    }

    suspend fun infoT(msg: suspend () -> String) {
        if (log.isInfoEnabled) msg().also { log.info(it) }
    }

    fun info(e: Throwable, msg: () -> String) {
        if (log.isInfoEnabled) log.info(msg(), e)
    }

    fun debug(msg: () -> String) {
        if (log.isDebugEnabled) log.debug(msg())
    }

    suspend fun debugT(msg: suspend () -> String) {
        if (log.isDebugEnabled) msg().also { log.debug(it) }
    }

    @Suppress("unused")
    fun debug(e: Throwable, msg: () -> String) {
        if (log.isDebugEnabled) log.debug(msg(), e)
    }

    fun trace(msg: () -> String) {
        if (log.isTraceEnabled) log.trace(msg())
    }

    @Suppress("unused")
    fun trace(e: Throwable, msg: () -> String) {
        if (log.isTraceEnabled) log.trace(msg(), e)
    }
}

