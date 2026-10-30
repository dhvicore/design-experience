package com.example.designex.economics.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/** Short interface sounds, synthesised once into cached WAV files and played through SoundPool. */
class SoundFx(context: Context) {

    enum class Fx { TAP, OPEN, SUCCESS, WRONG }

    private val pool = SoundPool.Builder()
        .setMaxStreams(4)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()
    private val soundIds = ConcurrentHashMap<Fx, Int>()
    private val ready = ConcurrentHashMap.newKeySet<Int>()

    init {
        pool.setOnLoadCompleteListener { _, id, status -> if (status == 0) ready += id }
        val dir = context.cacheDir
        Thread({
            Fx.entries.forEach { fx ->
                val file = File(dir, "econ_fx_${fx.name.lowercase()}.wav")
                file.writeBytes(wav(render(fx)))
                soundIds[fx] = pool.load(file.path, 1)
            }
        }, "fx-render").start()
    }

    fun play(fx: Fx) {
        val id = soundIds[fx] ?: return
        if (id in ready) pool.play(id, 0.7f, 0.7f, 1, 0, 1f)
    }

    fun release() = pool.release()

    private fun render(fx: Fx): FloatArray = when (fx) {
        Fx.TAP -> tones(listOf(1100.0), gap = 0.0, length = 0.04, decay = 90.0)
        Fx.OPEN -> tones(listOf(660.0, 990.0), gap = 0.05, length = 0.09, decay = 40.0)
        Fx.SUCCESS -> tones(listOf(523.25, 659.25, 783.99, 1046.5), gap = 0.085, length = 0.35, decay = 9.0)
        Fx.WRONG -> tones(listOf(330.0, 247.0), gap = 0.15, length = 0.18, decay = 12.0, buzzy = true)
    }

    private fun tones(freqs: List<Double>, gap: Double, length: Double, decay: Double, buzzy: Boolean = false): FloatArray {
        val total = ((gap * (freqs.size - 1) + length) * RATE).toInt()
        val out = FloatArray(total)
        freqs.forEachIndexed { n, f ->
            val start = (n * gap * RATE).toInt()
            for (i in 0 until (length * RATE).toInt()) {
                if (start + i >= total) break
                val t = i.toDouble() / RATE
                val p = 2 * PI * f * t
                val wave = if (buzzy) sin(p) + sin(3 * p) / 3 else sin(p) + 0.2 * sin(2 * p)
                out[start + i] += (wave * minOf(1.0, i / 60.0) * exp(-t * decay) * 0.45).toFloat()
            }
        }
        return out
    }

    private fun wav(samples: FloatArray): ByteArray {
        val data = samples.size * 2
        val buf = ByteBuffer.allocate(44 + data).order(ByteOrder.LITTLE_ENDIAN)
        buf.put("RIFF".toByteArray()).putInt(36 + data).put("WAVE".toByteArray())
        buf.put("fmt ".toByteArray()).putInt(16).putShort(1).putShort(1)
            .putInt(RATE).putInt(RATE * 2).putShort(2).putShort(16)
        buf.put("data".toByteArray()).putInt(data)
        samples.forEach { buf.putShort((it.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()) }
        return buf.array()
    }

    private companion object {
        const val RATE = 22050
    }
}
