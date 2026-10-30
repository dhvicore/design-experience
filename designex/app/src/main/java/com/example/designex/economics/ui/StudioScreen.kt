package com.example.designex.economics.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designex.economics.audio.Engine
import com.example.designex.economics.audio.SongRequest
import com.example.designex.economics.audio.SoundFx
import com.example.designex.economics.data.CatalogSong
import com.example.designex.economics.data.GenreStyles
import com.example.designex.economics.data.Lang
import com.example.designex.economics.data.LyricsFitter
import com.example.designex.ui.theme.Accent
import com.example.designex.ui.theme.DeepNavy
import com.example.designex.ui.theme.Glass
import com.example.designex.ui.theme.GlassBorder
import com.example.designex.ui.theme.GlassStrong
import com.example.designex.ui.theme.Gold
import com.example.designex.ui.theme.TextFaint
import com.example.designex.ui.theme.TextHi
import com.example.designex.ui.theme.TextLo

private const val ITEMS_BEFORE_LYRICS = 5

/** Pick a song → see the topic's lyrics fitted to it → have it sung by Suno or the on-phone singer. */
@Composable
fun StudioScreen(deps: AppDeps, topicId: String, back: () -> Unit) {
    val catalog = deps.catalog
    val topic = catalog.topic(topicId) ?: return
    val look = SubjectLook.of(topic.subjectId)
    val lang = deps.progress.language
    val player = deps.player

    val suggestions = remember(topicId, lang) { topic.songs[lang].orEmpty().map { catalog.asCatalogSong(it, lang) } }
    val genres = catalog.genres(lang)
    var song by remember(topicId, lang) { mutableStateOf(suggestions.firstOrNull() ?: genres.first().songs.first()) }
    var picking by remember(topicId, lang) { mutableStateOf(true) }
    var browseGenre by remember(topicId, lang) { mutableStateOf(song.genreId) }
    var variant by remember(topicId, lang) { mutableIntStateOf(0) }
    var engine by remember { mutableStateOf(if (player.sunoReady) Engine.SUNO else Engine.PHONE) }
    val lyrics = remember(topicId, lang, song, variant) { LyricsFitter.fit(topic, lang, song, variant) }
    val active = player.activeTopicId == topicId
    val busy = active && player.status != null
    val listState = rememberLazyListState()

    val lineItemIndex = remember(lyrics) {
        var item = ITEMS_BEFORE_LYRICS
        buildList { lyrics.sections.forEach { s -> item++; s.lines.forEach { _ -> add(item++) } } }
    }
    DisposableEffect(topicId) { onDispose { if (player.activeTopicId == topicId) player.stop() } }
    LaunchedEffect(lang, song, variant) { if (player.activeTopicId == topicId) player.stop() }
    LaunchedEffect(player.currentLine) {
        val line = player.currentLine
        if (active && line in lineItemIndex.indices) listState.animateScrollToItem((lineItemIndex[line] - 2).coerceAtLeast(0))
    }

    fun generate() {
        val genre = catalog.genre(lang, song.genreId)
        val vocals = when (lang) { Lang.EN -> "English vocals"; Lang.HI -> "Hindi vocals"; Lang.MR -> "Marathi vocals" }
        val style = listOfNotNull(genre?.sunoTags, song.mood.ifBlank { null }, "${song.bpm} bpm", vocals, genre?.instruments, "clear educational vocals")
            .filter { it.isNotBlank() }
            .joinToString(", ")
        player.play(
            SongRequest(
                topicId = topicId,
                title = topic.name[lang],
                lang = lang,
                song = song,
                beat = GenreStyles.beatStyle(song.genreId),
                lyrics = lyrics,
                sunoStyle = style,
                engine = engine,
            )
        )
    }

    LazyColumn(
        Modifier.fillMaxSize().navigationBarsPadding(),
        state = listState,
        contentPadding = PaddingValues(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            SceneHeader("studio-" + topic.id, 260.dp) {
                Row(
                    Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircleButton("‹", back)
                    Spacer(Modifier.width(12.dp))
                    Text("SONG STUDIO", color = TextHi, fontWeight = FontWeight.Bold, letterSpacing = 2.sp, fontSize = 13.sp)
                }
                Column(
                    Modifier
                        .align(Alignment.BottomStart)
                        .padding(20.dp)
                ) {
                    Text(topic.name[lang], fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = TextHi, lineHeight = 32.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StepChip(1, Copy.pickSong.of(lang), done = !picking)
                        StepChip(2, Copy.yourLyrics.of(lang), done = !picking)
                        StepChip(3, Copy.makeIt.of(lang), done = active && player.playing)
                    }
                }
            }
        }

        item {
            Panel(Modifier.padding(horizontal = 18.dp)) {
                Column {
                    StepTitle(1, Copy.pickSong.of(lang), look.color, lang)
                    Spacer(Modifier.height(12.dp))
                    if (!picking) {
                        SongRow(song, catalog.genre(lang, song.genreId)?.name, selected = true, color = look.color, lang = lang) { picking = true }
                        Spacer(Modifier.height(10.dp))
                        GhostButton(Copy.changeSong.of(lang), color = look.color) { picking = true }
                    } else {
                        if (suggestions.isNotEmpty()) {
                            SectionLabel(Copy.suggested.of(lang))
                            Spacer(Modifier.height(8.dp))
                            suggestions.forEach { s ->
                                SongRow(s, catalog.genre(lang, s.genreId)?.name, s == song, look.color, lang) {
                                    deps.fx.play(SoundFx.Fx.TAP)
                                    song = s
                                    picking = false
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        SectionLabel(Copy.browseStyles.of(lang))
                        Spacer(Modifier.height(8.dp))
                        ChipRow(genres.map { it.name }, genres.indexOfFirst { it.id == browseGenre }.coerceAtLeast(0)) {
                            deps.fx.play(SoundFx.Fx.TAP)
                            browseGenre = genres[it].id
                        }
                        Spacer(Modifier.height(10.dp))
                        catalog.genre(lang, browseGenre)?.songs?.forEach { s ->
                            SongRow(s, catalog.genre(lang, s.genreId)?.name, s == song, look.color, lang) {
                                deps.fx.play(SoundFx.Fx.TAP)
                                song = s
                                picking = false
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(Copy.tuneHonesty.of(lang), color = TextFaint, fontSize = 12.sp, lineHeight = 17.sp)
                }
            }
        }

        item {
            Panel(Modifier.padding(horizontal = 18.dp)) {
                Column {
                    StepTitle(2, Copy.yourLyrics.of(lang), look.color, lang)
                    Spacer(Modifier.height(8.dp))
                    MutedText(Copy.fittedTo.of(lang).fill("song" to song.title, "bpm" to song.bpm, "n" to lyrics.syllablesPerLine))
                    Spacer(Modifier.height(10.dp))
                    GhostButton("↻  " + Copy.newChorus.of(lang), color = look.color) {
                        deps.fx.play(SoundFx.Fx.TAP)
                        variant++
                    }
                }
            }
        }

        item {
            Box(
                Modifier
                    .padding(horizontal = 18.dp)
                    .fillMaxWidth()
                    .clip(CardShape)
                    .border(1.dp, GlassBorder, CardShape)
            ) {
                SceneArt(Scenes.Aurora, seedOf(topic.id), Modifier.matchParentSize())
                Column(
                    Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(DeepNavy.copy(alpha = 0.35f), DeepNavy.copy(alpha = 0.85f))))
                        .padding(18.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StepDot(3, TextHi)
                        Spacer(Modifier.width(10.dp))
                        Text(Copy.makeIt.of(lang), color = TextHi, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        EngineCard(
                            title = "Suno AI",
                            subtitle = if (player.sunoReady) Copy.engineSunoSub.of(lang) else Copy.sunoNeedsKey.of(lang),
                            selected = engine == Engine.SUNO,
                            enabled = player.sunoReady,
                            modifier = Modifier.weight(1f),
                        ) { engine = Engine.SUNO }
                        EngineCard(
                            title = Copy.enginePhone.of(lang),
                            subtitle = Copy.enginePhoneSub.of(lang),
                            selected = engine == Engine.PHONE || !player.sunoReady,
                            enabled = true,
                            modifier = Modifier.weight(1f),
                        ) { engine = Engine.PHONE }
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(50))
                                .background(Accent)
                                .clickable { if (active) player.stop() else generate() }
                                .padding(horizontal = 24.dp, vertical = 14.dp)
                        ) {
                            Text(if (active) "■  " + Copy.stop.of(lang) else "▶  " + Copy.generate.of(lang), color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                        if (busy) {
                            Column(Modifier.weight(1f)) {
                                Text(player.status.orEmpty() + "…", color = TextHi, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                player.progress?.let {
                                    Spacer(Modifier.height(6.dp))
                                    ProgressBar(it, Accent)
                                }
                            }
                        } else {
                            Equalizer(active && player.playing, Accent)
                        }
                    }
                    if (active && player.playing) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            if (player.engineUsed == Engine.SUNO) "♪ Suno AI" else "♪ " + Copy.enginePhone.of(lang) + " · ${song.bpm} BPM",
                            color = TextLo,
                            fontSize = 13.sp,
                        )
                    }
                    player.notice?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(it, color = Gold, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp, start = 22.dp, end = 18.dp)) {
                Text(Copy.readAlong.of(lang), fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = TextHi)
                Spacer(Modifier.weight(1f))
                Pill(song.title, look.color, look.color.copy(alpha = 0.14f))
            }
        }

        var flat = 0
        lyrics.sections.forEach { section ->
            item { Box(Modifier.padding(start = 22.dp, top = 6.dp)) { SectionLabel(section.title, look.color) } }
            section.lines.forEach { line ->
                val index = flat++
                item { LyricLine(line, active && player.engineUsed == Engine.PHONE && player.currentLine == index, look.color) }
            }
        }
    }
}

@Composable
private fun SongRow(song: CatalogSong, genreName: String?, selected: Boolean, color: Color, lang: Lang, onClick: () -> Unit) {
    val key = "song-" + song.title
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) GlassStrong else Glass)
            .border(if (selected) 1.5.dp else 1.dp, if (selected) color else GlassBorder, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(14.dp))
        ) {
            SceneArt(Scenes.forKey(key), seedOf(key), Modifier.fillMaxSize(), lake = false)
            Text("♫", color = TextHi, fontSize = 20.sp, modifier = Modifier.align(Alignment.Center))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, fontWeight = FontWeight.SemiBold, color = TextHi, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(song.artist, color = TextLo, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                listOfNotNull(genreName, "${song.bpm} BPM", song.mood.ifBlank { null }).joinToString(" · "),
                color = TextFaint,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (song.publicDomain) {
                Spacer(Modifier.height(4.dp))
                Pill(Copy.traditional.of(lang), Gold, Gold.copy(alpha = 0.14f))
            }
        }
        if (selected) Text("✓", color = color, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun EngineCard(title: String, subtitle: String, selected: Boolean, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val on = selected && enabled
    Column(
        modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (on) Accent else Glass)
            .border(1.dp, if (on) Accent else GlassBorder, RoundedCornerShape(18.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(14.dp)
    ) {
        Text(title, color = if (on) DeepNavy else TextHi.copy(alpha = if (enabled) 1f else 0.55f), fontWeight = FontWeight.Bold)
        Text(subtitle, color = if (on) DeepNavy.copy(alpha = 0.75f) else TextFaint, fontSize = 12.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun StepTitle(n: Int, title: String, color: Color, lang: Lang) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepDot(n, color)
        Spacer(Modifier.width(10.dp))
        Column {
            Text(Copy.step.of(lang).fill("n" to n).uppercase(), color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Text(title, color = TextHi, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StepDot(n: Int, color: Color) {
    Box(
        Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center,
    ) { Text("$n", color = color, fontWeight = FontWeight.Bold) }
}

@Composable
private fun StepChip(n: Int, label: String, done: Boolean) {
    Text(
        (if (done) "✓ " else "$n · ") + label,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (done) Accent else DeepNavy.copy(alpha = 0.45f))
            .border(1.dp, if (done) Accent else GlassBorder, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        color = if (done) DeepNavy else TextHi,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
    )
}

@Composable
private fun LyricLine(text: String, active: Boolean, color: Color) {
    val bg by animateColorAsState(if (active) color.copy(alpha = 0.18f) else Color.Transparent, label = "lyricBg")
    Text(
        text,
        modifier = Modifier
            .padding(horizontal = 18.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        color = if (active) color else TextHi.copy(alpha = 0.88f),
        fontSize = if (active) 19.sp else 16.sp,
        fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
        lineHeight = 25.sp,
    )
}
