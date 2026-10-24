package com.nmims.songstudio

import android.media.MediaPlayer
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nmims.songstudio.data.Curriculum
import com.nmims.songstudio.data.MusicChoices
import com.nmims.songstudio.generation.LyricPrompts
import com.nmims.songstudio.generation.SingingClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Explicit opt-in: consumes a real generation on the configured Colab GPU. */
@RunWith(AndroidJUnit4::class)
class LiveSingingTest {
    @Test fun selectedMelodyAndFittedLyricsGenerateAndPlayOnEmulator() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("liveSingingTest") == "true")
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val data = Curriculum(context)
        val topic = data.topics.first { it.title == "Rolle's Theorem" }
        val choice = MusicChoices.options(data, topic, "Bhajan", true).first { it.id == "raghupati-raghav-raja-ram" }
        val references = SingingClient.melodies()
        assertEquals(84, references.getValue(choice.id).bpm)
        val adapted = SingingClient.adapt(topic, "Bhajan", choice, LyricPrompts.draft(topic, "Bhajan", choice), true)
        assertTrue(adapted.lyrics.contains("अवकलज"))
        assertTrue(adapted.lyrics.contains("समान"))
        val generated = SingingClient.generate(context, "live-rolle-raghupati-demo", topic, "Bhajan", choice, adapted.lyrics, "Moderate", true) { }
        assertEquals("reference_cover", generated.mode)
        assertTrue(generated.audio.length() > 10000)
        val player = MediaPlayer()
        try {
            player.setDataSource(generated.audio.absolutePath)
            player.prepare()
            assertTrue(player.duration in 30000..45000)
            player.start(); SystemClock.sleep(1500)
            assertTrue(player.isPlaying)
            assertTrue(player.currentPosition > 1000)
            player.pause(); assertFalse(player.isPlaying)
            player.seekTo(10000); player.start(); SystemClock.sleep(500)
            assertTrue(player.currentPosition >= 10000)
        } finally { player.release() }
    }
}
