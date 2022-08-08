package com.example.client

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer

/**
 * Desc:
 * @author lijt
 * Created on 2022/8/4 14:52
 * Email:
 */
open class WebSocketClient(uri: URI, callback: Callback) : WebSocketClient(uri) {

    private var mClientCallback: Callback? = callback

    companion object {
        private const val TAG = "TAG"
    }

    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.d(TAG, "onOpen: ")
        mClientCallback?.onOpen(handshakedata)
    }

    override fun onMessage(message: String?) {
        Log.d(TAG, "onMessage: ")
        mClientCallback?.onMessage(message)
    }

    override fun onMessage(bytes: ByteBuffer?) {
        super.onMessage(bytes)
        Log.d(TAG, "onMessage: ByteBuffer = $bytes")
        mClientCallback?.onMessage(bytes)
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Log.d(TAG, "onClose: code = $code reason = $reason remote = $remote")
        if (code == -1) {
            return
        }
        Thread {
            try {
                reconnectBlocking()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    override fun onError(ex: Exception?) {
        Log.d(TAG, "onError: error = ${ex?.message}")
    }

    interface Callback {
        fun onOpen(serverHandshake: ServerHandshake?)

        fun onMessage(message: String?)

        fun onMessage(bytes: ByteBuffer?)
    }
}