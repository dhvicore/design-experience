package com.example.designex.economics.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.security.MessageDigest

/** The signed-in student's progress: learned topics, last topic, quiz bests, subject and language. */
class ProgressStore(private val context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("progress_guest", Context.MODE_PRIVATE)

    var userName by mutableStateOf<String?>(null)
        private set
    var learned by mutableStateOf(emptySet<String>())
        private set
    var lastTopicId by mutableStateOf<String?>(null)
        private set
    var language by mutableStateOf(Lang.EN)
        private set
    var subjectId by mutableStateOf("economics")
        private set
    var onboarded by mutableStateOf(false)
        private set
    private val quizBest = mutableStateMapOf<String, Int>()

    fun signIn(name: String, email: String) {
        val id = MessageDigest.getInstance("SHA-1").digest(email.toByteArray()).take(8).joinToString("") { "%02x".format(it) }
        prefs = context.getSharedPreferences("progress_$id", Context.MODE_PRIVATE)
        userName = name
        learned = prefs.getStringSet(KEY_LEARNED, emptySet())!!.toSet()
        lastTopicId = prefs.getString(KEY_LAST, null)
        language = Lang.entries.firstOrNull { it.name == prefs.getString(KEY_LANG, null) } ?: Lang.EN
        subjectId = prefs.getString(KEY_SUBJECT, "economics")!!
        onboarded = prefs.getBoolean(KEY_ONBOARDED, false)
        quizBest.clear()
    }

    fun finishOnboarding() {
        onboarded = true
        prefs.edit().putBoolean(KEY_ONBOARDED, true).apply()
    }

    fun signOut() {
        userName = null
    }

    fun toggleLearned(topicId: String): Boolean {
        val nowLearned = topicId !in learned
        learned = if (nowLearned) learned + topicId else learned - topicId
        prefs.edit().putStringSet(KEY_LEARNED, learned).apply()
        return nowLearned
    }

    fun opened(topicId: String) {
        lastTopicId = topicId
        prefs.edit().putString(KEY_LAST, topicId).apply()
    }

    fun bestQuizScore(subject: String): Int = quizBest.getOrPut(subject) { prefs.getInt("$KEY_QUIZ$subject", 0) }

    fun recordQuiz(subject: String, score: Int) {
        if (score > bestQuizScore(subject)) {
            quizBest[subject] = score
            prefs.edit().putInt("$KEY_QUIZ$subject", score).apply()
        }
    }

    fun changeLanguage(lang: Lang) {
        language = lang
        prefs.edit().putString(KEY_LANG, lang.name).apply()
    }

    fun chooseSubject(id: String) {
        subjectId = id
        prefs.edit().putString(KEY_SUBJECT, id).apply()
    }

    private companion object {
        const val KEY_LEARNED = "learned"
        const val KEY_LAST = "last_topic"
        const val KEY_QUIZ = "best_quiz_"
        const val KEY_LANG = "language"
        const val KEY_SUBJECT = "subject"
        const val KEY_ONBOARDED = "onboarded"
    }
}
