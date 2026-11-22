package com.example.test.data.remote

import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets

/** Tiny loopback HTTP server for the demo. It only implements GET /api/equipment. */
class FakeApiServer {
    @Volatile
    private var serverSocket: ServerSocket? = null

    val baseUrl: String
        get() = checkNotNull(serverSocket) { "Fake API server is not started" }
            .let { "http://127.0.0.1:${it.localPort}/api/" }

    fun start() {
        if (serverSocket != null) return
        synchronized(this) {
            if (serverSocket != null) return
            val socket = ServerSocket(0, 16, InetAddress.getByName("127.0.0.1"))
            serverSocket = socket
            Thread({ acceptRequests(socket) }, "vnpt-fake-api").apply {
                isDaemon = true
                start()
            }
            Log.i(TAG, "Fake HTTP API listening at $baseUrl")
        }
    }

    fun stop() {
        serverSocket?.close()
        serverSocket = null
    }

    private fun acceptRequests(server: ServerSocket) {
        while (!server.isClosed) {
            try {
                server.accept().use(::handleRequest)
            } catch (error: Exception) {
                if (!server.isClosed) Log.e(TAG, "Fake API request failed", error)
            }
        }
    }

    private fun handleRequest(socket: Socket) {
        val reader = BufferedReader(InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        val requestLine = reader.readLine().orEmpty()
        while (!reader.readLine().isNullOrEmpty()) Unit

        val (status, body) = if (requestLine.startsWith("GET /api/equipment ")) {
            Log.i(TAG, "GET /api/equipment")
            200 to EQUIPMENT_JSON
        } else {
            Log.w(TAG, "Unhandled fake API request: $requestLine")
            404 to "{\"message\":\"Not found\"}"
        }
        val bytes = body.toByteArray(StandardCharsets.UTF_8)
        val reason = if (status == 200) "OK" else "Not Found"
        val headers = "HTTP/1.1 $status $reason\r\n" +
            "Content-Type: application/json; charset=utf-8\r\n" +
            "Content-Length: ${bytes.size}\r\n" +
            "Connection: close\r\n\r\n"
        socket.getOutputStream().apply {
            write(headers.toByteArray(StandardCharsets.US_ASCII))
            write(bytes)
            flush()
        }
    }

    private companion object {
        const val TAG = "VNPT-FakeAPI"
        const val EQUIPMENT_JSON = """[
            {"id":1,"name":"Modem Quang iGate","code":"MDM-001","unit":"Cái","stock":50},
            {"id":2,"name":"Switch 8-Port","code":"SWT-008","unit":"Cái","stock":20},
            {"id":3,"name":"Kìm bấm mạng","code":"KIM-001","unit":"Bộ","stock":10}
        ]"""
    }
}
