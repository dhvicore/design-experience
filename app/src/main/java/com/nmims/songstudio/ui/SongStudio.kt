package com.nmims.songstudio.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.nmims.songstudio.data.SongPreferences
import com.nmims.songstudio.generation.LyricPrompts

@Composable
fun SongStudio() = StudioTheme {
    val context = LocalContext.current
    val data = remember { Curriculum(context) }
    var page by rememberSaveable { mutableIntStateOf(0) }
    var name by rememberSaveable { mutableStateOf("") }
    var sap by rememberSaveable { mutableStateOf("") }
    var unit by rememberSaveable { mutableStateOf("Unit 1") }
    var topicId by rememberSaveable { mutableIntStateOf(0) }
    var language by rememberSaveable { mutableStateOf("en") }
    var styleId by rememberSaveable { mutableStateOf(data.styles("en").first().id) }
    var songIndex by rememberSaveable { mutableIntStateOf(0) }
    var focus by rememberSaveable { mutableStateOf("Understand the concept and remember the formula") }
    var tempo by rememberSaveable { mutableStateOf("Moderate") }
    var lyrics by rememberSaveable { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }
    val topic = data.topics.first { it.id == topicId }
    val styles = data.styles(language)
    val style = styles.firstOrNull { it.id == styleId } ?: styles.first()
    val reference = style.songs[songIndex.coerceIn(style.songs.indices)]
    val preferences = SongPreferences(language, style, reference, focus, tempo)
    val titles = listOf("Welcome", "Your classroom", "Make it yours", "Write the words", "Bring it to life")
    fun copy(text: String) {
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager)
            .setPrimaryClip(ClipData.newPlainText("Song Studio", text))
        notice = "Copied."
    }
    fun open(url: String) {
        runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            .onFailure { notice = "No browser available. Open $url on your computer." }
    }
    BackHandler(page > 0) { page--; notice = "" }

    Surface(Modifier.fillMaxSize(), color = Paper) {
        Column(Modifier.safeDrawingPadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SAT / SONG STUDIO", style = MaterialTheme.typography.labelSmall)
                Text("NMIMS · STUDENT PROJECT", style = MaterialTheme.typography.labelSmall)
            }
            if (page > 0) {
                LinearProgressIndicator(progress = { page / 4f }, modifier = Modifier.fillMaxWidth(), color = Ink, trackColor = Lime)
                Text("STEP $page OF 4  /  ${titles[page].uppercase()}", style = MaterialTheme.typography.labelSmall)
            }
            when (page) {
                0 -> {
                    Spacer(Modifier.height(12.dp))
                    Text("A little rhythm.\nA lot of learning.", style = MaterialTheme.typography.headlineLarge)
                    Text("Turn your syllabus into something that stays with you.", style = MaterialTheme.typography.bodyLarge)
                    Panel(Lime) {
                        Text("CALCULUS, ALREADY HERE", style = MaterialTheme.typography.labelSmall)
                        Text("5 units. 38 topics. Your next study soundtrack.", style = MaterialTheme.typography.titleLarge)
                        Text("Built from the syllabus and song collection in your original prototype.")
                    }
                    Text("Enter the demo studio", style = MaterialTheme.typography.titleLarge)
                    OutlinedTextField(name, { name = it }, label = { Text("Your name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(sap, { sap = it }, label = { Text("SAP ID (optional, unverified)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Text("Demo access only. College portal authentication will be added separately.", style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { page = 1 }, enabled = name.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Enter studio →") }
                }
                1 -> {
                    Text("What are we\nlearning, ${name.trim().substringBefore(' ')}?", style = MaterialTheme.typography.headlineLarge)
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
                        language = it; styleId = data.styles(it).first().id; songIndex = 0; lyrics = ""
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
                        Text("A sound reference. The generated melody will be original.", style = MaterialTheme.typography.bodySmall)
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
                        Text("Load the topic's stored lyrics, edit them, or copy a grounded prompt for an AI draft. Live AI generation is not connected yet.")
                    }
                    Button(onClick = { lyrics = LyricPrompts.draft(topic, language); notice = "Loaded from your prototype. Review before synthesis." }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (lyrics.isBlank()) "Load syllabus lyric draft" else "Reset to syllabus draft")
                    }
                    OutlinedTextField(lyrics, { lyrics = it }, label = { Text("Your lyrics — editable") }, minLines = 10, modifier = Modifier.fillMaxWidth())
                    Text("Check formulas, theorem conditions and pronunciation. Translations in the original prototype still need subject review.", style = MaterialTheme.typography.bodySmall)
                    OutlinedButton(onClick = { copy(LyricPrompts.educational(topic, preferences, data.languages.getValue(language))) }, modifier = Modifier.fillMaxWidth()) { Text("Copy AI lyric prompt") }
                    Button(onClick = { notice = ""; page = 4 }, enabled = lyrics.isNotBlank(), modifier = Modifier.fillMaxWidth()) { Text("Review synthesis options →") }
                }
                4 -> {
                    Text("From words\nto a study song.", style = MaterialTheme.typography.headlineLarge)
                    Panel(Lavender) {
                        Text("STUDIO VOICE / MANUAL HANDOFF", style = MaterialTheme.typography.labelSmall)
                        Text("Make your song in Suno", style = MaterialTheme.typography.titleLarge)
                        Text("Copy the style and lyrics into Custom mode. This opens Suno; it does not generate audio inside this app.")
                        OutlinedButton(onClick = { copy(LyricPrompts.music(preferences, data.languages.getValue(language))) }) { Text("Copy music style") }
                        OutlinedButton(onClick = { copy(lyrics) }) { Text("Copy approved lyrics") }
                        Button(onClick = { open("https://suno.com/create") }) { Text("Open Suno to synthesize ↗") }
                    }
                    if (topic.title == "Rolle's Theorem") {
                        Panel(Lime) {
                            Text("EXISTING SUNO EXAMPLE", style = MaterialTheme.typography.labelSmall)
                            Text("Rolle's Theorem · English", style = MaterialTheme.typography.titleLarge)
                            Text("The natural-sounding song from your prototype. This prepared example has its own fixed lyrics and style.")
                            Button(onClick = { open("https://suno.com/song/38715c7a-f1c9-499e-b519-487c015b11a5") }) { Text("Listen on Suno ↗") }
                        }
                        if (language in listOf("en", "hi")) PreviewPlayer(language)
                    } else {
                        Text("No prepared audio is bundled for this topic. Your lyrics and music-style prompt are ready for the manual synthesis step.")
                    }
                    OutlinedButton(onClick = { notice = ""; page = 3 }, modifier = Modifier.fillMaxWidth()) { Text("Edit lyrics") }
                    OutlinedButton(onClick = { notice = ""; page = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Choose another topic") }
                }
            }
            if (notice.isNotEmpty()) Text(notice, color = MaterialTheme.colorScheme.primary)
            if (page > 0) TextButton(onClick = { page--; notice = "" }) { Text("← Back") }
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
