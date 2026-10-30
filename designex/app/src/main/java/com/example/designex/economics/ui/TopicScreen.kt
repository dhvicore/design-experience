package com.example.designex.economics.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designex.economics.audio.SoundFx
import com.example.designex.ui.theme.Gold
import com.example.designex.ui.theme.Mint
import com.example.designex.ui.theme.TextHi
import com.example.designex.ui.theme.TextLo

@Composable
fun TopicScreen(deps: AppDeps, topicId: String, open: (Route) -> Unit, back: () -> Unit) {
    val catalog = deps.catalog
    val topic = catalog.topic(topicId) ?: return
    val subject = catalog.subject(topic.subjectId)
    val look = SubjectLook.of(subject.id)
    val lang = deps.progress.language
    val context = LocalContext.current

    LaunchedEffect(topicId) { deps.progress.opened(topicId) }

    LazyColumn(
        Modifier.fillMaxSize().navigationBarsPadding(),
        contentPadding = PaddingValues(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SceneHeader(topic.id, 440.dp) {
                Row(
                    Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) { CircleButton("‹", back) }
                Column(
                    Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(topic.name[lang], color = TextHi, fontSize = 30.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, lineHeight = 36.sp)
                    Spacer(Modifier.height(6.dp))
                    Text(look.summary[lang], color = TextLo, fontSize = 14.sp, textAlign = TextAlign.Center)
                }
                Row(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 26.dp, start = 24.dp, end = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(36.dp),
                ) {
                    Meta(Copy.subject.of(lang).uppercase(), subject.name[lang])
                    Meta("${Copy.unit.of(lang).uppercase()} ${topic.unitNo}", subject.unit(topic.unitNo)?.name?.get(lang).orEmpty())
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 18.dp)) {
                LanguageSwitch(lang, onSelect = {
                    deps.fx.play(SoundFx.Fx.TAP)
                    deps.progress.changeLanguage(it)
                })
            }
        }

        item {
            Panel(Modifier.padding(horizontal = 18.dp)) {
                Column {
                    SectionLabel(Copy.concept.of(lang), look.color)
                    Spacer8()
                    Text(topic.description[lang], fontSize = 16.sp, lineHeight = 25.sp, color = TextHi)
                    topic.formula?.let { f ->
                        Spacer(Modifier.height(14.dp))
                        SectionLabel(Copy.formula.of(lang), Gold)
                        Spacer8()
                        Text(
                            f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Gold.copy(alpha = 0.10f))
                                .padding(14.dp),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp,
                            color = TextHi,
                        )
                    }
                    val keywords = topic.keywords[lang]
                    if (keywords.isNotEmpty()) {
                        Spacer(Modifier.height(14.dp))
                        SectionLabel(Copy.keyIdeas.of(lang), Gold)
                        Spacer8()
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            keywords.forEach { Pill(it, look.color, look.color.copy(alpha = 0.14f)) }
                        }
                    }
                }
            }
        }

        item {
            Box(
                Modifier
                    .padding(horizontal = 18.dp)
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(CardShape)
            ) {
                SceneArt(Scenes.Aurora, seedOf(topic.id) + 3, Modifier.fillMaxSize())
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(androidx.compose.ui.graphics.Brush.verticalGradient(listOf(com.example.designex.ui.theme.DeepNavy.copy(alpha = 0.2f), com.example.designex.ui.theme.DeepNavy.copy(alpha = 0.85f))))
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Bottom,
                ) {
                    Text("♪  " + Copy.makeSong.of(lang), color = TextHi, fontSize = 21.sp, fontWeight = FontWeight.SemiBold, lineHeight = 27.sp)
                    Text(Copy.makeSongSub.of(lang), color = TextLo, fontSize = 13.sp, lineHeight = 18.sp)
                    Spacer(Modifier.height(12.dp))
                    PrimaryButton("${Copy.step.of(lang).fill("n" to 1)}  →  ${Copy.pickSong.of(lang)}") { open(Route.Studio(topic.id)) }
                }
            }
        }

        val refs = topic.songs[lang].orEmpty()
        if (refs.isNotEmpty()) {
            item {
                Panel(Modifier.padding(horizontal = 18.dp)) {
                    Column {
                        SectionLabel("♪  " + Copy.matchedSong.of(lang), Mint)
                        val primary = refs.first()
                        Spacer(Modifier.height(10.dp))
                        Text(primary.title, fontWeight = FontWeight.SemiBold, fontSize = 19.sp, color = TextHi)
                        Text(primary.artist, color = TextLo, fontSize = 14.sp)
                        topic.reason?.let {
                            Spacer(Modifier.height(10.dp))
                            MutedText(it)
                        }
                        Spacer(Modifier.height(12.dp))
                        ListenButtons("${primary.title} ${primary.artist}", context) { deps.fx.play(SoundFx.Fx.TAP) }
                        refs.getOrNull(1)?.let { alt ->
                            Spacer(Modifier.height(10.dp))
                            Text("${Copy.alsoTry.of(lang)}:  ${alt.title} — ${alt.artist}", color = TextLo, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        item {
            val done = topicId in deps.progress.learned
            Column(Modifier.padding(horizontal = 18.dp)) {
                if (done) {
                    GhostButton(Copy.isLearned.of(lang), Modifier.fillMaxWidth(), color = Mint) {
                        deps.progress.toggleLearned(topicId)
                        deps.fx.play(SoundFx.Fx.TAP)
                    }
                } else {
                    PrimaryButton(Copy.markLearned.of(lang)) {
                        deps.progress.toggleLearned(topicId)
                        deps.fx.play(SoundFx.Fx.SUCCESS)
                    }
                }
            }
        }
    }
}

@Composable
private fun Meta(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(140.dp)) {
        Text(label, color = TextLo, fontSize = 11.sp, letterSpacing = 1.5.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
        Text(value, color = TextHi, fontSize = 14.sp, textAlign = TextAlign.Center, maxLines = 2)
    }
}
