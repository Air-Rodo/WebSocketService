package com.example.client

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.example.client.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "TAG"
        val mHandler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                when (msg.what) {
                    0 -> {
                        Log.d(TAG, "handleMessage: msg = ${msg.obj}")

                    }
                }
            }
        }
    }

    lateinit var binding: ActivityMainBinding
    private var mService: WebSocketClientService? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.btnBind.setOnClickListener {
            bindService(Intent(this@MainActivity, WebSocketClientService::class.java), mServiceConnection, BIND_AUTO_CREATE)
        }
        binding.btnUnBind.setOnClickListener {
            unbindService(mServiceConnection)
        }

        binding.btnSendMsg.setOnClickListener {
            val message = binding.etText.text.toString()
            Log.d(TAG, "sendMessage:$message")
            mService?.sendMessage(message)
            binding.etText.setText("")
        }
    }

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.d(TAG, "onServiceConnected: ")
            val binder = service as WebSocketClientService.JWebSocketClientBinder
            mService = binder.getService()
            mService?.setOnMessageListener(object : WebSocketClientService.OnMessageListener {
                override fun loadMessage(message: String?) {
                    runOnUiThread {
                        binding.tvMessage.text = message
                    }
                }
            })
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.d(TAG, "onServiceDisconnected: ")
        }
    }
}