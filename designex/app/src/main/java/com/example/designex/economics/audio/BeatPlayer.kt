package com.example.designex.economics.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.concurrent.atomic.AtomicBoolean

/** Streams a composed loop on a background thread until stopped. */
class BeatPlayer {
    private var session: AtomicBoolean? = null
    @Volatile private var volume = 0.8f

    fun start(style: BeatStyle, seed: Int, bpm: Int = style.bpm) {
        stop()
        val alive = AtomicBoolean(true)
        session = alive
        Thread({ stream(style, seed, bpm, alive) }, "beat-player").start()
    }

    fun setVolume(value: Float) {
        volume = value
    }

    fun stop() {
        session?.set(false)
        session = null
    }

    private fun stream(style: BeatStyle, seed: Int, bpm: Int, alive: AtomicBoolean) {
        val pcm = BeatComposer.compose(style, seed, bpm)
        if (!alive.get()) return
        val rate = BeatComposer.SAMPLE_RATE
        val minBuffer = AudioTrack.getMinBufferSize(rate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val track = AudioTrack.Builder()
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

        var applied = -1f
        var pos = 0
        try {
            track.play()
            while (alive.get()) {
                if (applied != volume) {
                    applied = volume
                    track.setVolume(applied)
                }
                val count = minOf(2048, pcm.size - pos)
                val written = track.write(pcm, pos, count)
                if (written < 0) break
                pos = (pos + written) % pcm.size
            }
            track.pause()
            track.flush()
        } catch (_: IllegalStateException) {
        } finally {
            track.release()
        }
    }
}
