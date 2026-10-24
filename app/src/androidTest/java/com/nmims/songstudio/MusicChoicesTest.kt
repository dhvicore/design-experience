package com.nmims.songstudio

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nmims.songstudio.data.Curriculum
import com.nmims.songstudio.data.MusicChoices
import com.nmims.songstudio.generation.LyricPrompts
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MusicChoicesTest {
    @Test fun categoriesAndRecommendationsFollowTopicAndIncludeRequestedSongs() {
        val data = Curriculum(InstrumentationRegistry.getInstrumentation().targetContext)
        assertEquals(listOf("Hindi", "English", "Bhajan", "Lo-fi"), MusicChoices.categories)
        MusicChoices.categories.forEach { category ->
            val recommendations = data.topics.map { MusicChoices.options(data, it, category).map { choice -> choice.id } }
            assertTrue(recommendations.all { it.size == 4 && it.distinct().size == 4 })
            assertTrue(recommendations.distinct().size > 20)
        }
        val allHindi = MusicChoices.options(data, data.topics.first(), "Hindi", all = true)
        assertTrue(allHindi.any { it.song.title == "Chikni Chameli" })
        assertTrue(allHindi.any { it.song.title == "Chammak Challo" })
    }

    @Test fun rolleBollywoodDemoUsesHindiAndKeepsEveryTheoremCondition() {
        val data = Curriculum(InstrumentationRegistry.getInstrumentation().targetContext)
        val topic = data.topics.first { it.title == "Rolle's Theorem" }
        val choice = MusicChoices.options(data, topic, "Hindi").first()
        assertTrue(MusicChoices.options(data, topic, "Hindi", true).any { it.id == "kala-chashma" })
        val draft = LyricPrompts.draft(topic, "Hindi", choice)
        listOf("बंद अंतराल", "सतत", "खुले अंतराल", "अवकलज मौजूद", "मान बराबर", "कम से कम एक सी", "अवकलज शून्य").forEach {
            assertTrue("Missing theorem condition: $it", draft.contains(it))
        }
        assertFalse(MusicChoices.options(data, topic, "English", true).any { it.id == "kala-chashma" })
    }

    @Test fun lyricDraftsDependOnCategoryAndSongAndPreserveRolleConditions() {
        val data = Curriculum(InstrumentationRegistry.getInstrumentation().targetContext)
        val topic = data.topics.first()
        val english = MusicChoices.options(data, topic, "English", all = true)
        val drafts = english.map { LyricPrompts.draft(topic, "English", it) }
        assertTrue(drafts.distinct().size > 1)
        assertTrue(drafts.all { it.contains("Continuous on the closed interval") && it.contains("Differentiable inside") })
        val hindi = LyricPrompts.draft(topic, "Hindi", MusicChoices.options(data, topic, "Hindi").first())
        val bhajan = LyricPrompts.draft(topic, "Bhajan", MusicChoices.options(data, topic, "Bhajan").first())
        assertNotEquals(hindi, bhajan)
        assertTrue(bhajan.contains("Om vidya-prakashaya namah"))
        data.topics.forEach { t -> MusicChoices.categories.forEach { c ->
            assertTrue(LyricPrompts.draft(t, c, MusicChoices.options(data, t, c).first()).contains("[Chorus]"))
        } }
    }
}
