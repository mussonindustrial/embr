package com.mussonindustrial.embr.sse.tags

import com.inductiveautomation.ignition.common.model.values.QualityCode
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType
import com.inductiveautomation.ignition.gateway.tags.model.GatewayTagManager

class SystemTagsProvider(private val tagManager: GatewayTagManager) {
    companion object {
        private const val NAMESPACE = "Gateway/EventStream"
        private const val PATH_SESSION_COUNT_UNCONNECTED = "$NAMESPACE/Sessions/Unconnected"
        private const val PATH_SESSION_COUNT_CONNECTED = "$NAMESPACE/Sessions/Connected"
    }

    init {
        clearNamespace()
        createAndInitialize(PATH_SESSION_COUNT_UNCONNECTED, DataType.Int8, 0)
        createAndInitialize(PATH_SESSION_COUNT_CONNECTED, DataType.Int8, 0)
    }

    private fun clearNamespace() {
        tagManager.systemTags.removeTag(NAMESPACE)
    }

    private fun createAndInitialize(path: String, type: DataType, value: Any) {
        tagManager.systemTags.configureTag(path, type)
        tagManager.systemTags.updateValue(path, value, QualityCode.Good)
    }

    private fun setValue(path: String, value: Any) {
        tagManager.systemTags.updateValue(path, value, QualityCode.Good)
    }

    var sessionCountConnected: Int = 0
        set(value) {
            setValue(PATH_SESSION_COUNT_CONNECTED, value)
            field = value
        }

    var sessionCountUnconnected: Int = 0
        set(value) {
            setValue(PATH_SESSION_COUNT_UNCONNECTED, value)
            field = value
        }
}
