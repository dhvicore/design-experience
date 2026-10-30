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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designex.economics.audio.SoundFx
import com.example.designex.economics.data.Lang
import com.example.designex.ui.theme.DeepNavy
import com.example.designex.ui.theme.GlassBorder
import com.example.designex.ui.theme.Gold
import com.example.designex.ui.theme.TextHi
import com.example.designex.ui.theme.TextLo
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun DashboardScreen(deps: AppDeps, open: (Route) -> Unit) {
    val progress = deps.progress
    val lang = progress.language
    val subject = deps.catalog.subject(progress.subjectId)
    val look = SubjectLook.of(subject.id)
    val topics = subject.topics
    val learnedCount = topics.count { it.id in progress.learned }
    val daily = remember(subject.id) { topics[LocalDate.now().dayOfYear % topics.size] }
    val last = progress.lastTopicId?.let(deps.catalog::topic)?.takeIf { it.subjectId == subject.id }
    val best = progress.bestQuizScore(subject.id)
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> Copy.goodMorning
        hour < 17 -> Copy.goodAfternoon
        else -> Copy.goodEvening
    }.of(lang)

    LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 120.dp)) {
        item {
            SceneHeader(look.sceneKey, 420.dp) {
                Row(
                    Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(progress.userName.orEmpty(), 40) { open(Route.Settings) }
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) { Logo(size = 30.sp) }
                    CircleButton("⚙") { open(Route.Settings) }
                }
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 20.dp, vertical = 22.dp)
                ) {
                    Text(
                        "☀  $greeting, ${progress.userName.orEmpty()}!",
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(DeepNavy.copy(alpha = 0.45f))
                            .border(1.dp, GlassBorder, RoundedCornerShape(50))
                            .padding(horizontal = 14.dp, vertical = 7.dp),
                        color = TextHi,
                        fontSize = 13.sp,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(subject.name[lang], color = TextHi, fontSize = 34.sp, fontWeight = FontWeight.SemiBold)
                    Text(look.summary[lang], color = TextLo, fontSize = 15.sp)
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
                ChipRow(deps.catalog.subjects.map { it.name[lang] }, deps.catalog.subjects.indexOf(subject)) {
                    deps.fx.play(SoundFx.Fx.TAP)
                    progress.chooseSubject(deps.catalog.subjects[it].id)
                }
                Spacer(Modifier.height(10.dp))
                LanguageSwitch(lang, onSelect = { l: Lang ->
                    deps.fx.play(SoundFx.Fx.TAP)
                    progress.changeLanguage(l)
                })
            }
        }

        if (last != null) {
            item {
                ListRow(last.id, Copy.continueWith.of(lang), last.name[lang], Modifier.padding(horizontal = 18.dp)) {
                    open(Route.TopicPage(last.id))
                }
                Spacer(Modifier.height(10.dp))
            }
        }
        item {
            ListRow(daily.id, "★  " + Copy.conceptOfDay.of(lang), daily.name[lang], Modifier.padding(horizontal = 18.dp)) {
                open(Route.TopicPage(daily.id))
            }
            Spacer(Modifier.height(14.dp))
        }

        item {
            Panel(Modifier.padding(horizontal = 18.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            SectionLabel(Copy.learned.of(lang))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text("$learnedCount", fontSize = 34.sp, fontWeight = FontWeight.SemiBold, color = look.color)
                                Text(" / ${topics.size}", fontSize = 18.sp, color = TextLo, modifier = Modifier.padding(bottom = 5.dp))
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            SectionLabel(Copy.quiz.of(lang))
                            Text("★ $best/5", color = Gold, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Spacer8()
                    ProgressBar(learnedCount / topics.size.toFloat(), look.color)
                }
            }
            Spacer(Modifier.height(22.dp))
        }

        item {
            Row(Modifier.padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(Copy.units.of(lang), color = TextHi, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                Text("${subject.units.size} · ${topics.size} ${Copy.topics.of(lang)}", color = TextLo, fontSize = 13.sp)
            }
            Spacer(Modifier.height(12.dp))
        }

        items(subject.units.chunked(2)) { pair ->
            Row(
                Modifier
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                pair.forEach { unit ->
                    val done = unit.topics.count { it.id in progress.learned }
                    SceneCard(
                        title = unit.name[lang],
                        subtitle = "${unit.topics.size} ${Copy.topics.of(lang)} · $done ✓",
                        key = "${subject.id}-unit-${unit.number}",
                        badge = "${Copy.unit.of(lang)} ${unit.number}",
                        modifier = Modifier.weight(1f),
                    ) { open(Route.UnitPage(unit.number)) }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

/** Frosted list row with a small painted thumbnail, like the reference's story rows. */
@Composable
fun ListRow(key: String, label: String, title: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(com.example.designex.ui.theme.Glass)
            .border(1.dp, GlassBorder, CardShape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
        ) { SceneArt(Scenes.forKey(key), seedOf(key), Modifier.fillMaxSize(), lake = false) }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(label, color = TextLo, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(title, color = TextHi, fontSize = 17.sp, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Text("›", color = TextLo, fontSize = 26.sp, modifier = Modifier.padding(horizontal = 8.dp))
    }
}

@Composable
fun Avatar(name: String, size: Int, onClick: (() -> Unit)? = null) {
    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(com.example.designex.ui.theme.Lavender, com.example.designex.ui.theme.SkyBlue)))
            .border(1.5.dp, TextHi.copy(alpha = 0.6f), CircleShape)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it },
        contentAlignment = Alignment.Center,
    ) {
        Text(name.take(1).uppercase(), color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = (size / 2.3).sp)
    }
}
