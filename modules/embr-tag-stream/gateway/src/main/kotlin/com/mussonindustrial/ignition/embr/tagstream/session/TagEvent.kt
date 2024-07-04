package com.mussonindustrial.ignition.embr.tagstream.session

enum class TagEvent(val eventType: String) {
    TagChange("tag_change"),
    AlarmEvent("alarm_event");

    companion object {
        fun fromValue(value: String): TagEvent = when (value) {
            "tag_change" -> TagChange
            "alarm_event" -> AlarmEvent
            else -> throw IllegalArgumentException()
        }
    }
}