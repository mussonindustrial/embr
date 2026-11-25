package com.mussonindustrial.ignition.embr.periscope.utils

import com.inductiveautomation.ignition.common.resourcecollection.Resource
import com.mussonindustrial.embr.common.crypto.sha256Delimited
import com.mussonindustrial.embr.common.crypto.toHex
import java.util.Arrays

fun Resource.getHashKey(): String {
    return resourceSignature.signature.bytes.toHex().take(20)
}

fun Collection<Resource>.getClientResourceHash(runtimeHash: String): String {
    val signatures = map {
        it.resourceSignature.signature.bytes
    }
        .sortedWith(Arrays::compareUnsigned)

    return (listOf(runtimeHash.toByteArray()) + signatures)
        .sha256Delimited()
        .take(8)
        .toByteArray()
        .toHex()
}
