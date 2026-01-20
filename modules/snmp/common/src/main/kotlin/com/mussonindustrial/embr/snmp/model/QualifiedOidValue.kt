package com.mussonindustrial.embr.snmp.model

import com.inductiveautomation.ignition.common.Dataset
import com.inductiveautomation.ignition.common.model.values.QualifiedValue
import com.inductiveautomation.ignition.common.util.DatasetBuilder

interface QualifiedOidValue : QualifiedValue {
    fun getOid(): Oid
}

fun List<List<QualifiedOidValue>>.toDataset(): Dataset {
    val builder = DatasetBuilder.newBuilder()

    val firstRow = firstOrNull()
    if (firstRow == null) {
        return builder.colNames("subindex").colTypes(Int::class.java).build()
    }

    val columnNames =
        listOf("subindex", *firstRow.map { it.getOid().parent.numeric }.toTypedArray())

    builder
        .colNames(columnNames)
        .colTypes(listOf(Int::class.java) + List(firstRow.size) { String::class.java })

    forEach { row ->
        builder.addRow(row.first().getOid().index, *row.map { it.value }.toTypedArray())
    }

    return builder.build()
}
