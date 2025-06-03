package com.mussonindustrial.ignition.embr.periscope.utils

import com.inductiveautomation.ignition.common.project.Project
import com.inductiveautomation.ignition.common.project.resource.ProjectResource
import com.mussonindustrial.ignition.embr.periscope.resources.ClientResource
import java.security.MessageDigest
import kotlin.sequences.forEach
import org.apache.commons.codec.binary.Hex

fun ProjectResource.getHashKey(): String {
    return Hex.encodeHexString(resourceSignature.signature).substring(0, 20)
}

fun Project.getClientResourceHash(): String {
    val resources = this.getResourcesOfType(ClientResource.type)
    val digest = MessageDigest.getInstance("SHA-256")

    resources
        .asSequence()
        .map { it.resourceSignature.signature }
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
