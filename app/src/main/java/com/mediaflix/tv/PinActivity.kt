package com.mediaflix.tv

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.view.KeyEvent
import android.view.View
import android.webkit.CookieManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class PinActivity : AppCompatActivity() {

    private val pin = StringBuilder()
    private lateinit var dots: List<TextView>
    private lateinit var errorText: TextView
    private lateinit var loadingView: View

    private var isValidating = false
    private var lastSubmitMs = 0L

    private companion object {
        const val PIN_LENGTH = 4
        const val MIN_SUBMIT_INTERVAL_MS = 1000L
        val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()
    }

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
        if (isValidating) return
        if (pin.length >= PIN_LENGTH) return

        pin.append(digit)
        errorText.visibility = View.INVISIBLE
        updateDots()
        if (pin.length == PIN_LENGTH) validatePin()
    }

    private fun removeDigit() {
        if (isValidating) return
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
        val now = SystemClock.elapsedRealtime()
        if (now - lastSubmitMs < MIN_SUBMIT_INTERVAL_MS) return
        lastSubmitMs = now

        isValidating = true
        loadingView.visibility = View.VISIBLE

        val json = JSONObject().put("pin", pin.toString()).toString()
        val body = json.toRequestBody(JSON_MEDIA)
        val request = Request.Builder()
            .url("${Config.SERVER_URL}/api/pin-login")
            .post(body)
            .build()

        Config.httpClient.newCall(request).enqueue(object : Callback {

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread { showError(getString(R.string.error_network)) }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { resp ->
                    if (resp.isSuccessful) {
                        val cm = CookieManager.getInstance()
                        resp.headers.values("Set-Cookie").forEach { cookie ->
                            cm.setCookie(Config.SERVER_URL, cookie)
                        }
                        cm.flush()
                        runOnUiThread {
                            clearPin()
                            startActivity(Intent(this@PinActivity, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        runOnUiThread { showError(getString(R.string.error_wrong_pin)) }
                    }
                }
            }
        })
    }

    private fun showError(msg: String) {
        loadingView.visibility = View.GONE
        errorText.text = msg
        errorText.visibility = View.VISIBLE
        clearPin()
        isValidating = false
    }

    private fun clearPin() {
        pin.setLength(0)
        updateDots()
    }

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
