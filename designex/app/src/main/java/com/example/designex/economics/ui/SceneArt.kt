package com.example.designex.economics.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/** Colours for one painted landscape. */
data class ScenePalette(
    val skyTop: Color,
    val skyBottom: Color,
    val glow: Color,
    val far: Color,
    val mid: Color,
    val near: Color,
    val night: Boolean,
    val aurora: Color? = null,
)

object Scenes {
    val Dawn = ScenePalette(Color(0xFF6E7FB0), Color(0xFFE9C3C0), Color(0xFFFFE3C4), Color(0xFF8C94BE), Color(0xFF5D6996), Color(0xFF34406A), false)
    val MistyLake = ScenePalette(Color(0xFF7D93BF), Color(0xFFD8E2F1), Color(0xFFF7FAFF), Color(0xFF9EAFD2), Color(0xFF6C80AE), Color(0xFF3D4D78), false)
    val Dusk = ScenePalette(Color(0xFF2F3A63), Color(0xFF9A88B8), Color(0xFFF6D3C2), Color(0xFF6F6E9E), Color(0xFF474D7C), Color(0xFF262E52), false)
    val Night = ScenePalette(Color(0xFF0F1630), Color(0xFF34436E), Color(0xFFF1F0E3), Color(0xFF3D4B78), Color(0xFF29355C), Color(0xFF161E3B), true)
    val Aurora = ScenePalette(Color(0xFF0D1A2E), Color(0xFF253F5C), Color(0xFFE7F3EC), Color(0xFF314A66), Color(0xFF203650), Color(0xFF111F33), true, Color(0xFF6FE0B5))
    val Ember = ScenePalette(Color(0xFF231B3B), Color(0xFFB0705E), Color(0xFFFFC08A), Color(0xFF6B4E6E), Color(0xFF443553), Color(0xFF221B33), true)

    private val all = listOf(MistyLake, Dusk, Night, Aurora, Dawn, Ember)

    fun forKey(key: String): ScenePalette = all[Math.floorMod(key.hashCode(), all.size)]
}

/** Painted landscape: sky, sun or moon, stars, aurora, layered mountains, mist and a lake reflection. */
@Composable
fun SceneArt(
    palette: ScenePalette,
    seed: Int,
    modifier: Modifier = Modifier,
    lake: Boolean = true,
    horizonAt: Float? = null,
    glowAt: Offset? = null,
) {
    Spacer(
        modifier.drawWithCache {
            val w = size.width
            val h = size.height
            val rnd = Random(seed)
            val horizon = h * (horizonAt ?: if (lake) 0.72f else 0.86f)
            val ridges = listOf(
                ridge(rnd, w, horizon, horizon * 0.50f, 3, 0.55f),
                ridge(rnd, w, horizon, horizon * 0.36f, 4, 0.75f),
                ridge(rnd, w, horizon, horizon * 0.20f, 6, 0.92f),
            )
            val colors = listOf(palette.far, palette.mid, palette.near)
            val stars = if (palette.night) List(70) { Offset(rnd.nextFloat() * w, rnd.nextFloat() * horizon * 0.7f) to rnd.nextFloat() } else emptyList()
            val randomGlow = Offset(0.25f + rnd.nextFloat() * 0.5f, (0.18f + rnd.nextFloat() * 0.14f) * horizon / h / 0.72f)
            val g = glowAt ?: randomGlow
            val glowCenter = Offset(w * g.x, h * g.y)
            val glowRadius = minOf(w, h) * if (palette.night) 0.07f else 0.11f
            val auroraPhase = rnd.nextFloat() * 6f

            onDrawBehind {
                drawRect(Brush.verticalGradient(listOf(palette.skyTop, palette.skyBottom), endY = horizon))
                stars.forEach { (p, b) -> drawCircle(Color.White.copy(alpha = 0.25f + 0.6f * b), radius = 0.6f + b * 1.6f, center = p) }
                palette.aurora?.let { drawAurora(it, w, h, auroraPhase) }
                drawCircle(
                    Brush.radialGradient(listOf(palette.glow.copy(alpha = 0.55f), Color.Transparent), glowCenter, glowRadius * 4f),
                    radius = glowRadius * 4f,
                    center = glowCenter,
                )
                drawCircle(palette.glow, radius = glowRadius, center = glowCenter)
                ridges.forEachIndexed { i, path ->
                    drawPath(path, Brush.verticalGradient(listOf(colors[i], colors[i].copy(alpha = 0.85f)), startY = horizon - h * 0.45f, endY = horizon))
                    drawRect(
                        Brush.verticalGradient(listOf(Color.Transparent, palette.skyBottom.copy(alpha = 0.35f - i * 0.1f)), startY = horizon - h * 0.12f, endY = horizon),
                    )
                }
                if (lake) {
                    drawRect(
                        Brush.verticalGradient(listOf(palette.near.copy(alpha = 0.95f), palette.mid, palette.skyTop.copy(alpha = 0.9f)), startY = horizon, endY = h),
                        topLeft = Offset(0f, horizon),
                    )
                    ridges.forEachIndexed { i, path ->
                        scale(1f, -0.45f, pivot = Offset(w / 2, horizon)) {
                            drawPath(path, colors[i].copy(alpha = 0.35f))
                        }
                    }
                    for (k in 0 until 6) {
                        val y = horizon + (h - horizon) * (0.15f + k * 0.14f)
                        drawLine(Color.White.copy(alpha = 0.06f), Offset(w * 0.1f, y), Offset(w * 0.9f, y), strokeWidth = 1.5f)
                    }
                }
                drawRect(
                    Brush.verticalGradient(listOf(Color.Transparent, Color.White.copy(alpha = 0.10f), Color.Transparent), startY = horizon - h * 0.08f, endY = horizon + h * 0.05f),
                )
            }
        }
    )
}

