package com.mussonindustrial.embr.snmp.agents.scripting

import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.mussonindustrial.embr.snmp.model.QualifiedOidValue
import org.python.core.PyObject

interface SnmpAgentScriptModule {

    companion object {
        const val PATH = "system.snmp.agent"
    }

    fun read(args: Array<PyObject>, keywords: Array<String>): List<QualifiedOidValue>

    fun write(args: Array<PyObject>, keywords: Array<String>): List<QualityCode>

    fun walk(args: Array<PyObject>, keywords: Array<String>): List<QualifiedOidValue>

    fun readTable(args: Array<PyObject>, keywords: Array<String>): Dataset
}
