package com.example.designex.economics

import com.example.designex.economics.audio.BeatComposer
import com.example.designex.economics.audio.BeatStyle
import com.example.designex.economics.audio.sing.PitchTracker
import com.example.designex.economics.audio.sing.Psola
import com.example.designex.economics.audio.sing.SongMood
import com.example.designex.economics.audio.sing.SongRenderer
import com.example.designex.economics.audio.sing.SpokenLine
import com.example.designex.economics.audio.sing.SyllableSplitter
import com.example.designex.economics.audio.sing.VocalMelody
import com.example.designex.economics.data.CatalogSong
import com.example.designex.economics.data.Lang
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

class SingingTest {
    private val rate = BeatComposer.SAMPLE_RATE

    /** Vowel-like bursts: a 160 Hz harmonic tone, [count] times, separated by near-silence. */
    private fun fakeSpeech(count: Int, hz: Double = 160.0, burst: Double = 0.14, gap: Double = 0.07): FloatArray {
        val burstLen = (burst * rate).toInt()
        val gapLen = (gap * rate).toInt()
        val out = FloatArray(count * (burstLen + gapLen) + gapLen)
        for (b in 0 until count) {
            val start = gapLen + b * (burstLen + gapLen)
            for (i in 0 until burstLen) {
                val t = i.toDouble() / rate
                val env = sin(PI * i / burstLen)
                val p = 2 * PI * hz * t
                out[start + i] = (env * (sin(p) + 0.5 * sin(2 * p) + 0.3 * sin(3 * p)) * 0.3).toFloat()
            }
        }
        return out
    }

    private fun estimateHz(x: FloatArray): Double {
        val track = PitchTracker.track(x, rate)
        return PitchTracker.medianHz(track.voicedPeriods(0, x.size), rate) ?: 0.0
    }

    @Test
    fun pitchTrackerFindsTheVoicePitch() {
        val hz = estimateHz(fakeSpeech(3, hz = 200.0, burst = 0.3))
        assertEquals(200.0, hz, 8.0)
    }

    @Test
    fun splitterFindsEachSyllable() {
        assertEquals(5, SyllableSplitter.split(fakeSpeech(5), rate).size)
        assertEquals(8, SyllableSplitter.split(fakeSpeech(8, burst = 0.12, gap = 0.05), rate).size)
    }

    @Test
    fun psolaHoldsTheSyllableAtTheTargetNoteAndLength() {
        val x = fakeSpeech(1, hz = 170.0, burst = 0.2)
        val track = PitchTracker.track(x, rate)
        val syllable = SyllableSplitter.split(x, rate).single()
        val length = (0.45 * rate).toInt()
        val sung = Psola.sing(x, track, syllable, targetHz = 261.63, outLength = length, sampleRate = rate)
        assertEquals(length, sung.size)
        assertTrue(sung.maxOf { abs(it) } > 0.05f)
        assertEquals(261.63, estimateHz(sung), 261.63 * 0.04)
    }

    @Test
    fun melodyLandsOnChordTonesOnStrongBeatsAndEndsHome() {
        BeatStyle.entries.forEach { style ->
            val harmony = BeatComposer.harmony(style)
            val notes = VocalMelody.line(
                syllables = 13, firstBar = 1, slotSpacing = 1, chorus = false, endOfSection = true,
                harmony = harmony, centerMidi = 57, motif = mood("pop", 104).verseMotif(7),
            )
            notes.forEachIndexed { i, note ->
                assertTrue("$style note in key", note % 12 in harmony.scalePitchClasses)
                if (i % 4 == 0) assertTrue("$style strong beat", note % 12 in harmony.chordForBar(1 + i / 8))
            }
            val finalChord = harmony.chordForBar(1 + (notes.size - 1) / 8)
            if (harmony.tonicPitchClass in finalChord) {
                assertEquals("$style ends on home note", harmony.tonicPitchClass, notes.last() % 12)
            } else {
                assertTrue("$style ends on a chord tone", notes.last() % 12 in finalChord)
            }
        }
    }

    @Test
    fun renderedSongPlacesEveryLineInOrder() {
        val lines = listOf(
            SpokenLine(fakeSpeech(6), chorus = false, endOfSection = false),
            SpokenLine(fakeSpeech(9), chorus = false, endOfSection = true),
            SpokenLine(null, chorus = true, endOfSection = false),
            SpokenLine(fakeSpeech(3), chorus = true, endOfSection = true),
        )
        val mood = mood("pop", 104)
        val song = SongRenderer.render(lines, mood, seed = 11)
        val bar = BeatComposer.barSamples(mood.bpm)
        assertEquals(0, song.pcm.size % bar)
        assertEquals(-1, song.lineAt(bar / 2))
        assertEquals(0, song.lineAt(bar))
        assertEquals(1, song.lineAt(3 * bar))
        assertEquals(3, song.lineAt(song.pcm.size - 1))
        assertTrue(song.pcm.maxOf { abs(it.toInt()) } > 20000)
    }

    @Test
    fun chosenSongSetsBeatTempoAndMelodyShape() {
        val party = mood("party", 122)
        assertTrue(!party.verseMotif(3).contentEquals(party.chorusMotif(3)))
        assertEquals(BeatStyle.BOLLY, party.style)
        assertEquals(122, party.bpm)
        assertEquals(BeatStyle.RAGA, mood("bhajan", 78).style)
        assertEquals(BeatStyle.LOFI, mood("desirap", 95).style)
    }

    private fun mood(genre: String, bpm: Int) =
        SongMood.of(CatalogSong("Song", "Artist", null, bpm, "", false, genre, Lang.HI))
}
