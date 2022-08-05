package com.example.client

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder
import java.util.concurrent.TimeUnit

/**
 * Desc:
 * @author lijt
 * Created on 2022/8/4 14:56
 * Email:
 */
class WebSocketClientService : Service() {

    companion object {
        private const val TAG = "TAG"
    }

    private var mWebSocketClient: JWebSocketClient? = null

    override fun onCreate() {
        super.onCreate()
        initSocketClient()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private val mClientCallback = object : JWebSocketClient.Callback {
        override fun onOpen(serverHandshake: ServerHandshake?) {

        }

        override fun onMessage(message: String?) {
            val msg = Message.obtain()
            msg.what = 0
            msg.obj = message
            MainActivity.mHandler.sendMessage(msg)
        }

        override fun onMessage(bytes: ByteBuffer?) {
            val msg = Message.obtain()
            msg.what = 0
            msg.obj = getString(bytes)
            MainActivity.mHandler.sendMessage(msg)
        }
    }

    private fun initSocketClient() {
        mWebSocketClient = JWebSocketClient(URI.create("ws://192.168.1.147:9999"), mClientCallback)
        try {
            mWebSocketClient?.connectBlocking(3, TimeUnit.SECONDS)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    inner class JWebSocketClientBinder : Binder() {
        fun getService(): WebSocketClientService {
            return this@WebSocketClientService
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return JWebSocketClientBinder()
    }

    fun sendMessage(message: String) {
        if (mWebSocketClient != null && mWebSocketClient?.isOpen == true) {
            Log.d(TAG, "sendMessage: $message")
            mWebSocketClient?.send(message)
        }
    }

    private fun closeConnect() {
        try {
            if (mWebSocketClient != null) {
                mWebSocketClient?.closeBlocking()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mWebSocketClient = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        closeConnect()
    }

    /**
     * 将ByteBuffer转换为String
     *
     * @param buffer
     * @return
     */
    fun getString(buffer: ByteBuffer?): String? {
        if (buffer == null) return null
        val charset: Charset?
        val decoder: CharsetDecoder?
        val charBuffer: CharBuffer?
        return try {
            charset = Charset.forName("UTF-8")
            decoder = charset.newDecoder()
            // charBuffer = decoder.decode(buffer);//用这个的话，只能输出来一次结果，第二次显示为空
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer())
            charBuffer.toString()
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
            ""
        }
    }

    private var listener: OnMessageListener? = null

    fun setOnMessageListener(listener: OnMessageListener) {
        this.listener = listener
    }

    interface OnMessageListener {
        fun loadMessage(message: String?)
    }
}