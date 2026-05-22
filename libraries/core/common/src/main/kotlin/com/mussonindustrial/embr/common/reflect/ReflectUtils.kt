package com.mussonindustrial.embr.common.reflect

import java.lang.reflect.Field
import java.lang.reflect.Method

fun <T : Any> T.getPrivateProperty(variableName: String): Any? {
    return javaClass.getDeclaredField(variableName).let { field ->
        field.isAccessible = true
        return@let field.get(this)
    }
}

fun Any.getPrivateMethod(methodName: String, vararg params: Class<*>): Method {
    val clazz =
        when (this) {
            is Class<*> -> this
            else -> this.javaClass
        }
    return clazz.getDeclaredMethod(methodName, *params).apply { trySetAccessible() }
}

fun <T : Any> T.setPrivateProperty(variableName: String, value: Any?) {
    javaClass.getDeclaredField(variableName).let { field ->
        field.trySetAccessible()
        field.set(this, value)
    }
}

fun <T : Any> T.getSuperPrivateProperty(variableName: String): Any {
    var c = javaClass as Class<*>?
    var field: Field? = null
    while (c != null && field == null) {
        try {
            field = c.getDeclaredField(variableName)
        } catch (e: NoSuchFieldException) {
            c = c.superclass
        }
    }

    if (field == null) {
        throw NoSuchFieldException()
    }

    return field.apply { isAccessible = true }.get(this)
}

fun <T : Any> T.getSuperPrivateMethod(
    methodName: String,
    vararg params: Class<*> = arrayOf(),
): Method {
    var c = javaClass as Class<*>?
    var method: Method? = null
    while (c != null && method == null) {
        try {
            method = c.getDeclaredMethod(methodName, *params)
        } catch (e: NoSuchMethodException) {
            c = c.superclass
        }
    }

    if (method == null) {
        throw NoSuchMethodException()
    }

    return method.apply { trySetAccessible() }
}
