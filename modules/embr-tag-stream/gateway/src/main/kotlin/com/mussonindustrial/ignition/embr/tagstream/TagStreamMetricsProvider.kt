package com.mussonindustrial.ignition.embr.tagstream

import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType
import com.inductiveautomation.ignition.gateway.tags.model.GatewayTagManager

class TagStreamMetricsProvider(private val tagManager: GatewayTagManager)  {
    companion object {
        private const val PATH_UNCONNECTED_SESSION_COUNT = "Gateway/TagStream/Sessions/Unconnected"
        private const val PATH_SESSION_COUNT = "Gateway/TagStream/Sessions/Connected"
    }
    init {
        clearNamespace()
        createAndInitialize(PATH_UNCONNECTED_SESSION_COUNT, DataType.Int8, 0)
        createAndInitialize(PATH_SESSION_COUNT, DataType.Int8, 0)
    }

    private fun clearNamespace() {
        tagManager.systemTags.removeTag("Gateway/TagStream")
    }
    private fun createAndInitialize(path: String, type: DataType, value: Any) {
        tagManager.systemTags.configureTag(path, type)
        tagManager.systemTags.updateValue(path, value, QualityCode.Good)
    }

    private fun setValue(path: String, value: Any) {
        tagManager.systemTags.updateValue(path, value, QualityCode.Good)
    }

    fun setUnconnectedSessionCount(sessionCount: Int) = setValue(PATH_UNCONNECTED_SESSION_COUNT, sessionCount)
    fun setConnectedSessionCount(sessionCount: Int) = setValue(PATH_SESSION_COUNT, sessionCount)

}