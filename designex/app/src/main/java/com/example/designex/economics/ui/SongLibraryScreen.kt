package com.example.designex.economics.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designex.economics.audio.SoundFx
import com.example.designex.economics.data.Song
import com.example.designex.ui.theme.Accent
import com.example.designex.ui.theme.TextHi
import com.example.designex.ui.theme.TextLo

@Composable
fun SongLibraryScreen(deps: AppDeps, open: (Route) -> Unit) {
    val catalog = deps.catalog
    val lang = deps.progress.language
    val context = LocalContext.current
    val subject = catalog.subject(deps.progress.subjectId)
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf<Song?>(null) }
    val genres = remember { catalog.librarySongs.map { it.genre }.distinct() }
    val filters = listOf(Copy.all.of(lang), "English", "Hindi") + genres
    val shown = remember(query, filter) {
        val q = query.trim().lowercase()
        catalog.librarySongs.filter { s ->
            val passFilter = when {
                filter == 0 -> true
                filter == 1 -> s.language == "English"
                filter == 2 -> s.language == "Hindi"
                else -> s.genre == genres[filter - 3]
            }
            passFilter && (q.isEmpty() || s.title.lowercase().contains(q) || s.artist.lowercase().contains(q) || s.movie.lowercase().contains(q))
        }
    }
    val tap = { deps.fx.play(SoundFx.Fx.TAP) }
    val featured = shown.firstOrNull()
    val rest = shown.drop(1).chunked(2)

    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 120.dp),
    ) {
        item {
            Column {
                ScreenTitle(Copy.songLibrary.of(lang))
                Text(Copy.songsCount.of(lang), color = TextLo, fontSize = 15.sp)
                Spacer(Modifier.height(14.dp))
                ChipRow(filters, filter) { tap(); filter = it; expanded = null }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; expanded = null },
                    placeholder = { Text(Copy.search.of(lang), color = TextLo) },
                    singleLine = true,
                    colors = glassFieldColors(),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
            }
        }

        if (featured != null) {
            item {
                SceneCard(
                    title = featured.title,
                    subtitle = "${featured.artist} · ${featured.genre} · ${featured.year}",
                    key = "song-" + featured.title,
                    badge = "★ " + Copy.featured.of(lang),
                    height = 200.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) { tap(); expanded = if (expanded == featured) null else featured }
                if (expanded == featured) SongDetail(featured, deps, subject, lang, context, open, tap)
                Spacer(Modifier.height(12.dp))
            }
        }

        itemsIndexed(rest) { _, pair ->
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    pair.forEach { song ->
                        SceneCard(
                            title = song.title,
                            subtitle = song.artist,
                            key = "song-" + song.title,
                            badge = "#${song.rank} · ${song.language}",
                            height = 170.dp,
                            modifier = Modifier.weight(1f),
                        ) { tap(); expanded = if (expanded == song) null else song }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
                pair.firstOrNull { it == expanded }?.let { SongDetail(it, deps, subject, lang, context, open, tap) }
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SongDetail(
    song: Song,
    deps: AppDeps,
    subject: com.example.designex.economics.data.Subject,
    lang: com.example.designex.economics.data.Lang,
    context: android.content.Context,
    open: (Route) -> Unit,
    tap: () -> Unit,
) {
    Spacer(Modifier.height(10.dp))
    Panel {
        Column {
            Text(song.title, color = TextHi, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Text(
                listOf(song.artist, song.language, song.genre, song.year.toString()).plus(listOfNotNull(song.movie.ifBlank { null })).joinToString(" · "),
                color = TextLo,
                fontSize = 13.sp,
            )
            val used = deps.catalog.topicsUsingSong(subject, song.title)
            if (used.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                SectionLabel(Copy.usedIn.of(lang))
                Spacer8()
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    used.forEach { t -> GhostButton(t.name[lang] + "  ›", color = Accent) { open(Route.TopicPage(t.id)) } }
                }
            }
            Spacer(Modifier.height(12.dp))
            ListenButtons("${song.title} ${song.artist}", context, tap)
        }
    }
}
