package com.example.designex.economics.audio

import com.example.designex.economics.data.SunoSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Talks to a Suno API provider (request shape of sunoapi.org): submit lyrics + style, poll until the
 * song is ready, then download the audio. Suno has no official public API, so a provider key is needed.
 */
class SunoClient(private val settings: SunoSettings) {

    class SunoException(message: String) : IOException(message)

    /** Returns the downloaded song file. [onStatus] receives short progress text. */
    suspend fun generate(
        lyrics: String,
        style: String,
        title: String,
        outFile: File,
        onStatus: (String) -> Unit,
    ): File = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("customMode", true)
            .put("instrumental", false)
            .put("model", "V4_5")
            .put("prompt", lyrics.take(3000))
            .put("style", style.take(200))
            .put("title", title.take(80))
            .put("callBackUrl", "https://example.com/suno-callback")
        onStatus("Sending lyrics to Suno")
        val created = request("POST", "/api/v1/generate", body)
        val taskId = findString(created, "taskId", "task_id")
            ?: throw SunoException(findString(created, "msg", "message") ?: "Suno did not return a task id")

        val started = System.currentTimeMillis()
        var audioUrl: String? = null
        while (audioUrl == null) {
            delay(5_000)
            val elapsed = (System.currentTimeMillis() - started) / 1000
            if (elapsed > 300) throw SunoException("Suno took longer than 5 minutes")
            onStatus("Suno is composing… ${elapsed / 60}:${"%02d".format(elapsed % 60)}")
            val info = request("GET", "/api/v1/generate/record-info?taskId=" + URLEncoder.encode(taskId, "UTF-8"), null)
            val status = findString(info, "status").orEmpty().uppercase()
            if ("FAIL" in status || "ERROR" in status) {
                throw SunoException(findString(info, "errorMessage", "msg") ?: "Suno could not make this song ($status)")
            }
            if (status == "SUCCESS" || status == "FIRST_SUCCESS") {
                audioUrl = findString(info, "audioUrl", "audio_url", "streamAudioUrl", "stream_audio_url")
            }
        }

        onStatus("Downloading your song")
        val conn = URL(audioUrl).openConnection() as HttpURLConnection
        conn.connectTimeout = 20_000
        conn.readTimeout = 60_000
        try {
            if (conn.responseCode !in 200..299) throw SunoException("Download failed (${conn.responseCode})")
            conn.inputStream.use { input -> outFile.outputStream().use { input.copyTo(it) } }
        } finally {
            conn.disconnect()
        }
        outFile
    }

    private fun request(method: String, path: String, body: JSONObject?): JSONObject {
        if (!settings.isConfigured) throw SunoException("Add your Suno API key in Settings first")
        val conn = URL(settings.baseUrl + path).openConnection() as HttpURLConnection
        conn.requestMethod = method
        conn.connectTimeout = 20_000
        conn.readTimeout = 30_000
        conn.setRequestProperty("Authorization", "Bearer ${settings.apiKey}")
        conn.setRequestProperty("Accept", "application/json")
        try {
            if (body != null) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json")
                conn.outputStream.use { it.write(body.toString().toByteArray()) }
            }
            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = runCatching { JSONObject(text) }.getOrElse { JSONObject() }
            if (code == 401 || code == 403) throw SunoException("Suno rejected the API key")
            if (code !in 200..299) throw SunoException(findString(json, "msg", "message") ?: "Suno error $code")
            val apiCode = json.optInt("code", 200)
            if (apiCode != 200) throw SunoException(findString(json, "msg", "message") ?: "Suno error $apiCode")
            return json
        } finally {
            conn.disconnect()
        }
    }

    /** Depth-first search for the first non-blank string under any of [keys]; tolerant of provider differences. */
    private fun findString(node: Any?, vararg keys: String): String? {
        when (node) {
            is JSONObject -> {
                for (k in keys) node.optString(k).takeIf { node.has(k) && it.isNotBlank() && it != "null" }?.let { return it }
                for (k in node.keys()) findString(node.opt(k), *keys)?.let { return it }
            }
            is JSONArray -> for (i in 0 until node.length()) findString(node.opt(i), *keys)?.let { return it }
        }
        return null
    }
}
