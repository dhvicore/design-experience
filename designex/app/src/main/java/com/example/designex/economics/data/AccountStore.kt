package com.example.designex.economics.data

import android.content.Context
import android.util.Base64
import org.json.JSONObject
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Student accounts kept on this phone only. Passwords are stored as salted PBKDF2 hashes. */
class AccountStore(context: Context) {
    private val prefs = context.getSharedPreferences("accounts", Context.MODE_PRIVATE)

    sealed interface Result {
        data class Ok(val name: String, val email: String) : Result
        data class Error(val reason: Reason) : Result
    }

    enum class Reason { BAD_EMAIL, SHORT_PASSWORD, MISSING_PASSWORD, MISSING_NAME, EXISTS, NOT_FOUND, WRONG_PASSWORD }

    val lastEmail: String get() = prefs.getString(KEY_LAST, "").orEmpty()

    fun signUp(name: String, email: String, password: String): Result {
        val e = email.trim().lowercase()
        when {
            name.isBlank() -> return Result.Error(Reason.MISSING_NAME)
            !EMAIL.matches(e) -> return Result.Error(Reason.BAD_EMAIL)
            password.length < 6 -> return Result.Error(Reason.SHORT_PASSWORD)
            prefs.contains(key(e)) -> return Result.Error(Reason.EXISTS)
        }
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val record = JSONObject()
            .put("name", name.trim())
            .put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .put("hash", Base64.encodeToString(hash(password, salt), Base64.NO_WRAP))
        prefs.edit().putString(key(e), record.toString()).putString(KEY_LAST, e).apply()
        return Result.Ok(name.trim(), e)
    }

    fun logIn(email: String, password: String): Result {
        val e = email.trim().lowercase()
        if (!EMAIL.matches(e)) return Result.Error(Reason.BAD_EMAIL)
        if (password.isEmpty()) return Result.Error(Reason.MISSING_PASSWORD)
        val record = prefs.getString(key(e), null)?.let(::JSONObject) ?: return Result.Error(Reason.NOT_FOUND)
        val salt = Base64.decode(record.getString("salt"), Base64.NO_WRAP)
        val expected = Base64.decode(record.getString("hash"), Base64.NO_WRAP)
        if (!hash(password, salt).contentEquals(expected)) return Result.Error(Reason.WRONG_PASSWORD)
        prefs.edit().putString(KEY_LAST, e).apply()
        return Result.Ok(record.getString("name"), e)
    }

    private fun hash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 120_000, 256)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
    }

    private fun key(email: String) = "acct_$email"

    private companion object {
        const val KEY_LAST = "last_email"
        val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}

/** Suno connection settings. The API key stays in this phone's private app storage and is never part of the code. */
class SunoSettings(context: Context) {
    private val prefs = context.getSharedPreferences("suno_settings", Context.MODE_PRIVATE)

    var apiKey: String
        get() = prefs.getString("api_key", "").orEmpty()
        set(value) = prefs.edit().putString("api_key", value.trim()).apply()

    var baseUrl: String
        get() = prefs.getString("base_url", DEFAULT_BASE_URL).orEmpty().ifBlank { DEFAULT_BASE_URL }
        set(value) = prefs.edit().putString("base_url", value.trim().trimEnd('/')).apply()

    val isConfigured: Boolean get() = apiKey.isNotBlank()

    companion object {
        const val DEFAULT_BASE_URL = "https://api.sunoapi.org"
    }
}
