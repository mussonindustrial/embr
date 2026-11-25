package com.mussonindustrial.ignition.embr.periscope.utils

import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.mussonindustrial.embr.common.crypto.sha256Delimited
import com.mussonindustrial.embr.common.crypto.toHex
import java.util.Arrays

fun ProjectResource.getHashKey(): String {
    return resourceSignature.signature.toHex().take(20)
}

fun Collection<ProjectResource>.getClientResourceHash(runtimeHash: String): String {
    val signatures = map { it.resourceSignature.signature }.sortedWith(Arrays::compareUnsigned)

    return (listOf(runtimeHash.toByteArray()) + signatures)
        .sha256Delimited()
        .take(8)
        .toByteArray()
        .toHex()
}
