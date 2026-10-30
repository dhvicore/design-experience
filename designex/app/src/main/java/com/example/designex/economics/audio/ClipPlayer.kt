package com.example.designex.economics.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper

/** Plays one finished song buffer and reports how far playback has got. */
class ClipPlayer {
    private class Session {
        @Volatile var alive = true
        @Volatile var track: AudioTrack? = null
    }

    private val main = Handler(Looper.getMainLooper())
    @Volatile private var session: Session? = null

    fun play(pcm: ShortArray, onFinished: () -> Unit) {
        stop()
        val s = Session()
        session = s
        Thread({ stream(pcm, s, onFinished) }, "song-player").start()
    }

    fun positionSamples(): Int = try {
        session?.track?.playbackHeadPosition ?: 0
    } catch (_: IllegalStateException) {
        0
    }

    fun stop() {
        session?.alive = false
        session = null
    }

    private fun stream(pcm: ShortArray, s: Session, onFinished: () -> Unit) {
        val rate = BeatComposer.SAMPLE_RATE
        val minBuffer = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val t = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setSampleRate(rate)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STREAM)
            .setBufferSizeInBytes(maxOf(minBuffer, rate))
            .build()
        s.track = t
        try {
            t.play()
            var pos = 0
            while (s.alive && pos < pcm.size) {
                val written = t.write(pcm, pos, minOf(2048, pcm.size - pos))
                if (written < 0) break
                pos += written
            }
            if (s.alive) {
                t.stop()
                val deadline = System.currentTimeMillis() + 1500L + 1000L * (pcm.size - t.playbackHeadPosition) / rate
                while (s.alive && t.playbackHeadPosition < pcm.size && System.currentTimeMillis() < deadline) Thread.sleep(40)
                if (s.alive) main.post { if (s.alive) onFinished() }
            } else {
                t.pause()
                t.flush()
            }
        } catch (_: IllegalStateException) {
        } finally {
            s.track = null
            t.release()
        }
    }
}
