package com.anush.sia

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

object AppLauncher {

    private val aliases = mapOf(
        "instagram" to listOf("instagram", "insta", "इंस्टाग्राम", "इंस्टा", "इंस्टाग्रम", "instaa"),
        "chrome" to listOf("chrome", "क्रोम", "क्रोम", "browser", "ब्राउज़र", "ब्राउजर"),
        "youtube" to listOf("youtube", "यूट्यूब", "युटुब", "यूटयूब", "यूटूब", "युट्यूब", "यू ट्यूब", "यु ट्यूब", "you tube", "utube", "यूट्यूब"),
        "whatsapp" to listOf("whatsapp", "व्हाट्सएप", "व्हाट्सऐप", "वाट्सएप", "वॉट्सऐप", "वॉट्सएप", "व्हाट्सअप", "whats app"),
        "flipkart" to listOf("flipkart", "फ्लिपकार्ट", "फ्लिपकार्ड"),
        "snapchat" to listOf("snapchat", "स्नैपचैट", "स्नेपचैट"),
        "camera" to listOf("camera", "कैमरा", "कैमरे"),
        "settings" to listOf("settings", "सेटिंग", "सेटिंग्स", "सेटिंग्ज"),
        "gallery" to listOf("gallery", "गैलरी", "photos", "फोटो", "फोटोज"),
        "phone" to listOf("phone", "फोन", "dialer")
    )

    private val verbs = listOf(
        "kholo", "khol", "open", "chalao", "chala do", "chalu karo", "start karo",
        "खोलो", "खोल", "खोल दो", "चलाओ", "चला दो", "चालू करो", "ओपन करो", "ओपन"
    )

    fun extractAppName(spoken: String): String? {
        val text = spoken.lowercase().trim()
        if (verbs.none { text.contains(it) }) return null
        var name = text
        for (v in verbs.sortedByDescending { it.length }) {
            name = name.replace(v, " ")
        }
        name = name.replace("sia", " ").replace("सिया", " ")
            .replace("bhai", " ").replace("भाई", " ")
            .replace("app", " ").replace("ऐप", " ")
            .replace("को", " ").replace("ko", " ")
            .replace("करो", " ").replace("karo", " ")
            .trim().replace(Regex("\\s+"), " ")
        return if (name.isBlank()) null else name
    }

    fun open(context: Context, spokenName: String): String {
        val pm = context.packageManager
        val wanted = spokenName.lowercase().trim()

        var key: String? = null
        for ((k, list) in aliases) {
            if (list.any { wanted.contains(it) }) { key = k; break }
        }

        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val apps = pm.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)

        var bestPackage: String? = null
        var bestLabel = ""
        for (info in apps) {
            val label = info.loadLabel(pm).toString().lowercase()
            val pkg = info.activityInfo.packageName
            val matched = if (key != null) {
                label.contains(key) || pkg.contains(key)
            } else {
                label.contains(wanted) || wanted.contains(label)
            }
            if (matched) {
                bestPackage = pkg
                bestLabel = info.loadLabel(pm).toString()
                break
            }
        }

        if (bestPackage == null) {
            return "NOT_INSTALLED"
        }
        val launch = pm.getLaunchIntentForPackage(bestPackage) ?: return "FAILED"
        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(launch)
            "OK:$bestLabel"
        } catch (e: Exception) {
            "FAILED"
        }
    }
}
