package com.mussonindustrial.embr.common.scripting

import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.BiConsumer
import java.util.function.BiFunction
import org.python.core.Py
import org.python.core.PyObject

@Suppress("Unused")
class PyCompletableFuture<T>(
    val future: CompletableFuture<T>,
    private val executor: PyScriptExecutor,
) {

    fun cancel(): Boolean {
        return future.cancel(false)
    }

    @JvmOverloads
    fun get(timeout: Long? = null): T {
        return if (timeout != null) {
            future.get(timeout, TimeUnit.MILLISECONDS)
        } else {
            future.get(60000L, TimeUnit.MILLISECONDS)
        }
    }

    fun handleException(callback: PyObject): PyCompletableFuture<*> {
        val biFunction = callback.asBiFunction()
        return PyCompletableFuture(future.handle(biFunction), executor)
    }

    fun isDone(): Boolean {
        return future.isDone
    }

    fun then(callback: PyObject): PyCompletableFuture<*> {
        val func = callback.asFunction()
        return PyCompletableFuture(future.thenApply(func), executor)
    }

    fun whenComplete(callback: PyObject) {
        val biConsumer = callback.asBiConsumer<Throwable>()
        future.whenComplete(biConsumer)
    }

    private fun <U : Any> PyObject.asBiConsumer() =
        BiConsumer<T, U> { result, error ->
            val pyResult = Py.java2py(result)
            val pyError = Py.java2py(error)
            executor.run(this@asBiConsumer, pyResult, pyError)
        }

    private fun PyObject.asBiFunction() =
        BiFunction<T, Throwable, Any?> { result, error ->
            val pyResult = Py.java2py(result)
            val pyError = Py.java2py(error)
            executor.run(this@asBiFunction, pyResult, pyError)
        }

    private fun PyObject.asFunction() =
        java.util.function.Function<T, Any?> { result ->
            val pyResult = Py.java2py(result)
            executor.run(this@asFunction, pyResult)?.__tojava__(Any::class.java)
        }
}

fun <T> CompletableFuture<T>.asPyCompletableFuture(executor: PyScriptExecutor) =
    PyCompletableFuture(this@asPyCompletableFuture, executor)
