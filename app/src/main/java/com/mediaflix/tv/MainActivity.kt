package com.mediaflix.tv

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.KeyEvent
import android.view.WindowManager
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var fullscreenContainer: FrameLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var errorView: TextView
    private var customView: View? = null
    private var customViewCallback: WebChromeClient.CustomViewCallback? = null

    private val allowedPermissions = setOf(
        PermissionRequest.RESOURCE_VIDEO_CAPTURE,
        PermissionRequest.RESOURCE_AUDIO_CAPTURE,
        PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID
    )

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        fullscreenContainer = findViewById(R.id.fullscreenContainer)
        progressBar = findViewById(R.id.progressBar)
        errorView = findViewById(R.id.errorView)

        with(webView.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            mediaPlaybackRequiresUserGesture = false
            // Sécurité : pas de contenu HTTP dans une page HTTPS
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            useWideViewPort = true
            loadWithOverviewMode = true
            // NE PAS mettre setSupportZoom(false) : ça désactivait le mécanisme
            // interne de focus/clavier sur certains WebView → les inputs
            // ne recevaient plus le focus. On cache juste les contrôles visuels.
            setSupportZoom(true)
            builtInZoomControls = false
            displayZoomControls = false
            userAgentString = userAgentString + " MediaflixTV/1.0"
        }

        webView.isFocusable = true
        webView.isFocusableInTouchMode = true
        webView.requestFocus()

        CookieManager.getInstance().apply {
            setAcceptCookie(true)
            setAcceptThirdPartyCookies(webView, true)
        }

        webView.webViewClient = object : WebViewClient() {

            // Filtre de domaine : tout lien hors flix-mediabox.ddns.net est ignoré
            override fun shouldOverrideUrlLoading(
                view: WebView, request: WebResourceRequest
            ): Boolean = !Config.isInternalUrl(request.url?.toString())

            override fun onPageFinished(view: WebView, url: String) {
                progressBar.visibility = View.GONE
            }

            override fun onReceivedError(
                view: WebView, request: WebResourceRequest, error: WebResourceError
            ) {
                if (request.isForMainFrame) {
                    runOnUiThread { showServerUnavailable() }
                }
            }
        }

        webView.addJavascriptInterface(object : Any() {
            @JavascriptInterface
            fun goToPin() {
                runOnUiThread { goToPinScreen() }
            }
        }, "MediaflixTV")

        webView.webChromeClient = object : WebChromeClient() {

            override fun onShowCustomView(view: View, callback: CustomViewCallback) {
                customView = view
                customViewCallback = callback
                fullscreenContainer.addView(view)
                fullscreenContainer.visibility = View.VISIBLE
                webView.visibility = View.GONE
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }

            override fun onHideCustomView() {
                fullscreenContainer.visibility = View.GONE
                fullscreenContainer.removeAllViews()
                webView.visibility = View.VISIBLE
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                customView = null
                customViewCallback?.onCustomViewHidden()
                customViewCallback = null
            }

            override fun onPermissionRequest(request: PermissionRequest) {
                if (!Config.isInternalUrl(request.origin?.toString())) {
                    request.deny()
                    return
                }
                val granted = request.resources.filter { it in allowedPermissions }.toTypedArray()
                if (granted.isNotEmpty()) request.grant(granted) else request.deny()
            }
        }

        loadServer()
    }

    private fun loadServer() {
        errorView.visibility = View.GONE
        progressBar.visibility = View.VISIBLE
        webView.visibility = View.VISIBLE
        webView.loadUrl(Config.SERVER_URL)
    }

    private fun showServerUnavailable() {
        progressBar.visibility = View.GONE
        webView.visibility = View.GONE
        errorView.text = getString(R.string.error_server_unavailable)
        errorView.visibility = View.VISIBLE
    }

    private fun goToPinScreen() {
        startActivity(Intent(this, PinActivity::class.java))
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 1. Fermer la vidéo plein écran si ouverte
            if (customView != null) {
                webView.webChromeClient?.onHideCustomView()
                return true
            }
            // 2. Si une page d'erreur est affichée, retenter
            if (errorView.visibility == View.VISIBLE) {
                loadServer()
                return true
            }
            // 3. Navigation arrière dans la WebView si possible
            if (webView.canGoBack()) {
                webView.goBack()
                return true
            }
            // 4. Sinon, verrouillage → écran PIN
            goToPinScreen()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        webView.onPause()
    }

    override fun onResume() {
        super.onResume()
        webView.onResume()
    }

    override fun onDestroy() {
        webView.destroy()
        super.onDestroy()
    }
}
