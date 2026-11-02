package com.example.designex.economics.data

import com.example.designex.economics.audio.BeatStyle

/** How each song style is played back: beat, tempo and the shape of the sung melody. */
object GenreStyles {
    private enum class Family(val style: BeatStyle, val steps: IntArray) {
        RAP(BeatStyle.LOFI, intArrayOf(0, 0, 1, 0, -1, 0, 1, -1)),
        POP(BeatStyle.POP, intArrayOf(1, 1, -1, 0, 2, -1, -1, 0)),
        BALLAD(BeatStyle.LOFI, intArrayOf(1, 2, -1, -1, 1, 0, -2, 1)),
        DANCE(BeatStyle.UPBEAT, intArrayOf(2, -1, 1, 1, -2, 1, -1, 0)),
        DEVOTIONAL(BeatStyle.RAGA, intArrayOf(0, 1, 0, -1, 1, 1, -1, -1)),
        BOLLYWOOD(BeatStyle.BOLLY, intArrayOf(1, 1, -2, 1, 2, -1, -2, 0)),
    }

    private val families = mapOf(
        "hiphop" to Family.RAP, "desirap" to Family.RAP,
        "pop" to Family.POP,
        "bollypop" to Family.BOLLYWOOD, "party" to Family.BOLLYWOOD,
        "dance" to Family.DANCE, "rock" to Family.DANCE,
        "rnb" to Family.BALLAD, "acoustic" to Family.BALLAD, "romantic" to Family.BALLAD,
        "sufi" to Family.DEVOTIONAL, "bhajan" to Family.DEVOTIONAL,
    )

    private val bpm = mapOf(
        "hiphop" to 92, "desirap" to 95, "pop" to 105, "bollypop" to 105,
        "dance" to 124, "party" to 122, "rock" to 120,
        "rnb" to 85, "acoustic" to 90, "romantic" to 80,
        "sufi" to 85, "bhajan" to 78,
    )

    private fun family(genreId: String) = families[genreId] ?: Family.POP

    fun beatStyle(genreId: String): BeatStyle = family(genreId).style

    fun melodySteps(genreId: String): IntArray = family(genreId).steps

    fun isDevotional(genreId: String) = family(genreId) == Family.DEVOTIONAL

    fun isRap(genreId: String) = family(genreId) == Family.RAP

    fun defaultBpm(genreId: String): Int = bpm[genreId] ?: 100

    /** Maps the research song list's genres onto the catalog styles of a language. */
    fun catalogIdFor(libraryGenre: String?, lang: Lang): String? = when (libraryGenre?.lowercase()) {
        null -> null
        "hip hop" -> when (lang) { Lang.EN -> "hiphop"; Lang.HI -> "desirap" }
        "romantic" -> when (lang) { Lang.EN -> "rnb"; Lang.HI -> "romantic" }
        "chill" -> when (lang) { Lang.EN -> "acoustic"; Lang.HI -> "sufi" }
        else -> when (lang) { Lang.EN -> "pop"; Lang.HI -> "bollypop" }
    }
}
