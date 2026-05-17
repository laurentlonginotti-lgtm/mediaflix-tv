package com.mediaflix.tv

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class PinActivity : AppCompatActivity() {

    private val pin = StringBuilder()
    private lateinit var dots: List<TextView>
    private lateinit var errorText: TextView
    private lateinit var loadingView: View
    private val SERVER_URL = "https://flix-mediabox.ddns.net:9443"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)

        dots = listOf(
            findViewById(R.id.dot1),
            findViewById(R.id.dot2),
            findViewById(R.id.dot3),
            findViewById(R.id.dot4)
        )
        errorText = findViewById(R.id.errorText)
        loadingView = findViewById(R.id.loadingView)

        val numMap = mapOf(
            R.id.btn0 to "0", R.id.btn1 to "1", R.id.btn2 to "2",
            R.id.btn3 to "3", R.id.btn4 to "4", R.id.btn5 to "5",
            R.id.btn6 to "6", R.id.btn7 to "7", R.id.btn8 to "8",
            R.id.btn9 to "9"
        )
        numMap.forEach { (id, digit) ->
            findViewById<Button>(id).setOnClickListener { addDigit(digit) }
        }
        findViewById<Button>(R.id.btnDel).setOnClickListener { removeDigit() }
        updateDots()
    }

    private fun addDigit(digit: String) {
        if (pin.length < 4) {
            pin.append(digit)
            errorText.visibility = View.INVISIBLE
            updateDots()
            if (pin.length == 4) validatePin()
        }
    }

    private fun removeDigit() {
        if (pin.isNotEmpty()) {
            pin.deleteCharAt(pin.length - 1)
            updateDots()
        }
    }

    private fun updateDots() {
        dots.forEachIndexed { i, tv ->
            tv.text = if (i < pin.length) "●" else "○"
        }
    }

    private fun validatePin() {
        loadingView.visibility = View.VISIBLE

        val body = """{"pin":"$pin"}""".toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url("$SERVER_URL/api/pin-login")
            .post(body)
            .build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                runOnUiThread { showError("Erreur réseau") }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    // Injecter les cookies de session dans le WebView
                    val cookieManager = CookieManager.getInstance()
                    response.headers.values("Set-Cookie").forEach { cookie ->
                        cookieManager.setCookie(SERVER_URL, cookie)
                    }
                    cookieManager.flush()

                    runOnUiThread {
                        startActivity(Intent(this@PinActivity, MainActivity::class.java))
                        finish()
                    }
                } else {
                    runOnUiThread { showError("PIN incorrect") }
                }
                response.close()
            }
        })
    }

    private fun showError(msg: String) {
        loadingView.visibility = View.GONE
        errorText.text = msg
        errorText.visibility = View.VISIBLE
        pin.clear()
        updateDots()
    }

    // Support touches numériques de la télécommande
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        val digit = when (keyCode) {
            KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_NUMPAD_0 -> "0"
            KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_NUMPAD_1 -> "1"
            KeyEvent.KEYCODE_2, KeyEvent.KEYCODE_NUMPAD_2 -> "2"
            KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_NUMPAD_3 -> "3"
            KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_NUMPAD_4 -> "4"
            KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_NUMPAD_5 -> "5"
            KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_NUMPAD_6 -> "6"
            KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_NUMPAD_7 -> "7"
            KeyEvent.KEYCODE_8, KeyEvent.KEYCODE_NUMPAD_8 -> "8"
            KeyEvent.KEYCODE_9, KeyEvent.KEYCODE_NUMPAD_9 -> "9"
            KeyEvent.KEYCODE_DEL -> { removeDigit(); return true }
            else -> return super.onKeyDown(keyCode, event)
        }
        addDigit(digit)
        return true
    }
}
