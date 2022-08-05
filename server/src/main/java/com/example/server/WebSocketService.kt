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
class WebSocketService : Service() {

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
        var mWebSocketServer: ServerSocket? = null
    }


    private val mWebSocketCallback = object : ServerSocket.Callback {
        override fun onOpen(handshake: ClientHandshake?) {

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

        override fun updateSocketClient(socketHashMap: HashMap<WebSocket?, String?>?) {
            val values = socketHashMap?.values
            if (values != null) {
                mClientIpList.addAll(values)
            }
            MainActivity.mHandler.sendEmptyMessage(1)
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
        var charset: Charset? = null
        var decoder: CharsetDecoder? = null
        var charBuffer: CharBuffer? = null
        return try {
            charset = Charset.forName("UTF-8")
            decoder = charset.newDecoder()
            // charBuffer = decoder.decode(buffer);//用这个的话，只能输出来一次结果，第二次显示为空
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer())
            charBuffer.toString()
        } catch (ex: Exception) {
            ex.printStackTrace()
            ""
        }
    }

    override fun onCreate() {
        super.onCreate()
        mWebSocketServer = ServerSocket("192.168.2.8", 8080, mWebSocketCallback)
        mWebSocketServer?.start()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            mWebSocketServer?.stop()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.d(TAG, "onDestroy: ")
    }
}