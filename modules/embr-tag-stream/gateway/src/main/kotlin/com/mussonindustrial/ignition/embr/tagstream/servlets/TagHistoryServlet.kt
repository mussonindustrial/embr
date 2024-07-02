package com.mussonindustrial.ignition.embr.tagstream.servlets

import com.inductiveautomation.ignition.common.StreamingDataset
import com.inductiveautomation.ignition.common.TypeUtilities
import com.inductiveautomation.ignition.common.gson.*
import com.inductiveautomation.ignition.common.sqltags.history.TagHistoryQueryParams
import com.inductiveautomation.ignition.common.sqltags.history.cache.TagHistoryCache
import com.inductiveautomation.ignition.common.util.fromJson
import com.mussonindustrial.ignition.embr.common.logging.getLogger
import com.mussonindustrial.ignition.embr.gateway.api.sendError
import com.mussonindustrial.ignition.embr.gateway.api.sendSuccess
import com.mussonindustrial.ignition.embr.tagstream.TagStreamGatewayContext
import com.mussonindustrial.ignition.embr.tagstream.api.TagHistoryRequest
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class TagHistoryServlet: HttpServlet() {

    private val logger = this.getLogger()
    private val context = TagStreamGatewayContext.INSTANCE
    private val tagHistoryManager = context.tagHistoryManager
    private val cache = TagHistoryCache()

    override fun doGet(request: HttpServletRequest, response: HttpServletResponse) {
        logger.trace("Get request received: {}", request)
        val path = request.requestURI.substring(request.contextPath.length + request.servletPath.length)

        if (path != "") {
            response.sendError("bad path")
            return
        }

        val tagHistoryRequest: TagHistoryRequest
        try {
            val json = JsonParser.parseReader(request.reader).asJsonObject
            logger.trace("Request body: {}", json)
            tagHistoryRequest = TagHistoryRequest.gsonAdapter.fromJson(json)

        } catch (e: Throwable) {
            logger.warn("Rejecting history request, malformed body.", e)
            response.sendError("malformed body")
            return
        }

        val doQuery = { params: TagHistoryQueryParams ->
            val dataset = StreamingDataset()
            tagHistoryManager.queryHistory(params, dataset)
            dataset
        }

        val results = cache.query(doQuery, tagHistoryRequest)
        logger.trace("Tag history results {} retrieved.", results)

        response.sendSuccess(TypeUtilities.datasetToGson(results))
    }
}