package com.example.test.data.remote

import android.content.Context

/** Release builds use the configured real backend URL; replace this sample URL for deployment. */
object LocalApiServerFactory {
    fun create(@Suppress("UNUSED_PARAMETER") context: Context): LocalApiServer =
        object : LocalApiServer {
            override val baseUrl = "https://api.example.invalid/api/"
            override fun close() = Unit
        }
}
