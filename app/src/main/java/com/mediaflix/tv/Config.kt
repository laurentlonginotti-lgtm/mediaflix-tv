package com.mediaflix.tv

import android.net.Uri
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object Config {
    const val SERVER_URL = "https://flix-mediabox.ddns.net:9443"
    val SERVER_HOST: String = Uri.parse(SERVER_URL).host ?: ""

    val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    fun isInternalUrl(url: String?): Boolean {
        if (url.isNullOrBlank()) return false
        val host = Uri.parse(url).host ?: return false
        return host.equals(SERVER_HOST, ignoreCase = true) ||
                host.endsWith(".$SERVER_HOST", ignoreCase = true)
    }
}
