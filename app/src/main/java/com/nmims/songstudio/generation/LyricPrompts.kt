package com.nmims.songstudio.generation

import com.nmims.songstudio.data.SongPreferences
import com.nmims.songstudio.data.Topic

/** Provider-independent contract. Suraj can pass this prompt through a backend API. */
object LyricPrompts {
    fun educational(topic: Topic, preferences: SongPreferences, languageName: String): String = """
        Write an original educational song for a college student.
        Topic: ${topic.title}
        Source explanation: ${topic.concept}
        Formula/conclusion: ${topic.formula}
        Language: $languageName
        Music: ${preferences.style.description}; ${preferences.style.instruments}
        Tempo preference: ${preferences.tempo}
        Student learning focus: ${preferences.focus}
        Structure: short verse, repeating concept chorus, second verse, chorus.
        Preserve mathematical meaning and state every required theorem condition.
        Explain symbols in singable words; show exact notation separately.
        Do not invent facts or copy a reference song's lyrics or melody.
        Flag missing source information for human review.
    """.trimIndent()

    fun music(preferences: SongPreferences, languageName: String): String =
        "${preferences.style.description}, ${preferences.reference.mood}, " +
            "${preferences.style.instruments}, $languageName vocals, ${preferences.tempo}, " +
            "clear pronunciation, original melody, educational song, memorable repeated chorus"

    fun draft(topic: Topic, language: String): String {
        val source = topic.lyrics[language].orEmpty()
        // Keep the original data intact; correct the missing theorem assumptions in this draft.
        val conditions = if (topic.title == "Rolle's Theorem" && language == "en")
            "Continuous on the closed interval, differentiable inside,\nEqual endpoint values — these conditions are our guide.\n" else ""
        return "[Verse]\n$conditions$source\n\n[Chorus]\n" + source.lines().takeLast(2).joinToString("\n")
    }
}
