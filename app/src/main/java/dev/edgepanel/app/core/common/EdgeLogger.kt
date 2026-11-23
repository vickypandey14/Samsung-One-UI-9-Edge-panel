package dev.edgepanel.app.core.common

interface EdgeLogger {
    fun debug(message: String)
    fun info(message: String)
    fun error(message: String, error: Throwable? = null)
}
