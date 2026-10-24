package com.nmims.songstudio

import android.media.MediaPlayer
import android.net.Uri
import android.os.SystemClock
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nmims.songstudio.playback.AudioFiles
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.UUID
import kotlin.math.sin

@RunWith(AndroidJUnit4::class)
class AudioImportTest {
    @Test fun importedAudioPersistsAndPlaysWithSeekAndPause() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val key = "test-${UUID.randomUUID()}"
        val source = File.createTempFile("test-tone", ".wav", context.cacheDir)
        val samples = 16000
        val wave = ByteBuffer.allocate(44 + samples * 2).order(ByteOrder.LITTLE_ENDIAN)
        wave.put("RIFF".toByteArray()).putInt(36 + samples * 2).put("WAVEfmt ".toByteArray())
            .putInt(16).putShort(1).putShort(1).putInt(8000).putInt(16000).putShort(2).putShort(16)
            .put("data".toByteArray()).putInt(samples * 2)
        repeat(samples) { wave.putShort((sin(it * 2 * Math.PI * 440 / 8000) * 3000).toInt().toShort()) }
        source.writeBytes(wave.array())
        val target = AudioFiles.file(context, key)
        try {
            AudioFiles.import(context, Uri.fromFile(source), key)
            assertTrue(target.exists())
            assertFalse(AudioFiles.file(context, "$key-other-topic").exists())
            val player = MediaPlayer()
            try {
                player.setDataSource(target.absolutePath); player.prepare(); player.start()
                SystemClock.sleep(300)
                assertTrue(player.isPlaying)
                assertTrue(player.currentPosition > 0)
                player.pause(); assertFalse(player.isPlaying)
                player.seekTo(1000); player.start()
                SystemClock.sleep(200)
                assertTrue(player.currentPosition >= 1000)
            } finally { player.release() }
        } finally { source.delete(); target.delete() }
    }

    @Test fun invalidImportDoesNotReplaceAnExistingFile() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val key = "test-${UUID.randomUUID()}"
        val source = File.createTempFile("invalid", ".mp3", context.cacheDir)
        val target = AudioFiles.file(context, key)
        target.writeText("previous-file")
        source.writeText("not an audio file")
        try {
            assertThrows(Exception::class.java) { AudioFiles.import(context, Uri.fromFile(source), key) }
            assertEquals("previous-file", target.readText())
        } finally { source.delete(); target.delete() }
    }
}
