package com.anush.sia

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object GeminiClient {

    private const val MODEL = "gemini-flash-latest"

    private const val SYSTEM_PROMPT =
        "Tum Sia ho, ek dost jaisi female voice assistant. " +
        "User Hindi ya Hinglish me bolta hai. Jawab chhota rakho, 1 se 3 vakya me, " +
        "saral Hindi me, Devanagari lipi me. Koi star, hash ya markdown mat use karo. " +
        "Agar jawab pata nahi to saaf bolo ki pata nahi."

    fun ask(question: String): String {
        var last = "ERR:NET"
        for (attempt in 1..3) {
            last = askOnce(question)
            val retryable = last == "ERR:503" || last == "ERR:429" || last == "ERR:500"
            if (!retryable) return last
            try {
                Thread.sleep(1500L * attempt)
            } catch (e: InterruptedException) {
                return last
            }
        }
        return last
    }

    private fun askOnce(question: String): String {
        val key = BuildConfig.GEMINI_API_KEY
        if (key.isBlank()) return "ERR:NO_KEY"
        return try {
            val url = URL(
                "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"
            )
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("x-goog-api-key", key)
            conn.connectTimeout = 15000
            conn.readTimeout = 30000
            conn.doOutput = true

            val body = JSONObject()
            body.put(
                "system_instruction",
                JSONObject().put(
                    "parts", JSONArray().put(JSONObject().put("text", SYSTEM_PROMPT))
                )
            )
            body.put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts", JSONArray().put(JSONObject().put("text", question))
                    )
                )
            )

            conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) return "ERR:$code"

            val json = JSONObject(text)
            val parts = json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
            val sb = StringBuilder()
            for (i in 0 until parts.length()) {
                sb.append(parts.getJSONObject(i).optString("text", ""))
            }
            sb.toString().trim()
        } catch (e: Exception) {
            "ERR:NET"
        }
    }
}
