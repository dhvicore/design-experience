package com.nmims.songstudio.ui

import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.nmims.songstudio.R
import kotlinx.coroutines.delay

/** Prepared computer-voice preview only: never presented as newly synthesized audio. */
@Composable
fun PreviewPlayer(language: String) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var player by remember { mutableStateOf<MediaPlayer?>(null) }
    var playing by remember { mutableStateOf(false) }
    var position by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(1f) }
    var failed by remember { mutableStateOf(false) }
    DisposableEffect(language, lifecycle) {
        fun release() { player?.release(); player = null; playing = false }
        fun prepare() {
            failed = false
            player = runCatching {
                MediaPlayer.create(context, if (language == "hi") R.raw.rolle_hi else R.raw.rolle_en)?.also { p ->
                    duration = p.duration.toFloat().coerceAtLeast(1f)
                    position = 0f
                    p.setOnCompletionListener { playing = false; position = duration }
                    p.setOnErrorListener { _, _, _ -> failed = true; release(); true }
                }
            }.getOrNull()
            if (player == null) failed = true
        }
        prepare()
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) release()
            if (event == Lifecycle.Event.ON_START && player == null) prepare()
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); release() }
    }
    LaunchedEffect(playing) {
        while (playing) { position = player?.currentPosition?.toFloat() ?: 0f; delay(250) }
    }
    Panel(Lime) {
        Text("OFFLINE / COMPUTER-VOICE PREVIEW", style = MaterialTheme.typography.labelSmall)
        Text("Rolle's Theorem", style = MaterialTheme.typography.titleLarge)
        Text("Original prototype recording · ${if (language == "hi") "Hindi" else "English"}")
        Text("Fixed Twinkle melody and original recording lyrics. This preview does not use your new style or edits.", style = MaterialTheme.typography.bodySmall)
        if (failed) Text("The preview could not be played. Try reopening this screen.")
        Slider(value = position.coerceIn(0f, duration), onValueChange = { position = it; player?.seekTo(it.toInt()) },
            valueRange = 0f..duration, enabled = player != null)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                player?.let { p ->
                    if (playing) p.pause() else {
                        if (position >= duration) { p.seekTo(0); position = 0f }
                        p.start()
                    }
                    playing = p.isPlaying
                }
            }, enabled = player != null) { Text(if (playing) "Pause" else "Play preview") }
            OutlinedButton(onClick = { player?.seekTo(0); position = 0f }, enabled = player != null) { Text("Replay") }
        }
    }
}
