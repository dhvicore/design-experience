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
import com.nmims.songstudio.generation.MelodyReference
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
    var melodyReferences by remember { mutableStateOf<Map<String, MelodyReference>>(emptyMap()) }
    var melodyError by remember { mutableStateOf("") }
    var preferReference by rememberSaveable { mutableStateOf(true) }
    suspend fun loadMelodies() {
        try { melodyReferences = SingingClient.melodies(); melodyError = "" }
        catch (_: Exception) { melodyError = "Music Server is unavailable. Start it on your Mac, then reconnect." }
    }
    LaunchedEffect(Unit) { loadMelodies() }
    var focus by rememberSaveable { mutableStateOf("Understand the concept and remember the formula") }
    var tempo by rememberSaveable { mutableStateOf("Moderate") }
    var lyrics by rememberSaveable { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    val topic = data.topics.first { it.id == topicId }
    val choices = MusicChoices.options(data, topic, category, showAllSongs)
    val choice = choices.firstOrNull { it.id == selectedSongId } ?: choices.first()
    val reference = choice.song
    val style = choice.style
    val melody = melodyReferences[choice.id]
    val useReference = preferReference && melody != null
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
                    if (melody != null) {
                        Text("Melody reference available · ${melody.bpm} BPM", style = MaterialTheme.typography.bodySmall)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(selected = preferReference, onClick = { preferReference = true; lyrics = "" }, label = { Text("Follow melody") })
                            FilterChip(selected = !preferReference, onClick = { preferReference = false; lyrics = "" }, label = { Text("Original style") })
                        }
                        if (useReference) Text("Uses the supplied instrumental melody guide and its tempo. Timing is approximate; listen to the generated result.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text("Original music in this style. This song's melody reference has not been added.", style = MaterialTheme.typography.bodySmall)
                    }
                    if (melodyError.isNotBlank()) {
                        Text(melodyError, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = { scope.launch { loadMelodies() } }) { Text("Reconnect Music Server") }
                    }
                    if (!useReference) Select("Tempo", tempo, listOf("Slow", "Moderate", "Upbeat")) { tempo = it }
                    OutlinedTextField(focus, { focus = it }, label = { Text("What do you want to learn?") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { notice = ""; page = 3 }, modifier = Modifier.fillMaxWidth()) { Text("Continue to lyrics →") }
                }
                3 -> {
                    Text("Make the lesson\nlyrical.", style = MaterialTheme.typography.headlineLarge)
                    Text("${topic.title} / ${data.languages[language]} / $category / ${reference.title}")
                    Panel(Lime) {
                        Text(if (useReference) "FIT WORDS TO MUSICAL PHRASES" else "YOUR EDUCATIONAL LYRICS", style = MaterialTheme.typography.labelSmall)
                        Text(if (useReference) "Fit the topic lyrics to the supplied melody’s phrase lengths, then review and edit before singing." else "Create a draft from the topic facts in your selected language and musical style.")
                    }
                    Button(enabled = !busy, onClick = {
                        busy = true; notice = ""; generationError = ""
                        scope.launch {
                            try {
                                val draft = LyricPrompts.draft(topic, category, choice)
                                if (useReference) {
                                    val adapted = SingingClient.adapt(topic, category, choice, draft, true)
                                    lyrics = adapted.lyrics; notice = adapted.note
                                } else {
                                    lyrics = draft; notice = "Draft ready. Review the academic meaning before singing."
                                }
                            } catch (e: Exception) { generationError = e.message ?: "Could not prepare lyrics. Check the Music Server." }
                            finally { busy = false }
                        }
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (busy) "Fitting lyric phrases…" else if (lyrics.isBlank()) "Generate lyric draft" else "Regenerate lyric draft")
                    }
                    if (generationError.isNotBlank()) Text(generationError, color = MaterialTheme.colorScheme.error)
                    OutlinedTextField(lyrics, { lyrics = it }, enabled = !busy, label = { Text("Your lyrics — editable") }, minLines = 10, modifier = Modifier.fillMaxWidth())
                    Text("Check formulas, theorem conditions and pronunciation. Translations in the original prototype still need subject review.", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { notice = ""; page = 4 }, enabled = lyrics.isNotBlank() && !busy, modifier = Modifier.fillMaxWidth()) { Text("Continue to song →") }
                }
                4 -> {
                    Text("From words\nto a study song.", style = MaterialTheme.typography.headlineLarge)
                    Panel(Lavender) {
                        Text("YOUR SONG", style = MaterialTheme.typography.labelSmall)
                        Text(topic.title, style = MaterialTheme.typography.titleLarge)
                        Text("${if (useReference) "Melody guide" else "Style reference"}: ${reference.title} — ${reference.artist}")
                        Text("${data.languages[language]} · ${style.name} · ${if (useReference) "${melody?.bpm} BPM" else tempo}")
                        Text("Generate vocals and music here, then listen in the app.")
                    }
                    val audioKey = listOf(student.id, topic.id, category, reference.title, tempo, focus, lyrics, useReference, if (useReference) melody?.sha else "original-v3").joinToString("\u0000")
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
                                SingingClient.generate(context.applicationContext, audioKey, topic, category, choice, lyrics, tempo, useReference) { generationStatus = it }
                                audioRevision++; generationStatus = "Your song is ready. Press Play song."
                            } catch (e: Exception) { generationError = e.message ?: "Could not generate the song. Please retry." }
                            finally { busy = false }
                        }
                    }, modifier = Modifier.fillMaxWidth()) { Text(if (busy) "Creating your song…" else "Generate song") }
                    Text(if (useReference) "The singing model receives the melody audio and your fitted lyrics. Melody fidelity and pronunciation can vary." else "An original melody will be composed. To follow a specific tune, its melody reference must be in the library.", style = MaterialTheme.typography.bodySmall)
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
