package com.mussonindustrial.ignition.embr.tagstream.emitters

import com.inductiveautomation.ignition.common.gson.JsonSerializer

data class RegisteredEventEmitter<T: EventEmitter>(val clazz: Class<T>, val gsonAdapter: JsonSerializer<T>)