/** A soft mountain ridge: alternating peaks and valleys joined with smooth curves. */
private fun ridge(rnd: Random, w: Float, base: Float, height: Float, peaks: Int, jag: Float): Path {
    val points = mutableListOf(Offset(-w * 0.05f, base - height * (0.25f + rnd.nextFloat() * 0.3f)))
    val step = w * 1.1f / peaks
    var x = -w * 0.05f
    for (i in 0 until peaks) {
        points += Offset(x + step * (0.35f + rnd.nextFloat() * 0.3f), base - height * (0.6f + rnd.nextFloat() * 0.4f))
        x += step
        points += Offset(x, base - height * (0.15f + rnd.nextFloat() * 0.3f) * jag)
    }
    val path = Path()
    path.moveTo(points.first().x, base)
    path.lineTo(points.first().x, points.first().y)
    for (i in 1 until points.size) {
        val prev = points[i - 1]
        val cur = points[i]
        val mid = Offset((prev.x + cur.x) / 2, (prev.y + cur.y) / 2)
        path.quadraticTo(prev.x, prev.y, mid.x, mid.y)
    }
    path.lineTo(points.last().x, points.last().y)
    path.lineTo(w * 1.05f, base)
    path.close()
    return path
}

private fun DrawScope.drawAurora(color: Color, w: Float, h: Float, phase: Float) {
    for (band in 0 until 3) {
        val path = Path()
        val top = h * (0.12f + band * 0.07f)
        path.moveTo(0f, top)
        var x = 0f
        while (x <= w) {
            path.lineTo(x, top + sin((x / w) * 2 * PI.toFloat() * 1.3f + phase + band) * h * 0.05f)
            x += w / 40
        }
        path.lineTo(w, top + h * 0.22f)
        path.lineTo(0f, top + h * 0.22f)
        path.close()
        drawPath(path, Brush.verticalGradient(listOf(color.copy(alpha = 0.32f - band * 0.08f), Color.Transparent), startY = top, endY = top + h * 0.22f))
    }
}

/** A small deterministic number from a string, for scene seeds. */
fun seedOf(key: String): Int = abs(key.hashCode())
