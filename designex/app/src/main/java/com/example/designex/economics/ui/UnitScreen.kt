package com.example.designex.economics.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designex.economics.audio.SoundFx
import com.example.designex.ui.theme.TextLo

/** Learn tab: unit chips and a picture grid of topics. [selectedUnit] null shows every unit. */
@Composable
fun UnitScreen(deps: AppDeps, selectedUnit: Int?, open: (Route) -> Unit, onUnitChange: (Int?) -> Unit) {
    val lang = deps.progress.language
    val subject = deps.catalog.subject(deps.progress.subjectId)
    val unit = selectedUnit?.let(subject::unit)
    val topics = unit?.topics ?: subject.topics
    val chips = listOf(Copy.allUnits.of(lang)) + subject.units.map { "${Copy.unit.of(lang)} ${it.number}" }

    LazyColumn(
        Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 120.dp),
    ) {
        item {
            Column {
                ScreenTitle(subject.name[lang])
                Text(unit?.name?.get(lang) ?: "${subject.units.size} ${Copy.units.of(lang).lowercase()} · ${subject.topics.size} ${Copy.topics.of(lang)}", color = TextLo, fontSize = 15.sp)
                Spacer(Modifier.height(14.dp))
                ChipRow(chips, if (unit == null) 0 else subject.units.indexOf(unit) + 1) {
                    deps.fx.play(SoundFx.Fx.TAP)
                    onUnitChange(if (it == 0) null else subject.units[it - 1].number)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
        items(topics.chunked(2)) { pair ->
            Row(Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                pair.forEach { topic ->
                    val u = subject.unit(topic.unitNo)
                    val number = "${topic.unitNo}.${(u?.topics?.indexOf(topic) ?: 0) + 1}"
                    SceneCard(
                        title = topic.name[lang],
                        subtitle = topic.formula ?: topic.keywords[lang].take(2).joinToString(" · "),
                        key = topic.id,
                        badge = if (topic.id in deps.progress.learned) "✓ $number" else number,
                        height = 200.dp,
                        modifier = Modifier.weight(1f),
                    ) { open(Route.TopicPage(topic.id)) }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}
