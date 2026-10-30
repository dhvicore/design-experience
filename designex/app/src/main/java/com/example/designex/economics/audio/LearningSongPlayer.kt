package com.example.designex.economics.audio

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.designex.economics.audio.sing.RenderedSong
import com.example.designex.economics.audio.sing.SongMood
import com.example.designex.economics.audio.sing.SongRenderer
import com.example.designex.economics.audio.sing.SpokenLine
import com.example.designex.economics.audio.sing.WavReader
import com.example.designex.economics.data.CatalogSong
import com.example.designex.economics.data.FittedLyrics
import com.example.designex.economics.data.Lang
import com.example.designex.economics.data.SunoSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest

enum class Engine { SUNO, PHONE }

class SongRequest(
    val topicId: String,
    val title: String,
    val lang: Lang,
    val song: CatalogSong,
    val beat: BeatStyle,
    val lyrics: FittedLyrics,
    val sunoStyle: String,
    val engine: Engine,
)

/** Makes and plays a study song with Suno (when a key is set) or the on-phone singer, exposing progress to the UI. */
class LearningSongPlayer(
    context: Context,
    private val beat: BeatPlayer,
    private val voice: VoiceSynth,
    private val sunoSettings: SunoSettings,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val cacheDir = context.cacheDir
    private val voiceDir = File(cacheDir, "econ_voice").apply { mkdirs() }
    private val sunoDir = File(cacheDir, "suno").apply { mkdirs() }
    private val suno = SunoClient(sunoSettings)
    private val clip = ClipPlayer()
    private var media: MediaPlayer? = null
    private val rendered = object : LinkedHashMap<String, RenderedSong>(4, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, RenderedSong>) = size > 3
    }
    private var job: Job? = null

    var activeTopicId by mutableStateOf<String?>(null)
        private set
    /** Progress text while a song is being made, or null. */
    var status by mutableStateOf<String?>(null)
        private set
    var progress by mutableStateOf<Float?>(null)
        private set
    var playing by mutableStateOf(false)
        private set
    var currentLine by mutableIntStateOf(-1)
        private set
    var engineUsed by mutableStateOf<Engine?>(null)
        private set
    var notice by mutableStateOf<String?>(null)
        private set

    val sunoReady: Boolean get() = sunoSettings.isConfigured

    fun play(request: SongRequest) {
        stop()
        activeTopicId = request.topicId
        notice = null
        job = scope.launch {
            try {
                if (request.engine == Engine.SUNO && sunoSettings.isConfigured) {
                    val file = try {
                        makeWithSuno(request)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        notice = "Suno: ${e.message}. Playing the on-phone version instead."
                        null
                    }
                    if (file != null) {
                        playFile(file)
                        return@launch
                    }
                }
                playOnPhone(request)
            } finally {
                status = null
                progress = null
            }
        }
    }

    private suspend fun makeWithSuno(request: SongRequest): File {
        val key = hash("${request.title}|${request.lang}|${request.song.title}|${request.sunoStyle}|${request.lyrics.asText()}")
        val out = File(sunoDir, "$key.mp3")
        if (out.length() > 10_000) return out
        progress = null
        return suno.generate(request.lyrics.asText(), request.sunoStyle, request.title, out) { status = it }
    }

    private suspend fun playFile(file: File) {
        engineUsed = Engine.SUNO
        status = null
        val player = withContext(Dispatchers.IO) {
            MediaPlayer().apply {
                setDataSource(file.path)
                prepare()
            }
        }
        media = player
        player.setOnCompletionListener { stop() }
        player.start()
        playing = true
        currentLine = -1
    }

    private suspend fun playOnPhone(request: SongRequest) {
        engineUsed = Engine.PHONE
        val mood = SongMood.of(request.song, request.beat)
        val seed = request.topicId.hashCode()
        val key = hash("${request.lang}|${request.song.title}|${request.beat}|${request.lyrics.asText()}")
        val song = rendered[key] ?: prepareOnPhone(request, mood, seed)?.also { rendered[key] = it }
        status = null
        progress = null
        if (song == null) {
            notice = "No voice for this language on this phone, so only the beat plays. Check Settings › Text-to-speech."
            beat.setVolume(0.8f)
            beat.start(mood.style, seed, mood.bpm)
            playing = true
            return
        }
        clip.play(song.pcm) { stop() }
        playing = true
        while (scope.isActive && playing) {
            currentLine = song.lineAt(clip.positionSamples())
            delay(80)
        }
    }

    private suspend fun prepareOnPhone(request: SongRequest, mood: SongMood, seed: Int): RenderedSong? {
        val lines = request.lyrics.sections.flatMap { section ->
            section.lines.mapIndexed { i, text -> Triple(text, section.isChorus, i == section.lines.lastIndex) }
        }
        progress = 0f
        val spoken = lines.mapIndexed { i, (text, chorus, end) ->
            status = "Recording the voice ${i + 1}/${lines.size}"
            val file = File(voiceDir, hash("${request.lang.name}|$text").take(20) + ".wav")
            val ok = (file.length() > 2048) || voice.record(text, request.lang, file)
            val audio = if (ok) withContext(Dispatchers.Default) { WavReader.read(file, BeatComposer.SAMPLE_RATE) } else null
            if (ok && audio == null) file.delete()
            progress = (i + 1f) / (lines.size + 1)
            SpokenLine(audio, chorus, end)
        }
        if (spoken.count { it.audio != null } * 2 < spoken.size) return null
        status = "Singing it to the tune"
        return withContext(Dispatchers.Default) { SongRenderer.render(spoken, mood, seed) }
    }

    private fun hash(text: String): String =
        MessageDigest.getInstance("SHA-1").digest(text.toByteArray()).joinToString("") { "%02x".format(it) }

    fun stop() {
        job?.cancel()
        job = null
        clip.stop()
        beat.stop()
        voice.stop()
        media?.run {
            runCatching { stop() }
            release()
        }
        media = null
        activeTopicId = null
        status = null
        progress = null
        playing = false
        currentLine = -1
    }

    fun release() {
        stop()
        scope.cancel()
        voice.release()
    }
}
