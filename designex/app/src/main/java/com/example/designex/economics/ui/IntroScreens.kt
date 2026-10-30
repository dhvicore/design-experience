package com.example.designex.economics.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designex.ui.theme.DeepNavy
import com.example.designex.ui.theme.Dusk
import com.example.designex.ui.theme.Mist
import com.example.designex.ui.theme.TextHi
import com.example.designex.ui.theme.TextLo
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onDone: () -> Unit) {
    val fade = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        fade.animateTo(1f, tween(900))
        delay(900)
        onDone()
    }
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Mist, Dusk, DeepNavy))),
    ) {
        Column(Modifier.align(Alignment.Center).alpha(fade.value), horizontalAlignment = Alignment.CenterHorizontally) {
            Logo(size = 52.sp)
            Text("learn your syllabus through music", color = TextLo, fontSize = 14.sp)
        }
        CircularProgressIndicator(
            color = TextHi.copy(alpha = 0.8f),
            strokeWidth = 2.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 64.dp)
                .size(26.dp),
        )
    }
}

@Composable
fun WelcomeScreen(onDone: () -> Unit) {
    val fade = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        fade.animateTo(1f, tween(1000))
        delay(1200)
        onDone()
    }
    Box(Modifier.fillMaxSize()) {
        SceneArt(Scenes.MistyLake, 7, Modifier.fillMaxSize())
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(DeepNavy.copy(alpha = 0.55f), Color.Transparent, DeepNavy.copy(alpha = 0.7f))))
        )
        Column(Modifier.align(Alignment.Center).alpha(fade.value), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome to", color = TextHi, fontSize = 30.sp)
            Spacer(Modifier.height(4.dp))
            Logo(size = 46.sp)
        }
    }
}
