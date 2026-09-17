package com.mussonindustrial.ignition.embr.periscope.pystore

class PyStorePath private constructor(val segments: List<String>) {
    val isRoot: Boolean
        get() = segments.isEmpty()

    fun child(segment: String): PyStorePath = create(segments + validateSegment(segment))

    fun resolve(relative: PyStorePath): PyStorePath =
        when {
            relative.isRoot -> this
            isRoot -> relative
            else -> create(segments + relative.segments)
        }

    fun isAncestorOf(other: PyStorePath): Boolean =
        segments.size <= other.segments.size &&
            segments.indices.all {
                segments[it] == other.segments[it]
            }

    fun isRelatedTo(other: PyStorePath): Boolean = isAncestorOf(other) || other.isAncestorOf(this)

    override fun equals(other: Any?): Boolean = other is PyStorePath && segments == other.segments

    override fun hashCode(): Int = segments.hashCode()

    override fun toString(): String =
        segments.joinToString(".") {
            escape(it)
        }

    companion object {
        @JvmField val ROOT = PyStorePath(emptyList())

        @JvmStatic
        fun of(vararg segments: String): PyStorePath = create(segments.map(::validateSegment))

        @JvmStatic
        fun parse(path: String?): PyStorePath {
            if (path.isNullOrEmpty()) {
                return ROOT
            }

            val segments = mutableListOf<String>()
            val current = StringBuilder()
            var escaping = false

            fun finishSegment() {
                require(current.isNotEmpty()) {
                    "Invalid PyStore path '$path': empty segment"
                }

                segments += current.toString()
                current.setLength(0)
            }

            for (char in path) {
                when {
                    escaping -> {
                        require(char == '.' || char == '\\') {
                            "Invalid PyStore path '$path': invalid escape '\\$char'"
                        }

                        current.append(char)
                        escaping = false
                    }

                    char == '\\' -> escaping = true

                    char == '.' -> finishSegment()

                    else -> current.append(char)
                }
            }

            require(!escaping) {
                "Invalid PyStore path '$path': trailing escape"
            }

            finishSegment()

            return create(segments)
        }

        private fun create(segments: List<String>): PyStorePath =
            if (segments.isEmpty()) {
                ROOT
            } else {
                PyStorePath(segments)
            }

        private fun validateSegment(segment: String): String {
            require(segment.isNotEmpty()) {
                "Path segment cannot be empty"
            }

            return segment
        }

        private fun escape(segment: String): String =
            segment.replace("\\", "\\\\").replace(".", "\\.")
    }
}
