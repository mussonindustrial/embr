package com.mussonindustrial.ignition.embr.periscope.component

import java.util.prefs.Preferences

class PaletteHistory(project: String) {

    private val pinnedKey = "pinned.${project.take(MAX_PROJECT_LENGTH)}"
    private val recentKey = "recent.${project.take(MAX_PROJECT_LENGTH)}"

    val pinned: List<String>
        get() = read(pinnedKey)

    val recent: List<String>
        get() = read(recentKey)

    fun isPinned(id: String) = id in pinned

    fun togglePin(id: String) {
        val ids = pinned
        write(pinnedKey, if (id in ids) ids - id else ids + id)
    }

    fun recordUse(id: String) {
        write(recentKey, (listOf(id) + (recent - id)).take(MAX_RECENT))
    }

    fun clearRecent() {
        write(recentKey, emptyList())
    }

    private fun read(key: String): List<String> {
        return PREFERENCES.get(key, "").split('\n').filter { it.isNotEmpty() }
    }

    private fun write(key: String, ids: List<String>) {
        PREFERENCES.put(key, ids.joinToString("\n"))
    }

    companion object {
        private const val MAX_RECENT = 8
        private const val MAX_PROJECT_LENGTH = 64
        private val PREFERENCES =
            Preferences.userNodeForPackage(PaletteHistory::class.java).node("palette")
    }
}
