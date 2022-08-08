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

    companion object {
        private const val TAG = "TAG"
        val mClientIpList = mutableListOf<String?>()
        val mService = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    1 -> {
                        val message = msg.obj as String
                        mWebSocketServer?.send(mClientIpList.toList(), message)
                    }
                }
            }
        }
        var mWebSocketServer: WebSocketServer? = null
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
            val values = socketHashMap?.values
            if (values != null) {
                mClientIpList.addAll(values)
            }
            ServerActivity.mHandler.sendEmptyMessage(1)
        }

        override fun removeSocketClient(conn: WebSocket?) {
            val ip = conn?.remoteSocketAddress?.address?.hostAddress
            mClientIpList.remove(ip)
            ServerActivity.mHandler.sendEmptyMessage(1)
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
//             charBuffer = decoder.decode(buffer);//用这个的话，只能输出来一次结果，第二次显示为空
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