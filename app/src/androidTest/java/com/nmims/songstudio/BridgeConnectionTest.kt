package com.nmims.songstudio

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.net.HttpURLConnection
import java.net.URL

/** Opt-in check of the real emulator-to-Mac route; does not submit a GPU job. */
@RunWith(AndroidJUnit4::class)
class BridgeConnectionTest {
    @Test fun emulatorReachesRunningMusicBridge() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("musicBridgeTest") == "true")
        val connection = URL("http://10.0.2.2:8787/health").openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 5000; connection.readTimeout = 5000
            assertEquals(200, connection.responseCode)
            val response = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            assertTrue(response.getBoolean("ok"))
            assertTrue(response.getString("provider").contains("ACE-Step"))
        } finally { connection.disconnect() }
    }
}
