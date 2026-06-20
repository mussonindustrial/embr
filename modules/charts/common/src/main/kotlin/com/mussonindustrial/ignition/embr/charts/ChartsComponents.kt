package com.mussonindustrial.ignition.embr.charts

import com.inductiveautomation.perspective.common.api.BrowserResource
import java.util.UUID

object ChartsComponents {

    private val instanceHash = UUID.randomUUID().toString().replace("-", "").substring(0, 16)

    val BROWSER_RESOURCES: Set<BrowserResource> =
        mutableSetOf(
            BrowserResource(
                "embr-charts-client",
                "/data/embr-charts/resources/embr-charts-client.js",
                BrowserResource.ResourceType.JS,
                instanceHash,
            )
        )
}
