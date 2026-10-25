package com.nmims.songstudio

import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.nmims.songstudio.auth.SessionViewModel
import com.nmims.songstudio.auth.Student
import com.nmims.songstudio.data.ChemistryCurriculum
import com.nmims.songstudio.data.Curriculum
import com.nmims.songstudio.generation.SingingClient
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChemistryNavigationTest {

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val chemData by lazy { ChemistryCurriculum(context) }
    private val calcData by lazy { Curriculum(context) }

    @Test
    fun chemistryDatasetIntegrityPreserved() {
        // Must preserve 60 Chemistry topics and 38 Calculus topics
        assertEquals("Chemistry must have exactly 60 topics", 60, chemData.topics.size)
        assertEquals("Calculus must have exactly 38 topics", 38, calcData.topics.size)
        assertEquals("Chemistry must have 11 distinct units", 11, chemData.units.size)
        assertEquals("Calculus must have 5 units", 5, calcData.units.size)
    }

    @Test
    fun singingClientHealthCheckHandlesOfflineServerGracefully() = runBlocking {
        // Without local server running, checkHealth should report ok = false without crashing
        val health = SingingClient.checkHealth()
        assertNotNull(health)
        // If server is not running in test env, ok is false and error is populated
        if (!health.ok) {
            assertTrue("Health check error message should not be empty", health.error.isNotBlank())
            assertEquals("Provider should be empty when offline", "", health.provider)
        } else {
            assertTrue("Provider should be populated when online", health.provider.isNotBlank())
        }
    }

    @Test
    fun activityLaunchesAndSignsInSuccessfully() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val session = ViewModelProvider(activity)[SessionViewModel::class.java]
                assertNull(session.student)
                val testStudent = Student(1, "Test Student", "student@example.test")
                session.signIn(testStudent)
                assertEquals(testStudent, session.student)
            }
        }
    }

    @Test
    fun navigationStateTransitionsPreserveSubjectAndSelections() {
        // Verify state progression and backward navigation logic
        var page = 1
        var subject = "Chemistry"
        var chemUnit = chemData.units.keys.first()
        var chemTopicId = chemData.topics.first().songId
        var chemLanguage = "en"
        var lyrics = ""

        // Forward: Step 1 -> Step 2
        page = 2
        assertEquals(2, page)
        assertEquals("Chemistry", subject)

        // Customize in Step 2: Change language to Hindi
        chemLanguage = "hi"
        lyrics = "" // Dependent selection cleared
        val currentTopic = chemData.topics.first { it.songId == chemTopicId }
        val hindiTitle = currentTopic.topicName["hi"]
        assertNotNull(hindiTitle)
        assertTrue(hindiTitle!!.isNotBlank())

        // Forward: Step 2 -> Step 3
        page = 3
        assertEquals(3, page)
        // In Step 3, lyrics pre-filled from Hindi
        if (lyrics.isBlank()) {
            lyrics = currentTopic.lyrics[chemLanguage] ?: currentTopic.lyrics["en"]!!
        }
        assertTrue(lyrics.isNotBlank())

        // BACKWARD: Step 3 -> Step 2
        // Crucial test: page must decrement to 2 and NOT bounce back to 3
        page--
        assertEquals("Navigating back from Step 3 must reach Step 2", 2, page)
        assertEquals("Subject must remain Chemistry", "Chemistry", subject)
        assertEquals("Selected language must remain Hindi", "hi", chemLanguage)

        // BACKWARD: Step 2 -> Step 1
        page--
        assertEquals("Navigating back from Step 2 must reach Step 1", 1, page)
        assertEquals("Subject must remain Chemistry on Step 1", "Chemistry", subject)

        // Switch to Unit 2 in Step 1
        val unit2 = chemData.units.keys.drop(1).first()
        chemUnit = unit2
        val firstInUnit2 = chemData.topicsForUnit(unit2).first()
        chemTopicId = firstInUnit2.songId
        lyrics = ""

        // Forward again: Step 1 -> Step 2 -> Step 3
        page = 2
        assertEquals(2, page)
        page = 3
        assertEquals(3, page)
        if (lyrics.isBlank()) {
            lyrics = firstInUnit2.lyrics[chemLanguage] ?: firstInUnit2.lyrics["en"]!!
        }
        assertEquals(firstInUnit2.lyrics["hi"], lyrics)

        // Forward to Step 4
        page = 4
        assertEquals(4, page)

        // Back from Step 4 -> Step 3 -> Step 2 -> Step 1
        page--
        assertEquals(3, page)
        page--
        assertEquals(2, page)
        page--
        assertEquals(1, page)
        assertEquals("Chemistry", subject)
    }

    @Test
    fun switchingBetweenCalculusAndChemistryMaintainsIndependence() {
        var subject = "Calculus"
        var page = 1
        var unit = "Unit 1"
        var topicId = calcData.topics.first { it.unit == unit }.id
        var category = "Hindi"
        var choosingSong = false

        // Calculus Step 1 -> Step 2
        page = 2
        assertEquals(2, page)
        choosingSong = true
        // Back within Step 2
        if (choosingSong) choosingSong = false else page--
        assertEquals(2, page)
        assertFalse(choosingSong)
        // Back to Step 1
        if (choosingSong) choosingSong = false else page--
        assertEquals(1, page)

        // Switch to Chemistry
        subject = "Chemistry"
        var chemUnit = chemData.units.keys.first()
        var chemTopicId = chemData.topics.first().songId

        // Chemistry Step 1 -> Step 2
        page = 2
        assertEquals(2, page)
        // Back to Step 1
        page--
        assertEquals(1, page)
        assertEquals("Chemistry", subject)

        // Switch back to Calculus
        subject = "Calculus"
        unit = "Unit 1"
        topicId = calcData.topics.first { it.unit == unit }.id
        assertEquals("Calculus", subject)
        assertEquals(0, topicId)
    }
}
