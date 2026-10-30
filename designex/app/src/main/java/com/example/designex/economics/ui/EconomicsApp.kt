package com.example.designex.economics.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designex.economics.audio.LearningSongPlayer
import com.example.designex.economics.audio.SoundFx
import com.example.designex.economics.data.AccountStore
import com.example.designex.economics.data.ProgressStore
import com.example.designex.economics.data.StudyCatalog
import com.example.designex.economics.data.SunoSettings
import com.example.designex.ui.theme.Accent
import com.example.designex.ui.theme.DeepNavy
import com.example.designex.ui.theme.GlassBorder
import com.example.designex.ui.theme.TextFaint

enum class Tab { HOME, LEARN, SONGS, QUIZ, PROFILE }

sealed interface Route {
    data class UnitPage(val number: Int) : Route
    data class TopicPage(val id: String) : Route
    data class Studio(val topicId: String) : Route
    data object Songs : Route
    data object Quiz : Route
    data object Settings : Route
}

class AppDeps(
    val catalog: StudyCatalog,
    val progress: ProgressStore,
    val accounts: AccountStore,
    val suno: SunoSettings,
    val player: LearningSongPlayer,
    val fx: SoundFx,
)

/** Splash → login → welcome → first-time subject and language → tabbed app. */
@Composable
fun EconomicsApp(deps: AppDeps) {
    var splash by rememberSaveable { mutableStateOf(true) }
    var welcomeFor by rememberSaveable { mutableStateOf<String?>(null) }
    val user = deps.progress.userName

    val phase = when {
        splash -> "splash"
        user == null -> "login"
        welcomeFor == user -> "welcome"
        !deps.progress.onboarded -> "onboarding"
        else -> "main"
    }
    Crossfade(phase, animationSpec = tween(600), label = "phase") { p ->
        when (p) {
            "splash" -> SplashScreen { splash = false }
            "login" -> LoginScreen(deps.accounts) { name, email ->
                deps.progress.signIn(name, email)
                deps.fx.play(SoundFx.Fx.SUCCESS)
                welcomeFor = name
            }
            "welcome" -> WelcomeScreen { welcomeFor = null }
            "onboarding" -> Onboarding(deps)
            else -> MainTabs(deps)
        }
    }
}

@Composable
private fun Onboarding(deps: AppDeps) {
    var step by rememberSaveable { mutableStateOf(0) }
    BackHandler(enabled = step > 0) { step = 0 }
    Crossfade(step, label = "onboarding") { s ->
        if (s == 0) {
            SubjectScreen(deps) {
                deps.fx.play(SoundFx.Fx.OPEN)
                step = 1
            }
        } else {
            LanguageScreen(deps, back = { step = 0 }) {
                deps.fx.play(SoundFx.Fx.SUCCESS)
                deps.progress.finishOnboarding()
            }
        }
    }
}

@Composable
private fun MainTabs(deps: AppDeps) {
    var tab by rememberSaveable { mutableStateOf(Tab.HOME) }
    var learnUnit by remember { mutableStateOf<Int?>(null) }
    val stack = remember { mutableStateListOf<Route>() }

    val open: (Route) -> Unit = { route ->
        deps.fx.play(SoundFx.Fx.OPEN)
        when (route) {
            Route.Songs -> { stack.clear(); tab = Tab.SONGS }
            Route.Quiz -> { stack.clear(); tab = Tab.QUIZ }
            Route.Settings -> { stack.clear(); tab = Tab.PROFILE }
            is Route.UnitPage -> { stack.clear(); learnUnit = route.number; tab = Tab.LEARN }
            else -> stack.add(route)
        }
    }
    val back: () -> Unit = {
        deps.fx.play(SoundFx.Fx.TAP)
        if (stack.isNotEmpty()) stack.removeAt(stack.lastIndex) else tab = Tab.HOME
    }
    val logOut: () -> Unit = {
        deps.player.stop()
        stack.clear()
        deps.progress.signOut()
    }
    BackHandler(enabled = stack.isNotEmpty() || tab != Tab.HOME) { back() }

    Box(
        Modifier
            .fillMaxSize()
            .nightBackground()
    ) {
        val target: Any = stack.lastOrNull() ?: tab
        Crossfade(target, label = "screen") { screen ->
            when (screen) {
                Tab.HOME -> DashboardScreen(deps, open)
                Tab.LEARN -> UnitScreen(deps, learnUnit, open) { learnUnit = it }
                Tab.SONGS -> SongLibraryScreen(deps, open)
                Tab.QUIZ -> QuizScreen(deps)
                Tab.PROFILE -> SettingsScreen(deps, logOut)
                is Route.TopicPage -> TopicScreen(deps, screen.id, open, back)
                is Route.Studio -> StudioScreen(deps, screen.topicId, back)
                else -> Unit
            }
        }
        if (stack.isEmpty()) {
            BottomBar(
                current = tab,
                lang = deps.progress.language,
                modifier = Modifier.align(Alignment.BottomCenter),
            ) {
                deps.fx.play(SoundFx.Fx.TAP)
                if (it == Tab.LEARN && tab != Tab.LEARN) learnUnit = null
                tab = it
            }
        }
    }
}

