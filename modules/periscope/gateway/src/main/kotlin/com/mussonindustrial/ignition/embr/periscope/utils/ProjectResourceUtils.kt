package com.mussonindustrial.ignition.embr.periscope.utils

import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.project.Project
import com.inductiveautomation.ignition.common.project.RuntimeProject
import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.inductiveautomation.ignition.gateway.project.ProjectManager
import com.inductiveautomation.ignition.gateway.project.ResourceFilter
import com.mussonindustrial.embr.common.crypto.sha256Delimited
import com.mussonindustrial.embr.common.crypto.toHex
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import java.util.Arrays
import kotlin.jvm.optionals.getOrNull

fun ProjectManager.findClientResources(projectName: String): RuntimeProject? {
    val filter = ResourceFilter(ApplicationScope.ALL, listOf(ClientResource.type))
    return getProject(projectName, filter).getOrNull()
}

fun ProjectResource.getHashKey(): String {
    return resourceSignature.signature.toHex().take(20)
}

fun Project.getClientResourceHash(runtimeHash: String): String {
    val signatures =
        getResourcesOfType(ClientResource.type)
            .map { it.resourceSignature.signature }
            .sortedWith(Arrays::compareUnsigned)

    return (listOf(runtimeHash.toByteArray()) + signatures)
        .sha256Delimited()
        .take(8)
        .toByteArray()
        .toHex()
}
