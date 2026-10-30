package com.example.designex.economics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designex.economics.audio.SoundFx
import com.example.designex.economics.data.Lang
import com.example.designex.ui.theme.DeepNavy
import com.example.designex.ui.theme.Dusk
import com.example.designex.ui.theme.TextLo

/** Shared layout for the onboarding questions: title, options, Continue at the bottom over misty mountains. */
@Composable
private fun QuestionLayout(
    title: String,
    subtitle: String,
    button: String,
    onBack: (() -> Unit)?,
    onContinue: () -> Unit,
    options: @Composable () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Dusk, DeepNavy)))
    ) {
        SceneArt(
            Scenes.MistyLake.copy(skyTop = DeepNavy.copy(alpha = 0f), skyBottom = DeepNavy.copy(alpha = 0f)),
            21,
            Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .fillMaxHeight(0.45f),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(Dusk.copy(alpha = 0.2f), DeepNavy.copy(alpha = 0.55f))))
        )
        Column(
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            if (onBack != null) CircleButton("‹", onBack) else Spacer(Modifier.height(42.dp))
            Spacer(Modifier.height(36.dp))
            ScreenTitle(title)
            Spacer(Modifier.height(6.dp))
            Text(subtitle, color = TextLo, fontSize = 15.sp, lineHeight = 21.sp)
            Spacer(Modifier.height(28.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { options() }
            Spacer(Modifier.weight(1f))
            PrimaryButton(button, onClick = onContinue)
        }
    }
}

@Composable
fun SubjectScreen(deps: AppDeps, onContinue: () -> Unit) {
    val lang = deps.progress.language
    var chosen by remember { mutableStateOf(deps.progress.subjectId) }
    QuestionLayout(
        title = Copy.whatToLearn.of(lang),
        subtitle = Copy.selectRelevant.of(lang),
        button = Copy.continueBtn.of(lang),
        onBack = null,
        onContinue = {
            deps.progress.chooseSubject(chosen)
            onContinue()
        },
    ) {
        deps.catalog.subjects.forEach { s ->
            val look = SubjectLook.of(s.id)
            OptionPill(look.badge, "${s.name[lang]}  ·  ${s.topics.size} ${Copy.topics.of(lang)}", s.id == chosen) {
                deps.fx.play(SoundFx.Fx.TAP)
                chosen = s.id
            }
        }
    }
}

@Composable
fun LanguageScreen(deps: AppDeps, back: () -> Unit, onContinue: () -> Unit) {
    var chosen by remember { mutableStateOf(deps.progress.language) }
    QuestionLayout(
        title = Copy.whichLanguage.of(chosen),
        subtitle = Copy.languageSub.of(chosen),
        button = Copy.continueBtn.of(chosen),
        onBack = back,
        onContinue = {
            deps.progress.changeLanguage(chosen)
            onContinue()
        },
    ) {
        listOf(Lang.EN to "Aa", Lang.HI to "अ", Lang.MR to "म").forEach { (l, glyph) ->
            OptionPill(glyph, l.label, l == chosen) {
                deps.fx.play(SoundFx.Fx.TAP)
                chosen = l
            }
        }
    }
}
