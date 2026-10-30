package com.example.designex.economics

import com.example.designex.economics.audio.BeatComposer
import com.example.designex.economics.audio.BeatStyle
import com.example.designex.economics.data.CatalogSong
import com.example.designex.economics.data.Lang
import com.example.designex.economics.data.LyricsFitter
import com.example.designex.economics.data.Localized
import com.example.designex.economics.data.LocalizedList
import com.example.designex.economics.data.Topic
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EconomicsAudioAndLyricsTest {

    private fun song(genre: String, bpm: Int, lang: Lang = Lang.EN) =
        CatalogSong("Test Song", "Artist", 2020, bpm, "upbeat", false, genre, lang)

    private val datasetLyrics = """
        [Verse 1]
        Matter in the world is built from tiny atoms bright,
        Dalton saw the pieces making every thing in sight.
        Same element, same kind; indivisible in the frame,
        Different elements join, and compounds get their name.

        [Chorus]
        Atoms, atoms — Dalton showed the way,
        Element, compound, matter every day.
    """.trimIndent()

    private val topic = Topic(
        id = "chem-1",
        subjectId = "chemistry",
        unitNo = 1,
        name = Localized("Dalton's Atomic Theory", "डाल्टन का परमाणु सिद्धांत", "डाल्टनचा अणू सिद्धांत"),
        description = Localized(
            "Matter is made of atoms. Atoms of one element are identical.",
            "पदार्थ परमाणुओं से बना है। एक तत्व के परमाणु समान होते हैं।",
            "पदार्थ अणूंनी बनलेला आहे. एका मूलद्रव्याचे अणू सारखे असतात.",
        ),
        keywords = LocalizedList(listOf("atoms", "elements"), listOf("परमाणु", "तत्व"), listOf("अणू", "मूलद्रव्य")),
        formula = null,
        lyrics = mapOf(Lang.EN to datasetLyrics),
        songs = emptyMap(),
        reason = null,
    )

    @Test
    fun everyBeatStyleRendersAFourBarLoopAtTheSongsTempo() {
        BeatStyle.entries.forEach { style ->
            listOf(80, 124).forEach { bpm ->
                val pcm = BeatComposer.compose(style, seed = 7, bpm = bpm)
                assertEquals("$style $bpm", BeatComposer.barSamples(bpm) * 4, pcm.size)
                val peak = pcm.maxOf { kotlin.math.abs(it.toInt()) }
                assertTrue("$style is audible", peak > 20000)
            }
        }
    }

    @Test
    fun differentTopicsGetDifferentMelodies() {
        val a = BeatComposer.compose(BeatStyle.POP, "eco-1-1".hashCode())
        val b = BeatComposer.compose(BeatStyle.POP, "chem-6".hashCode())
        assertTrue(!a.contentEquals(b))
    }

    @Test
    fun popSongsGetVerseChorusStructureFromTheDatasetLyrics() {
        val fitted = LyricsFitter.fit(topic, Lang.EN, song("pop", 105))
        assertEquals(listOf("Verse 1", "Chorus"), fitted.sections.map { it.title })
        assertTrue(fitted.sections[1].isChorus)
        assertTrue(fitted.asText().startsWith("[Verse 1]\nMatter in the world"))
    }

    @Test
    fun devotionalSongsOpenAndCloseEachStanzaWithTheRefrain() {
        val fitted = LyricsFitter.fit(topic, Lang.EN, song("bhajan", 78))
        assertEquals(listOf("Refrain", "Verse 1", "Refrain"), fitted.sections.map { it.title })
    }

    @Test
    fun rapSongsUseVerseAndHookWithLongerLines() {
        val rap = LyricsFitter.fit(topic, Lang.EN, song("hiphop", 92))
        val ballad = LyricsFitter.fit(topic, Lang.EN, song("romantic", 78))
        assertEquals("Hook", rap.sections[1].title)
        assertTrue(rap.syllablesPerLine > ballad.syllablesPerLine)
    }

    @Test
    fun linesAreReflowedToSuitTheTempo() {
        val target = 9
        val long = "Different elements join together in fixed ratios, and compounds then get their very own name today"
        val out = LyricsFitter.reflow(listOf(long, "Yes", "it is"), target)
        assertTrue(out.size >= 2)
        out.forEach { assertTrue("'$it' too long", LyricsFitter.syllables(it) <= target * 1.5) }
    }

    @Test
    fun syllablesAreCountedForEnglishAndDevanagari() {
        assertEquals(4, LyricsFitter.syllables("economics"))
        assertEquals(3, LyricsFitter.syllables("नमस्ते"))
        assertEquals(2, LyricsFitter.syllables("cart, smart"))
    }

    @Test
    fun topicsWithoutDatasetLyricsGetTemplateLyricsWithAHook() {
        val hindi = LyricsFitter.fit(topic, Lang.HI, song("bollypop", 105, Lang.HI))
        val chorus = hindi.sections.first { it.isChorus }
        assertTrue(chorus.lines.first().contains("डाल्टन का परमाणु सिद्धांत"))
        assertTrue(hindi.lines.any { it.contains("परमाणु") })
        val other = LyricsFitter.fit(topic, Lang.HI, song("bollypop", 105, Lang.HI), variant = 1)
        assertTrue(other.sections.first { it.isChorus }.lines.first() != chorus.lines.first())
    }
}
