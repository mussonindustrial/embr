package com.mussonindustrial.ignition.embr.periscope.utils

import com.inductiveautomation.ignition.common.model.ApplicationScope
import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.inductiveautomation.ignition.common.resourcecollection.ResourceCollection
import com.inductiveautomation.ignition.common.resourcecollection.ResourceFilter
import com.inductiveautomation.ignition.common.resourcecollection.RuntimeResourceCollection
import com.inductiveautomation.ignition.gateway.project.ProjectManager
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import java.security.MessageDigest
import kotlin.jvm.optionals.getOrNull
import kotlin.sequences.forEach
import org.apache.commons.codec.binary.Hex

fun ProjectManager.findClientResources(projectName: String): RuntimeResourceCollection? {
    val filter = ResourceFilter(ApplicationScope.ALL, listOf(ClientResource.type))
    return find(projectName, filter).getOrNull()
}

fun Resource.getHashKey(): String {
    return Hex.encodeHexString(resourceSignature.signature.bytes).substring(0, 20)
}

fun ResourceCollection.getClientResourceHash(runtimeHash: String): String {
    val resources = this.getResourcesOfType(ClientResource.type)
    val digest = MessageDigest.getInstance("SHA-256")
    digest.update(runtimeHash.toByteArray())

    resources
        .asSequence()
        .map { it.resourceSignature.signature.bytes }
        .sortedWith { a, b ->
            val len = minOf(a.size, b.size)
            for (i in 0 until len) {
                val cmp = (a[i].toInt() and 0xFF) - (b[i].toInt() and 0xFF)
                if (cmp != 0) return@sortedWith cmp
            }
            a.size - b.size
        }
        .forEach { sig ->
            digest.update(sig)
            digest.update(0)
        }
    return digest.digest().take(8).joinToString("") { "%02x".format(it) }
}
