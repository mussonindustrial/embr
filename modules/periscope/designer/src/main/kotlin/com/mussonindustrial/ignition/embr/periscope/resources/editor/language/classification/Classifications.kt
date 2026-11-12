package com.mussonindustrial.ignition.embr.periscope.resources.editor.language.classification

class Classifications(private val tokens: IntArray, private val tokenTypes: List<Int>) {

    private val count: Int
        get() = tokens.size / 3

    fun forEachIn(start: Int, end: Int, action: (start: Int, end: Int, tokenType: Int) -> Unit) {
        var i = firstEndingAfter(start)
        while (i < count) {
            val tokenStart = tokens[i * 3]
            if (tokenStart >= end) return
            val tokenEnd = tokenStart + tokens[i * 3 + 1]
            if (tokenEnd > tokenStart) {
                action(
                    maxOf(tokenStart, start),
                    minOf(tokenEnd, end),
                    tokenTypes[tokens[i * 3 + 2]],
                )
            }
            i++
        }
    }

    fun inserted(offset: Int, length: Int) {
        for (i in 0 until count) {
            val start = tokens[i * 3]
            when {
                start >= offset -> tokens[i * 3] = start + length
                start + tokens[i * 3 + 1] > offset -> tokens[i * 3 + 1] += length
            }
        }
    }

    fun removed(offset: Int, length: Int) {
        val removedEnd = offset + length
        for (i in 0 until count) {
            val start = tokens[i * 3]
            val end = start + tokens[i * 3 + 1]
            tokens[i * 3] = shiftedForRemoval(start, offset, removedEnd)
            tokens[i * 3 + 1] = shiftedForRemoval(end, offset, removedEnd) - tokens[i * 3]
        }
    }

    private fun shiftedForRemoval(position: Int, offset: Int, removedEnd: Int): Int {
        return when {
            position >= removedEnd -> position - (removedEnd - offset)
            position > offset -> offset
            else -> position
        }
    }

    private fun firstEndingAfter(offset: Int): Int {
        var low = 0
        var high = count
        while (low < high) {
            val mid = (low + high) / 2
            if (tokens[mid * 3] + tokens[mid * 3 + 1] <= offset) low = mid + 1 else high = mid
        }
        return low
    }
}
