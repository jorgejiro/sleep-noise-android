package com.jjrapps.sleepnoise.domain.model

/**
 * The sounds the app makes.
 *
 * Two of them are named after colours because that is what people search for; the
 * other two exist because of what they *do*. [Pink] is the all-rounder, and
 * [Masking] is shaped to cover conversation — see the specification §2.1.
 *
 * [key] is what gets persisted, so it must stay stable even if this enum is renamed
 * or reordered: a stored `"brown"` has to keep meaning brown noise for ever.
 */
enum class NoiseType(val key: String) {
    White("white"),
    Pink("pink"),
    Brown("brown"),
    Masking("masking");

    /**
     * The next sound in the order the app shows them, wrapping round at the end.
     *
     * Wrapping is not a detail: these two feed the arrows in the notification, and
     * an order with ends would leave the first and last sound with a dead arrow
     * beside them — which is the exact thing those arrows were given a job to stop
     * being. See ADR 007.
     */
    fun next(): NoiseType = entries[(ordinal + 1) % entries.size]

    /** The previous sound, wrapping round at the beginning. See [next]. */
    fun previous(): NoiseType = entries[(ordinal + entries.size - 1) % entries.size]

    companion object {
        /**
         * What a fresh install starts with (RF-01).
         *
         * [Masking] and not a colour: the app's most demanding use is covering noise
         * you did not choose — a room with people talking — and brown noise is close
         * to useless for that, with 1,4 % of its energy in the band that carries
         * speech. Someone who wants brown for sleeping finds it in one tap; someone
         * who installed the app to blot out an office would have judged it by a sound
         * that cannot do the job.
         */
        val Default = Masking

        fun fromKey(key: String?): NoiseType =
            entries.firstOrNull { it.key == key } ?: Default
    }
}
