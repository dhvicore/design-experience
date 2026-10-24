package com.nmims.songstudio

import android.content.Context
import android.media.AudioManager
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MediaVolumeTest {
    @Test fun foregroundAppCanRaiseTheMediaStream() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val requested = InstrumentationRegistry.getArguments().getString("demoMediaVolume")?.toInt()
        val audio = instrumentation.targetContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val original = audio.getStreamVolume(AudioManager.STREAM_MUSIC)
        val target = requested ?: (original + 1).coerceAtMost(audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            try {
                scenario.onActivity { audio.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0) }
                SystemClock.sleep(1000)
                assertEquals(target, audio.getStreamVolume(AudioManager.STREAM_MUSIC))
            } finally {
                if (requested == null) scenario.onActivity { audio.setStreamVolume(AudioManager.STREAM_MUSIC, original, 0) }
            }
        }
    }
}
