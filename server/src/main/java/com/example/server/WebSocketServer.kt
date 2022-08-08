package com.example.server

import android.util.Log
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress
import java.nio.ByteBuffer

/**
 * Desc:
 * @author lijt
 * Created on 2022/8/4 10:28
 * Email:
 */
class WebSocketServer(hostName: String, port: Int, callback: Callback) : WebSocketServer(InetSocketAddress(hostName, port)) {

    private var callback: Callback? = callback

    private val mSocketClients = hashMapOf<WebSocket?, String?>()

    companion object {
        private const val TAG = "TAG"
    }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        Log.d(TAG, "onOpen: ${conn?.remoteSocketAddress?.address?.hostName}")
        mSocketClients[conn] = conn?.remoteSocketAddress?.address?.hostAddress
        callback?.onOpen(handshake)
        callback?.clientConnect(conn)
        callback?.addSocketClient(mSocketClients)
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        Log.d(TAG, "onClose: ${conn?.remoteSocketAddress?.address?.hostName}")
        mSocketClients.remove(conn)
        callback?.removeSocketClient(conn)
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        Log.d(TAG, "onMessage: $message")
        callback?.onMessage(message)
    }

    override fun onMessage(conn: WebSocket?, message: ByteBuffer?) {
        super.onMessage(conn, message)
        Log.d(TAG, "onMessage: ByteBuffer conn = $conn")
        callback?.onMessage(message)
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        Log.d(TAG, "onError: conn = ${conn?.remoteSocketAddress?.address?.hostName} ,error = ${ex?.message}")
    }

    override fun onStart() {
        Log.d(TAG, "onStart: 启动WebSocket服务...")
    }

    fun send(message: String?) {
        for (key in mSocketClients.entries) {
            key.key?.send(message)
        }
    }

    interface Callback {
        fun onOpen(handshake: ClientHandshake?)

        fun onMessage(message: String?)

        fun onMessage(bytes: ByteBuffer?)

        fun addSocketClient(socketHashMap: HashMap<WebSocket?, String?>?)

        fun clientConnect(conn: WebSocket?)

        fun removeSocketClient(conn: WebSocket?)
    }
}