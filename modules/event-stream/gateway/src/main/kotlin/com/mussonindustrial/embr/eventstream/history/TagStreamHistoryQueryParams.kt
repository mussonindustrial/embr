package com.mussonindustrial.embr.eventstream.history

import com.inductiveautomation.ignition.common.sqltags.history.Aggregate
import com.inductiveautomation.ignition.common.sqltags.history.ReturnFormat
import com.inductiveautomation.ignition.common.sqltags.history.TagHistoryQueryParams
import com.inductiveautomation.ignition.common.tags.model.TagPath
import com.inductiveautomation.ignition.common.util.Flags
import com.mussonindustrial.embr.eventstream.api.TagHistoryRequest
import com.mussonindustrial.embr.eventstream.streams.TagStream
import java.util.Date

class TagStreamHistoryQueryParams(
    private val tagEmitter: TagStream,
    private val request: TagHistoryRequest,
) : TagHistoryQueryParams {
    override fun getPaths(): List<TagPath> {
        return tagEmitter.tagListeners.map { it.tagPath }
    }

    override fun getAliases(): List<String> {
        return tagEmitter.tagListeners.map { it.id.toString() }
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
        return ReturnFormat.Tall
    }

    override fun getQueryFlags(): Flags {
        return Flags()
    }
}
