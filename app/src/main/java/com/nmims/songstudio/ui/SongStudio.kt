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

@Composable
fun SongStudio(student: Student, onLogout: () -> Unit, importRevision: Int = 0,
    importBusy: Boolean = false, importError: String = "", onImportAudio: (String) -> Unit = {}) = StudioTheme {
    val context = LocalContext.current
    val data = remember { Curriculum(context) }
    var page by rememberSaveable { mutableIntStateOf(1) }
    var unit by rememberSaveable { mutableStateOf("Unit 1") }
    var topicId by rememberSaveable { mutableIntStateOf(0) }
    var language by rememberSaveable { mutableStateOf("en") }
    var collection by rememberSaveable { mutableStateOf("en") }
    var styleId by rememberSaveable { mutableStateOf(data.styles("en").first().id) }
    var songIndex by rememberSaveable { mutableIntStateOf(0) }
    var focus by rememberSaveable { mutableStateOf("Understand the concept and remember the formula") }
    var tempo by rememberSaveable { mutableStateOf("Moderate") }
    var lyrics by rememberSaveable { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    val topic = data.topics.first { it.id == topicId }
    val styles = data.styles(collection)
    val style = styles.firstOrNull { it.id == styleId } ?: styles.first()
    val reference = style.songs[songIndex.coerceIn(style.songs.indices)]
    val titles = listOf("Welcome", "Your classroom", "Make it yours", "Write the words", "Bring it to life")
    BackHandler(page > 1) { page--; notice = "" }

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
                        unit = chosen; topicId = data.topics.first { it.unit == chosen }.id; lyrics = ""
                    }
                    val topics = data.topics.filter { it.unit == unit }
                    Select("Topic", topicId, topics.map { it.id }, { id -> topics.first { it.id == id }.title }) { topicId = it; lyrics = "" }
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
                    Select("Language", language, data.languages.keys.toList(), { data.languages.getValue(it) }) {
                        language = it; lyrics = ""
                    }
                    Select("Music collection", collection, data.collections.keys.toList(), { data.collections.getValue(it) }) {
                        collection = it; styleId = data.styles(it).first().id; songIndex = 0; lyrics = ""
                    }
                    Select("Music style", style.id, styles.map { it.id }, { id -> styles.first { it.id == id }.name }) {
                        styleId = it; songIndex = 0; lyrics = ""
                    }
                    Select("Reference song", songIndex, style.songs.indices.toList(), { style.songs[it].title }) { songIndex = it; lyrics = "" }
                    Panel(Lavender) {
                        Text(reference.title, style = MaterialTheme.typography.titleLarge)
                        Text(reference.artist)
                        Text("${reference.mood} · reference tempo ${reference.bpm} BPM")
                        Text(style.instruments)
                        Text("Selected song melody requested. Playback will be available only when a supported singing service returns that adaptation.", style = MaterialTheme.typography.bodySmall)
                    }
                    Select("Tempo", tempo, listOf("Slow", "Moderate", "Upbeat")) { tempo = it }
                    OutlinedTextField(focus, { focus = it }, label = { Text("What do you want to learn?") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { notice = ""; page = 3 }, modifier = Modifier.fillMaxWidth()) { Text("Continue to lyrics →") }
                }
                3 -> {
                    Text("Make the lesson\nlyrical.", style = MaterialTheme.typography.headlineLarge)
                    Text("${topic.title} / ${data.languages[language]} / ${style.name}")
                    Panel(Lime) {
                        Text("START WITH YOUR EXISTING WORK", style = MaterialTheme.typography.labelSmall)
                        Text("Create a lyric draft from the selected topic and review its meaning before turning it into music.")
                    }
                    Button(onClick = { lyrics = LyricPrompts.draft(topic, language); notice = "Draft prepared from the existing Calculus dataset. Review before synthesis." }, modifier = Modifier.fillMaxWidth()) {
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
                        Text("Import your finished educational song to listen here. Automatic music generation is not connected yet.")
                    }
                    val audioKey = listOf(student.id, topic.id, language, collection, style.id, reference.title, tempo, focus, lyrics).joinToString("\u0000")
                    val audioFile = remember(audioKey, importRevision) { AudioFiles.file(context, audioKey) }
                    if (audioFile.exists()) SongPlayer(audioFile, importRevision)
                    Button(onClick = { onImportAudio(audioKey) }, enabled = !importBusy, modifier = Modifier.fillMaxWidth()) {
                        Text(if (importBusy) "Importing song…" else if (audioFile.exists()) "Replace audio file" else "Import song audio")
                    }
                    Text("Choose a downloaded MP3, WAV or M4A of your educational song. It stays on this device and can be played offline.", style = MaterialTheme.typography.bodySmall)
                    if (importError.isNotBlank()) Text(importError, color = MaterialTheme.colorScheme.error)
                    OutlinedButton(onClick = { notice = ""; page = 3 }, modifier = Modifier.fillMaxWidth()) { Text("Edit lyrics") }
                    OutlinedButton(onClick = { notice = ""; page = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Choose another topic") }
                }
            }
            if (notice.isNotEmpty()) Text(notice, color = MaterialTheme.colorScheme.primary)
            if (page > 1) TextButton(onClick = { page--; notice = "" }) { Text("← Back") }
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
