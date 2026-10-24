package com.nmims.songstudio

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.nmims.songstudio.auth.AccountStore
import com.nmims.songstudio.auth.SessionViewModel
import com.nmims.songstudio.auth.Student
import com.nmims.songstudio.data.Curriculum
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class AccountAndCurriculumTest {
    @Test fun persistentAccountsRejectWrongPasswordsAndDuplicates() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = "test_accounts_${UUID.randomUUID()}.db"
        try {
            AccountStore(context, db).use { it.signUp("Test Student", "student@example.test", "test-password-123") }
            AccountStore(context, db).use { store ->
                assertEquals("Test Student", store.login(" STUDENT@example.test ", "test-password-123").name)
                assertThrows(IllegalArgumentException::class.java) { store.login("student@example.test", "wrong-password") }
                assertThrows(IllegalArgumentException::class.java) { store.login("missing@example.test", "wrong-password") }
                assertThrows(IllegalArgumentException::class.java) { store.signUp("Duplicate", "STUDENT@example.test", "another-password") }
                assertThrows(IllegalArgumentException::class.java) { store.signUp("Student", "bad-email", "test-password-123") }
                assertThrows(IllegalArgumentException::class.java) { store.signUp("Student", "new@example.test", "short") }
            }
        } finally { context.deleteDatabase(db) }
    }

    @Test fun emailSurvivesStoreRecreationAndFailedLoginsDoNotReplaceIt() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = "test_remembered_email_${UUID.randomUUID()}.db"
        try {
            AccountStore(context, db).use { store ->
                assertEquals("", store.rememberedEmail())
                store.signUp("Test Student", " FIRST@example.test ", "test-password-123")
            }
            AccountStore(context, db).use { store ->
                assertEquals("first@example.test", store.rememberedEmail())
                store.signUp("Other Student", "second@example.test", "another-password")
                store.login("FIRST@example.test", "test-password-123")
            }
            AccountStore(context, db).use { store ->
                assertEquals("first@example.test", store.rememberedEmail())
                assertThrows(IllegalArgumentException::class.java) { store.login("second@example.test", "wrong-password") }
                assertEquals("first@example.test", store.rememberedEmail())
            }
            // Upgrade path: existing account databases predate the remembered-email preference.
            context.getSharedPreferences("${db}_login", android.content.Context.MODE_PRIVATE).edit().clear().commit()
            AccountStore(context, db).use { assertEquals("second@example.test", it.rememberedEmail()) }
        } finally {
            context.deleteDatabase(db)
            context.getSharedPreferences("${db}_login", android.content.Context.MODE_PRIVATE).edit().clear().commit()
        }
    }

    @Test fun backgroundingClearsLoginWhileRotationKeepsIt() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            var state: SessionViewModel? = null
            scenario.onActivity { activity ->
                state = ViewModelProvider(activity)[SessionViewModel::class.java]
                state!!.signIn(Student(1, "Test Student", "test@example.test"))
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                state = ViewModelProvider(activity)[SessionViewModel::class.java]
                assertNotNull(state!!.student)
            }
            scenario.moveToState(Lifecycle.State.CREATED)
            instrumentation.runOnMainSync { assertNull(state!!.student) }
            scenario.moveToState(Lifecycle.State.RESUMED)
            scenario.onActivity { assertNull(ViewModelProvider(it)[SessionViewModel::class.java].student) }
        }
    }

    @Test fun allCalculusUnitsAndMusicChoicesAreAvailable() {
        val data = Curriculum(InstrumentationRegistry.getInstrumentation().targetContext)
        assertEquals(38, data.topics.size)
        assertEquals(listOf(9, 5, 5, 8, 11), data.units.keys.map { unit -> data.topics.count { it.unit == unit } })
        assertTrue(data.topics.all { it.concept.isNotBlank() && it.formula.isNotBlank() })
        assertTrue(data.styles("en").any { it.name == "Rock" && it.songs.size > 1 })
        assertTrue(data.styles("en").any { it.name == "Lo-fi" && it.songs.size > 1 })
        assertTrue(data.styles("hi").any { it.name == "Bhajan" && it.songs.size > 1 })
        assertTrue(data.collections.keys.all { data.styles(it).all { style -> style.songs.isNotEmpty() } })
    }
}
