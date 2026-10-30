package com.example.designex.economics.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designex.economics.data.Lang
import com.example.designex.ui.theme.Accent
import com.example.designex.ui.theme.DeepNavy
import com.example.designex.ui.theme.Glass
import com.example.designex.ui.theme.GlassBorder
import com.example.designex.ui.theme.GlassStrong
import com.example.designex.ui.theme.Slate
import com.example.designex.ui.theme.TextFaint
import com.example.designex.ui.theme.TextHi
import com.example.designex.ui.theme.TextLo
import kotlin.math.PI
import kotlin.math.sin

val CardShape = RoundedCornerShape(22.dp)

/** The night-sky backdrop behind every screen. */
fun Modifier.nightBackground(): Modifier =
    background(Brush.verticalGradient(listOf(DeepNavy, Slate, DeepNavy)))

@Composable
fun Logo(size: TextUnit = 40.sp, color: Color = TextHi) {
    Text("LearnTune", fontFamily = FontFamily.Cursive, fontSize = size, color = color, fontWeight = FontWeight.Bold)
}

/** Frosted-glass card. */
@Composable
fun Panel(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    padding: Dp = 18.dp,
    content: @Composable () -> Unit,
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(Glass)
            .border(1.dp, GlassBorder, CardShape)
            .let { if (onClick != null) it.clickable(onClick = onClick) else it }
            .padding(padding)
    ) { content() }
}

/** Picture card with painted scenery and a frosted label strip, like a story tile. */
@Composable
fun SceneCard(
    title: String,
    subtitle: String,
    key: String,
    modifier: Modifier = Modifier,
    height: Dp = 180.dp,
    badge: String? = null,
    onClick: () -> Unit,
) {
    Box(
        modifier
            .height(height)
            .clip(CardShape)
            .border(1.dp, GlassBorder, CardShape)
            .clickable(onClick = onClick)
    ) {
        SceneArt(Scenes.forKey(key), seedOf(key), Modifier.fillMaxSize())
        badge?.let {
            Text(
                it,
                modifier = Modifier
                    .padding(10.dp)
                    .clip(RoundedCornerShape(50))
                    .background(DeepNavy.copy(alpha = 0.55f))
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                color = TextHi,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Column(
            Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color.Transparent, DeepNavy.copy(alpha = 0.92f))))
                .padding(start = 12.dp, end = 12.dp, top = 24.dp, bottom = 12.dp)
        ) {
            Text(title, color = TextHi, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 19.sp)
            if (subtitle.isNotBlank()) Text(subtitle, color = TextLo, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Hero banner: painted scenery with content laid over the bottom. */
@Composable
fun SceneHeader(key: String, height: Dp, content: @Composable BoxScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(bottomStart = 34.dp, bottomEnd = 34.dp))
    ) {
        val glowX = if (seedOf(key) % 2 == 0) 0.8f else 0.2f
        SceneArt(Scenes.forKey(key), seedOf(key), Modifier.fillMaxSize(), glowAt = androidx.compose.ui.geometry.Offset(glowX, 0.36f))
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(DeepNavy.copy(alpha = 0.45f), Color.Transparent, DeepNavy.copy(alpha = 0.9f))))
        )
        content()
    }
}

@Composable
fun SectionLabel(text: String, color: Color = TextLo) {
    Text(text.uppercase(), color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
}

@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, modifier = modifier, color = TextHi, fontSize = 28.sp, fontWeight = FontWeight.SemiBold, lineHeight = 34.sp)
}

@Composable
fun Pill(text: String, color: Color, background: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        color = color,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
    )
}

/** Horizontally scrolling filter chips; the selected one is filled. */
@Composable
fun ChipRow(options: List<String>, selected: Int, modifier: Modifier = Modifier, onSelect: (Int) -> Unit) {
    Row(
        modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Text(
                label,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (on) Accent else Glass)
                    .border(1.dp, if (on) Accent else GlassBorder, RoundedCornerShape(50))
                    .clickable { onSelect(i) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = if (on) DeepNavy else TextLo,
                fontWeight = if (on) FontWeight.Bold else FontWeight.Medium,
                fontSize = 14.sp,
            )
        }
    }
}

/** Outlined pill option used in onboarding. */
@Composable
fun OptionPill(icon: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(if (selected) GlassStrong else Color.Transparent)
            .border(1.5.dp, if (selected) Accent else GlassBorder.copy(alpha = 0.5f), RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, color = if (selected) Accent else TextLo, fontSize = 16.sp, modifier = Modifier.width(30.dp), textAlign = TextAlign.Center)
        Spacer(Modifier.width(10.dp))
        Text(label, color = TextHi, fontSize = 16.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal, modifier = Modifier.weight(1f))
        if (selected) Text("✓", color = Accent, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PrimaryButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(50))
            .background(if (enabled) Accent else Glass)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = if (enabled) DeepNavy else TextFaint, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun GhostButton(text: String, modifier: Modifier = Modifier, color: Color = TextHi, onClick: () -> Unit) {
    Text(
        text,
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Glass)
            .border(1.dp, GlassBorder, RoundedCornerShape(50))
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 10.dp),
        color = color,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
    )
}

/** Round frosted back button. */
@Composable
fun BackRow(@Suppress("UNUSED_PARAMETER") lang: Lang, onBack: () -> Unit) {
    CircleButton("‹", onBack)
}

@Composable
fun CircleButton(symbol: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(DeepNavy.copy(alpha = 0.45f))
            .border(1.dp, GlassBorder, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(symbol, color = TextHi, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
}

@Composable
fun LanguageSwitch(selected: Lang, onSelect: (Lang) -> Unit, @Suppress("UNUSED_PARAMETER") onDark: Boolean = true) {
    ChipRow(Lang.entries.map { it.label }, Lang.entries.indexOf(selected)) { onSelect(Lang.entries[it]) }
}

@Composable
fun NumberBadge(text: String, color: Color, size: Int = 44) {
    Box(
        Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.18f))
            .border(1.dp, color.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = color, fontWeight = FontWeight.Bold, fontSize = (size / 2.6).sp)
    }
}

@Composable
fun ProgressBar(fraction: Float, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.12f))
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction.coerceIn(0f, 1f))
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(color)
        )
    }
}

@Composable
fun Equalizer(active: Boolean, color: Color, modifier: Modifier = Modifier) {
    val phase by rememberInfiniteTransition(label = "eq").animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Restart),
        label = "eqPhase",
    )
    Row(modifier.height(36.dp), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.Bottom) {
        repeat(18) { i ->
            val level = if (active) 0.25f + 0.75f * ((sin(phase + i * 0.7f) + 1f) / 2f) else 0.18f
            Box(
                Modifier
                    .width(5.dp)
                    .height((36 * level).dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color.copy(alpha = if (active) 0.9f else 0.3f))
            )
        }
    }
}

@Composable
fun ListenButtons(query: String, context: Context, onTap: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        GhostButton("▶  YouTube", color = Color(0xFFFF8A9B)) {
            onTap()
            openLink(context, "https://www.youtube.com/results?search_query=" + Uri.encode(query))
        }
        GhostButton("♫  Spotify", color = Color(0xFF8EE6A8)) {
            onTap()
            openLink(context, "https://open.spotify.com/search/" + Uri.encode(query))
        }
    }
}

private fun openLink(context: Context, url: String) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
    }
}

@Composable
fun MutedText(text: String, modifier: Modifier = Modifier, maxLines: Int = Int.MAX_VALUE) {
    Text(
        text,
        modifier = modifier,
        color = TextLo,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
fun Spacer8() = Spacer(Modifier.height(8.dp))

val ScreenPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 120.dp)
