package com.mussonindustrial.embr.perspective.gateway.reflect

import com.inductiveautomation.perspective.gateway.model.ComponentModel
import com.inductiveautomation.perspective.gateway.script.ComponentModelScriptWrapper
import com.mussonindustrial.embr.common.reflect.getPrivateProperty
import org.python.core.PyFrame

fun ComponentModelScriptWrapper.getComponentModel(): ComponentModel {
    return this.getPrivateProperty("componentModel") as ComponentModel
}

fun PyFrame.getComponentModelScriptWrapper(name: String = "self"): ComponentModelScriptWrapper? {
    val key = name.intern()
    var frame: PyFrame? = this

    while (frame != null) {
        (frame.locals.__finditem__(key) as? ComponentModelScriptWrapper)?.let {
            return it
        }

        frame = frame.f_back
    }

    return null
}
