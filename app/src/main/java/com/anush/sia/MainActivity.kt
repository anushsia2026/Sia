package com.anush.sia

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var statusText: TextView
    private lateinit var chatText: TextView
    private lateinit var micButton: Button
    private lateinit var scroll: ScrollView
    private var tts: TextToSpeech? = null
    private var recognizer: SpeechRecognizer? = null
    private var ttsReady = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
        tts = TextToSpeech(this, this)
        ensureMicPermission()
    }

    private fun buildScreen() {
        val root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setBackgroundColor(Color.parseColor("#121212"))
        root.setPadding(40, 100, 40, 40)

        val title = TextView(this)
        title.text = "SIA"
        title.textSize = 36f
        title.setTextColor(Color.WHITE)
        title.gravity = Gravity.CENTER
        root.addView(title)

        statusText = TextView(this)
        statusText.text = "Taiyaar"
        statusText.textSize = 18f
        statusText.setTextColor(Color.parseColor("#80CBC4"))
        statusText.gravity = Gravity.CENTER
        statusText.setPadding(0, 20, 0, 20)
        root.addView(statusText)

        scroll = ScrollView(this)
        chatText = TextView(this)
        chatText.textSize = 18f
        chatText.setTextColor(Color.parseColor("#EEEEEE"))
        scroll.addView(chatText)
        val scrollParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        )
        root.addView(scroll, scrollParams)

        micButton = Button(this)
        micButton.text = "Bolo"
        micButton.textSize = 22f
        micButton.setOnClickListener { startListening() }
        root.addView(
            micButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        )

        setContentView(root)
    }

    private fun ensureMicPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.RECORD_AUDIO), 1
            )
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("hi", "IN"))
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
            if (!ttsReady) {
                addChat("Sia", "Hindi awaaz is phone me nahi mili. Settings me Hindi TTS download karna hoga.")
            }
        } else {
            addChat("Sia", "Awaaz engine shuru nahi hua.")
        }
    }

    private fun addChat(who: String, msg: String) {
        chatText.append("$who: $msg\n\n")
        scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    private fun speak(msg: String) {
        addChat("Sia", msg)
        if (ttsReady) {
            tts?.speak(msg, TextToSpeech.QUEUE_FLUSH, null, "sia")
        }
    }

    private fun setStatus(s: String) {
        statusText.text = s
    }

    private fun startListening() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            speak("Mic ki permission chahiye. Settings me Sia ko Microphone allow karo.")
            ensureMicPermission()
            return
        }
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            speak("Is phone me speech recognition available nahi hai.")
            return
        }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { setStatus("Sun rahi hoon...") }
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() { setStatus("Soch rahi hoon...") }
            override fun onError(error: Int) {
                setStatus("Taiyaar")
                speak("Mujhe samajh nahi aaya, dobara bolo.")
            }
            override fun onResults(results: Bundle?) {
                val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val heard = list?.firstOrNull()
                if (heard.isNullOrBlank()) {
                    setStatus("Taiyaar")
                    speak("Mujhe samajh nahi aaya, dobara bolo.")
                } else {
                    addChat("Aap", heard)
                    setStatus("Taiyaar")
                    speak("Maine suna: $heard")
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
        recognizer?.startListening(intent)
    }

    override fun onDestroy() {
        recognizer?.destroy()
        tts?.shutdown()
        super.onDestroy()
    }
}
