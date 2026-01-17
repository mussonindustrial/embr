package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.mussonindustrial.embr.common.scripting.PyCompletableFuture
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue
import org.python.core.PyObject

interface SnmpAgentScriptModule {

    companion object {
        const val PATH = "system.snmp.agent"
    }

    fun readAsync(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): PyCompletableFuture<List<QualifiedOidValue>>

    fun readBlocking(args: Array<PyObject>, keywords: Array<String>): List<QualifiedOidValue>

    fun writeAsync(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): PyCompletableFuture<List<QualityCode>>

    fun writeBlocking(args: Array<PyObject>, keywords: Array<String>): List<QualityCode>

    fun walkAsync(
        args: Array<PyObject>,
        keywords: Array<String>,
    ): PyCompletableFuture<List<QualifiedOidValue>>

    fun walkBlocking(args: Array<PyObject>, keywords: Array<String>): List<QualifiedOidValue>

    fun readTableAsync(args: Array<PyObject>, keywords: Array<String>): PyCompletableFuture<Dataset>

    fun readTableBlocking(args: Array<PyObject>, keywords: Array<String>): Dataset
}
