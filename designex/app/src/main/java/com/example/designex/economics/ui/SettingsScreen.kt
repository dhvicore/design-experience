package com.example.designex.economics.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designex.economics.audio.SoundFx
import com.example.designex.economics.data.SunoSettings
import com.example.designex.ui.theme.Accent
import com.example.designex.ui.theme.Coral
import com.example.designex.ui.theme.Gold
import com.example.designex.ui.theme.Mint
import com.example.designex.ui.theme.TextFaint
import com.example.designex.ui.theme.TextHi
import com.example.designex.ui.theme.TextLo

/** Profile tab: who is signed in, progress, subject and language, Suno key and voices. */
@Composable
fun SettingsScreen(deps: AppDeps, onLogOut: () -> Unit) {
    val progress = deps.progress
    val lang = progress.language
    var key by remember { mutableStateOf(deps.suno.apiKey) }
    var url by remember { mutableStateOf(deps.suno.baseUrl) }
    var showKey by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }
    val learnedAll = deps.catalog.subjects.sumOf { s -> s.topics.count { it.id in progress.learned } }
    val totalAll = deps.catalog.subjects.sumOf { it.topics.size }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding(),
    ) {
        SceneHeader("profile-" + progress.userName, 300.dp) {
            Column(
                Modifier
                    .align(Alignment.Center)
                    .statusBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(Copy.tabProfile.of(lang), color = TextHi, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(14.dp))
                Avatar(progress.userName.orEmpty(), 96)
                Spacer(Modifier.height(10.dp))
                Text(progress.userName.orEmpty(), color = TextHi, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                Text(deps.accounts.lastEmail, color = TextLo, fontSize = 14.sp)
            }
        }

        Column(
            Modifier
                .padding(horizontal = 18.dp)
                .padding(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Stat("$learnedAll/$totalAll", Copy.learned.of(lang), Mint, Modifier.weight(1f))
                Stat(
                    "★ " + deps.catalog.subjects.maxOf { progress.bestQuizScore(it.id) } + "/5",
                    Copy.best.of(lang) + " · " + Copy.quiz.of(lang),
                    Gold,
                    Modifier.weight(1f),
                )
            }

            Panel {
                Column {
                    SectionLabel(Copy.subjectAndLanguage.of(lang), Accent)
                    Spacer(Modifier.height(12.dp))
                    ChipRow(deps.catalog.subjects.map { it.name[lang] }, deps.catalog.subjects.indexOfFirst { it.id == progress.subjectId }) {
                        deps.fx.play(SoundFx.Fx.TAP)
                        progress.chooseSubject(deps.catalog.subjects[it].id)
                    }
                    Spacer(Modifier.height(10.dp))
                    LanguageSwitch(lang, onSelect = {
                        deps.fx.play(SoundFx.Fx.TAP)
                        progress.changeLanguage(it)
                    })
                }
            }

            Panel {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionLabel("Suno AI singing", Accent)
                        Spacer(Modifier.weight(1f))
                        Pill(
                            if (deps.suno.isConfigured) "Key added" else "Not set up",
                            if (deps.suno.isConfigured) Mint else TextLo,
                            if (deps.suno.isConfigured) Mint.copy(alpha = 0.15f) else TextFaint.copy(alpha = 0.15f),
                        )
                    }
                    Spacer8()
                    MutedText(
                        "Suno has no official public API, so songs go through a Suno API provider that gives you a key. " +
                            "Paste it here to have real singers perform your study songs. Without a key, the free on-phone singer is used."
                    )
                    Spacer(Modifier.height(14.dp))
                    OutlinedTextField(
                        value = key,
                        onValueChange = { key = it; saved = false },
                        label = { Text("API key") },
                        singleLine = true,
                        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            Text(
                                if (showKey) "Hide" else "Show",
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showKey = !showKey }
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                color = Accent,
                                fontSize = 13.sp,
                            )
                        },
                        colors = glassFieldColors(),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it; saved = false },
                        label = { Text("Provider address") },
                        singleLine = true,
                        colors = glassFieldColors(),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.width(120.dp)) {
                            PrimaryButton("Save") {
                                deps.suno.apiKey = key
                                deps.suno.baseUrl = url
                                saved = true
                                deps.fx.play(SoundFx.Fx.SUCCESS)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Reset address",
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { url = SunoSettings.DEFAULT_BASE_URL; saved = false }
                                .padding(8.dp),
                            color = TextLo,
                            fontSize = 13.sp,
                        )
                        Spacer(Modifier.weight(1f))
                        if (saved) Text("Saved ✓", color = Mint, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Your key is stored only in this phone's private app storage. It is never part of the app's code, so it is never uploaded to GitHub.",
                        color = TextFaint,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                    )
                }
            }

            Panel {
                Column {
                    SectionLabel("Voices for the on-phone singer", Accent)
                    Spacer8()
                    MutedText(
                        "The on-phone singer uses your phone's text-to-speech voices. English works offline on most phones. " +
                            "For offline Hindi and Marathi, open Settings › Text-to-speech › Google › Install voice data and download them."
                    )
                }
            }

            GhostButton("Log out", Modifier.fillMaxWidth(), color = Coral, onClick = onLogOut)
            Text(Copy.prototypeNote.of(lang), color = TextFaint, fontSize = 12.sp, lineHeight = 17.sp)
        }
    }
}

@Composable
private fun Stat(value: String, label: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Panel(modifier) {
        Column {
            Text(value, color = color, fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
            Text(label, color = TextLo, fontSize = 12.sp, maxLines = 1)
        }
    }
}
