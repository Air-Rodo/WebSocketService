package com.example.server

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.server.databinding.ActivityMainBinding

class ServerActivity : AppCompatActivity() {

    private var mWebSocketServerService: WebSocketServerService? = null

    companion object {
        private const val TAG = "TAG"
        val mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    0 -> {
                        Log.d(TAG, "ClientMessage: = ${msg.obj}")
                    }
                    1 -> {
                        val stringBuffer = StringBuffer()
                        if (WebSocketServerService.mClientIpList.size < 1) {
                            stringBuffer.append("当前无设备连接\n")
                        } else {
                            for (ip in WebSocketServerService.mClientIpList) {
                                stringBuffer.append("$ip:已连接\n")
                            }
                        }
                        Log.d(TAG, "Client is Connect = $stringBuffer")
                    }
                }
            }
        }
    }

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        initService()
        initListener()
    }

    private fun initService() {
        bindService(Intent(this@ServerActivity, WebSocketServerService::class.java), mServiceConnection, BIND_AUTO_CREATE)
    }

    private fun initListener() {
        binding.btnStartServer.setOnClickListener {
            mWebSocketServerService?.start()
        }

        binding.btnStopServer.setOnClickListener {
            mWebSocketServerService?.stop()
        }

        binding.btnSendMsg.setOnClickListener {
            val message = Message()
            message.what = 1
            message.obj = binding.etText.text.toString()
            WebSocketServerService.mService.sendMessage(message)
            binding.etText.setText("")
        }
    }

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected: ")
            val binder = service as WebSocketServerService.WebSocketServerBinder
            mWebSocketServerService = binder.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected: ")
            mWebSocketServerService = null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(mServiceConnection)
        Log.d(TAG, "onDestroy: ")
    }
}