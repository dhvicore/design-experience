package com.example.designex

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import com.example.designex.ui.theme.DeepNavy
import androidx.compose.ui.Modifier
import com.example.designex.economics.audio.BeatPlayer
import com.example.designex.economics.audio.LearningSongPlayer
import com.example.designex.economics.audio.SoundFx
import com.example.designex.economics.audio.VoiceSynth
import com.example.designex.economics.data.AccountStore
import com.example.designex.economics.data.ProgressStore
import com.example.designex.economics.data.StudyRepository
import com.example.designex.economics.data.SunoSettings
import com.example.designex.economics.ui.AppDeps
import com.example.designex.economics.ui.EconomicsApp
import com.example.designex.ui.theme.DesignexTheme

class MainActivity : ComponentActivity() {
    private lateinit var fx: SoundFx
    private lateinit var player: LearningSongPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        val suno = SunoSettings(this)
        fx = SoundFx(this)
        player = LearningSongPlayer(this, BeatPlayer(), VoiceSynth(this), suno)
        val deps = AppDeps(
            catalog = StudyRepository.load(this),
            progress = ProgressStore(this),
            accounts = AccountStore(this),
            suno = suno,
            player = player,
            fx = fx,
        )
        setContent {
            DesignexTheme {
                Box(Modifier.fillMaxSize().background(DeepNavy)) {
                    EconomicsApp(deps)
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .windowInsetsTopHeight(WindowInsets.statusBars)
                            .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(DeepNavy.copy(alpha = 0.75f), DeepNavy.copy(alpha = 0f))))
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        player.stop()
    }

    override fun onDestroy() {
        player.release()
        fx.release()
        super.onDestroy()
    }
}
