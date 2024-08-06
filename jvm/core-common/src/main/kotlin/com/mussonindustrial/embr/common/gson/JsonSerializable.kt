package com.mussonindustrial.embr.common.gson

import com.inductiveautomation.ignition.common.gson.JsonDeserializer
import com.inductiveautomation.ignition.common.gson.JsonSerializer

interface JsonSerializable<T> : JsonSerializer<T>, JsonDeserializer<T>
