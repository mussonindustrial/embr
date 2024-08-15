package com.mussonindustrial.embr.common.reflect

import java.lang.reflect.Method

fun <T : Any> T.getPrivateProperty(variableName: String): Any? {
    return javaClass.getDeclaredField(variableName).let { field ->
        field.isAccessible = true
        return@let field.get(this)
    }
}

fun <T : Any> T.getPrivateMethod(methodName: String, vararg params: Class<*> = arrayOf()): Method {
    return javaClass.getDeclaredMethod(methodName, *params).let { method ->
        method.trySetAccessible()
        return@let method
    }
}
