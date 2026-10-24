package com.nmims.songstudio.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nmims.songstudio.data.Curriculum
import com.nmims.songstudio.auth.Student
import com.nmims.songstudio.generation.LyricPrompts
import com.nmims.songstudio.playback.AudioFiles
import com.nmims.songstudio.playback.SongPlayer
import com.nmims.songstudio.data.MusicChoices
import com.nmims.songstudio.generation.SingingClient
import kotlinx.coroutines.launch

@Composable
fun SongStudio(student: Student, onLogout: () -> Unit) = StudioTheme {
    val context = LocalContext.current
    val data = remember { Curriculum(context) }
    var page by rememberSaveable { mutableIntStateOf(1) }
    var unit by rememberSaveable { mutableStateOf("Unit 1") }
    var topicId by rememberSaveable { mutableIntStateOf(0) }
    var category by rememberSaveable { mutableStateOf("Hindi") }
    var showAllSongs by rememberSaveable { mutableStateOf(false) }
    var selectedSongId by rememberSaveable { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var generationStatus by remember { mutableStateOf("") }
    var generationError by remember { mutableStateOf("") }
    var audioRevision by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    var focus by rememberSaveable { mutableStateOf("Understand the concept and remember the formula") }
    var tempo by rememberSaveable { mutableStateOf("Moderate") }
    var lyrics by rememberSaveable { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    val topic = data.topics.first { it.id == topicId }
    val choices = MusicChoices.options(data, topic, category, showAllSongs)
    val choice = choices.firstOrNull { it.id == selectedSongId } ?: choices.first()
    val reference = choice.song
    val style = choice.style
    val language = if (category in listOf("Hindi", "Bhajan")) "hi" else "en"
    val titles = listOf("Welcome", "Your classroom", "Make it yours", "Write the words", "Bring it to life")
    BackHandler(page > 1) { if (!busy) { page--; notice = "" } }

    Surface(Modifier.fillMaxSize(), color = Paper) {
        Column(Modifier.safeDrawingPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SAT / SONG STUDIO", style = MaterialTheme.typography.labelSmall)
                TextButton(onClick = onLogout) { Text("Log out") }
            }
            if (page > 0) {
                LinearProgressIndicator(progress = { page / 4f }, modifier = Modifier.fillMaxWidth(), color = Ink, trackColor = Lime)
                Text("STEP $page OF 4  /  ${titles[page].uppercase()}", style = MaterialTheme.typography.labelSmall)
            }
            when (page) {
                1 -> {
                    Text("What are we\nlearning, ${student.name.substringBefore(' ')}?", style = MaterialTheme.typography.headlineLarge)
                    Panel(Lime) {
                        Text("YOUR AVAILABLE COURSE", style = MaterialTheme.typography.labelSmall)
                        Text("Engineering Mathematics I", style = MaterialTheme.typography.titleLarge)
                        Text("Calculus · 702BS0C001")
                        Text("Branch, year and semester mappings await the team's verified academic dataset.", style = MaterialTheme.typography.bodySmall)
                    }
                    Select("Subject", "Calculus", listOf("Calculus")) { }
                    Select("Chapter / unit", unit, data.units.keys.toList(), { "${it}: ${data.units[it]}" }) { chosen ->
                        unit = chosen; topicId = data.topics.first { it.unit == chosen }.id; lyrics = ""; selectedSongId = ""; showAllSongs = false
                    }
                    val topics = data.topics.filter { it.unit == unit }
                    Select("Topic", topicId, topics.map { it.id }, { id -> topics.first { it.id == id }.title }) { topicId = it; lyrics = ""; selectedSongId = ""; showAllSongs = false }
                    Panel(Lavender) {
                        Text(topic.title, style = MaterialTheme.typography.titleLarge)
                        Text(topic.concept)
                        Text(topic.formula, fontWeight = FontWeight.SemiBold)
                    }
                    Button(onClick = { page = 2 }, modifier = Modifier.fillMaxWidth()) { Text("Choose your sound →") }
                }
                2 -> {
                    Text("Your topic.\nYour kind of music.", style = MaterialTheme.typography.headlineLarge)
                    Text("${topic.unit} / ${topic.title}", style = MaterialTheme.typography.bodyMedium)
                    Text("Choose your music", style = MaterialTheme.typography.titleLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        MusicChoices.categories.forEach { option ->
                            FilterChip(selected = category == option, onClick = {
                                category = option; selectedSongId = ""; showAllSongs = false; lyrics = ""
                            }, label = { Text(option) })
                        }
                    }
                    Select("Song suggestions for this topic", choice.id, choices.map { it.id }, { id -> choices.first { it.id == id }.song.title }) {
                        selectedSongId = it; lyrics = ""; generationError = ""
                    }
                    TextButton(onClick = { showAllSongs = !showAllSongs; selectedSongId = ""; lyrics = "" }) {
                        Text(if (showAllSongs) "Show topic recommendations" else "Browse all $category songs")
                    }
                    Panel(Lavender) {
                        Text(reference.title, style = MaterialTheme.typography.titleLarge)
                        Text(reference.artist)
                        Text("${reference.mood} · target tempo ${reference.bpm} BPM")
                        Text(style.instruments)
                        Text("${if (category == "Bhajan") "Hindi / Sanskrit devotional lyrics" else data.languages[language]} · ${style.name}")
                    }
                    Select("Tempo", tempo, listOf("Slow", "Moderate", "Upbeat")) { tempo = it }
                    OutlinedTextField(focus, { focus = it }, label = { Text("What do you want to learn?") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { notice = ""; page = 3 }, modifier = Modifier.fillMaxWidth()) { Text("Continue to lyrics →") }
                }
                3 -> {
                    Text("Make the lesson\nlyrical.", style = MaterialTheme.typography.headlineLarge)
                    Text("${topic.title} / ${data.languages[language]} / $category / ${reference.title}")
                    Panel(Lime) {
                        Text("START WITH YOUR EXISTING WORK", style = MaterialTheme.typography.labelSmall)
                        Text("Create an original draft from the topic facts, with phrasing and a refrain for your selected music. Hindi and Bhajan produce different language and lyric forms.")
                    }
                    Button(onClick = { lyrics = LyricPrompts.draft(topic, category, choice); notice = "Lyric draft adapted for ${reference.title}. Review the academic meaning before singing." }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (lyrics.isBlank()) "Generate lyric draft" else "Regenerate lyric draft")
                    }
                    OutlinedTextField(lyrics, { lyrics = it }, label = { Text("Your lyrics — editable") }, minLines = 10, modifier = Modifier.fillMaxWidth())
                    Text("Check formulas, theorem conditions and pronunciation. Translations in the original prototype still need subject review.", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { notice = ""; page = 4 }, enabled = lyrics.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Continue to song →") }
                }
                4 -> {
                    Text("From words\nto a study song.", style = MaterialTheme.typography.headlineLarge)
                    Panel(Lavender) {
                        Text("YOUR SONG", style = MaterialTheme.typography.labelSmall)
                        Text(topic.title, style = MaterialTheme.typography.titleLarge)
                        Text("Requested melody: ${reference.title} — ${reference.artist}")
                        Text("${data.languages[language]} · ${style.name} · $tempo")
                        Text("Generate vocals and music here, then listen in the app.")
                    }
                    val audioKey = listOf(student.id, topic.id, category, reference.title, tempo, focus, lyrics).joinToString("\u0000")
                    val audioFile = remember(audioKey, audioRevision) { AudioFiles.file(context, audioKey) }
                    if (audioFile.exists()) {
                        val mode = runCatching { audioFile.resolveSibling(audioFile.name + ".mode").readText() }.getOrDefault("original_melody")
                        Text(if (mode == "reference_cover") "Generated using a supplied melody reference" else "AI-generated original melody in the selected style", style = MaterialTheme.typography.bodySmall)
                        SongPlayer(audioFile, audioRevision)
                    }
                    if (busy) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        Text(generationStatus.ifBlank { "Connecting to the singing model…" })
                    }
                    Button(enabled = !busy, onClick = {
                        busy = true; generationError = ""; generationStatus = "Connecting to the singing model…"
                        scope.launch {
                            try {
                                SingingClient.generate(context.applicationContext, audioKey, topic, category, choice, lyrics, tempo) { generationStatus = it }
                                audioRevision++; generationStatus = "Your song is ready. Press Play song."
                            } catch (e: Exception) { generationError = e.message ?: "Could not generate the song. Please retry." }
                            finally { busy = false }
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text(if (busy) "Creating your song…" else "Generate song") }
                    Text("Exact tune matching needs the selected song's melody dataset. Until supplied, generation creates an original melody in that musical style.", style = MaterialTheme.typography.bodySmall)
                    if (generationError.isNotBlank()) Text(generationError, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(enabled = !busy, onClick = { notice = ""; page = 3 }, modifier = Modifier.fillMaxWidth()) { Text("Edit lyrics") }
                    OutlinedButton(enabled = !busy, onClick = { notice = ""; page = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Choose another topic") }
                }
            }
            if (notice.isNotEmpty()) Text(notice, color = MaterialTheme.colorScheme.primary)
            if (page > 1) TextButton(enabled = !busy, onClick = { page--; notice = "" }) { Text("← Back") }
            HorizontalDivider()
            Text("MUSIC-BASED LEARNING / CALCULUS EDITION", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun Panel(color: androidx.compose.ui.graphics.Color, content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().background(color, RoundedCornerShape(22.dp)).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
}

@Composable
fun <T> Select(label: String, selected: T, values: List<T>, display: (T) -> String = { it.toString() }, onSelect: (T) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge)
        Box {
            OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp), shape = RoundedCornerShape(12.dp)) {
                Text(display(selected), modifier = Modifier.weight(1f))
                Text("⌄")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.heightIn(max = 360.dp)) {
                values.forEach { value -> DropdownMenuItem(text = { Text(display(value)) }, onClick = { onSelect(value); expanded = false }) }
            }
        }
    }
}
