package com.example.medishare.utils

import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private const val TAG = "ApiManager"

suspend fun fetchWikiSummary(term: String): String {
    val encodedTerm = URLEncoder.encode(term, "UTF-8")
    val lang = if (isEnglish(term)) "en" else "he"
    val url = "https://$lang.wikipedia.org/api/rest_v1/page/summary/$encodedTerm"

    return try {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connect()

        if (connection.responseCode == 200) {
            val response = connection.inputStream.bufferedReader().use { it.readText() }
            Log.d("WikiAPI", "[$lang] Response for $term: $response")

            val json = JSONObject(response)
            val extract = json.optString("extract", "")
            if (extract.isNotEmpty()) extract else "לא נמצא הסבר למונח \"$term\"."
        } else {
            "לא נמצא הסבר למונח \"$term\"."
        }
    } catch (e: Exception) {
        Log.e("WikiAPI", "שגיאה בשליפת הסבר למונח $term: ${e.message}")
        "שגיאה בחיפוש ההסבר למונח \"$term\"."
    }
}

fun isEnglish(text: String): Boolean {
    return text.matches(Regex("^[a-zA-Z0-9\\s]+\$"))
}
