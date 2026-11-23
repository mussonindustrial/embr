package com.mussonindustrial.ignition.embr.periscope.utils

import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceCollection
import com.inductiveautomation.ignition.common.resourcecollection.ResourceFilter
import com.inductiveautomation.ignition.common.resourcecollection.RuntimeResourceCollection
import com.inductiveautomation.ignition.gateway.project.ProjectManager
import com.mussonindustrial.embr.common.crypto.sha256Delimited
import com.mussonindustrial.embr.common.crypto.toHex
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import java.util.Arrays
import kotlin.jvm.optionals.getOrNull

fun ProjectManager.findClientResources(projectName: String): RuntimeResourceCollection? {
    val filter = ResourceFilter(ApplicationScope.ALL, listOf(ClientResource.type))
    return find(projectName, filter).getOrNull()
}

fun Resource.getHashKey(): String {
    return resourceSignature.signature.bytes.toHex().take(20)
}

fun ResourceCollection.getClientResourceHash(runtimeHash: String): String {
    val signatures =
        getResourcesOfType(ClientResource.type)
            .map { it.resourceSignature.signature.bytes }
            .sortedWith(Arrays::compareUnsigned)

    return (listOf(runtimeHash.toByteArray()) + signatures)
        .sha256Delimited()
        .take(8)
        .toByteArray()
        .toHex()
}
