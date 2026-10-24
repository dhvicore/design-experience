package com.nmims.songstudio.auth

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

data class Student(val id: Long, val name: String, val email: String)

/** Device-local accounts. No SAP/portal authentication or persisted sign-in token. */
class AccountStore(context: Context, databaseName: String = "student_accounts.db") : SQLiteOpenHelper(context, databaseName, null, 1) {
    private val preferences = context.applicationContext.getSharedPreferences("${databaseName}_login", Context.MODE_PRIVATE)

    fun rememberedEmail(): String = preferences.getString("email", null) ?: readableDatabase.query(
        "accounts", arrayOf("email"), null, null, null, null, "id DESC", "1"
    ).use { rows -> if (rows.moveToFirst()) rows.getString(0) else "" }

    private fun rememberEmail(email: String) {
        preferences.edit().putString("email", email).commit()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE accounts (id INTEGER PRIMARY KEY, name TEXT NOT NULL, email TEXT NOT NULL UNIQUE, salt TEXT NOT NULL, password_hash TEXT NOT NULL)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit

    fun signUp(name: String, email: String, password: String) {
        require(name.trim().length >= 2) { "Enter your full name." }
        val normalized = email.trim().lowercase(java.util.Locale.ROOT)
        require(android.util.Patterns.EMAIL_ADDRESS.matcher(normalized).matches()) { "Enter a valid email address." }
        require(password.length >= 8) { "Use at least 8 characters for your password." }
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val values = ContentValues().apply {
            put("name", name.trim()); put("email", normalized)
            put("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            put("password_hash", Base64.encodeToString(hash(password, salt), Base64.NO_WRAP))
        }
        try { writableDatabase.insertOrThrow("accounts", null, values) }
        catch (_: SQLiteConstraintException) { throw IllegalArgumentException("An account already exists with this email. Please log in.") }
        rememberEmail(normalized)
    }

    fun login(email: String, password: String): Student {
        val normalized = email.trim().lowercase(java.util.Locale.ROOT)
        readableDatabase.query("accounts", null, "email = ?", arrayOf(normalized), null, null, null).use { rows ->
            val exists = rows.moveToFirst()
            val salt = if (exists) Base64.decode(rows.getString(rows.getColumnIndexOrThrow("salt")), Base64.NO_WRAP) else ByteArray(16)
            val expected = if (exists) Base64.decode(rows.getString(rows.getColumnIndexOrThrow("password_hash")), Base64.NO_WRAP) else ByteArray(32)
            val valid = MessageDigest.isEqual(hash(password, salt), expected)
            require(exists && valid) { "Email or password is incorrect." }
            rememberEmail(normalized)
            return Student(rows.getLong(rows.getColumnIndexOrThrow("id")),
                rows.getString(rows.getColumnIndexOrThrow("name")), rows.getString(rows.getColumnIndexOrThrow("email")))
        }
    }

    private fun hash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 210_000, 256)
        return try { SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded }
        finally { spec.clearPassword() }
    }
}