@Composable
private fun BottomBar(current: Tab, lang: com.example.designex.economics.data.Lang, modifier: Modifier, onSelect: (Tab) -> Unit) {
    val labels = mapOf(
        Tab.HOME to Copy.tabHome, Tab.LEARN to Copy.tabLearn, Tab.SONGS to Copy.tabSongs,
        Tab.QUIZ to Copy.tabQuiz, Tab.PROFILE to Copy.tabProfile,
    )
    Row(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(androidx.compose.ui.graphics.Color(0xFF222D4B))
            .border(1.dp, GlassBorder, RoundedCornerShape(28.dp))
            .padding(vertical = 10.dp),
    ) {
        Tab.entries.forEach { t ->
            val on = t == current
            val color = if (on) Accent else TextFaint
            Column(
                Modifier
                    .weight(1f)
                    .clickable(remember { MutableInteractionSource() }, indication = null) { onSelect(t) },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Canvas(Modifier.size(24.dp)) { drawTabIcon(t, color, on) }
                Spacer(Modifier.height(4.dp))
                Text(labels.getValue(t)[lang], color = color, fontSize = 11.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.Medium)
            }
        }
    }
}

private fun DrawScope.drawTabIcon(tab: Tab, color: Color, filled: Boolean) {
    val w = size.width
    val stroke = Stroke(width = w * 0.09f, cap = StrokeCap.Round)
    val style = if (filled) androidx.compose.ui.graphics.drawscope.Fill else stroke
    when (tab) {
        Tab.HOME -> {
            val p = Path().apply {
                moveTo(w * 0.5f, w * 0.12f); lineTo(w * 0.9f, w * 0.48f); lineTo(w * 0.8f, w * 0.48f)
                lineTo(w * 0.8f, w * 0.88f); lineTo(w * 0.2f, w * 0.88f); lineTo(w * 0.2f, w * 0.48f)
                lineTo(w * 0.1f, w * 0.48f); close()
            }
            drawPath(p, color, style = style)
        }
        Tab.LEARN -> {
            drawRoundRect(color, Offset(w * 0.12f, w * 0.18f), Size(w * 0.36f, w * 0.64f), style = style)
            drawRoundRect(color, Offset(w * 0.52f, w * 0.18f), Size(w * 0.36f, w * 0.64f), style = style)
        }
        Tab.SONGS -> {
            drawCircle(color, w * 0.15f, Offset(w * 0.32f, w * 0.75f))
            drawCircle(color, w * 0.15f, Offset(w * 0.75f, w * 0.65f))
            drawLine(color, Offset(w * 0.45f, w * 0.75f), Offset(w * 0.45f, w * 0.15f), w * 0.09f, StrokeCap.Round)
            drawLine(color, Offset(w * 0.88f, w * 0.65f), Offset(w * 0.88f, w * 0.08f), w * 0.09f, StrokeCap.Round)
            drawLine(color, Offset(w * 0.45f, w * 0.15f), Offset(w * 0.88f, w * 0.08f), w * 0.12f, StrokeCap.Round)
        }
        Tab.QUIZ -> {
            val p = Path()
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) w * 0.45f else w * 0.2f
                val a = Math.toRadians(-90.0 + i * 36).toFloat()
                val x = w / 2 + r * kotlin.math.cos(a)
                val y = w / 2 + r * kotlin.math.sin(a)
                if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
            }
            p.close()
            drawPath(p, color, style = style)
        }
        Tab.PROFILE -> {
            drawCircle(color, w * 0.2f, Offset(w * 0.5f, w * 0.32f), style = style)
            drawArc(color, 180f, 180f, useCenter = filled, topLeft = Offset(w * 0.14f, w * 0.6f), size = Size(w * 0.72f, w * 0.6f), style = style)
        }
    }
}
