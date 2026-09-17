package com.mussonindustrial.ignition.embr.periscope.utils

import org.python.core.Py
import org.python.core.PyObject

internal fun PyObject.toJavaValue(): Any? {
    if (this === Py.None) {
        return null
    }

    val converted = __tojava__(Any::class.java)

    return if (converted === Py.NoConversion) {
        this
    } else {
        converted
    }
}

internal fun Any?.toPyValue(): PyObject =
    when (this) {
        null -> Py.None
        is PyObject -> this
        else -> Py.java2py(this)
    }
