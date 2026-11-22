package com.example.test.data.remote

import android.content.Context

object LocalApiServerFactory {
    fun create(context: Context): LocalApiServer = FakeApiServer(context).apply { start() }
}
