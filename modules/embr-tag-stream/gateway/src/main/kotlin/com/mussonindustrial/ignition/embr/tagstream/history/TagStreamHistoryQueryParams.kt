package com.mussonindustrial.ignition.embr.tagstream.history

import com.inductiveautomation.ignition.common.sqltags.history.Aggregate
import com.inductiveautomation.ignition.common.sqltags.history.ReturnFormat
import com.inductiveautomation.ignition.common.sqltags.history.TagHistoryQueryParams
import com.inductiveautomation.ignition.common.tags.model.TagPath
import com.inductiveautomation.ignition.common.util.Flags
import com.mussonindustrial.ignition.embr.tagstream.TagStreamManager
import com.mussonindustrial.ignition.embr.tagstream.api.TagHistoryRequest
import java.util.*

class TagStreamHistoryQueryParams(
    private val session: TagStreamManager.Session,
    private val request: TagHistoryRequest
): TagHistoryQueryParams {
    override fun getPaths(): List<TagPath> {
        return session.tags?.map { it.tagPath } ?: listOf()
    }

    override fun getAliases(): List<String> {
        return session.tags?.map { it.id.toString() } ?: listOf()
    }

    override fun getStartDate(): Date {
        return request.startDate
    }

    override fun getEndDate(): Date {
        return request.endDate
    }

    override fun getReturnSize(): Int {
        return request.returnSize
    }

    override fun getAggregationMode(): Aggregate {
        return request.aggregationMode
    }

    override fun getColumnAggregationModes(): List<Aggregate> {
        return listOf()
    }

    override fun getReturnFormat(): ReturnFormat {
        return ReturnFormat.Wide
    }

    override fun getQueryFlags(): Flags {
        return Flags()
    }
}