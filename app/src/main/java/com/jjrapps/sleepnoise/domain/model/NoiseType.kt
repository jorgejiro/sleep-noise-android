package com.jjrapps.sleepnoise.domain.model

/**
 * The two sounds the app ships. Two, on purpose: see the specification §2.
 *
 * [key] is what gets persisted, so it must stay stable even if the enum is
 * renamed or reordered — a stored `"brown"` has to keep meaning brown noise
 * across every future version.
 */
enum class NoiseType(val key: String) {
    White("white"),
    Brown("brown");

    companion object {
        /** Brown noise after a fresh install (RF-01). */
        val Default = Brown

        fun fromKey(key: String?): NoiseType =
            entries.firstOrNull { it.key == key } ?: Default
    }
}
