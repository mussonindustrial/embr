package com.mussonindustrial.embr.common.cache

import java.util.Collections

class LruCache<K, V>(private val capacity: Int) {

    private val entries =
        Collections.synchronizedMap(
            object : LinkedHashMap<K, V>(capacity, 0.75f, true) {
                override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>): Boolean {
                    return size > capacity
                }
            },
        )

    operator fun get(key: K): V? {
        return entries[key]
    }

    operator fun set(key: K, value: V) {
        entries[key] = value
    }

    fun getOrPut(key: K, compute: () -> V): V {
        return entries.computeIfAbsent(key) { compute() }
    }
}
