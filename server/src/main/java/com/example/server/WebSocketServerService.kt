package com.example.server

import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder

/**
 * Desc:
 * @author lijt
 * Created on 2022/8/3 17:20
 * Email:
 */
class WebSocketServerService : Service() {

    private var mWebSocketServer: WebSocketServer? = null

    companion object {
        private const val TAG = "TAG"
        val mClientIpList = mutableListOf<String?>()
    }


    private val mWebSocketCallback = object : WebSocketServer.Callback {
        override fun onOpen(handshake: ClientHandshake?) {

        }

        override fun onMessage(message: String?) {
            val msg = Message.obtain()
            msg.what = 0
            msg.obj = message
            ServerActivity.mHandler.sendMessage(msg)
        }

        override fun onMessage(bytes: ByteBuffer?) {
            val msg = Message.obtain()
            msg.what = 0
            msg.obj = getString(bytes)
            ServerActivity.mHandler.sendMessage(msg)
        }

        override fun addSocketClient(socketHashMap: HashMap<WebSocket?, String?>?) {
            mClientIpList.clear()
            val values = socketHashMap?.values
            if (values != null) {
                mClientIpList.addAll(values)
            }
//            ServerActivity.mHandler.sendEmptyMessage(1)
        }

        override fun clientConnect(conn: WebSocket?) {
            val msg = Message()
            msg.what = 2
            msg.obj = conn?.remoteSocketAddress?.address?.hostName
            ServerActivity.mHandler.sendMessage(msg)
        }

        override fun removeSocketClient(conn: WebSocket?) {
            val ip = conn?.remoteSocketAddress?.address?.hostAddress
            mClientIpList.remove(ip)
            val msg = Message()
            msg.what = 3
            msg.obj = ip
            ServerActivity.mHandler.sendMessage(msg)
        }
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
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer())
            charBuffer.toString()
        } catch (ex: Exception) {
            ex.printStackTrace()
            ""
        }
    }

    override fun onCreate() {
        super.onCreate()
        start()
    }

    inner class WebSocketServerBinder : Binder() {
        fun getService(): WebSocketServerService {
            return this@WebSocketServerService
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return WebSocketServerBinder()
    }

    fun start() {
        if (mWebSocketServer == null) {
            mWebSocketServer = WebSocketServer("192.168.2.8", 8080, mWebSocketCallback)
            mWebSocketServer?.start()
        }
    }

    fun stop() {
        try {
            mWebSocketServer?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mWebSocketServer = null
        Log.d(TAG, "stop: 停止WebSocketServer")
    }

    fun sendMessage(message: String) {
        Log.d(TAG, "sendMessage: $message")
        mWebSocketServer?.send(message)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mWebSocketServer?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mWebSocketServer = null
        Log.d(TAG, "onDestroy: ")
    }
}