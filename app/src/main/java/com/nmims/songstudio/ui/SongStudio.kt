package com.nmims.songstudio.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nmims.songstudio.data.Curriculum
import com.nmims.songstudio.data.ChemistryCurriculum
import com.nmims.songstudio.data.ChemistryTopic
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
    val chemData = remember { ChemistryCurriculum(context) }
    var page by rememberSaveable { mutableIntStateOf(1) }
    var subject by rememberSaveable { mutableStateOf("Calculus") }

    // Calculus selections
    var unit by rememberSaveable { mutableStateOf("Unit 1") }
    var topicId by rememberSaveable { mutableIntStateOf(0) }
    var category by rememberSaveable { mutableStateOf("Hindi") }
    var choosingSong by rememberSaveable { mutableStateOf(false) }
    var showAllSongs by rememberSaveable { mutableStateOf(false) }
    var selectedSongId by rememberSaveable { mutableStateOf("") }
    var preferReference by rememberSaveable { mutableStateOf(true) }

    // Chemistry selections
    var chemUnit by rememberSaveable { mutableStateOf(chemData.units.keys.first()) }
    var chemTopicId by rememberSaveable { mutableIntStateOf(chemData.topics.first().songId) }
    var chemLanguage by rememberSaveable { mutableStateOf("en") }

    // Shared customizations
    var tempo by rememberSaveable { mutableStateOf("Moderate") }
    var focus by rememberSaveable { mutableStateOf("Understand the concept and remember the formula") }
    var lyrics by rememberSaveable { mutableStateOf("") }
    var notice by remember { mutableStateOf("") }

    // Server health and melodies
    var isCheckingServer by remember { mutableStateOf(false) }
    var serverConnected by remember { mutableStateOf<Boolean?>(null) }
    var serverProvider by remember { mutableStateOf("") }
    var serverError by remember { mutableStateOf("") }
    var melodyReferences by remember { mutableStateOf<Map<String, MelodyReference>>(emptyMap()) }

    var busy by remember { mutableStateOf(false) }
    var generationStatus by remember { mutableStateOf("") }
    var generationError by remember { mutableStateOf("") }
    var audioRevision by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    suspend fun checkServerHealth() {
        if (isCheckingServer) return
        isCheckingServer = true
        serverError = ""
        try {
            val health = SingingClient.checkHealth()
            if (health.ok) {
                serverConnected = true
                serverProvider = health.provider
                serverError = ""
                try {
                    melodyReferences = SingingClient.melodies()
                } catch (_: Exception) {
                    // Melodies might fail or be empty, but server is responding
                }
            } else {
                serverConnected = false
                serverProvider = ""
                serverError = "Music server is unavailable. Ensure the local music bridge is started separately on your development computer, then reconnect."
            }
        } catch (e: Exception) {
            serverConnected = false
            serverProvider = ""
            serverError = "Music server is unavailable. Ensure the local music bridge is started separately on your development computer, then reconnect."
        } finally {
            isCheckingServer = false
        }
    }

    LaunchedEffect(Unit) { checkServerHealth() }

    // Current selections depend on subject
    val isChemistry = subject == "Chemistry"
    val topic = if (!isChemistry) (data.topics.firstOrNull { it.id == topicId } ?: data.topics.first()) else null
    val chemTopic = if (isChemistry) (chemData.topics.firstOrNull { it.songId == chemTopicId } ?: chemData.topics.first()) else null

    // Music choices are only available for Calculus
    val choices = if (!isChemistry) MusicChoices.options(data, topic!!, category, showAllSongs) else emptyList()
    val choice = if (!isChemistry) (choices.firstOrNull { it.id == selectedSongId } ?: choices.first()) else null
    val reference = choice?.song
    val style = choice?.style
    val melody = if (!isChemistry) melodyReferences[choice?.id] else null
    val useReference = !isChemistry && preferReference && melody != null
    val language = if (isChemistry) chemLanguage else if (category in listOf("Hindi", "Bhajan")) "hi" else "en"

    val titles = listOf("Welcome", "Your classroom", "Make it yours", "Write the words", "Bring it to life")

    fun goBack() {
        if (page == 2 && choosingSong && !isChemistry) {
            choosingSong = false
        } else if (page > 1) {
            page--
        }
        notice = ""
        generationError = ""
    }

    BackHandler(page > 1) { if (!busy) goBack() }

    val editionLabel = if (isChemistry) "CHEMISTRY EDITION" else "CALCULUS EDITION"

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
                        Text("YOUR AVAILABLE COURSES", style = MaterialTheme.typography.labelSmall)
                        Text(if (isChemistry) "Applied Chemistry" else "Engineering Mathematics I", style = MaterialTheme.typography.titleLarge)
                        Text(if (isChemistry) "Chemistry · 11 units · 60 topics" else "Calculus · 5 units · 38 topics")
                        Text("Select your subject, chapter and topic below to begin.", style = MaterialTheme.typography.bodySmall)
                    }
                    Select("Subject", subject, listOf("Calculus", "Chemistry")) { chosen ->
                        if (subject != chosen) {
                            subject = chosen
                            lyrics = ""; selectedSongId = ""; showAllSongs = false; choosingSong = false
                            if (chosen == "Chemistry") {
                                val firstInChem = chemData.topics.first()
                                chemUnit = firstInChem.unit
                                chemTopicId = firstInChem.songId
                            } else {
                                unit = "Unit 1"
                                topicId = data.topics.first { it.unit == unit }.id
                            }
                        }
                    }
                    if (isChemistry) {
                        // Chemistry unit selection
                        val chemUnits = chemData.units.keys.toList()
                        val currentChemUnit = chemTopic!!.unit
                        Select("Chapter / unit", currentChemUnit, chemUnits) { chosen ->
                            if (currentChemUnit != chosen) {
                                chemUnit = chosen
                                val firstInUnit = chemData.topicsForUnit(chosen).first()
                                chemTopicId = firstInUnit.songId
                                lyrics = ""; selectedSongId = ""; showAllSongs = false
                            }
                        }
                        // Chemistry topic selection
                        val unitTopics = chemData.topicsForUnit(chemTopic.unit)
                        Select("Topic", chemTopicId, unitTopics.map { it.songId }, { id ->
                            val t = unitTopics.firstOrNull { it.songId == id } ?: unitTopics.first()
                            t.topicName[chemLanguage] ?: t.topicName["en"]!!
                        }) {
                            if (chemTopicId != it) {
                                chemTopicId = it
                                lyrics = ""; selectedSongId = ""; showAllSongs = false
                            }
                        }
                        // Chemistry topic detail panel
                        Panel(Lavender) {
                            Text(chemTopic.topicName[chemLanguage] ?: chemTopic.topicName["en"]!!, style = MaterialTheme.typography.titleLarge)
                            Text(chemTopic.explanation[chemLanguage] ?: chemTopic.explanation["en"]!!)
                            Text("Keywords: ${chemTopic.keywords[chemLanguage] ?: chemTopic.keywords["en"]!!}", fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        // Calculus flow
                        Select("Chapter / unit", unit, data.units.keys.toList(), { "${it}: ${data.units[it]}" }) { chosen ->
                            if (unit != chosen) {
                                unit = chosen
                                topicId = data.topics.first { it.unit == chosen }.id
                                lyrics = ""; selectedSongId = ""; showAllSongs = false
                            }
                        }
                        val topics = data.topics.filter { it.unit == unit }
                        Select("Topic", topicId, topics.map { it.id }, { id -> topics.firstOrNull { it.id == id }?.title ?: "" }) {
                            if (topicId != it) {
                                topicId = it
                                lyrics = ""; selectedSongId = ""; showAllSongs = false
                            }
                        }
                        Panel(Lavender) {
                            Text(topic!!.title, style = MaterialTheme.typography.titleLarge)
                            Text(topic.concept)
                            Text(topic.formula, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Button(onClick = {
                        choosingSong = false
                        notice = ""
                        page = 2
                    }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (isChemistry) "Customize song & language →" else "Choose your sound →")
                    }
                }
                2 -> {
                    // Language & Music customization
                    if (isChemistry) {
                        val ct = chemTopic!!
                        Text("Customize your song\n& language.", style = MaterialTheme.typography.headlineLarge)
                        Text("${ct.unit} / ${ct.topicName[chemLanguage] ?: ct.topicName["en"]!!}", style = MaterialTheme.typography.bodyMedium)

                        // Chemistry educational language picker
                        Select("Educational language", chemLanguage, chemData.languages.keys.toList(), { chemData.languages[it] ?: it }) { chosen ->
                            if (chemLanguage != chosen) {
                                chemLanguage = chosen
                                lyrics = "" // Reset lyrics so Step 3 automatically loads the new language lyrics
                            }
                        }

                        // Chemistry song reference details
                        Panel(Lavender) {
                            Text("SONG REFERENCE", style = MaterialTheme.typography.labelSmall)
                            Text(ct.songTitle[chemLanguage] ?: ct.songTitle["en"]!!, style = MaterialTheme.typography.titleLarge)
                            Text(ct.artistName[chemLanguage] ?: ct.artistName["en"]!!)
                            Text(ct.lyricsApproach, style = MaterialTheme.typography.bodySmall)
                        }

                        // Customization: Tempo and Focus
                        Select("Tempo", tempo, listOf("Slow", "Moderate", "Upbeat")) { tempo = it }
                        OutlinedTextField(focus, { focus = it }, label = { Text("What do you want to learn?") }, minLines = 2, modifier = Modifier.fillMaxWidth())

                        // Server connection status & recovery
                        ServerStatusCard(
                            serverConnected = serverConnected,
                            isChecking = isCheckingServer,
                            provider = serverProvider,
                            errorMessage = serverError,
                            onReconnect = { scope.launch { checkServerHealth() } }
                        )

                        Button(onClick = { notice = ""; page = 3 }, modifier = Modifier.fillMaxWidth()) {
                            Text("Continue to lyrics →")
                        }
                    } else {
                        Text(if (choosingSong) "Choose a $category song." else "Choose your music category.", style = MaterialTheme.typography.headlineLarge)
                        Text("${topic!!.unit} / ${topic.title}", style = MaterialTheme.typography.bodyMedium)
                        if (!choosingSong) {
                            Text("First choose a category. Then choose one of its songs.")
                            MusicChoices.categories.forEach { option ->
                                OutlinedButton(onClick = {
                                    category = option; selectedSongId = ""; showAllSongs = false
                                    lyrics = ""; generationError = ""; choosingSong = true
                                }, modifier = Modifier.fillMaxWidth(), contentPadding = PaddingValues(20.dp)) {
                                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(option, style = MaterialTheme.typography.titleLarge)
                                        Text(when (option) {
                                            "Hindi" -> "Bollywood / Hindi songs · Hindi lyrics"
                                            "English" -> "English songs · English lyrics"
                                            "Bhajan" -> "Devotional songs · Hindi / Sanskrit phrases"
                                            else -> "Relaxed music · English lyrics"
                                        }, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        } else {
                            TextButton(onClick = { choosingSong = false }) { Text("← Change category ($category)") }
                            Select("Song suggestions for this topic", choice!!.id, choices.map { it.id }, { id -> choices.first { it.id == id }.song.title }) {
                                selectedSongId = it; lyrics = ""; generationError = ""
                            }
                            TextButton(onClick = { showAllSongs = !showAllSongs; selectedSongId = ""; lyrics = "" }) {
                                Text(if (showAllSongs) "Show topic recommendations" else "Browse all $category songs")
                            }
                            Panel(Lavender) {
                                Text(reference!!.title, style = MaterialTheme.typography.titleLarge)
                                Text(reference.artist)
                                Text("${reference.mood} · target tempo ${reference.bpm} BPM")
                                Text(style!!.instruments)
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

                            if (!useReference) Select("Tempo", tempo, listOf("Slow", "Moderate", "Upbeat")) { tempo = it }
                            OutlinedTextField(focus, { focus = it }, label = { Text("What do you want to learn?") }, minLines = 2, modifier = Modifier.fillMaxWidth())

                            // Server connection status & recovery
                            ServerStatusCard(
                                serverConnected = serverConnected,
                                isChecking = isCheckingServer,
                                provider = serverProvider,
                                errorMessage = serverError,
                                onReconnect = { scope.launch { checkServerHealth() } }
                            )

                            Button(onClick = { notice = ""; page = 3 }, modifier = Modifier.fillMaxWidth()) { Text("Continue to lyrics →") }
                        }
                    }
                }
                3 -> {
                    Text("Make the lesson\nlyrical.", style = MaterialTheme.typography.headlineLarge)
                    if (isChemistry) {
                        val ct = chemTopic!!
                        val langName = chemData.languages[chemLanguage] ?: "English"
                        Text("${ct.topicName[chemLanguage] ?: ct.topicName["en"]!!} / $langName", style = MaterialTheme.typography.bodyMedium)
                        Panel(Lime) {
                            Text("YOUR EDUCATIONAL LYRICS", style = MaterialTheme.typography.labelSmall)
                            Text("The educational lyrics from your Chemistry database are shown below. Review and edit before proceeding.")
                        }
                        // Pre-fill lyrics from Chemistry database if empty
                        if (lyrics.isBlank()) {
                            lyrics = ct.lyrics[chemLanguage] ?: ct.lyrics["en"]!!
                        }
                        Button(enabled = !busy, onClick = {
                            // Reset to the original database lyrics for this language
                            lyrics = ct.lyrics[chemLanguage] ?: ct.lyrics["en"]!!
                            notice = "Lyrics restored from the Chemistry database."
                        }, modifier = Modifier.fillMaxWidth()) {
                            Text("Restore original lyrics")
                        }
                    } else {
                        Text("${topic!!.title} / ${data.languages[language]} / $category / ${reference!!.title}", style = MaterialTheme.typography.bodyMedium)
                        Panel(Lime) {
                            Text(if (useReference) "FIT WORDS TO MUSICAL PHRASES" else "YOUR EDUCATIONAL LYRICS", style = MaterialTheme.typography.labelSmall)
                            Text(if (useReference) "Fit the topic lyrics to the supplied melody's phrase lengths, then review and edit before singing." else "Create a draft from the topic facts in your selected language and musical style.")
                        }
                        Button(enabled = !busy, onClick = {
                            busy = true; notice = ""; generationError = ""
                            scope.launch {
                                try {
                                    val draft = LyricPrompts.draft(topic, category, choice!!)
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
                    }
                    if (generationError.isNotBlank()) Text(generationError, color = MaterialTheme.colorScheme.error)
                    OutlinedTextField(lyrics, { lyrics = it }, enabled = !busy, label = { Text("Your lyrics — editable") }, minLines = 10, modifier = Modifier.fillMaxWidth())
                    if (isChemistry) {
                        val ct = chemTopic!!
                        Panel(Lavender) {
                            Text("EXPLANATION", style = MaterialTheme.typography.labelSmall)
                            Text(ct.explanation[chemLanguage] ?: ct.explanation["en"]!!)
                        }
                        Panel(Lavender) {
                            Text("KEYWORDS", style = MaterialTheme.typography.labelSmall)
                            Text(ct.keywords[chemLanguage] ?: ct.keywords["en"]!!, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        Text("Check formulas, theorem conditions and pronunciation. Translations in the original prototype still need subject review.", style = MaterialTheme.typography.bodySmall)
                    }
                    Button(onClick = { notice = ""; page = 4 }, enabled = lyrics.isNotBlank() && !busy, modifier = Modifier.fillMaxWidth()) { Text("Continue to song →") }
                }
                4 -> {
                    Text("From words\nto a study song.", style = MaterialTheme.typography.headlineLarge)
                    if (isChemistry) {
                        val ct = chemTopic!!
                        val langName = chemData.languages[chemLanguage] ?: "English"
                        val songRef = ct.songTitle[chemLanguage] ?: ct.songTitle["en"]!!
                        val artistRef = ct.artistName[chemLanguage] ?: ct.artistName["en"]!!
                        Panel(Lavender) {
                            Text("YOUR SONG", style = MaterialTheme.typography.labelSmall)
                            Text(ct.topicName[chemLanguage] ?: ct.topicName["en"]!!, style = MaterialTheme.typography.titleLarge)
                            Text("Song reference: $songRef — $artistRef")
                            Text("$langName · ${ct.unit}")
                            Text("Generate vocals and music here, then listen in the app.")
                        }
                        val audioKey = listOf(student.id, "chemistry", ct.songId, chemLanguage, songRef, tempo, focus, lyrics).joinToString("\u0000")
                        val audioFile = remember(audioKey, audioRevision) { AudioFiles.file(context, audioKey) }
                        if (audioFile.exists()) {
                            val mode = runCatching { audioFile.resolveSibling(audioFile.name + ".mode").readText() }.getOrDefault("original_melody")
                            Text(if (mode == "reference_cover") "Generated using a supplied melody reference" else "AI-generated original melody in the selected style", style = MaterialTheme.typography.bodySmall)
                            SongPlayer(audioFile, audioRevision)
                            LyricsPanel(lyrics)
                        }
                        if (busy) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Text(generationStatus.ifBlank { "Connecting to the singing model…" })
                        }
                        Button(enabled = !busy, onClick = {
                            busy = true; generationError = ""; generationStatus = "Connecting to the singing model…"
                            scope.launch {
                                try {
                                    val synthTopic = com.nmims.songstudio.data.Topic(
                                        ct.songId, ct.unit,
                                        ct.topicName[chemLanguage] ?: ct.topicName["en"]!!,
                                        ct.explanation[chemLanguage] ?: ct.explanation["en"]!!,
                                        ct.keywords[chemLanguage] ?: ct.keywords["en"]!!,
                                        ct.lyrics, ct.songTitle
                                    )
                                    val synthCategory = if (chemLanguage == "hi") "Hindi" else "English"
                                    val synthChoices = MusicChoices.options(data, data.topics.first(), synthCategory, false)
                                    val synthChoice = synthChoices.first()
                                    SingingClient.generate(context.applicationContext, audioKey, synthTopic, synthCategory, synthChoice, lyrics, tempo, false) { generationStatus = it }
                                    audioRevision++; generationStatus = "Your song is ready. Press Play song."
                                } catch (e: Exception) { generationError = e.message ?: "Could not generate the song. Please retry." }
                                finally { busy = false }
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text(if (busy) "Creating your song…" else "Generate song") }
                        Text("An original melody will be composed by the singing server using your educational lyrics.", style = MaterialTheme.typography.bodySmall)
                        if (generationError.isNotBlank()) Text(generationError, color = MaterialTheme.colorScheme.error)

                        // Server connection status & recovery on generation screen
                        ServerStatusCard(
                            serverConnected = serverConnected,
                            isChecking = isCheckingServer,
                            provider = serverProvider,
                            errorMessage = serverError,
                            onReconnect = { scope.launch { checkServerHealth() } }
                        )

                        if (!audioFile.exists()) LyricsPanel(lyrics)
                    } else {
                        Panel(Lavender) {
                            Text("YOUR SONG", style = MaterialTheme.typography.labelSmall)
                            Text(topic!!.title, style = MaterialTheme.typography.titleLarge)
                            Text("${if (useReference) "Melody guide" else "Style reference"}: ${reference!!.title} — ${reference.artist}")
                            Text("${data.languages[language]} · ${style!!.name} · ${if (useReference) "${melody?.bpm} BPM" else tempo}")
                            Text("Generate vocals and music here, then listen in the app.")
                        }
                        val audioKey = listOf(student.id, topic!!.id, category, reference!!.title, tempo, focus, lyrics, useReference, if (useReference) melody?.sha else "original-v3").joinToString("\u0000")
                        val audioFile = remember(audioKey, audioRevision) { AudioFiles.file(context, audioKey) }
                        if (audioFile.exists()) {
                            val mode = runCatching { audioFile.resolveSibling(audioFile.name + ".mode").readText() }.getOrDefault("original_melody")
                            Text(if (mode == "reference_cover") "Generated using a supplied melody reference" else "AI-generated original melody in the selected style", style = MaterialTheme.typography.bodySmall)
                            SongPlayer(audioFile, audioRevision)
                            LyricsPanel(lyrics)
                        }
                        if (busy) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                            Text(generationStatus.ifBlank { "Connecting to the singing model…" })
                        }
                        Button(enabled = !busy, onClick = {
                            busy = true; generationError = ""; generationStatus = "Connecting to the singing model…"
                            scope.launch {
                                try {
                                    SingingClient.generate(context.applicationContext, audioKey, topic, category, choice!!, lyrics, tempo, useReference) { generationStatus = it }
                                    audioRevision++; generationStatus = "Your song is ready. Press Play song."
                                } catch (e: Exception) { generationError = e.message ?: "Could not generate the song. Please retry." }
                                finally { busy = false }
                            }
                        }, modifier = Modifier.fillMaxWidth()) { Text(if (busy) "Creating your song…" else "Generate song") }
                        Text(if (useReference) "The singing model receives the melody audio and your fitted lyrics. Melody fidelity and pronunciation can vary." else "An original melody will be composed. To follow a specific tune, its melody reference must be in the library.", style = MaterialTheme.typography.bodySmall)
                        if (generationError.isNotBlank()) Text(generationError, color = MaterialTheme.colorScheme.error)

                        // Server connection status & recovery on generation screen
                        ServerStatusCard(
                            serverConnected = serverConnected,
                            isChecking = isCheckingServer,
                            provider = serverProvider,
                            errorMessage = serverError,
                            onReconnect = { scope.launch { checkServerHealth() } }
                        )

                        if (!audioFile.exists()) LyricsPanel(lyrics)
                    }
                    OutlinedButton(enabled = !busy, onClick = { notice = ""; page = 3 }, modifier = Modifier.fillMaxWidth()) { Text("Edit lyrics") }
                    OutlinedButton(enabled = !busy, onClick = { notice = ""; page = 1 }, modifier = Modifier.fillMaxWidth()) { Text("Choose another topic") }
                }
            }
            if (notice.isNotEmpty()) Text(notice, color = MaterialTheme.colorScheme.primary)
            if (page > 1) TextButton(enabled = !busy, onClick = { goBack() }) { Text("← Back") }
            HorizontalDivider()
            Text("MUSIC-BASED LEARNING / $editionLabel", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun ServerStatusCard(
    serverConnected: Boolean?,
    isChecking: Boolean,
    provider: String,
    errorMessage: String,
    onReconnect: () -> Unit
) {
    Panel(Lavender) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("MUSIC SERVER", style = MaterialTheme.typography.labelSmall)
            if (isChecking) {
                Text("Checking…", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            } else when (serverConnected) {
                true -> Text("Connected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                false -> Text("Unavailable", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                null -> Text("Unknown", style = MaterialTheme.typography.labelSmall)
            }
        }

        if (isChecking) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                Text("Verifying music server connection…", style = MaterialTheme.typography.bodySmall)
            }
        } else if (serverConnected == true) {
            Text("Music bridge is online${if (provider.isNotBlank()) " ($provider)" else ""}. Ready to generate songs.", style = MaterialTheme.typography.bodySmall)
        } else {
            Text(
                "The local music bridge is unavailable. Start it separately on your development computer (e.g., run python server/music_server.py), then tap Reconnect.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            Button(
                onClick = onReconnect,
                enabled = !isChecking,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isChecking) "Checking connection…" else "Reconnect Music Server")
            }
        }
    }
}

@Composable
private fun LyricsPanel(lyrics: String) {
    Panel(Lavender) {
        Text("YOUR LYRICS", style = MaterialTheme.typography.labelSmall)
        Text("Read along", style = MaterialTheme.typography.titleLarge)
        Text("Your prepared lyrics. Line highlighting is not synced to the audio yet.", style = MaterialTheme.typography.bodySmall)
        SelectionContainer {
            Text(lyrics, style = MaterialTheme.typography.bodyLarge)
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
