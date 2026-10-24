package com.nmims.songstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.nmims.songstudio.ui.SongStudio
import com.nmims.songstudio.ui.StudioTheme
import com.nmims.songstudio.auth.AuthScreen
import com.nmims.songstudio.auth.SessionViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.nmims.songstudio.playback.AudioFiles

class MainActivity : ComponentActivity() {
    private val session by lazy { ViewModelProvider(this)[SessionViewModel::class.java] }
    private val audioPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        session.audioPickerOpen = false
        val key = session.pendingAudioKey
        session.pendingAudioKey = null
        if (uri != null && key != null) lifecycleScope.launch {
            session.importBusy = true
            session.importError = ""
            try {
                withContext(Dispatchers.IO) { AudioFiles.import(applicationContext, uri, key) }
                session.importRevision++
            } catch (e: Exception) { session.importError = e.message ?: "Could not import the song." }
            finally { session.importBusy = false }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StudioTheme {
                val student = session.student
                if (student == null) AuthScreen { session.signIn(it) }
                else SongStudio(student, onLogout = { session.signOut() },
                    importRevision = session.importRevision, importBusy = session.importBusy,
                    importError = session.importError, onImportAudio = { key ->
                        session.pendingAudioKey = key; session.audioPickerOpen = true
                        runCatching { audioPicker.launch("audio/*") }.onFailure {
                            session.audioPickerOpen = false; session.pendingAudioKey = null
                            session.importError = "No file picker is available on this device."
                        }
                    })
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // The user-requested system audio picker is part of the current app task.
        if (!isChangingConfigurations && !session.audioPickerOpen) session.signOut()
    }
}
