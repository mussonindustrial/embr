package com.mussonindustrial.embr.perspective.gateway.reflect

import com.inductiveautomation.perspective.gateway.model.ComponentModel
import com.inductiveautomation.perspective.gateway.script.ComponentModelScriptWrapper
import com.mussonindustrial.embr.common.reflect.getPrivateProperty
import org.python.core.PyFrame
import org.python.core.PyString

fun ComponentModelScriptWrapper.getComponentModel(): ComponentModel {
    return this.getPrivateProperty("componentModel") as ComponentModel
}

fun PyFrame.getComponentModelScriptWrapper(name: String = "self"): ComponentModelScriptWrapper? {
    return this.locals.__getitem__(PyString(name)) as? ComponentModelScriptWrapper ?: return null
}
