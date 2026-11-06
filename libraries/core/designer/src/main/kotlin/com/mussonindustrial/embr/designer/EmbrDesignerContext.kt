package com.mussonindustrial.embr.designer

import com.inductiveautomation.ignition.common.licensing.LicenseMode
import com.inductiveautomation.ignition.common.modules.ModuleInfo
import com.inductiveautomation.ignition.designer.model.DesignerContext
import com.mussonindustrial.embr.common.EmbrCommonContextExtension

interface EmbrDesignerContext :
    DesignerContext, EmbrCommonContextExtension, EmbrDesignerContextExtension {

    val embrModules: List<ModuleInfo>
        get() = modules.filter { it.id.contains("com.mussonindustrial.embr") }.sortedBy { it.name }

    val unlicensedEmbrModules: List<ModuleInfo>
        get() = embrModules.filter { getLicenseState(it.id).licenseMode != LicenseMode.Activated }

    fun getModuleSafe(moduleId: String): Any? {
        return try {
            getModule(moduleId)
        } catch (_: Throwable) {
            null
        }
    }

    fun <T> ifModule(moduleId: String, action: () -> T): T? {
        if (getModuleSafe(moduleId) != null) {
            return action()
        }
        return null
    }
}
