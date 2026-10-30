package com.example.designex.economics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designex.economics.audio.SoundFx
import com.example.designex.economics.data.Topic
import com.example.designex.ui.theme.Accent
import com.example.designex.ui.theme.Coral
import com.example.designex.ui.theme.DeepNavy
import com.example.designex.ui.theme.Glass
import com.example.designex.ui.theme.GlassBorder
import com.example.designex.ui.theme.Gold
import com.example.designex.ui.theme.Mint
import com.example.designex.ui.theme.TextHi
import com.example.designex.ui.theme.TextLo

private const val QUESTIONS = 5

private class Question(val answer: Topic, val options: List<Topic>)

@Composable
fun QuizScreen(deps: AppDeps) {
    val lang = deps.progress.language
    val subject = deps.catalog.subject(deps.progress.subjectId)
    var round by remember { mutableIntStateOf(0) }
    val questions = remember(round, subject.id) {
        val all = subject.topics
        all.shuffled().take(QUESTIONS).map { answer ->
            Question(answer, (all.filter { it.id != answer.id }.shuffled().take(3) + answer).shuffled())
        }
    }
    var index by remember(round, subject.id) { mutableIntStateOf(0) }
    var score by remember(round, subject.id) { mutableIntStateOf(0) }
    var picked by remember(round, index, subject.id) { mutableStateOf<Topic?>(null) }
    val finished = index >= QUESTIONS

    Column(
        Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 12.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        ScreenTitle(Copy.quiz.of(lang))
        Text(subject.name[lang] + " · " + Copy.question.of(lang), color = TextLo, fontSize = 15.sp)

        if (finished) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(CardShape)
            ) {
                SceneArt(if (score >= 3) Scenes.Aurora else Scenes.Dusk, round + 5, Modifier.fillMaxSize())
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(Brush.verticalGradient(listOf(DeepNavy.copy(alpha = 0.3f), DeepNavy.copy(alpha = 0.8f))))
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    SectionLabel(Copy.score.of(lang), TextHi)
                    Text("$score / $QUESTIONS", fontSize = 60.sp, fontWeight = FontWeight.SemiBold, color = if (score >= 3) Mint else Gold)
                    Text("${Copy.best.of(lang)}: ${deps.progress.bestQuizScore(subject.id)} / $QUESTIONS", color = TextLo)
                    Spacer(Modifier.height(18.dp))
                    PrimaryButton(Copy.playAgain.of(lang)) {
                        deps.fx.play(SoundFx.Fx.OPEN)
                        round++
                    }
                }
            }
            return@Column
        }

        val q = questions[index]
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${index + 1} / $QUESTIONS", color = Accent, fontWeight = FontWeight.Bold)
            Spacer(Modifier.weight(1f))
            Text("★ $score", color = Gold, fontWeight = FontWeight.Bold)
        }
        ProgressBar((index + if (picked != null) 1 else 0) / QUESTIONS.toFloat(), Accent)

        Panel {
            Column {
                SectionLabel(Copy.question.of(lang), Accent)
                Spacer8()
                Text(q.answer.description[lang], fontSize = 17.sp, lineHeight = 26.sp, color = TextHi)
            }
        }

        q.options.forEach { option ->
            val chosen = picked
            val tone: Color? = when {
                chosen == null -> null
                option.id == q.answer.id -> Mint
                option.id == chosen.id -> Coral
                else -> null
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(50))
                    .background(tone?.copy(alpha = 0.18f) ?: Glass)
                    .border(1.5.dp, tone ?: GlassBorder, RoundedCornerShape(50))
                    .clickable(enabled = chosen == null) {
                        picked = option
                        val right = option.id == q.answer.id
                        if (right) score++
                        deps.fx.play(if (right) SoundFx.Fx.SUCCESS else SoundFx.Fx.WRONG)
                    }
                    .padding(horizontal = 20.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(option.name[lang], color = TextHi, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                when (tone) {
                    Mint -> Text("✓", color = Mint, fontWeight = FontWeight.Bold)
                    Coral -> Text("✕", color = Coral, fontWeight = FontWeight.Bold)
                    else -> Unit
                }
            }
        }

        if (picked != null) {
            val last = index == QUESTIONS - 1
            PrimaryButton((if (last) Copy.finish else Copy.next).of(lang) + "  →") {
                deps.fx.play(SoundFx.Fx.TAP)
                if (last) deps.progress.recordQuiz(subject.id, score)
                index++
            }
        }
    }
}
